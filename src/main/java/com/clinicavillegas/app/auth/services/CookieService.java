package com.clinicavillegas.app.auth.services;


import com.clinicavillegas.app.common.EndpointPaths;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/"; // Root path for general cookies

    public void setTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        addCookie(response, TOKEN_COOKIE_NAME, token, maxAgeSeconds, COOKIE_PATH);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        // It's generally a good practice to set refresh tokens to a more specific path
        // if they are only used by a specific endpoint (e.g., /auth/refresh)
        // However, if it's needed globally, keep COOKIE_PATH.
        // For simplicity, I'm keeping COOKIE_PATH for both in this refactor, but be aware of the option.
        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, token, maxAgeSeconds, COOKIE_PATH);
    }

    public void unsetTokenCookie(HttpServletResponse response) {
        addCookie(response, TOKEN_COOKIE_NAME, "", 0, COOKIE_PATH);
    }

    public void unsetRefreshTokenCookie(HttpServletResponse response) {
        // Ensure the path matches the one used when setting the cookie for successful deletion
        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, "", 0, COOKIE_PATH);
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        return getCookieValue(request, TOKEN_COOKIE_NAME);
    }

    public String getRefreshTokenFromRequest(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * Helper method to create and add a ResponseCookie to the HttpServletResponse.
     */
    private void addCookie(HttpServletResponse response, String name, String value, long maxAge, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true) // Should always be true in production with HTTPS
                .sameSite("Strict") // Protects against CSRF
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString()); // Use addHeader to avoid overwriting other headers
    }

    /**
     * Helper method to retrieve a cookie value from the HttpServletRequest.
     */
    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
