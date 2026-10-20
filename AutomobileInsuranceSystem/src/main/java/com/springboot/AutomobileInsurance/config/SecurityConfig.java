package com.springboot.AutomobileInsurance.config;


import com.springboot.AutomobileInsurance.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final MyUserDetailsService myUserDetailsService ;

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize

                        //admin
                        .requestMatchers(HttpMethod.POST,"/api/admin/add").permitAll()

                        //officer
                        .requestMatchers(HttpMethod.POST,"/api/officer/add").hasAuthority("ADMIN")

                        //inspector
                        .requestMatchers(HttpMethod.POST,"/api/inspector/add").hasAuthority("OFFICER")

                        //customer
                        .requestMatchers(HttpMethod.POST,"/api/customer/add").permitAll()

                        //proposal
                        .requestMatchers(HttpMethod.POST,"/api/proposal/submit").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,"/api/proposal/assign/{inspectorId}/{proposalId}").hasAuthority("OFFICER")
                        .requestMatchers(HttpMethod.PUT,"/api/proposal/approve-idv/{proposalId}").hasAuthority("OFFICER")
                        .requestMatchers(HttpMethod.GET,"/api/proposal/customer/v1/{customerId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/proposal/customer/v2").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/proposal/inspector").hasAuthority("INSPECTOR")
                        .requestMatchers(HttpMethod.GET,"/api/proposal/officer").hasAuthority("OFFICER")






                        .anyRequest().permitAll()
                );
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationProvider getProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(myUserDetailsService);
        authenticationProvider.setPasswordEncoder(getEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getEncoder(){
        return new BCryptPasswordEncoder();
    }
}
