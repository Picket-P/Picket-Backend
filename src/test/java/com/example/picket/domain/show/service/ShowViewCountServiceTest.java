package com.example.picket.domain.show.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.RedisSystemException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@EnableAsync
@MockitoSettings(strictness = Strictness.LENIENT)
class ShowViewCountServiceTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ShowViewCountService showViewCountService;

    @BeforeEach
    void setUp() {
        Mockito.reset(redisTemplate, valueOperations, showRepository);
        showViewCountService = new ShowViewCountService(showRepository, redisTemplate);
    }

    @Test
    void 첫_조회_조회수_증가_성공() throws Exception {
        // given
        Long showId = 1L;
        Long userId = 1L;
        AuthUser authUser = AuthUser.create(userId, UserRole.USER);
        String redisKey = String.format("view:show:%d:user:%d", showId, userId);

        Show show = Show.create(showId, "제목1", "포스터1.jpg", Category.MUSICAL, "내용1", "장소1",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2);
        ReflectionTestUtils.setField(show, "viewCount", 0);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(null);
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        doNothing().when(valueOperations).set(eq(redisKey), eq("viewed"), any(Duration.class));

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
        AuthUser authUser = AuthUser.create(userId, UserRole.USER);
        String redisKey = String.format("view:show:%d:user:%d", showId, userId);

        Show show = Show.create(showId, "제목1", "포스터1.jpg", Category.MUSICAL, "내용1", "장소1",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2);
        ReflectionTestUtils.setField(show, "viewCount", 0);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn("viewed");
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));

        // when
        CompletableFuture<Integer> result = showViewCountService.incrementViewCount(authUser, showId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.get(3, TimeUnit.SECONDS)).isNull();
        verify(valueOperations, times(1)).get(redisKey);
        verify(valueOperations, times(0)).set(anyString(), anyString(), any(Duration.class));
        verify(showRepository, times(0)).findById(any());
    }


    @Test
    void authUser_null_완료_결과_null() throws Exception {
        // given
        Long showId = 1L;

        // when
        CompletableFuture<Integer> result = showViewCountService.incrementViewCount(null, showId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.get(3, TimeUnit.SECONDS)).isNull();
        verify(redisTemplate, times(0)).opsForValue();
        verify(showRepository, times(0)).findById(any());
    }

    @Test
    void show_없음_예외_발생() {
        // given
        Long showId = 1L;
        Long userId = 1L;
        AuthUser authUser = AuthUser.create(userId, UserRole.USER);
        String redisKey = String.format("view:show:%d:user:%d", showId, userId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(null);
        when(showRepository.findById(showId)).thenReturn(Optional.empty());
        doNothing().when(valueOperations).set(eq(redisKey), eq("viewed"), any(Duration.class));

        // when & then
        assertThatThrownBy(() -> showViewCountService.incrementViewCount(authUser, showId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 공연을 찾을 수 없습니다.");
    }

    @Test
    void redis_오류_예외_발생() {
        // given
        Long showId = 1L;
        Long userId = 1L;
        AuthUser authUser = AuthUser.create(userId, UserRole.USER);
        String redisKey = String.format("view:show:%d:user:%d", showId, userId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenThrow(new RedisSystemException("Redis connection failed", null));
        when(showRepository.findById(showId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> showViewCountService.incrementViewCount(authUser, showId))
                .isInstanceOf(RedisSystemException.class)
                .hasMessage("Redis connection failed");
    }

}