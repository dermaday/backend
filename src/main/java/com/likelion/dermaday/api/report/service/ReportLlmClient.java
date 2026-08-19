package com.likelion.dermaday.api.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.dermaday.api.report.dto.ReportResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ReportLlmClient {

    private static final String SYSTEM = """
            너는 스킨케어 루틴 팁 작성기다. 입력 JSON의 제품 순서를 절대 바꾸거나 추가·삭제하지 말고,
            각 제품에 팁과 태그만 단다.
            - tip: "가볍게 결 정돈"처럼 6~12자 명사형 구. 마침표 없음.
            - tags: 2개 이하, 각 2~5자. 성분 효능 단정 금지.
            - skinType을 참고해 표현만 조절한다. 의학적 주장·진단 금지.
            - JSON으로만 응답: {"steps":[{"product":"...","tip":"...","tags":["..."]}]}""";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final boolean enabled;

    public ReportLlmClient(
            @Value("${app.llm.api-key:}") String apiKey,
            @Value("${app.llm.model:gpt-5.6-terra}") String model,
            ObjectMapper objectMapper
    ) {
        this.enabled = apiKey != null && !apiKey.isBlank();
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + (apiKey == null ? "" : apiKey))
                .build();
    }

    public record TipResult(List<ReportResponse.RoutineStep> steps, boolean applied) {
    }

    public TipResult writeTips(List<ReportResponse.RoutineStep> steps, String skinTypeName) {
        if (!enabled || steps.isEmpty()) {
            return new TipResult(steps, false);
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("skinType", skinTypeName);
            payload.put("steps", steps.stream().map(ReportResponse.RoutineStep::productName).toList());

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM),
                            Map.of("role", "user", "content", objectMapper.writeValueAsString(payload))),
                    "response_format", Map.of("type", "json_object"),
                    "reasoning_effort", "low");

            JsonNode res = restClient.post().uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode generated = objectMapper
                    .readTree(res.path("choices").path(0).path("message").path("content").asText())
                    .path("steps");
            return new TipResult(merge(steps, generated), true);
        } catch (Exception e) {
            log.warn("루틴 팁 생성 실패, 기본 문구로 대체", e);
            return new TipResult(steps, false);
        }
    }

    private List<ReportResponse.RoutineStep> merge(List<ReportResponse.RoutineStep> steps, JsonNode generated) {
        if (!generated.isArray() || generated.size() != steps.size()) {
            throw new IllegalStateException("LLM 응답 스텝 수 불일치");
        }
        List<ReportResponse.RoutineStep> merged = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            ReportResponse.RoutineStep step = steps.get(i);
            JsonNode node = generated.get(i);
            if (!step.productName().equals(node.path("product").asText())) {
                throw new IllegalStateException("LLM 응답 제품 순서 불일치");
            }
            List<String> tags = new ArrayList<>();
            node.path("tags").forEach(t -> tags.add(t.asText()));
            merged.add(new ReportResponse.RoutineStep(
                    step.order(),
                    step.productName(),
                    step.categoryPill(),
                    tags.isEmpty() ? step.tags() : tags,
                    node.path("tip").asText().isBlank() ? step.tip() : node.path("tip").asText()
            ));
        }
        return merged;
    }
}
