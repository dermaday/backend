package com.likelion.dermaday.api.notification.service;

import java.util.List;

public interface PushGateway {

    PushSendResult send(List<String> fids, PushMessage message);
}
