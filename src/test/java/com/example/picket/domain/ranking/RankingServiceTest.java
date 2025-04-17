package com.example.picket.domain.ranking;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.ranking.entity.HotShow;
import com.example.picket.domain.ranking.entity.LikeShow;
import com.example.picket.domain.ranking.entity.PopularKeyword;
import com.example.picket.domain.ranking.repository.SearchLogRepository;
import com.example.picket.domain.ranking.service.RankingService;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private SearchLogRepository searchLogRepository;
    @Mock
    private ShowRepository showRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ListOperations<String, String> listOperations;

    @InjectMocks
    private RankingService rankingService;

    private static final ShowStatus[] ACTIVE_STATUSES = {
            ShowStatus.RESERVATION_PENDING,
            ShowStatus.RESERVATION_ONGOING,
            ShowStatus.PERFORMANCE_ONGOING,
            ShowStatus.RESERVATION_CLOSED
    };

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // 모든 Mock 초기화
        reset(redisTemplate, listOperations, searchLogRepository, showRepository, likeRepository, objectMapper);
    }

    @Test
    void 인기_키워드_Redis_조회_성공() throws Exception {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        PopularKeyword keyword = PopularKeyword.toEntity(Category.MUSICAL, 10L, now);
        String json = "{\"category\":\"MUSICAL\",\"keywordCount\":10,\"updatedAt\":\"" + now + "\"}";
        when(listOperations.range("ranking:popular_keywords", 0, -1)).thenReturn(List.of(json));
        when(objectMapper.readValue(json, PopularKeyword.class)).thenReturn(keyword);

        // When
        CompletableFuture<List<PopularKeyword>> future = rankingService.getPopularKeywordsAsync();
        List<PopularKeyword> result = future.join();

        // Then
        assertEquals(1, result.size());
        assertEquals(Category.MUSICAL, result.get(0).getCategory());
        assertEquals(10L, result.get(0).getKeywordCount());
        verify(listOperations).range("ranking:popular_keywords", 0, -1);
        verify(objectMapper).readValue(json, PopularKeyword.class);
        verify(searchLogRepository, never()).findTopKeywordsSince(any());
    }

    @Test
    void 인기_키워드_Redis_데이터_없음_RDS_폴백_성공() {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("ranking:popular_keywords", 0, -1)).thenReturn(Collections.emptyList());
        List<Object[]> rows = List.of(
                new Object[]{Category.MUSICAL, 10L},
                new Object[]{Category.CONCERT, 5L}
        );
        when(searchLogRepository.findTopKeywordsSince(any())).thenReturn(rows);

        // When
        CompletableFuture<List<PopularKeyword>> future = rankingService.getPopularKeywordsAsync();
        List<PopularKeyword> result = future.join();

        // Then
        assertEquals(2, result.size());
        assertEquals(Category.MUSICAL, result.get(0).getCategory());
        assertEquals(10L, result.get(0).getKeywordCount());
        assertEquals(Category.CONCERT, result.get(1).getCategory());
        assertEquals(5L, result.get(1).getKeywordCount());
        verify(listOperations).range("ranking:popular_keywords", 0, -1);
        verify(searchLogRepository).findTopKeywordsSince(any());
    }

    @Test
    void 인기_키워드_Redis_역직렬화_실패() throws Exception {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        String json = "{\"category\":\"MUSICAL\",\"keywordCount\":10,\"updatedAt\":\"" + now + "\"}";
        when(listOperations.range("ranking:popular_keywords", 0, -1)).thenReturn(List.of(json));
        when(objectMapper.readValue(json, PopularKeyword.class)).thenThrow(new RuntimeException("역직렬화 오류"));

        // When
        CompletableFuture<List<PopularKeyword>> future = rankingService.getPopularKeywordsAsync();
        List<PopularKeyword> result = future.join();

        // Then
        assertTrue(result.isEmpty());
        verify(listOperations).range("ranking:popular_keywords", 0, -1);
        verify(objectMapper).readValue(json, PopularKeyword.class);
        verify(searchLogRepository, never()).findTopKeywordsSince(any());
    }

    @Test
    void 인기_키워드_Redis_연결_오류_빈_리스트_반환() {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("ranking:popular_keywords", 0, -1)).thenThrow(new RuntimeException("Redis 연결 오류"));

        // When
        CompletableFuture<List<PopularKeyword>> future = rankingService.getPopularKeywordsAsync();
        List<PopularKeyword> result = future.join();

        // Then
        assertTrue(result.isEmpty());
        verify(listOperations).range("ranking:popular_keywords", 0, -1);
        verify(searchLogRepository, never()).findTopKeywordsSince(any());
    }

    @Test
    void 인기_공연_Redis_조회_성공() throws Exception {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        HotShow hotShow = HotShow.toEntity(1L, "Show 1", 1000, ShowStatus.RESERVATION_ONGOING, now);
        String json = "{\"showId\":1,\"title\":\"Show 1\",\"viewCount\":1000,\"status\":\"RESERVATION_ONGOING\",\"updatedAt\":\"" + now + "\"}";
        when(listOperations.range("ranking:hot_shows", 0, -1)).thenReturn(List.of(json));
        when(objectMapper.readValue(json, HotShow.class)).thenReturn(hotShow);

        // When
        CompletableFuture<List<HotShow>> future = rankingService.getHotShowsAsync();
        List<HotShow> result = future.join();

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getShowId());
        assertEquals("Show 1", result.get(0).getTitle());
        assertEquals(1000, result.get(0).getViewCount());
        assertEquals(ShowStatus.RESERVATION_ONGOING, result.get(0).getStatus());
        verify(listOperations).range("ranking:hot_shows", 0, -1);
        verify(objectMapper).readValue(json, HotShow.class);
        verify(showRepository, never()).findTop10ByStatusNotAndOrderByViewCountDesc(any());
    }

    @Test
    void 인기_공연_Redis_데이터_없음_RDS_폴백_성공() throws Exception {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("ranking:hot_shows", 0, -1)).thenReturn(Collections.emptyList());
        Show show = Show.toEntity(
                1L, // directorId
                "Show 1",
                "poster1.jpg",
                Category.MUSICAL,
                "Description 1",
                "Seoul",
                now.plusDays(1),
                now.plusDays(2),
                2 // ticketsLimitPerUser
        );
        // 강제로 id, viewCount, status 설정
        java.lang.reflect.Field idField = Show.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(show, 1L);
        java.lang.reflect.Field viewCountField = Show.class.getDeclaredField("viewCount");
        viewCountField.setAccessible(true);
        viewCountField.set(show, 1000);
        show.updateStatus(ShowStatus.RESERVATION_ONGOING);
        when(showRepository.findTop10ByStatusNotAndOrderByViewCountDesc(any())).thenReturn(List.of(show));

        // When
        CompletableFuture<List<HotShow>> future = rankingService.getHotShowsAsync();
        List<HotShow> result = future.join();

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getShowId());
        assertEquals("Show 1", result.get(0).getTitle());
        assertEquals(1000, result.get(0).getViewCount());
        assertEquals(ShowStatus.RESERVATION_ONGOING, result.get(0).getStatus());
        verify(listOperations).range("ranking:hot_shows", 0, -1);
        verify(showRepository).findTop10ByStatusNotAndOrderByViewCountDesc(any());
    }

    @Test
    void 인기_공연_Redis_역직렬화_실패() throws Exception {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        String json = "{\"showId\":1,\"title\":\"Show 1\",\"viewCount\":1000,\"status\":\"RESERVATION_ONGOING\",\"updatedAt\":\"" + now + "\"}";
        when(listOperations.range("ranking:hot_shows", 0, -1)).thenReturn(List.of(json));
        when(objectMapper.readValue(json, HotShow.class)).thenThrow(new RuntimeException("역직렬화 오류"));

        // When
        CompletableFuture<List<HotShow>> future = rankingService.getHotShowsAsync();
        List<HotShow> result = future.join();

        // Then
        assertTrue(result.isEmpty());
        verify(listOperations).range("ranking:hot_shows", 0, -1);
        verify(objectMapper).readValue(json, HotShow.class);
        verify(showRepository, never()).findTop10ByStatusNotAndOrderByViewCountDesc(any());
    }

    @Test
    void 인기_공연_Redis_연결_오류_빈_리스트_반환() {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("ranking:hot_shows", 0, -1)).thenThrow(new RuntimeException("Redis 연결 오류"));

        // When
        CompletableFuture<List<HotShow>> future = rankingService.getHotShowsAsync();
        List<HotShow> result = future.join();

        // Then
        assertTrue(result.isEmpty());
        verify(listOperations).range("ranking:hot_shows", 0, -1);
        verify(showRepository, never()).findTop10ByStatusNotAndOrderByViewCountDesc(any());
    }

    @Test
    void 좋아요_공연_Redis_조회_성공() throws Exception {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        LikeShow likeShow = LikeShow.toEntity(1L, "Show 1", 5L, ShowStatus.RESERVATION_ONGOING, now);
        String json = "{\"showId\":1,\"title\":\"Show 1\",\"likeCount\":5,\"status\":\"RESERVATION_ONGOING\",\"updatedAt\":\"" + now + "\"}";
        when(listOperations.range("ranking:like_shows", 0, -1)).thenReturn(List.of(json));
        when(objectMapper.readValue(json, LikeShow.class)).thenReturn(likeShow);

        // When
        CompletableFuture<List<LikeShow>> future = rankingService.getLikeShowsAsync();
        List<LikeShow> result = future.join();

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getShowId());
        assertEquals("Show 1", result.get(0).getTitle());
        assertEquals(5L, result.get(0).getLikeCount());
        assertEquals(ShowStatus.RESERVATION_ONGOING, result.get(0).getStatus());
        verify(listOperations).range("ranking:like_shows", 0, -1);
        verify(objectMapper).readValue(json, LikeShow.class);
        verify(likeRepository, never()).findTop10ShowsByLikeCountAndStatusIn(any(), any());
    }

    @Test
    void 좋아요_공연_Redis_데이터_없음_RDS_폴백_성공() {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("ranking:like_shows", 0, -1)).thenReturn(Collections.emptyList());
        List<Object[]> rows = List.of(
                new Object[]{1L, "Show 1", 5L, ShowStatus.RESERVATION_ONGOING},
                new Object[]{2L, "Show 2", 3L, ShowStatus.PERFORMANCE_ONGOING}
        );
        when(likeRepository.findTop10ShowsByLikeCountAndStatusIn(any(ShowStatus[].class), any(Pageable.class))).thenReturn(rows);

        // When
        CompletableFuture<List<LikeShow>> future = rankingService.getLikeShowsAsync();
        List<LikeShow> result = future.join();

        // Then
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getShowId());
        assertEquals("Show 1", result.get(0).getTitle());
        assertEquals(5L, result.get(0).getLikeCount());
        assertEquals(ShowStatus.RESERVATION_ONGOING, result.get(0).getStatus());
        assertEquals(2L, result.get(1).getShowId());
        assertEquals("Show 2", result.get(1).getTitle());
        assertEquals(3L, result.get(1).getLikeCount());
        assertEquals(ShowStatus.PERFORMANCE_ONGOING, result.get(1).getStatus());
        verify(listOperations).range("ranking:like_shows", 0, -1);
        verify(likeRepository).findTop10ShowsByLikeCountAndStatusIn(
                eq(new ShowStatus[]{ShowStatus.RESERVATION_PENDING, ShowStatus.RESERVATION_ONGOING,
                        ShowStatus.RESERVATION_CLOSED, ShowStatus.PERFORMANCE_ONGOING}),
                any(Pageable.class));
    }

    @Test
    void 좋아요_공연_Redis_역직렬화_실패() throws Exception {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        String json = "{\"showId\":1,\"title\":\"Show 1\",\"likeCount\":5,\"status\":\"RESERVATION_ONGOING\",\"updatedAt\":\"" + now + "\"}";
        when(listOperations.range("ranking:like_shows", 0, -1)).thenReturn(List.of(json));
        when(objectMapper.readValue(json, LikeShow.class)).thenThrow(new RuntimeException("역직렬화 오류"));

        // When
        CompletableFuture<List<LikeShow>> future = rankingService.getLikeShowsAsync();
        List<LikeShow> result = future.join();

        // Then
        assertTrue(result.isEmpty());
        verify(listOperations).range("ranking:like_shows", 0, -1);
        verify(objectMapper).readValue(json, LikeShow.class);
        verify(likeRepository, never()).findTop10ShowsByLikeCountAndStatusIn(any(), any());
    }

    @Test
    void 좋아요_공연_Redis_연결_오류_빈_리스트_반환() {
        // Given
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("ranking:like_shows", 0, -1)).thenThrow(new RuntimeException("Redis 연결 오류"));

        // When
        CompletableFuture<List<LikeShow>> future = rankingService.getLikeShowsAsync();
        List<LikeShow> result = future.join();

        // Then
        assertTrue(result.isEmpty());
        verify(listOperations).range("ranking:like_shows", 0, -1);
        verify(likeRepository, never()).findTop10ShowsByLikeCountAndStatusIn(any(), any());
    }
}