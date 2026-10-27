package com.personal.marketnote.community.adapter.in.web.post.controller;

import com.personal.marketnote.common.adapter.in.ratelimit.RateLimited;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.PostFilterCategory;
import com.personal.marketnote.community.domain.post.PostFilterValue;
import com.personal.marketnote.community.domain.post.PostSearchTarget;
import com.personal.marketnote.community.domain.post.PostSortProperty;
import com.personal.marketnote.community.domain.post.PostTargetGroupType;
import com.personal.marketnote.community.domain.post.PostTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PostController 테스트")
class PostControllerTest {

    @Nested
    @DisplayName("RateLimited 어노테이션 부착 검증")
    class RateLimitedAnnotation {

        @Test
        @DisplayName("getPosts 메서드에 @RateLimited(key=posts-cursor, maxRequests=60, windowSeconds=60)이 부착되어 있다")
        void getPostsHasRateLimited() throws NoSuchMethodException {
            Method method = PostController.class.getMethod(
                    "getPosts",
                    Board.class, String.class, PostTargetGroupType.class, Long.class,
                    PostTargetType.class, Long.class, Long.class, int.class,
                    Sort.Direction.class, PostSortProperty.class,
                    PostSearchTarget.class, String.class,
                    PostFilterCategory.class, PostFilterValue.class,
                    OAuth2AuthenticatedPrincipal.class
            );

            RateLimited rateLimited = method.getAnnotation(RateLimited.class);

            assertThat(rateLimited).isNotNull();
            assertThat(rateLimited.key()).isEqualTo("posts-cursor");
            assertThat(rateLimited.maxRequests()).isEqualTo(60);
            assertThat(rateLimited.windowSeconds()).isEqualTo(60);
        }
    }
}
