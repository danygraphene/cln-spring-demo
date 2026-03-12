package com.example.clnspringdemo.dto;

public record PayOfferRequest(
        String offer,
        Long amountMsat,
        String label,
        String description
) {}
