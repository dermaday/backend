package com.likelion.dermaday.api.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.push", name = "enabled", havingValue = "true")
public class FirebasePushGateway implements PushGateway {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public PushSendResult send(List<String> fids, PushMessage message) {
        if (fids.isEmpty()) {
            return new PushSendResult(0, List.of());
        }
        MulticastMessage multicastMessage = MulticastMessage.builder()
                .addAllFids(fids)
                .setNotification(Notification.builder()
                        .setTitle(message.title())
                        .setBody(message.body())
                        .build())
                .setWebpushConfig(WebpushConfig.builder()
                        .setFcmOptions(WebpushFcmOptions.withLink(message.link().toString()))
                        .build())
                .putData("type", message.type().name())
                .putData("link", message.link().toString())
                .build();
        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(multicastMessage);
            return new PushSendResult(response.getSuccessCount(), invalidFids(fids, response.getResponses()));
        } catch (FirebaseMessagingException exception) {
            throw new IllegalStateException("FCM request failed", exception);
        }
    }

    private List<String> invalidFids(List<String> fids, List<SendResponse> responses) {
        List<String> invalidFids = new ArrayList<>();
        for (int index = 0; index < responses.size(); index++) {
            SendResponse response = responses.get(index);
            if (!response.isSuccessful() && isInvalid(response.getException())) {
                invalidFids.add(fids.get(index));
            }
        }
        return invalidFids;
    }

    private boolean isInvalid(FirebaseMessagingException exception) {
        if (exception == null) {
            return false;
        }
        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.UNREGISTERED;
    }
}
