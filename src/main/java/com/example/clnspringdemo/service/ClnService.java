package com.example.clnspringdemo.service;

import cln.NodeGrpc;
import cln.NodeOuterClass.*;
import cln.Primitives.*;
import com.example.clnspringdemo.dto.ChannelInfo;
import com.example.clnspringdemo.dto.NodeInfo;
import com.example.clnspringdemo.dto.PaymentResult;
import com.example.clnspringdemo.dto.PaymentInfo;
import com.example.clnspringdemo.dto.OpenChannelResult;
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
        PayRequest.Builder payRequest = PayRequest.newBuilder()
                .setBolt11(bolt12Invoice)
                .setLabel(label != null ? label : "offer-payment-" + System.currentTimeMillis());

        PayResponse payResponse = nodeStub.pay(payRequest.build());

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

    public PaymentInfo getPayment(String paymentHashHex) {
        ListpaysResponse response = nodeStub.listPays(
                ListpaysRequest.newBuilder()
                        .setPaymentHash(ByteString.copyFrom(HexFormat.of().parseHex(paymentHashHex)))
                        .build()
        );
        if (response.getPaysCount() == 0) return null;
        var p = response.getPays(0);
        return new PaymentInfo(
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
        );
    }

    public OpenChannelResult openChannel(String connection, long capacitySat, boolean privateChannel) {
        String nodeId = connection;
        String host = null;
        Integer port = null;

        if (connection.contains("@")) {
            String[] parts = connection.split("@", 2);
            nodeId = parts[0];
            String hostPart = parts[1];
            int lastColon = hostPart.lastIndexOf(':');
            if (lastColon > 0 && lastColon < hostPart.length() - 1) {
                host = hostPart.substring(0, lastColon);
                try {
                    port = Integer.parseInt(hostPart.substring(lastColon + 1));
                } catch (NumberFormatException ignored) {
                }
            } else {
                host = hostPart;
            }
        }

        ConnectRequest.Builder connect = ConnectRequest.newBuilder().setId(nodeId);
        if (host != null && !host.isBlank()) {
            connect.setHost(host);
            if (port != null) {
                connect.setPort(port);
            }
        }
        nodeStub.connectPeer(connect.build());

        long capacityMsat = capacitySat * 1000L;
        FundchannelRequest.Builder fund = FundchannelRequest.newBuilder()
                .setId(ByteString.copyFrom(HexFormat.of().parseHex(nodeId)))
                .setAmount(AmountOrAll.newBuilder().setAmount(Amount.newBuilder().setMsat(capacityMsat)));
        if (privateChannel) {
            fund.setAnnounce(false);
        }
        FundchannelResponse resp = nodeStub.fundChannel(fund.build());

        return new OpenChannelResult(
                bytesToHex(resp.getTxid()),
                bytesToHex(resp.getChannelId())
        );
    }

    public long getOnchainBalanceSat() {
        ListfundsResponse response = nodeStub.listFunds(ListfundsRequest.newBuilder().build());
        long msat = response.getOutputsList().stream()
                .filter(o -> o.getStatus() != ListfundsOutputs.ListfundsOutputsStatus.SPENT)
                .mapToLong(o -> o.getAmountMsat().getMsat())
                .sum();
        return msat / 1000L;
    }

    public long getLightningBalanceSat() {
        ListfundsResponse response = nodeStub.listFunds(ListfundsRequest.newBuilder().build());
        long msat = response.getChannelsList().stream()
                .mapToLong(c -> c.getOurAmountMsat().getMsat())
                .sum();
        return msat / 1000L;
    }

    public String newOnchainAddress() {
        NewaddrResponse response = nodeStub.newAddr(
                NewaddrRequest.newBuilder().setAddresstype(NewaddrRequest.NewaddrAddresstype.BECH32).build()
        );
        if (response.hasBech32()) return response.getBech32();
        if (response.hasP2Tr()) return response.getP2Tr();
        return "";
    }

    public String createInvoice(long amountSat, String description) {
        String label = "invoice-" + System.currentTimeMillis();
        InvoiceResponse response = nodeStub.invoice(
                InvoiceRequest.newBuilder()
                        .setLabel(label)
                        .setDescription(description != null ? description : "invoice")
                        .setAmountMsat(AmountOrAny.newBuilder().setAmount(Amount.newBuilder().setMsat(amountSat * 1000L)))
                        .build()
        );
        return response.getBolt11();
    }

    private String bytesToHex(ByteString bytes) {
        return HexFormat.of().formatHex(bytes.toByteArray());
    }
}
