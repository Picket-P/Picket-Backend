package com.example.picket.integration.session;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisSessionTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private Cookie[] cookies;

    @BeforeEach
    void 회원가입_및_이메일_로그인_요청_및_세션_저장() throws Exception {
        //회원가입
        mockMvc.perform(post("/api/v1/auth/signup/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"" +
                                ",\"password\":\"Test123!!\"" +
                                ", \"nickname\": \"테스터\"" +
                                ", \"birth\": \"2000-12-12\"" +
                                ", \"gender\": \"MALE\"}")
                )
                .andExpect(status().isOk());

        // 로그인
        MvcResult result = mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"Test123!!\"}"))
                .andExpect(status().isOk())
                .andReturn();

        cookies = result.getResponse().getCookies();
    }

    @AfterEach
    void tearDown() {
        RedisConnection connection = redisConnectionFactory.getConnection();
        connection.flushAll(); // 테스트 후 Redis 전체 비우기
    }

    @Test
    void Redis에_세션이_저장() {
        RedisConnection connection = redisConnectionFactory.getConnection();
        Set<byte[]> keys = connection.keys("spring:session:sessions:*".getBytes());
        assertThat(keys).isNotEmpty();
    }

    @Test
    void TTL이_요청_시마다_자동_갱신_체크() throws Exception {
        RedisConnection connection = redisConnectionFactory.getConnection();
        String sessionId = extractSessionIdFromCookie(cookies);

        // TTL이 줄어들도록 대기
        Thread.sleep(5000);
        Long ttlBefore = connection.ttl(("spring:session:sessions:" + sessionId).getBytes());
        System.out.println("TTL Before = " + ttlBefore);

        mockMvc.perform(get("/api/v1/users").cookie(cookies))
                .andExpect(status().isOk());

        Long ttlAfter = connection.ttl(("spring:session:sessions:" + sessionId).getBytes());
        System.out.println("TTL After  = " + ttlAfter);

        assertThat(ttlAfter).isGreaterThan(ttlBefore);
    }

    @Test
    void 세션_만료_시_재로그인() throws Exception {
        String sessionId = extractSessionIdFromCookie(cookies);
        RedisConnection connection = redisConnectionFactory.getConnection();
        connection.del(("spring:session:sessions:" + sessionId).getBytes());

        mockMvc.perform(get("/api/v1/auth/users").cookie(cookies))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그아웃_호출시_Redis_세션_삭제() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signout").cookie(cookies))
                .andExpect(status().isOk());

        String sessionId = extractSessionIdFromCookie(cookies);
        RedisConnection connection = redisConnectionFactory.getConnection();
        Boolean exists = connection.exists(("spring:session:sessions:" + sessionId).getBytes());
        assertThat(exists).isFalse();
    }

    private String extractSessionIdFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("JSESSIONID"))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow();
    }
}
