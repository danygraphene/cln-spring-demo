package com.example.clnspringdemo.dto;

public record PaymentResult(
        String preimage,
        String paymentHash,
        long amountMsat,
        long amountSentMsat,
        String status
) {}
