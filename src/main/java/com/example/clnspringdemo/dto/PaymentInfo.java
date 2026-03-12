package com.example.clnspringdemo.dto;

public record PaymentInfo(
        String paymentHash,
        String status,
        String destination,
        long createdAt,
        long completedAt,
        long amountMsat,
        long amountSentMsat,
        String label,
        String bolt11,
        String bolt12
) {}
