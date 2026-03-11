package com.example.clnspringdemo.controller;

import com.example.clnspringdemo.dto.ChannelInfo;
import com.example.clnspringdemo.dto.NodeInfo;
import com.example.clnspringdemo.dto.PayOfferRequest;
import com.example.clnspringdemo.dto.PaymentResult;
import com.example.clnspringdemo.service.ClnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClnController {

    private final ClnService clnService;

    public ClnController(ClnService clnService) {
        this.clnService = clnService;
    }

    @GetMapping("/info")
    public ResponseEntity<NodeInfo> getInfo() {
        return ResponseEntity.ok(clnService.getInfo());
    }

    @GetMapping("/channels")
    public ResponseEntity<List<ChannelInfo>> listChannels() {
        return ResponseEntity.ok(clnService.listChannels());
    }

    @PostMapping("/pay-offer")
    public ResponseEntity<PaymentResult> payOffer(@RequestBody PayOfferRequest request) {
        PaymentResult result = clnService.payOffer(
                request.offer(),
                request.amountMsat() != null ? request.amountMsat() : 0,
                request.label()
        );
        return ResponseEntity.ok(result);
    }
}
