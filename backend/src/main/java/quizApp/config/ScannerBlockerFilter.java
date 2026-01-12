package quizApp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
@Slf4j
@Component
public class ScannerBlockerFilter extends OncePerRequestFilter {

    private final List<String> suspiciousPatterns = Arrays.asList(
            ".php", ".asp", ".aspx", ".jsp", ".cgi", ".pl", ".py",
            "/wp-", "/admin/", "/shell", "/backdoor", "/cmd", "/cgi-bin",
            "/console", "/phpmyadmin", "/mysql", "/sql", "/backup",
            "/wp-content", "/wp-includes", "/wp-admin", "/xmlrpc",
            "/well-known", "/sitemap", "/robots"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String path = request.getRequestURI().toLowerCase();
        String method = request.getMethod();

        // Блокируем только GET-запросы к подозрительным путям
        if ("GET".equals(method) && isSuspiciousPath(path)) {
            response.setStatus(404);  // Возвращаем 404
            response.setContentType("text/plain");
            response.getWriter().write("Not Found");

            // Логируем для анализа
            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            log.warn("🚨 Blocked scanner: IP={}, Path={}, UA={}", ip, path, userAgent);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isSuspiciousPath(String path) {
        return suspiciousPatterns.stream()
                .anyMatch(pattern -> {
                    // Для путей, начинающихся с /
                    if (pattern.startsWith("/")) {
                        return path.startsWith(pattern);
                    }
                    // Для расширений файлов
                    return path.contains(pattern);
                });
    }
}