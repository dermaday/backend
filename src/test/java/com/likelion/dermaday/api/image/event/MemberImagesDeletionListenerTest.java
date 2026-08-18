package com.likelion.dermaday.api.image.event;

import com.likelion.dermaday.api.image.service.ImageStorageService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MemberImagesDeletionListenerTest {

    @Test
    void continuesDeletingRemainingImagesWhenOneDeletionFails() {
        ImageStorageService imageStorageService = mock(ImageStorageService.class);
        MemberImagesDeletionListener listener = new MemberImagesDeletionListener(imageStorageService);
        doThrow(new IllegalStateException("S3 failure"))
                .when(imageStorageService)
                .deleteMemberObject(1L, "members/1/images/first.png");

        listener.deleteAfterCommit(new MemberImagesDeletionRequested(1L, List.of(
                "members/1/images/first.png",
                "members/1/images/second.png"
        )));

        verify(imageStorageService).deleteMemberObject(1L, "members/1/images/first.png");
        verify(imageStorageService).deleteMemberObject(1L, "members/1/images/second.png");
    }
}
