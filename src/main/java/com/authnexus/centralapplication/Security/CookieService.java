package com.authnexus.centralapplication.Security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
@Getter
@Service
public class CookieService {

    private final String refreshTokenCookieName ;
    private final boolean cookieHttpOnly ;
    private final boolean cookieSecure;

    private final String cookieDomain;
    private final String cookieSameSite;

    public CookieService(@Value("${security.jwt.refresh-token-cookie-name} ") String refreshTokenCookieName,
                         @Value("${security.jwt.cookie-same-site} ")String cookieSameSite,
                         @Value("${security.jwt.cookie-http-only} ") boolean cookieHttpOnly,
                         @Value("${security.jwt.cookie-secure} ")boolean cookieSecure,
                         @Value("${security.jwt.cookie-domain} ")String cookieDomain) {

        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }
    //method to attach cookie to response
    public void attackRefreshCookie(HttpServletResponse response , String val, int maxAge ){
        var cookie = ResponseCookie.from(refreshTokenCookieName, val)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .domain(cookieDomain)
                .sameSite(cookieSameSite)
                .maxAge(maxAge)
                .path("/");
        if(cookieDomain !=null && !cookieDomain.isBlank() ){
            cookie.domain(cookieDomain);
        }
        ResponseCookie responseCookie=cookie.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public void clearRefreshCookie(HttpServletResponse response){
        var cookie = ResponseCookie.from(refreshTokenCookieName, "")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .domain(cookieDomain)
                .sameSite(cookieSameSite)
                .maxAge(0)
                .path("/");
        if(cookieDomain !=null && !cookieDomain.isBlank() ){
            cookie.domain(cookieDomain);
        }
        ResponseCookie responseCookie=cookie.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public void addNoStoreHeaders(HttpServletResponse response ){
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");

    }

}
