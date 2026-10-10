package com.library.main;

import com.library.config.AppConfig;
import com.library.service.AuthorService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AuthorController {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        AuthorService authorService = context.getBean(AuthorService.class);

        String name = "George Orwell";
        String country = "United Kingdom";

        authorService.insert(name, country);
        System.out.println("record inserted...");
    }
}