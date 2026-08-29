package com.moneylog.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final JWTTokenProvider jwtTokenProvider;
    private final PrincipalDetailsService principalDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 쿠키에서 토큰 추출
        String token = resolveToken(request);

        if(token != null && jwtTokenProvider.validateToken(token)) {
            try {
                // 2. 토큰 검증 통과 시, 토큰에서 이메일을 꺼내 사용자 조회
                String email = jwtTokenProvider.getEmailFromToken(token);
                UserDetails userDetails = principalDetailsService.loadUserByUsername(email);

                // 3. SecurityContext에 인증 정보 등록 (이후 요청 처리 동안 "로그인된 사용자"로 인식됨)
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException e) {
                // 탈퇴 등 사용자 못찾는 경우 등록안하고 넘어감(permitAll 경로 막히는것 방지)
            }

        }

        // 4. 다음 필터로 전달 (토큰이 없거나 유효하지 않아도 항상 진행)
        filterChain.doFilter(request, response);

    }

    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            return null;
        }

        for(Cookie cookie : cookies) {
            if(cookie.getName().equals("accessToken")) {
                return cookie.getValue();
            }
        }

        return null;
    }

}
