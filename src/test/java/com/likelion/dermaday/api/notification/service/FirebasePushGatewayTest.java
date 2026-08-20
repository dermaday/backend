package com.likelion.dermaday.api.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.likelion.dermaday.api.notification.domain.PushNotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebasePushGatewayTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Test
    void removesOnlyUnregisteredFids() throws Exception {
        BatchResponse batchResponse = mock(BatchResponse.class);
        SendResponse unregistered = failedResponse(MessagingErrorCode.UNREGISTERED);
        SendResponse invalidArgument = failedResponse(MessagingErrorCode.INVALID_ARGUMENT);
        when(batchResponse.getSuccessCount()).thenReturn(0);
        when(batchResponse.getResponses()).thenReturn(List.of(unregistered, invalidArgument));
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(batchResponse);

        PushSendResult result = new FirebasePushGateway(firebaseMessaging).send(
                List.of("unregistered-fid", "invalid-argument-fid"),
                new PushMessage(
                        "Dermaday",
                        "알림 본문",
                        URI.create("https://dermaday.me/home"),
                        PushNotificationType.PRODUCT_UNLOCK
                )
        );

        assertEquals(0, result.acceptedCount());
        assertEquals(List.of("unregistered-fid"), result.invalidFids());
    }

    private SendResponse failedResponse(MessagingErrorCode errorCode) {
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessagingErrorCode()).thenReturn(errorCode);
        SendResponse response = mock(SendResponse.class);
        when(response.isSuccessful()).thenReturn(false);
        when(response.getException()).thenReturn(exception);
        return response;
    }
}
