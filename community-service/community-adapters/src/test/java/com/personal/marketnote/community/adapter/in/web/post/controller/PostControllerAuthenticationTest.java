package com.personal.marketnote.community.adapter.in.web.post.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.exception.token.AuthenticationFailedException;
import com.personal.marketnote.community.adapter.in.web.post.request.RegisterPostRequest;
import com.personal.marketnote.community.adapter.in.web.post.response.GetPostsResponse;
import com.personal.marketnote.community.adapter.in.web.post.response.PostItemResponse;
import com.personal.marketnote.community.adapter.in.web.post.response.RegisterPostResponse;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.PostFilterCategory;
import com.personal.marketnote.community.domain.post.PostFilterValue;
import com.personal.marketnote.community.domain.post.PostSearchTarget;
import com.personal.marketnote.community.domain.post.PostSortProperty;
import com.personal.marketnote.community.domain.post.PostTargetType;
import com.personal.marketnote.community.port.in.command.post.GetPostQuery;
import com.personal.marketnote.community.port.in.command.post.RegisterPostCommand;
import com.personal.marketnote.community.port.in.result.post.GetPostsResult;
import com.personal.marketnote.community.port.in.result.post.PostItemResult;
import com.personal.marketnote.community.port.in.result.post.RegisterPostResult;
import com.personal.marketnote.community.port.in.usecase.post.GetPostKeyUseCase;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.in.usecase.post.GetUserOneOnOneInquiryPostsUseCase;
import com.personal.marketnote.community.port.in.usecase.post.GetUserProductInquiryPostsUseCase;
import com.personal.marketnote.community.port.in.usecase.post.RegisterPostUseCase;
import com.personal.marketnote.community.port.in.usecase.post.UpdatePostUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostController 인증 검증")
class PostControllerAuthenticationTest {

    @Mock
    private RegisterPostUseCase registerPostUseCase;

    @Mock
    private GetPostUseCase getPostUseCase;

    @Mock
    private GetPostKeyUseCase getPostKeyUseCase;

    @Mock
    private GetUserProductInquiryPostsUseCase getUserProductInquiryPostsUseCase;

    @Mock
    private GetUserOneOnOneInquiryPostsUseCase getUserOneOnOneInquiryPostsUseCase;

    @Mock
    private UpdatePostUseCase updatePostUseCase;

    @InjectMocks
    private PostController controller;

    private RegisterPostRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = buildRegisterRequest(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", null);
    }

    @Nested
    @DisplayName("registerPost 인증")
    class RegisterPostAuth {

        @Test
        @DisplayName("판매자 권한이면 isSeller=true로 UseCase가 호출된다")
        void callsUseCaseWithIsSellerTrueWhenSellerAuthority() {
            OAuth2AuthenticatedPrincipal principal = principal("100", "ROLE_SELLER");
            when(registerPostUseCase.registerPost(eq(true), any(RegisterPostCommand.class)))
                    .thenReturn(new RegisterPostResult(1L, UUID.randomUUID()));

            ResponseEntity<BaseResponse<RegisterPostResponse>> response =
                    controller.registerPost(registerRequest, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerPostUseCase).registerPost(eq(true), any(RegisterPostCommand.class));
        }

        @Test
        @DisplayName("관리자/판매자가 아니면 isSeller=false로 호출된다")
        void callsUseCaseWithIsSellerFalseWhenBuyer() {
            OAuth2AuthenticatedPrincipal principal = principal("100", "ROLE_BUYER");
            when(registerPostUseCase.registerPost(eq(false), any(RegisterPostCommand.class)))
                    .thenReturn(new RegisterPostResult(1L, UUID.randomUUID()));

            controller.registerPost(registerRequest, principal);

            verify(registerPostUseCase).registerPost(eq(false), any(RegisterPostCommand.class));
        }

