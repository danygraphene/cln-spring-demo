package com.example.clnspringdemo.dto;

public record NodeInfo(
        String nodeId,
        String alias,
        String network,
        int blockHeight
) {}
