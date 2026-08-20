package com.likelion.dermaday.api.notification.event;

import com.likelion.dermaday.api.cosmetic.domain.Cosmetic;
import com.likelion.dermaday.api.cosmetic.repository.CosmeticRepository;
import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
import com.likelion.dermaday.api.image.service.ImageStorageService;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecord;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CosmeticNotificationCancellationEventTest {

    @Mock
    private CosmeticRepository cosmeticRepository;

    @Mock
    private TreatmentRecordRepository treatmentRecordRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void publishesSnapshotCancellationWhenCosmeticIsDeleted() {
        Cosmetic cosmetic = mock(Cosmetic.class);
        TreatmentRecord treatmentRecord = mock(TreatmentRecord.class);
        when(cosmeticRepository.findByIdAndTreatmentRecord_Member_Id(11L, 1L))
                .thenReturn(Optional.of(cosmetic));
        when(cosmetic.getTreatmentRecord()).thenReturn(treatmentRecord);
        when(treatmentRecord.getId()).thenReturn(100L);
        CosmeticService cosmeticService = new CosmeticService(
                cosmeticRepository,
                treatmentRecordRepository,
                imageStorageService,
                eventPublisher
        );

        cosmeticService.delete(1L, 11L);

        ArgumentCaptor<CosmeticNotificationCancellationRequested> captor =
                ArgumentCaptor.forClass(CosmeticNotificationCancellationRequested.class);
        verify(cosmeticRepository).delete(cosmetic);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(1L, captor.getValue().memberId());
        assertEquals(100L, captor.getValue().treatmentRecordId());
        assertEquals(11L, captor.getValue().cosmeticId());
        assertNotNull(captor.getValue().deletedAt());
    }
}
