package com.example.picket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PR실패테스트 {

    @Test
    void test() {
        // given
        int expected = 5;

        // when
        int answer = 3 + 1;

        // then
        assertEquals(expected, answer);
    }
}
