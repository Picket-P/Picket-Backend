package com.example.picket.domain.ranking.service;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.ranking.dto.response.HotShowResponse;
import com.example.picket.domain.ranking.dto.response.LikeShowResponse;
import com.example.picket.domain.ranking.dto.response.PopularKeywordResponse;
import com.example.picket.domain.ranking.entity.HotShow;
import com.example.picket.domain.ranking.entity.LikeShow;
import com.example.picket.domain.ranking.entity.PopularKeyword;
import com.example.picket.domain.ranking.repository.SearchLogRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

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

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void Redis에서_인기_검색어_조회_성공() throws JsonProcessingException {
        // Given
        List<String> jsonKeywords = Arrays.asList(
                "{\"category\":\"MUSICAL\",\"keywordCount\":100,\"createdAt\":\"2025-04-21T10:00:00\"}",
                "{\"category\":\"CONCERT\",\"keywordCount\":50,\"createdAt\":\"2025-04-21T10:00:00\"}"
        );

        PopularKeyword keyword1 = PopularKeyword.create(Category.MUSICAL, 100L, now);
        PopularKeyword keyword2 = PopularKeyword.create(Category.CONCERT, 50L, now);

        when(listOperations.range("ranking:popular_keywords", 0, -1)).thenReturn(jsonKeywords);
        when(objectMapper.readValue(jsonKeywords.get(0), PopularKeyword.class)).thenReturn(keyword1);
        when(objectMapper.readValue(jsonKeywords.get(1), PopularKeyword.class)).thenReturn(keyword2);

        // When
        List<PopularKeywordResponse> result = rankingService.getPopularKeywords();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategory()).isEqualTo(Category.MUSICAL);
        assertThat(result.get(0).getKeywordCount()).isEqualTo(100L);
        assertThat(result.get(1).getCategory()).isEqualTo(Category.CONCERT);
        assertThat(result.get(1).getKeywordCount()).isEqualTo(50L);

        verify(listOperations).range("ranking:popular_keywords", 0, -1);
        verify(objectMapper, times(2)).readValue(anyString(), eq(PopularKeyword.class));
    }

    @Test
    void Redis에서_검색어_조회_실패_시_RDS_폴백() {
        // Given
        when(listOperations.range("ranking:popular_keywords", 0, -1)).thenReturn(Collections.emptyList());

        Object[] row1 = {Category.MUSICAL, 100L};
        Object[] row2 = {Category.CONCERT, 50L};
        List<Object[]> topKeywords = Arrays.asList(row1, row2);

        when(searchLogRepository.findTopKeywordsSince(any(LocalDateTime.class))).thenReturn(topKeywords);

        // When
        List<PopularKeywordResponse> result = rankingService.getPopularKeywords();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategory()).isEqualTo(Category.MUSICAL);
        assertThat(result.get(0).getKeywordCount()).isEqualTo(100L);
        assertThat(result.get(1).getCategory()).isEqualTo(Category.CONCERT);
        assertThat(result.get(1).getKeywordCount()).isEqualTo(50L);

        verify(listOperations).range("ranking:popular_keywords", 0, -1);
        verify(searchLogRepository).findTopKeywordsSince(any(LocalDateTime.class));
    }

    @Test
    void Redis에서_검색어_역직렬화_실패_시_필터링() throws JsonProcessingException {
        // Given
        List<String> jsonKeywords = Arrays.asList(
                "{\"category\":\"MUSICAL\",\"keywordCount\":100,\"createdAt\":\"2025-04-21T10:00:00\"}",
                "{\"category\":\"CONCERT\",\"keywordCount\":50,\"createdAt\":\"2025-04-21T10:00:00\"}"
        );

        PopularKeyword keyword1 = PopularKeyword.create(Category.MUSICAL, 100L, now);

        when(listOperations.range("ranking:popular_keywords", 0, -1)).thenReturn(jsonKeywords);
        when(objectMapper.readValue(jsonKeywords.get(0), PopularKeyword.class)).thenReturn(keyword1);
        when(objectMapper.readValue(jsonKeywords.get(1), PopularKeyword.class)).thenThrow(new JsonProcessingException("Error") {
        });

        // When
        List<PopularKeywordResponse> result = rankingService.getPopularKeywords();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(Category.MUSICAL);
        assertThat(result.get(0).getKeywordCount()).isEqualTo(100L);

        verify(listOperations).range("ranking:popular_keywords", 0, -1);
        verify(objectMapper, times(2)).readValue(anyString(), eq(PopularKeyword.class));
    }

    @Test
    void 인기_검색어_조회_중_예외_발생_시_빈_리스트_반환() {
        // Given
        when(listOperations.range("ranking:popular_keywords", 0, -1)).thenThrow(new RuntimeException("Redis error"));

        // When
        List<PopularKeywordResponse> result = rankingService.getPopularKeywords();

        // Then
        assertThat(result).isEmpty();
        verify(listOperations).range("ranking:popular_keywords", 0, -1);
    }

    @Test
    void Redis에서_인기_공연_조회_성공() throws JsonProcessingException {
        // Given
        List<String> jsonShows = Arrays.asList(
                "{\"showId\":1,\"title\":\"Show1\",\"viewCount\":1000,\"status\":\"RESERVATION_ONGOING\",\"createdAt\":\"2025-04-21T10:00:00\"}",
                "{\"showId\":2,\"title\":\"Show2\",\"viewCount\":500,\"status\":\"PERFORMANCE_ONGOING\",\"createdAt\":\"2025-04-21T10:00:00\"}"
        );

        HotShow show1 = HotShow.create(1L, "Show1", 1000, ShowStatus.RESERVATION_ONGOING, now);
        HotShow show2 = HotShow.create(2L, "Show2", 500, ShowStatus.PERFORMANCE_ONGOING, now);

        when(listOperations.range("ranking:hot_shows", 0, -1)).thenReturn(jsonShows);
        when(objectMapper.readValue(jsonShows.get(0), HotShow.class)).thenReturn(show1);
        when(objectMapper.readValue(jsonShows.get(1), HotShow.class)).thenReturn(show2);

        // When
        List<HotShowResponse> result = rankingService.getHotShows();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getShowId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Show1");
        assertThat(result.get(0).getViewCount()).isEqualTo(1000);
        assertThat(result.get(0).getStatus()).isEqualTo(ShowStatus.RESERVATION_ONGOING);

        assertThat(result.get(1).getShowId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("Show2");
        assertThat(result.get(1).getViewCount()).isEqualTo(500);
        assertThat(result.get(1).getStatus()).isEqualTo(ShowStatus.PERFORMANCE_ONGOING);

        verify(listOperations).range("ranking:hot_shows", 0, -1);
        verify(objectMapper, times(2)).readValue(anyString(), eq(HotShow.class));
    }

    @Test
    void Redis에서_인기_공연_조회_실패_시_RDS_폴백() {
        // Given
        when(listOperations.range("ranking:hot_shows", 0, -1)).thenReturn(Collections.emptyList());

        Show show1 = mock(Show.class);
        when(show1.getId()).thenReturn(1L);
        when(show1.getTitle()).thenReturn("Show1");
        when(show1.getViewCount()).thenReturn(1000);
        when(show1.getStatus()).thenReturn(ShowStatus.RESERVATION_ONGOING);

        Show show2 = mock(Show.class);
        when(show2.getId()).thenReturn(2L);
        when(show2.getTitle()).thenReturn("Show2");
        when(show2.getViewCount()).thenReturn(500);
        when(show2.getStatus()).thenReturn(ShowStatus.PERFORMANCE_ONGOING);

        List<Show> topShows = Arrays.asList(show1, show2);
        when(showRepository.findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED)).thenReturn(topShows);

        // When
        List<HotShowResponse> result = rankingService.getHotShows();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getShowId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Show1");
        assertThat(result.get(0).getViewCount()).isEqualTo(1000);
        assertThat(result.get(0).getStatus()).isEqualTo(ShowStatus.RESERVATION_ONGOING);

        assertThat(result.get(1).getShowId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("Show2");
        assertThat(result.get(1).getViewCount()).isEqualTo(500);
        assertThat(result.get(1).getStatus()).isEqualTo(ShowStatus.PERFORMANCE_ONGOING);

        verify(listOperations).range("ranking:hot_shows", 0, -1);
        verify(showRepository).findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED);
    }

    @Test
    void Redis에서_인기_공연_역직렬화_실패_시_필터링() throws JsonProcessingException {
        // Given
        List<String> jsonShows = Arrays.asList(
                "{\"showId\":1,\"title\":\"Show1\",\"viewCount\":1000,\"status\":\"RESERVATION_ONGOING\",\"createdAt\":\"2025-04-21T10:00:00\"}",
                "{\"showId\":2,\"title\":\"Show2\",\"viewCount\":500,\"status\":\"PERFORMANCE_ONGOING\",\"createdAt\":\"2025-04-21T10:00:00\"}"
        );

        HotShow show1 = HotShow.create(1L, "Show1", 1000, ShowStatus.RESERVATION_ONGOING, now);

        when(listOperations.range("ranking:hot_shows", 0, -1)).thenReturn(jsonShows);
        when(objectMapper.readValue(jsonShows.get(0), HotShow.class)).thenReturn(show1);
        when(objectMapper.readValue(jsonShows.get(1), HotShow.class)).thenThrow(new JsonProcessingException("Error") {
        });

        // When
        List<HotShowResponse> result = rankingService.getHotShows();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getShowId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Show1");
        assertThat(result.get(0).getViewCount()).isEqualTo(1000);
        assertThat(result.get(0).getStatus()).isEqualTo(ShowStatus.RESERVATION_ONGOING);

        verify(listOperations).range("ranking:hot_shows", 0, -1);
        verify(objectMapper, times(2)).readValue(anyString(), eq(HotShow.class));
    }

    @Test
    void 인기_공연_조회_중_예외_발생_시_빈_리스트_반환() {
        // Given
        when(listOperations.range("ranking:hot_shows", 0, -1)).thenThrow(new RuntimeException("Redis error"));

        // When
        List<HotShowResponse> result = rankingService.getHotShows();

        // Then
        assertThat(result).isEmpty();
        verify(listOperations).range("ranking:hot_shows", 0, -1);
    }

    @Test
    void Redis에서_좋아요_공연_조회_성공() throws JsonProcessingException {
        // Given
        List<String> jsonShows = Arrays.asList(
                "{\"showId\":1,\"title\":\"Show1\",\"likeCount\":500,\"status\":\"RESERVATION_ONGOING\",\"createdAt\":\"2025-04-21T10:00:00\"}",
                "{\"showId\":2,\"title\":\"Show2\",\"likeCount\":300,\"status\":\"PERFORMANCE_ONGOING\",\"createdAt\":\"2025-04-21T10:00:00\"}"
        );

        LikeShow show1 = LikeShow.create(1L, "Show1", 500L, ShowStatus.RESERVATION_ONGOING, now);
        LikeShow show2 = LikeShow.create(2L, "Show2", 300L, ShowStatus.PERFORMANCE_ONGOING, now);

        when(listOperations.range("ranking:like_shows", 0, -1)).thenReturn(jsonShows);
        when(objectMapper.readValue(jsonShows.get(0), LikeShow.class)).thenReturn(show1);
        when(objectMapper.readValue(jsonShows.get(1), LikeShow.class)).thenReturn(show2);

        // When
        List<LikeShowResponse> result = rankingService.getLikeShows();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getShowId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Show1");
        assertThat(result.get(0).getLikeCount()).isEqualTo(500L);
        assertThat(result.get(0).getStatus()).isEqualTo(ShowStatus.RESERVATION_ONGOING);

        assertThat(result.get(1).getShowId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("Show2");
        assertThat(result.get(1).getLikeCount()).isEqualTo(300L);
        assertThat(result.get(1).getStatus()).isEqualTo(ShowStatus.PERFORMANCE_ONGOING);

        verify(listOperations).range("ranking:like_shows", 0, -1);
        verify(objectMapper, times(2)).readValue(anyString(), eq(LikeShow.class));
    }

    @Test
    void Redis에서_좋아요_공연_조회_실패_시_RDS_폴백() {
        // Given
        when(listOperations.range("ranking:like_shows", 0, -1)).thenReturn(Collections.emptyList());

        Object[] row1 = {1L, "Show1", 500L, ShowStatus.RESERVATION_ONGOING};
        Object[] row2 = {2L, "Show2", 300L, ShowStatus.PERFORMANCE_ONGOING};
        List<Object[]> topShows = Arrays.asList(row1, row2);

        Pageable pageable = PageRequest.of(0, 10);
        ShowStatus[] activeStatuses = {
                ShowStatus.RESERVATION_PENDING,
                ShowStatus.RESERVATION_ONGOING,
                ShowStatus.RESERVATION_CLOSED,
                ShowStatus.PERFORMANCE_ONGOING
        };

        when(likeRepository.findTop10ShowsByLikeCountAndStatusIn(eq(activeStatuses), any(Pageable.class))).thenReturn(topShows);

        // When
        List<LikeShowResponse> result = rankingService.getLikeShows();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getShowId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Show1");
        assertThat(result.get(0).getLikeCount()).isEqualTo(500L);
        assertThat(result.get(0).getStatus()).isEqualTo(ShowStatus.RESERVATION_ONGOING);

        assertThat(result.get(1).getShowId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("Show2");
        assertThat(result.get(1).getLikeCount()).isEqualTo(300L);
        assertThat(result.get(1).getStatus()).isEqualTo(ShowStatus.PERFORMANCE_ONGOING);

        verify(listOperations).range("ranking:like_shows", 0, -1);
        verify(likeRepository).findTop10ShowsByLikeCountAndStatusIn(eq(activeStatuses), any(Pageable.class));
    }

    @Test
    void Redis에서_좋아요_공연_역직렬화_실패_시_필터링() throws JsonProcessingException {
        // Given
        List<String> jsonShows = Arrays.asList(
                "{\"showId\":1,\"title\":\"Show1\",\"likeCount\":500,\"status\":\"RESERVATION_ONGOING\",\"createdAt\":\"2025-04-21T10:00:00\"}",
                "{\"showId\":2,\"title\":\"Show2\",\"likeCount\":300,\"status\":\"PERFORMANCE_ONGOING\",\"createdAt\":\"2025-04-21T10:00:00\"}"
        );

        LikeShow show1 = LikeShow.create(1L, "Show1", 500L, ShowStatus.RESERVATION_ONGOING, now);

        when(listOperations.range("ranking:like_shows", 0, -1)).thenReturn(jsonShows);
        when(objectMapper.readValue(jsonShows.get(0), LikeShow.class)).thenReturn(show1);
        when(objectMapper.readValue(jsonShows.get(1), LikeShow.class)).thenThrow(new JsonProcessingException("Error") {
        });

        // When
        List<LikeShowResponse> result = rankingService.getLikeShows();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getShowId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Show1");
        assertThat(result.get(0).getLikeCount()).isEqualTo(500L);
        assertThat(result.get(0).getStatus()).isEqualTo(ShowStatus.RESERVATION_ONGOING);

        verify(listOperations).range("ranking:like_shows", 0, -1);
        verify(objectMapper, times(2)).readValue(anyString(), eq(LikeShow.class));
    }

    @Test
    void 좋아요_공연_조회_중_예외_발생_시_빈_리스트_반환() {
        // Given
        when(listOperations.range("ranking:like_shows", 0, -1)).thenThrow(new RuntimeException("Redis error"));

        // When
        List<LikeShowResponse> result = rankingService.getLikeShows();

        // Then
        assertThat(result).isEmpty();
        verify(listOperations).range("ranking:like_shows", 0, -1);
    }

    @Test
    void RDS_폴백_시_빈_리스트_반환_확인_인기_공연() {
        // Given
        when(listOperations.range("ranking:hot_shows", 0, -1)).thenReturn(Collections.emptyList());
        when(showRepository.findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED)).thenReturn(new ArrayList<>());

        // When
        List<HotShowResponse> result = rankingService.getHotShows();

        // Then
        assertThat(result).isEmpty();
        verify(listOperations).range("ranking:hot_shows", 0, -1);
        verify(showRepository).findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED);
    }

    @Test
    void RDS_폴백_시_빈_리스트_반환_확인_좋아요_공연() {
        // Given
        when(listOperations.range("ranking:like_shows", 0, -1)).thenReturn(Collections.emptyList());
        ShowStatus[] activeStatuses = {
                ShowStatus.RESERVATION_PENDING,
                ShowStatus.RESERVATION_ONGOING,
                ShowStatus.RESERVATION_CLOSED,
                ShowStatus.PERFORMANCE_ONGOING
        };
        when(likeRepository.findTop10ShowsByLikeCountAndStatusIn(eq(activeStatuses), any(Pageable.class))).thenReturn(new ArrayList<>());

        // When
        List<LikeShowResponse> result = rankingService.getLikeShows();

        // Then
        assertThat(result).isEmpty();
        verify(listOperations).range("ranking:like_shows", 0, -1);
        verify(likeRepository).findTop10ShowsByLikeCountAndStatusIn(eq(activeStatuses), any(Pageable.class));
    }

    @Test
    void Redis에서_null_반환_시_RDS_폴백_인기_키워드() {
        // Given
        when(listOperations.range("ranking:popular_keywords", 0, -1)).thenReturn(null);

        Object[] row1 = {Category.MUSICAL, 100L};
        Object[] row2 = {Category.CONCERT, 50L};
        List<Object[]> topKeywords = Arrays.asList(row1, row2);

        when(searchLogRepository.findTopKeywordsSince(any(LocalDateTime.class))).thenReturn(topKeywords);

        // When
        List<PopularKeywordResponse> result = rankingService.getPopularKeywords();

        // Then
        assertThat(result).hasSize(2);
        verify(listOperations).range("ranking:popular_keywords", 0, -1);
        verify(searchLogRepository).findTopKeywordsSince(any(LocalDateTime.class));
    }

    @Test
    void Redis에서_null_반환_시_RDS_폴백_인기_공연() {
        // Given
        when(listOperations.range("ranking:hot_shows", 0, -1)).thenReturn(null);

        Show show1 = mock(Show.class);
        when(show1.getId()).thenReturn(1L);
        when(show1.getTitle()).thenReturn("Show1");
        when(show1.getViewCount()).thenReturn(1000);
        when(show1.getStatus()).thenReturn(ShowStatus.RESERVATION_ONGOING);

        List<Show> topShows = Collections.singletonList(show1);
        when(showRepository.findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED)).thenReturn(topShows);

        // When
        List<HotShowResponse> result = rankingService.getHotShows();

        // Then
        assertThat(result).hasSize(1);
        verify(listOperations).range("ranking:hot_shows", 0, -1);
        verify(showRepository).findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED);
    }

    @Test
    void Redis에서_null_반환_시_RDS_폴백_좋아요_공연() {
        // Given
        when(listOperations.range("ranking:like_shows", 0, -1)).thenReturn(null);

        Object[] row1 = {1L, "Show1", 500L, ShowStatus.RESERVATION_ONGOING};
        List<Object[]> topShows = Collections.singletonList(row1);

        ShowStatus[] activeStatuses = {
                ShowStatus.RESERVATION_PENDING,
                ShowStatus.RESERVATION_ONGOING,
                ShowStatus.RESERVATION_CLOSED,
                ShowStatus.PERFORMANCE_ONGOING
        };

        when(likeRepository.findTop10ShowsByLikeCountAndStatusIn(eq(activeStatuses), any(Pageable.class))).thenReturn(topShows);

        // When
        List<LikeShowResponse> result = rankingService.getLikeShows();

        // Then
        assertThat(result).hasSize(1);
        verify(listOperations).range("ranking:like_shows", 0, -1);
        verify(likeRepository).findTop10ShowsByLikeCountAndStatusIn(eq(activeStatuses), any(Pageable.class));
    }
}