package com.example.clnspringdemo.service;

import cln.NodeGrpc;
import cln.NodeOuterClass.*;
import cln.Primitives.*;
import com.example.clnspringdemo.dto.ChannelInfo;
import com.example.clnspringdemo.dto.NodeInfo;
import com.example.clnspringdemo.dto.PaymentResult;
import com.example.clnspringdemo.dto.PaymentInfo;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.util.HexFormat;
import java.util.List;

@Service
public class ClnService {

    private final NodeGrpc.NodeBlockingStub nodeStub;

    public ClnService(NodeGrpc.NodeBlockingStub nodeStub) {
        this.nodeStub = nodeStub;
    }

    public NodeInfo getInfo() {
        GetinfoResponse response = nodeStub.getinfo(GetinfoRequest.newBuilder().build());
        return new NodeInfo(
                bytesToHex(response.getId()),
                response.getAlias(),
                response.getNetwork(),
                response.getBlockheight()
        );
    }

    public List<ChannelInfo> listChannels() {
        ListpeerchannelsResponse response = nodeStub.listPeerChannels(
                ListpeerchannelsRequest.newBuilder().build()
        );

        return response.getChannelsList().stream()
                .map(ch -> new ChannelInfo(
                        ch.hasShortChannelId() ? ch.getShortChannelId() : "pending",
                        bytesToHex(ch.getPeerId()),
                        ch.getState().name(),
                        ch.hasTotalMsat() ? ch.getTotalMsat().getMsat() : 0,
                        ch.hasSpendableMsat() ? ch.getSpendableMsat().getMsat() : 0,
                        ch.hasReceivableMsat() ? ch.getReceivableMsat().getMsat() : 0
                ))
                .toList();
    }

    public PaymentResult payOffer(String offer, long amountMsat, String label) {
        // Step 1: Fetch invoice from the offer
        FetchinvoiceRequest.Builder fetchRequest = FetchinvoiceRequest.newBuilder()
                .setOffer(offer);
        
        if (amountMsat > 0) {
            fetchRequest.setAmountMsat(Amount.newBuilder().setMsat(amountMsat).build());
        }

        FetchinvoiceResponse fetchResponse = nodeStub.fetchInvoice(fetchRequest.build());
        String bolt12Invoice = fetchResponse.getInvoice();

        // Step 2: Pay the fetched invoice
        PayRequest payRequest = PayRequest.newBuilder()
                .setBolt11(bolt12Invoice)
                .setLabel(label != null ? label : "offer-payment-" + System.currentTimeMillis())
                .build();

        PayResponse payResponse = nodeStub.pay(payRequest);

        return new PaymentResult(
                bytesToHex(payResponse.getPaymentPreimage()),
                bytesToHex(payResponse.getPaymentHash()),
                payResponse.getAmountMsat().getMsat(),
                payResponse.getAmountSentMsat().getMsat(),
                payResponse.getStatus().name()
        );
    }

    public List<PaymentInfo> listPayments() {
        ListpaysResponse response = nodeStub.listPays(
                ListpaysRequest.newBuilder().build()
        );

        return response.getPaysList().stream()
                .map(p -> new PaymentInfo(
                        bytesToHex(p.getPaymentHash()),
                        p.getStatus().name(),
                        p.hasDestination() ? bytesToHex(p.getDestination()) : "",
                        p.getCreatedAt(),
                        p.hasCompletedAt() ? p.getCompletedAt() : 0,
                        p.hasAmountMsat() ? p.getAmountMsat().getMsat() : 0,
                        p.hasAmountSentMsat() ? p.getAmountSentMsat().getMsat() : 0,
                        p.hasLabel() ? p.getLabel() : "",
                        p.hasBolt11() ? p.getBolt11() : "",
                        p.hasBolt12() ? p.getBolt12() : ""
                ))
                .toList();
    }

    private String bytesToHex(ByteString bytes) {
        return HexFormat.of().formatHex(bytes.toByteArray());
    }
}
