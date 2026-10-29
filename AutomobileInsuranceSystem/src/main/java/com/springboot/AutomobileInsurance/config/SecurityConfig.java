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
                        .requestMatchers(HttpMethod.GET, "/api/inspector/all").hasAuthority("OFFICER")

                        //customer
                        .requestMatchers(HttpMethod.POST,"/api/customer/add").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/customer/v1/{id}").hasAnyAuthority("CUSTOMER","OFFICER")
                        .requestMatchers(HttpMethod.GET,"/api/customer/profile/v2").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/customer/all").hasAuthority("OFFICER")
                        .requestMatchers(HttpMethod.DELETE,"/api/customer/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/customer/v1/{id}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,"/api/customer/profile/v2").hasAuthority("CUSTOMER")


                        //proposal
                        .requestMatchers(HttpMethod.POST,"/api/proposal/submit").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,"/api/proposal/assign/{inspectorId}/{proposalId}").hasAuthority("OFFICER")
                        .requestMatchers(HttpMethod.PUT,"/api/proposal/approve-idv/{proposalId}").hasAuthority("OFFICER")
                        .requestMatchers(HttpMethod.GET,"/api/proposal/customer/v1/{customerId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/proposal/customer/v2").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/proposal/inspector").hasAuthority("INSPECTOR")
                        .requestMatchers(HttpMethod.GET,"/api/proposal/officer").hasAuthority("OFFICER")
                        .requestMatchers(HttpMethod.GET,"/api/proposal/status/{status}").hasAuthority("OFFICER")

                        //Inspection
                        .requestMatchers(HttpMethod.PUT,"/api/inspection/submit/{proposalId}").hasAuthority("INSPECTOR")
                        .requestMatchers(HttpMethod.GET,"/api/inspection/inspector/v1").hasAuthority("INSPECTOR")
                        .requestMatchers(HttpMethod.GET,"/api/inspection/status/v2/{status}").hasAuthority("INSPECTOR")
                        .requestMatchers(HttpMethod.GET,"/api/inspection/proposal/v1/{proposalId}").hasAuthority("OFFICER")
                        .requestMatchers(HttpMethod.GET,"/api/inspection/inspector/v2/{inspectorId}").hasAuthority("OFFICER")

                        //Quote
                        .requestMatchers(HttpMethod.POST,"/api/quote/generate/{proposalId}").hasAuthority("OFFICER")
                        .requestMatchers(HttpMethod.GET,"/api/quote/proposal/{proposalId}").hasAnyAuthority("CUSTOMER","OFFICER")

                        //Payment
                        .requestMatchers(HttpMethod.POST,"/api/payment/process/{quoteId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/payment/receipt/{paymentId}").hasAnyAuthority("CUSTOMER","OFFICER")

                        //Policy
                        .requestMatchers(HttpMethod.GET,"/api/policy/number/v1/{policyNumber}").hasAnyAuthority("CUSTOMER","OFFICER")
                        .requestMatchers(HttpMethod.GET,"/api/policy/quote/v2/{quoteId}").hasAnyAuthority("CUSTOMER","OFFICER")
                        .requestMatchers(HttpMethod.GET,"/api/policy/proposal/v3/{proposalId}").hasAnyAuthority("CUSTOMER","OFFICER")
                        .requestMatchers(HttpMethod.GET,"/api/policy/customer/my-policies/v4").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/policy/all").hasAuthority("OFFICER")

                        // Claim Workflow
                        .requestMatchers(HttpMethod.POST, "/api/claim/file").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/claim/assign/{inspectorId}/{claimId}").hasAuthority("OFFICER")
                        .requestMatchers(HttpMethod.GET, "/api/claim/customer/my-claims/v4").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/claim/review/{claimId}").hasAuthority("OFFICER")

                        .requestMatchers(HttpMethod.GET, "/api/claim/id/v1/{id}").hasAnyAuthority("CUSTOMER", "OFFICER")
                        .requestMatchers(HttpMethod.GET, "/api/claim/number/v2/{claimNo}").hasAnyAuthority("CUSTOMER", "OFFICER")
                        .requestMatchers(HttpMethod.GET, "/api/claim/policy/v3/{policyId}").hasAnyAuthority("CUSTOMER", "OFFICER")
                        .requestMatchers(HttpMethod.GET, "/api/claim/customer/my-claims/v4").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/claim/all").hasAuthority("OFFICER")

                        //Claim Assessment
                        .requestMatchers(HttpMethod.GET, "/api/assessment/inspector/my-tasks").hasAuthority("INSPECTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/assessment/submit/{id}").hasAuthority("INSPECTOR")

                        .requestMatchers(HttpMethod.GET, "/api/assessment/v1/{id}").hasAnyAuthority("INSPECTOR", "OFFICER")
                        .requestMatchers(HttpMethod.GET, "/api/assessment/claim/v2/{claimId}").hasAnyAuthority("INSPECTOR", "OFFICER")
                        .requestMatchers(HttpMethod.GET, "/api/assessment/inspector/v3/{inspectorId}").hasAnyAuthority("INSPECTOR", "OFFICER")
                        .requestMatchers(HttpMethod.GET, "/api/assessment/all").hasAuthority("OFFICER")

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
