package com.likelion.dermaday.api.image.event;

import com.likelion.dermaday.api.image.service.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@ActiveProfiles("test")
class MemberImagesDeletionTransactionIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @Test
    void deletesImageOnlyAfterTransactionCommits() {
        MemberImagesDeletionRequested event = new MemberImagesDeletionRequested(
                1L,
                List.of("members/1/images/committed.png")
        );

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            eventPublisher.publishEvent(event);
            verifyNoInteractions(imageStorageService);
        });

        verify(imageStorageService).deleteMemberObject(1L, "members/1/images/committed.png");
    }

    @Test
    void keepsImageWhenTransactionRollsBack() {
        clearInvocations(imageStorageService);
        MemberImagesDeletionRequested event = new MemberImagesDeletionRequested(
                2L,
                List.of("members/2/images/rolled-back.png")
        );

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            eventPublisher.publishEvent(event);
            status.setRollbackOnly();
        });

        verifyNoInteractions(imageStorageService);
    }
}
