package com.example.picket.domain.show.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@EnableAsync
class ShowViewCountServiceTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ShowViewCountService showViewCountService;

    @Test
    void 첫_조회_조회수_증가_성공() throws Exception {
        // given
        Long showId = 1L;
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        String redisKey = String.format("view:show:%d:user:%d", showId, userId);

        Show show = Show.toEntity(showId, "제목1", "포스터1.jpg", Category.MUSICAL, "내용1", "장소1",
            LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2);
        ReflectionTestUtils.setField(show, "viewCount", 0);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(null);
        given(showRepository.findById(showId)).willReturn(Optional.of(show));
        lenient().doNothing().when(valueOperations).set(eq(redisKey), eq("viewed"), any(Duration.class));

        // when
        CompletableFuture<Integer> result = showViewCountService.incrementViewCount(authUser, showId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.get(3, TimeUnit.SECONDS)).isEqualTo(1);
        verify(valueOperations, times(1)).get(redisKey);
        verify(valueOperations, times(1)).set(eq(redisKey), eq("viewed"), any(Duration.class));
        verify(showRepository, times(1)).findById(showId);
    }

    @Test
    void 재조회_조회수_증가_없음() throws Exception {
        // given
        Long showId = 1L;
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        String redisKey = String.format("view:show:%d:user:%d", showId, userId);

        doReturn(valueOperations).when(redisTemplate).opsForValue();
        doReturn("viewed").when(valueOperations).get(redisKey);

        // when
        CompletableFuture<Integer> result = showViewCountService.incrementViewCount(authUser, showId);

        // then
        assertThat(result).isNull();
        verify(valueOperations, times(1)).get(redisKey);
        verify(valueOperations, times(0)).set(anyString(), anyString(), any(Duration.class));
        verify(showRepository, times(0)).findById(any());
    }
}