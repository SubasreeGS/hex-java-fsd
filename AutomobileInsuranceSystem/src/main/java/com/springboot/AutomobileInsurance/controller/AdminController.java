package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.AdminDto;
import com.springboot.AutomobileInsurance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor

public class AdminController {
   private final UserService userService;

    //create admin
    @PostMapping("/api/admin/add")
    public void insertAdmin(@RequestBody AdminDto dto)
    {
        userService.insertAdmin(dto);
    }
}
