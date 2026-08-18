package com.likelion.dermaday.api.whs.config;

import com.likelion.dermaday.api.whs.domain.WhsMockData;
import com.likelion.dermaday.api.whs.repository.WhsMockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WhsMockDataInitializer implements ApplicationRunner {

    public static final String DEFAULT_MOCK_KEY = "DEFAULT";

    private static final String DEFAULT_PAYLOAD = """
            {
              "skinType": "NORMAL",
              "treatments": [
                {
                  "treatmentType": "ULTHERA",
                  "treatedOn": "2026-08-08",
                  "reaction": "COMFORTABLE"
                },
                {
                  "treatmentType": "OLIGIO",
                  "treatedOn": "2026-08-10",
                  "reaction": "IRRITATED"
                }
              ],
              "cosmetics": [
                {
                  "name": "센텔라 스킨",
                  "productType": "TONER_SKIN",
                  "ingredients": ["RETINOL"],
                  "imageObjectKey": "assets/cosmetics/centella-skin.png"
                },
                {
                  "name": "아베 폼클렌저",
                  "productType": "ESSENCE_AMPOULE_SERUM",
                  "ingredients": ["GENERAL_COSMETIC"],
                  "imageObjectKey": "assets/cosmetics/abe-cleanser.png"
                }
              ]
            }
            """;

    private final WhsMockDataRepository whsMockDataRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!whsMockDataRepository.existsByMockKey(DEFAULT_MOCK_KEY)) {
            whsMockDataRepository.save(WhsMockData.create(DEFAULT_MOCK_KEY, DEFAULT_PAYLOAD));
        }
    }
}