        @Test
        @DisplayName("공지 게시판 작성 시 관리자 권한이 없으면 AccessDeniedException")
        void throwsAccessDeniedWhenNoticeWithoutAdminAuthority() {
            RegisterPostRequest noticeRequest = buildRegisterRequest(Board.NOTICE, "GENERAL", null);
            OAuth2AuthenticatedPrincipal principal = principal("100", "ROLE_BUYER");

            assertThatThrownBy(() -> controller.registerPost(noticeRequest, principal))
                    .isInstanceOf(AccessDeniedException.class);
            verifyNoInteractions(registerPostUseCase);
        }

        @Test
        @DisplayName("상품 문의 답글(parentId 존재) 작성 시 판매자/관리자가 아니면 AccessDeniedException")
        void throwsAccessDeniedForProductInquiryReplyWithoutSellerOrAdmin() {
            RegisterPostRequest replyRequest = buildRegisterRequest(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", 999L);
            OAuth2AuthenticatedPrincipal principal = principal("100", "ROLE_BUYER");

            assertThatThrownBy(() -> controller.registerPost(replyRequest, principal))
                    .isInstanceOf(AccessDeniedException.class);
            verify(registerPostUseCase, never()).registerPost(anyBoolean(), any());
        }
    }

    @Nested
    @DisplayName("getPosts 인증")
    class GetPostsAuth {

        @Test
        @DisplayName("비회원 게시판(NOTICE)은 principal 없어도 통과한다")
        void allowsNonMemberBoardWithoutPrincipal() {
            when(getPostUseCase.getPosts(any())).thenReturn(emptyGetPostsResult());

            ResponseEntity<BaseResponse<GetPostsResponse>> response =
                    invokeGetPosts(Board.NOTICE, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getPostUseCase).getPosts(any());
        }

        @Test
        @DisplayName("targetType이 있으면 principal 없어도 통과한다")
        void allowsTargetTypePresentWithoutPrincipal() {
            when(getPostUseCase.getPosts(any())).thenReturn(emptyGetPostsResult());

            ResponseEntity<BaseResponse<GetPostsResponse>> response =
                    invokeGetPosts(Board.PRODUCT_INQUERY, PostTargetType.PRICE_POLICY, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getPostUseCase).getPosts(any());
        }

        @Test
        @DisplayName("회원 게시판인데 principal이 null이면 AuthenticationFailedException")
        void throwsAuthFailedWhenMemberOnlyBoardAndPrincipalNull() {
            assertThatThrownBy(() -> invokeGetPosts(Board.PRODUCT_INQUERY, null, null))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(getPostUseCase);
        }

        @Test
        @DisplayName("회원 게시판인데 principal name이 -1이면 AuthenticationFailedException")
        void throwsAuthFailedWhenPrincipalNameIsMinusOne() {
            OAuth2AuthenticatedPrincipal principal = principal("-1", "ROLE_BUYER");

            assertThatThrownBy(() -> invokeGetPosts(Board.PRODUCT_INQUERY, null, principal))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(getPostUseCase);
        }

