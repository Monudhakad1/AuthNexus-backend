package com.authnexus.centralapplication.controller;

import com.authnexus.centralapplication.Mapper.UserMapper;
import com.authnexus.centralapplication.Security.CookieService;
import com.authnexus.centralapplication.Security.JwtServices;
import com.authnexus.centralapplication.domains.dto.LoginRequest;
import com.authnexus.centralapplication.domains.dto.RefreshTokenRequest;
import com.authnexus.centralapplication.domains.dto.TokenResponse;
import com.authnexus.centralapplication.domains.dto.UserDto;
import com.authnexus.centralapplication.domains.entities.RefreshToken;
import com.authnexus.centralapplication.domains.entities.User;
import com.authnexus.centralapplication.repository.RefreshTokenRepo;
import com.authnexus.centralapplication.repository.UserRepository;
import com.authnexus.centralapplication.services.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtServices jwtServices;
    private final RefreshTokenRepo refreshTokenRepo;
    private final UserMapper mapper;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        Authentication authenticate = authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("Invalid Username"));
        if (!user.isEnable()) {
            throw new DisabledException("User is disabled");
        }

        String jti = UUID.randomUUID().toString();
        var refreshTokenOp = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtServices.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        //save info of rf token
        refreshTokenRepo.save(refreshTokenOp);
        String refreshToken = jwtServices.generateRefreshToken(user, refreshTokenOp.getJti());  // we can directly use jti dont need to fetch again from database
        String accessToken = jwtServices.generateAccessToken(user);

        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtServices.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);
        TokenResponse tokenResponse = TokenResponse.of(
                accessToken, refreshToken,
                jwtServices.getAccessTtlSeconds(),
                mapper.toUserDto(user)
        );
        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

    }

    //refresh token regeneration
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request) {

        String refreshToken = readRefreshTokenFromRequest(body, request)
                .orElseThrow(() -> new BadCredentialsException("Refresh token is missing"));

        // call service layer
        if (jwtServices.isAccessToken(refreshToken)) {
            throw new BadCredentialsException("Provided token is not a refresh token");
        }

        String jti = jwtServices.getJti(refreshToken);
        UUID userId = jwtServices.getUserId(jti);
        RefreshToken storedRefreshToken = refreshTokenRepo.findByJti(jti).orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (storedRefreshToken.isRevoked()) {
            throw new BadCredentialsException("Refresh token is revoked");
        }

        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token is expired");
        }

        if (!storedRefreshToken.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Refresh token does not belong to the expected user");
        }

        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepo.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();

        var newRefreshTokenOp = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtServices.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepo.save(newRefreshTokenOp);

        String newAccessToken = jwtServices.generateAccessToken(user);
        String newRefreshToken = jwtServices.generateRefreshToken(user, newJti);

        cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtServices.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        return ResponseEntity.ok(TokenResponse.of(newAccessToken, newRefreshToken, jwtServices.getAccessTtlSeconds(), mapper.toUserDto(user)));

    }

    @PostMapping("/logout")
    public ResponseEntity<TokenResponse> logout(
            HttpServletResponse response,
            HttpServletRequest request
    ){
        readRefreshTokenFromRequest(null, request).ifPresent(rt -> {
            try {
                String jti = jwtServices.getJti(rt);
                refreshTokenRepo.findByJti(jti).ifPresent(storedRt -> {
                    storedRt.setRevoked(true);
                    refreshTokenRepo.save(storedRt);
                });
            } catch (JwtException e) {

            }
        });
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
    private Optional<String> readRefreshTokenFromRequest(
            RefreshTokenRequest body,
            HttpServletRequest request) {

        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }

        if (request.getCookies() != null) {
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> !v.isBlank())
                    .findFirst();
            if (fromCookie.isPresent()) {
                return fromCookie;
            }
        }
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }

        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isEmpty()) {
            return Optional.of(refreshHeader);
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer", 0, 7)) {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()) {
                try {
                    if (jwtServices.isRefreshToken(candidate)) {
                        return Optional.of(candidate);
                    }
                } catch (Exception e) {

                }
            }
        }
        return Optional.empty();
    }


    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }

}
