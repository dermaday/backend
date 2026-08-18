package com.likelion.dermaday.api.auth.security;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

final class JsonTestSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonTestSupport() {
    }

    static String readDataToken(String json) {
        JsonNode root = OBJECT_MAPPER.readTree(json);
        return root.path("data").path("token").asString();
    }
}
