package top.yxlgx.wink.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import top.yxlgx.wink.config.security.filter.JwtAuthenticationFilter;
import top.yxlgx.wink.config.security.handler.JwtAccessDeniedHandler;
import top.yxlgx.wink.config.security.handler.JwtLogoutHandler;
import top.yxlgx.wink.config.security.handler.JwtAuthenticationEntryPoint;
import top.yxlgx.wink.entity.User;
import top.yxlgx.wink.repository.UserRepository;

import java.util.Optional;

/**
 * @author yanxin
 * @Description:
 */
//@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final ApplicationContext applicationContext;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = applicationContext.getBean(JwtAuthenticationFilter.class);
        AuthenticationProvider authenticationProvider = applicationContext.getBean(AuthenticationProvider.class);
        LogoutHandler logoutHandler = applicationContext.getBean(JwtLogoutHandler.class);
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint = applicationContext.getBean(JwtAuthenticationEntryPoint.class);
        JwtAccessDeniedHandler jwtAccessDeniedHandler = applicationContext.getBean(JwtAccessDeniedHandler.class);
        http
                //??????csrf(??????????????????????????????)
                .csrf()
                .disable()
                // ???????????????
                .authorizeHttpRequests()
                .requestMatchers("/login")
                .permitAll()
                // ??????????????????????????????????????????
                .anyRequest()
                .authenticated()
                .and()
                // ????????????
                .sessionManagement()
                // ???????????????session???????????????session????????????
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // ??????????????????
                .and()
                .authenticationProvider(authenticationProvider)
                // ??????JWT?????????
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()
                // ????????????
                .logout()
                .logoutUrl("/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
        ;


        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {

            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                UserRepository userRepository = applicationContext.getBean(UserRepository.class);
                Optional<User> userOptional = userRepository.findByUsername(username);
                if (userOptional.isPresent()) {
                    return userOptional.get();

                } else {
                    throw new UsernameNotFoundException("User not found");
                }
            }
        };
    }

    /**
     * @return ?????????????????????????????????????????????
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // ?????????????????????????????????
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // ??????????????????????????????????????????????????????????????????????????????????????????
        authProvider.setUserDetailsService(userDetailsService());
        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * @param config
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
