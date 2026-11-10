package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.response.DetailsDto;
import com.springboot.AutomobileInsurance.dto.response.TokenDto;
import com.springboot.AutomobileInsurance.service.UserService;
import com.springboot.AutomobileInsurance.utility.JwtUtility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")

public class AuthController {

    private final JwtUtility jwtUtility;
    private final UserService userService;

    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    /* This api is authenticated by Spring. which means if the control reaches this method, Spring
     * already has checked the credentials and it can share the username */
    @GetMapping("/api/auth/login")
    public TokenDto login(Principal principal){

        String loggedInUsername = principal.getName();
        log.info("User '{}' successfully authenticated. Generating JWT token.", loggedInUsername);

        String token = jwtUtility.generateToken(loggedInUsername);
        return new TokenDto(token,
                jwtUtility.extractExpiration(token),
                LocalDateTime.now(ZoneId.of("UTC")).toString());
    }

    /*
     * Fetch User details based on the given token
     * If this method is called, the user has given the token to Spring and
     * I can ask spring for logged-in username.
     * */
    @GetMapping("/api/auth/user-details")
    public DetailsDto getUserDetails(Principal principal){
        String loggedInUsername = principal.getName();
        return userService.getUserDetails(loggedInUsername);
    }

}