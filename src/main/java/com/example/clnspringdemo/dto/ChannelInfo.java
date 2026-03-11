package com.example.clnspringdemo.dto;

public record ChannelInfo(
        String shortChannelId,
        String peerId,
        String state,
        long capacityMsat,
        long spendableMsat,
        long receivableMsat
) {}
