package com.library.main;

import com.library.config.AppConfig;
import com.library.enums.MembershipType;
import com.library.service.MemberService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MemberController {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        MemberService memberService = context.getBean(MemberService.class);

        String name = "Alice Smith";
        String email = "alice@gmail.com";
        MembershipType membershipType = MembershipType.PREMIUM;

        memberService.insert(name, email, membershipType);
        System.out.println("record inserted...");
    }
}