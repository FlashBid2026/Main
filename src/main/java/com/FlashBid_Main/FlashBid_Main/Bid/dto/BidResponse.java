package com.FlashBid_Main.FlashBid_Main.Bid.dto;

public record BidResponse(
    boolean success,
    String message,
    Long currentPrice,
    String winnerNickname
) {}