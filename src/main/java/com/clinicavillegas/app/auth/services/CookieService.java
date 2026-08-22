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
    private static final String COOKIE_PATH_ACCESS_TOKEN = "/";
    private static final String COOKIE_PATH_REFRESH_TOKEN = EndpointPaths.AUTH_BASE + "/refresh";

    public void setTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        addCookie(response, TOKEN_COOKIE_NAME, token, maxAgeSeconds, COOKIE_PATH_ACCESS_TOKEN);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, token, maxAgeSeconds, COOKIE_PATH_REFRESH_TOKEN);
    }

    public void unsetTokenCookie(HttpServletResponse response) {
        addCookie(response, TOKEN_COOKIE_NAME, "", 0, COOKIE_PATH_ACCESS_TOKEN);
    }

    public void unsetRefreshTokenCookie(HttpServletResponse response) {
        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, "", 0, COOKIE_PATH_REFRESH_TOKEN);
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
                .secure(true)
                .sameSite("None")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
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
