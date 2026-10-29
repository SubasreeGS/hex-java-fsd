package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.response.QuoteResponseDto;
import com.springboot.AutomobileInsurance.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    //officer generated
    @PostMapping("/api/quote/generate/{proposalId}")
    public void generateQuote(@PathVariable Long proposalId) {
         quoteService.generateQuote(proposalId);
    }

    //get quote by the proposal id
    @GetMapping("/api/quote/proposal/{proposalId}")
    public QuoteResponseDto getByProposalId(@PathVariable Long proposalId) {
        return quoteService.getByProposalId(proposalId);
    }
}