package com.likelion.dermaday.api.image.event;

import com.likelion.dermaday.api.image.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberImagesDeletionListener {

    private final ImageStorageService imageStorageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteAfterCommit(MemberImagesDeletionRequested event) {
        for (String objectKey : event.objectKeys()) {
            try {
                imageStorageService.deleteMemberObject(event.memberId(), objectKey);
            } catch (RuntimeException exception) {
                log.error(
                        "Failed to delete committed member image. memberId={}, objectKey={}",
                        event.memberId(),
                        objectKey,
                        exception
                );
            }
        }
    }
}
