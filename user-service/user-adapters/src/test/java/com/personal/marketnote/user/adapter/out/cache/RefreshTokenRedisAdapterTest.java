package com.personal.marketnote.user.adapter.out.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenRedisAdapter 리프레시 토큰 Redis 캐시")
class RefreshTokenRedisAdapterTest {

    @InjectMocks
    private RefreshTokenRedisAdapter adapter;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @DisplayName("회원 ID 기반 키로 Redis에서 리프레시 토큰을 삭제한다")
    void deletesRefreshTokenByUserId() {
        // when
        adapter.deleteByUserId(100L);

        // then
        verify(stringRedisTemplate).delete("refreshToken:100");
        verifyNoMoreInteractions(stringRedisTemplate);
    }

    @Test
    @DisplayName("다른 회원 ID를 전달하면 해당 ID 키로 삭제 호출이 전파된다")
    void usesSpecifiedUserIdForDeletion() {
        // when
        adapter.deleteByUserId(42L);

        // then
        verify(stringRedisTemplate).delete("refreshToken:42");
        verifyNoMoreInteractions(stringRedisTemplate);
    }
}
