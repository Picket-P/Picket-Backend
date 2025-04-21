package com.example.picket.domain;

import com.example.picket.common.annotation.Auth;
import com.example.picket.domain.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class RaceConditionTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void 티켓예매_동시성_테스트() throws InterruptedException {
        Long userId = 2L;
        Long showId = 1L;
        Long showDateId = 1L;
        Long seatId = 1L;
        int THREAD_COUNT = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    bookingService.booking(showId, showDateId, userId, List.of(seatId));
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
    }

    @Test
    void showDate_업데이트_동시성_테스트() throws InterruptedException {
        Long userId1 = 2L;
        Long userId2 = 3L;
        Long showId = 1L;
        Long showDateId = 1L;
        List<Long> seatIds1 = List.of(3L);
        List<Long> seatIds2 = List.of(4L);


        Thread thread1 = new Thread(() -> {
            try {
                bookingService.booking(showId, showDateId, userId1, seatIds1);
            } catch (InterruptedException e) {
                throw new IllegalStateException("락 획득 실패");
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                bookingService.booking(showId, showDateId, userId2, seatIds2);
            } catch (InterruptedException e) {
                throw new IllegalStateException("락 획득 실패");
            }
        });

        // 스레드 시작
        thread1.start();
        thread2.start();

        // main 스레드가 두 스레드가 끝날 때까지 기다리게 함
        thread1.join();
        thread2.join();

    }


}
