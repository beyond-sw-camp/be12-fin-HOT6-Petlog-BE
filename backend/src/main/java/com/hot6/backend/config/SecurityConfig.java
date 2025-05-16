package com.hot6.backend.config;


import com.hot6.backend.config.filter.JwtFilter;
import com.hot6.backend.config.filter.LoginFilter;
import com.hot6.backend.redis.RefreshTokenRepository;
import com.hot6.backend.user.CustomOAuth2UserService;
import com.hot6.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration configuration;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final RefreshTokenRepository refreshTokenRepository;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomOAuth2UserService customOAuth2UserService(UserService userService) {
        return new CustomOAuth2UserService(userService);
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http,
                                         CustomOAuth2UserService customOAuth2UserService,
                                         CustomAuthFailureHandler customAuthFailureHandler,
                                         OAuth2FailureHandler oAuth2FailureHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 엔드포인트들
                .requestMatchers(
                        "/user/sign-up",
                        "/user/login",
                        "/user/verify-email",
                        "/user/auth/check",
                        "/user/email/check",
                        "/user/token/refresh",
                        "/oauth2/authorization/kakao",
                        "/user/login/error/**",
                        "/login/oauth2/**",
                        "/chat/",
                        "/chat/search",
                        "/chat/chatroom/{chatRoomIdx:[0-9]+}",
                        "/chat/chatroom/ws-doc",
                        "/post/list/**",
                        "/post/search",
                        "/category/list",
                        "/place/**",
                        "/actuator/health"
                ).permitAll()


                .requestMatchers("/schedule").hasAuthority("USER")
                .requestMatchers("/admin").hasAuthority("ADMIN")
                .requestMatchers("/category/register").hasAuthority("ADMIN")
                .requestMatchers("/category/{categoryIdx:[0-9]+}").hasAuthority("ADMIN")

                .anyRequest().authenticated()
        );

        http.oauth2Login(config -> {
            config.successHandler(oAuth2SuccessHandler);
            config.failureHandler(oAuth2FailureHandler);
            config.userInfoEndpoint(endpoint ->
                    endpoint.userService(customOAuth2UserService)
            );
        });

        // 기존에 사용자한테 설정하도록 한 쿠키(JSESSIONID)를 사용하지 않도록 하는 설정
        http.sessionManagement(AbstractHttpConfigurer::disable);

        http.addFilterAt(
                new LoginFilter(configuration.getAuthenticationManager(), customAuthFailureHandler, refreshTokenRepository),
                UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
