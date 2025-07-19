package com.estate.configuration;

import com.estate.domain.constant.MessageCode;
import com.estate.domain.entity.User;
import com.estate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected WebSecurityCustomizer ignoringCustomizer(){
        return (web) -> web.ignoring().antMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/assets/**");
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf()
            .and()
                .sessionManagement()
                .maximumSessions(1).expiredUrl("/237in").and()
            .and()
                .exceptionHandling()
                .accessDeniedPage("/error/403")
            .and()
                .formLogin()
                    .usernameParameter("email")
                    .loginPage("/237in")
                    .loginProcessingUrl("/237in")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureHandler(new AuthenticationFailureHandler())
            .and()
                .logout()
                    .deleteCookies("JSESSIONID")
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(new AuthenticationLogoutSuccessHandler())
                    .logoutSuccessUrl("/237in")
                    .invalidateHttpSession(true)
            .and()
                .authorizeRequests()
                    .antMatchers("/dashboard", "/housing/**", "/lease/**", "/log/**", "/payment/**", "/setting/**", "/standing/**", "/student/**", "/user/**", "/policy").authenticated()
                    .anyRequest().permitAll();
        return http.build();
    }

    @Component
    @RequiredArgsConstructor
    public static class CustomAuthenticationProvider implements AuthenticationProvider {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String email = authentication.getName();
            String password = (String) authentication.getCredentials();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null || !user.getEmail().equals(email)) throw new BadCredentialsException(MessageCode.INCORRECT_EMAIL);
            if(!passwordEncoder.matches(password, user.getPassword())) throw new BadCredentialsException(MessageCode.INCORRECT_PASSWORD);
            if(!user.isActive()) throw new BadCredentialsException(MessageCode.ACCOUNT_DISABLED);
            user.setLastLogin(LocalDateTime.now());
            user = userRepository.save(user);
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attributes.getRequest().getSession(true);
            session.setAttribute("user", user);
            Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream().map(Enum::name).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(email, password, authorities);
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return authentication.equals(UsernamePasswordAuthenticationToken.class);
        }

    }

    private static class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
            String url = "/237in";
            if(exception != null) {
                String message = exception.getMessage();
                if(MessageCode.INCORRECT_EMAIL.equals(message)){
                    url = "/237in?error=1";
                }else if(MessageCode.INCORRECT_PASSWORD.equals(message)){
                    url = "/237in?error=2";
                }else if(MessageCode.ACCOUNT_DISABLED.equals(message)){
                    url = "/237in?error=3";
                }
            }
            RequestDispatcher dispatcher = request.getRequestDispatcher(url);
            dispatcher.forward(request, response);
        }
    }

    private static class AuthenticationLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
        @Override
        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
            response.sendRedirect(request.getContextPath() + "/237in");
        }
    }
}