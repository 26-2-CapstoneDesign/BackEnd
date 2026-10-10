package com.youthguide.api.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CSRF 토큰은 필요할 때만 만들어지므로, 매 요청마다 토큰을 읽어서 XSRF-TOKEN 쿠키가 항상 내려가게 한다.
 * 프론트는 아무 GET 요청(예: /api/v1/me) 후 쿠키 값을 X-XSRF-TOKEN 헤더로 보내면 된다.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		if (csrfToken != null) {
			csrfToken.getToken();
		}
		filterChain.doFilter(request, response);
	}
}
