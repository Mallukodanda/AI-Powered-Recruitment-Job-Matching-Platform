package com.recruitment.platform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that populates the SLF4J Mapped Diagnostic Context (MDC) with traceId and user principal
 * for end-to-end request tracing and structured observability.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Request-ID";
    public static final String MDC_TRACE_ID_KEY = "traceId";
    public static final String MDC_USER_KEY = "user";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract or generate unique trace ID
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString().substring(0, 8);
            }
            MDC.put(MDC_TRACE_ID_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);

            // Extract user from security context if already present
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                MDC.put(MDC_USER_KEY, auth.getName());
            } else {
                MDC.put(MDC_USER_KEY, "anonymous");
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
