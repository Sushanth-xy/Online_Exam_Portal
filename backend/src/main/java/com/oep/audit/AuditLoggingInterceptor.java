package com.oep.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC Handler Interceptor for Automated Audit Trail Logging.
 * Implements SRS OEP-SR-004 and STP TC-Sec-04.
 */
@Component
public class AuditLoggingInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    public AuditLoggingInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Only log API endpoints
        if (!uri.startsWith("/api/v1/")) {
            return;
        }

        // Avoid logging repeated lightweight polling if any, but log all mutating actions
        // and audit-critical actions (submissions, grading, auth, exam delivery)
        boolean isMutating = "POST".equalsIgnoreCase(method) ||
                "PUT".equalsIgnoreCase(method) ||
                "DELETE".equalsIgnoreCase(method) ||
                "PATCH".equalsIgnoreCase(method);

        boolean isAuditSensitive = uri.contains("/exams/") ||
                uri.contains("/grading/") ||
                uri.contains("/auth/") ||
                uri.contains("/admin/");

        if (!isMutating && !isAuditSensitive) {
            return;
        }

        String userEmail = resolveUserEmail(request);
        String role = resolveUserRole();
        String ipAddress = extractClientIp(request);
        int status = response.getStatus();
        String action = categorizeAction(method, uri);
        String details = "Method=" + method + " URI=" + uri + " Status=" + status;

        try {
            auditService.logEvent(userEmail, role, action, method, uri, ipAddress, status, details);
        } catch (Exception e) {
            // Audit logging should never bring down the primary HTTP response
        }
    }

    private String resolveUserEmail(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "anonymous";
    }

    private String resolveUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                    .findFirst()
                    .orElse("AUTHENTICATED");
        }
        return "ANONYMOUS";
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    private String categorizeAction(String method, String uri) {
        if (uri.matches(".*/api/v1/exams/\\d+/submit.*")) {
            return "EXAM_SUBMISSION";
        } else if (uri.matches(".*/api/v1/grading/submissions/\\d+/grade.*")) {
            return "MANUAL_GRADING";
        } else if (uri.contains("/api/v1/grading/")) {
            return "GRADING_QUERY";
        } else if (uri.matches(".*/api/v1/exams/\\d+/start.*")) {
            return "EXAM_START";
        } else if (uri.matches(".*/api/v1/exams/\\d+/save.*")) {
            return "EXAM_AUTOSAVE";
        } else if (uri.contains("/api/v1/auth/login")) {
            return "AUTH_LOGIN";
        } else if (uri.contains("/api/v1/auth/register")) {
            return "AUTH_REGISTER";
        } else if (uri.contains("/api/v1/auth/forgot-password")) {
            return "AUTH_FORGOT_PASSWORD";
        } else if (uri.contains("/api/v1/auth/reset-password")) {
            return "AUTH_PASSWORD_RESET";
        } else if (uri.contains("/api/v1/question-bank")) {
            return "QUESTION_BANK_" + method.toUpperCase();
        } else if (uri.contains("/api/v1/admin/")) {
            return "ADMIN_OPERATION";
        } else if (uri.equals("/api/v1/exams") && "POST".equalsIgnoreCase(method)) {
            return "EXAM_CREATION";
        }
        return method.toUpperCase() + "_API";
    }
}
