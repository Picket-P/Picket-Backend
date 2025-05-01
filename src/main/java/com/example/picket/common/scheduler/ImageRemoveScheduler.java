package com.example.picket.common.scheduler;

import com.example.picket.common.service.S3Service;
import com.example.picket.domain.images.repository.ShowImageRepository;
import com.example.picket.domain.images.repository.UserImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImageRemoveScheduler {

    private final UserImageRepository userImageRepository;
    private final ShowImageRepository showImageRepository;
    private final S3Service s3Service;

    @Scheduled(cron = "0 0 * * * ?")
    @SchedulerLock(name = "IMAGE-REMOVED-SCHEDULER", lockAtLeastFor = "PT1M", lockAtMostFor = "PT5M")

    public void cleanUpRemovedImage() {
        log.info("S3 고아객체 이미지 삭제 스케줄러 시작");
        LocalDateTime dayBefore = LocalDateTime.now().minusDays(1);
        deleteRemovedUserImages(dayBefore);
        deleteRemovedShowImages(dayBefore);
        log.info("S3 고아객체 이미지 삭제 스케줄러 완료");
    }

    private void deleteRemovedUserImages(LocalDateTime dayBefore) {
        AtomicInteger count = new AtomicInteger();
        userImageRepository.findByOrphan(dayBefore).forEach(image -> {
            try {
                s3Service.delete(image.getImageUrl());
                userImageRepository.delete(image);
                count.getAndIncrement();
            } catch (Exception e) {
                log.error("S3에서 공연 포스터 이미지 삭제 실패: {}, 오류: {}", image.getImageUrl(), e.getMessage());
            }
        });
        log.info("{}개의 유저 프로필 이미지가 S3에서 삭제되었습니다.", count);
    }

    private void deleteRemovedShowImages(LocalDateTime dayBefore) {
        AtomicInteger count = new AtomicInteger();
        showImageRepository.findByOrphan(dayBefore).forEach(image -> {
            try {
                s3Service.delete(image.getImageUrl());
                showImageRepository.delete(image);
                count.getAndIncrement();
            } catch (Exception e) {
                log.error("S3에서 공연 포스터 이미지 삭제 실패: {}, 오류: {}", image.getImageUrl(), e.getMessage());
            }
        });

        log.info("{}개의 공연 포스터 이미지가 S3에서 삭제되었습니다.", count);
    }

}