        @Test
        @DisplayName("회원 게시판이고 유효한 principal이면 통과한다")
        void allowsValidPrincipalForMemberOnlyBoard() {
            OAuth2AuthenticatedPrincipal principal = principal("100", "ROLE_BUYER");
            when(getPostUseCase.getPosts(any())).thenReturn(emptyGetPostsResult());

            ResponseEntity<BaseResponse<GetPostsResponse>> response =
                    invokeGetPosts(Board.PRODUCT_INQUERY, null, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("getPost 인증")
    class GetPostAuth {

        @Test
        @DisplayName("비회원 게시판(FAQ)은 principal 없어도 통과한다")
        void allowsNonMemberBoardWithoutPrincipal() {
            when(getPostUseCase.getPost(any(GetPostQuery.class))).thenReturn(emptyPostItemResult());

            ResponseEntity<BaseResponse<PostItemResponse>> response =
                    controller.getPost(Board.FAQ, null, 1L, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getPostUseCase).getPost(any(GetPostQuery.class));
        }

        @Test
        @DisplayName("회원 게시판인데 principal이 null이면 AuthenticationFailedException")
        void throwsAuthFailedWhenPrincipalNull() {
            assertThatThrownBy(() -> controller.getPost(Board.ONE_ON_ONE_INQUERY, null, 1L, null))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(getPostUseCase);
        }

        @Test
        @DisplayName("회원 게시판인데 principal name이 -1이면 AuthenticationFailedException")
        void throwsAuthFailedWhenPrincipalNameIsMinusOne() {
            OAuth2AuthenticatedPrincipal principal = principal("-1", "ROLE_BUYER");

            assertThatThrownBy(() -> controller.getPost(Board.ONE_ON_ONE_INQUERY, null, 1L, principal))
                    .isInstanceOf(AuthenticationFailedException.class);
        }

        @Test
        @DisplayName("targetType이 있으면 principal 없어도 통과한다")
        void allowsTargetTypePresentWithoutPrincipal() {
            when(getPostUseCase.getPost(any(GetPostQuery.class))).thenReturn(emptyPostItemResult());

            ResponseEntity<BaseResponse<PostItemResponse>> response =
                    controller.getPost(Board.PRODUCT_INQUERY, PostTargetType.PRICE_POLICY, 1L, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getPostUseCase).getPost(any(GetPostQuery.class));
        }
    }

    @Nested
    @DisplayName("getPostKey 인증")
    class GetPostKeyAuth {

        @Test
        @DisplayName("principal이 null이면 AuthenticationFailedException")
        void throwsWhenPrincipalNull() {
            assertThatThrownBy(() -> controller.getPostKey(1L, null))
                    .isInstanceOf(AuthenticationFailedException.class);
        }

        @Test
        @DisplayName("principal name이 -1이면 AuthenticationFailedException")
        void throwsWhenPrincipalNameIsMinusOne() {
            OAuth2AuthenticatedPrincipal principal = principal("-1", "ROLE_BUYER");

            assertThatThrownBy(() -> controller.getPostKey(1L, principal))
                    .isInstanceOf(AuthenticationFailedException.class);
        }
    }

    private ResponseEntity<BaseResponse<GetPostsResponse>> invokeGetPosts(
            Board board, PostTargetType targetType, OAuth2AuthenticatedPrincipal principal
    ) {
        return controller.getPosts(
                board, null, null, null, targetType, null, null, 10,
                Sort.Direction.DESC,
                PostSortProperty.ID,
                (PostSearchTarget) null, null,
                (PostFilterCategory) null, (PostFilterValue) null,
                principal
        );
    }

    private GetPostsResult emptyGetPostsResult() {
        return GetPostsResult.of(false, null, 0L, List.of());
    }

    private PostItemResult emptyPostItemResult() {
        return PostItemResult.builder()
                .id(1L)
                .board("FAQ")
                .replies(List.of())
                .build();
    }

    private RegisterPostRequest buildRegisterRequest(Board board, String category, Long parentId) {
        RegisterPostRequest request = new RegisterPostRequest();
        ReflectionTestUtils.setField(request, "board", board);
        ReflectionTestUtils.setField(request, "category", category);
        ReflectionTestUtils.setField(request, "writerName", "작성자");
        ReflectionTestUtils.setField(request, "title", "제목");
        ReflectionTestUtils.setField(request, "content", "내용");
        ReflectionTestUtils.setField(request, "isPrivate", false);
        ReflectionTestUtils.setField(request, "isPhoto", false);
        if (parentId != null) {
            ReflectionTestUtils.setField(request, "parentId", parentId);
        }
        return request;
    }

    private OAuth2AuthenticatedPrincipal principal(String name, String authority) {
        return new OAuth2AuthenticatedPrincipal() {
            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority(authority));
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
