package com.github.computerhuis.huishub.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LogFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "RID";

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        MDC.put(REQUEST_ID, " [%s]".formatted(UUID.randomUUID().toString()));
        filterChain.doFilter(request, response);
        MDC.clear();
    }
}
