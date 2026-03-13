package com.authnexus.centralapplication.Security;

import com.authnexus.centralapplication.domains.entities.Provider;
import com.authnexus.centralapplication.domains.entities.RefreshToken;
import com.authnexus.centralapplication.domains.entities.User;
import com.authnexus.centralapplication.repository.RefreshTokenRepo;
import com.authnexus.centralapplication.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final JwtServices jwtServices;
    private final CookieService cookieService;
    private final RefreshTokenRepo refreshTokenRepo;

    @Value("${app.auth.frontend-success-redirect}")
    private String frontEndSuccessUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        logger.info("Authentication Success");
        logger.info(authentication.toString());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String registrationId = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        logger.info("Registration Id: {}", registrationId);
        logger.info("OAuth attributes: {}", oAuth2User.getAttributes());

        User user;

        switch (registrationId) {

            case "google" -> {

                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();

                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .imageUrl(picture)
                        .enable(true)
                        .provider(Provider.GOOGLE)
                        .providerId(googleId)
                        .build();

                user = userRepository.findByEmail(email)
                        .orElseGet(() -> {
                            User savedUser = userRepository.save(newUser);
                            logger.info("User created: {}", savedUser.getEmail());
                            return savedUser;
                        });

                logger.info("User resolved: {}", user.getEmail());
            }

            case "github" -> {
                String name = String.valueOf(oAuth2User.getAttributes().getOrDefault("login", ""));
                String email = String.valueOf(oAuth2User.getAttributes().getOrDefault("email", ""));
                String githubId = String.valueOf(oAuth2User.getAttributes().getOrDefault("id", ""));
                String imageUrl = String.valueOf(oAuth2User.getAttributes().getOrDefault("avatar_url", ""));

                // GitHub may not return email unless proper scope is granted.
                if (email == null || email.isBlank()) {
                  email=name+"@github.com";
                }

                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .imageUrl(imageUrl)
                        .enable(true)
                        .provider(Provider.GITHUB)
                        .providerId(githubId)
                        .build();

                user = userRepository.findByEmail(email)
                        .orElseGet(() -> {
                            User savedUser = userRepository.save(newUser);
                            logger.info("User created: {}", savedUser.getEmail());
                            return savedUser;
                        });

                logger.info("User resolved: {}", user.getEmail());
            }

            default -> {
                throw new RuntimeException("Invalid registration id");
            }
        }


        String jti = UUID.randomUUID().toString();

        RefreshToken refreshTokenOp = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtServices.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepo.save(refreshTokenOp);

        /*
         * Generate JWT tokens
         */
        String accessToken = jwtServices.generateAccessToken(user);
        String refreshToken = jwtServices.generateRefreshToken(user, refreshTokenOp.getJti());

        cookieService.attachRefreshCookie(
                response,
                refreshToken,
                (int) jwtServices.getRefreshTtlSeconds()
        );


         //Return response

//        response.setContentType("application/json");
//        response.getWriter().write("Login successful");
        response.sendRedirect(frontEndSuccessUrl);
    }
}