package com.moneylog.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JWTAuthorizationFilterTest {

    @Mock
    private JWTTokenProvider jwtTokenProvider;

    @Mock
    private PrincipalDetailsService principalDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTAuthorizationFilter jwtAuthorizationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 유효한_토큰이면_SecurityContext에_인증정보가_설정된다() throws Exception {
        // given
        String token = "valid-token";
        String email = "test@example.com";
        UserDetails userDetails = mock(UserDetails.class);

        given(request.getHeader("Authorization")).willReturn("Bearer " + token);
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getEmailFromToken(token)).willReturn(email);
        given(principalDetailsService.loadUserByUsername(email)).willReturn(userDetails);
        given(userDetails.getAuthorities()).willReturn(List.of());

        //when
        jwtAuthorizationFilter.doFilter(request, response, filterChain);

        //then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void 토큰이_없으면_SecurityContext에_인증정보가_설정되지_않는다() throws Exception {
        //given
        given(request.getHeader("Authorization")).willReturn(null);

        //when
        jwtAuthorizationFilter.doFilter(request, response, filterChain);

        //then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        verify(filterChain).doFilter(request, response);
    }

}