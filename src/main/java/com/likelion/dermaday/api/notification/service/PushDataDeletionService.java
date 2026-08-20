package com.likelion.dermaday.api.notification.service;

import com.likelion.dermaday.api.notification.repository.PushInstallationRepository;
import com.likelion.dermaday.api.notification.repository.PushNotificationJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PushDataDeletionService {

    private final PushNotificationJobRepository jobRepository;
    private final PushInstallationRepository installationRepository;

    public void deleteAllByMember(Long memberId) {
        jobRepository.deleteAllByMember_Id(memberId);
        installationRepository.deleteAllByMember_Id(memberId);
    }
}
