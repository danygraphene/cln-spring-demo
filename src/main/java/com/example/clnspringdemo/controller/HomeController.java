package com.example.clnspringdemo.controller;

import com.example.clnspringdemo.dto.NodeInfo;
import com.example.clnspringdemo.service.ClnService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Controller
public class HomeController {

    private final ClnService clnService;

    public HomeController(ClnService clnService) {
        this.clnService = clnService;
    }

    @GetMapping("/")
    @ResponseBody
    public String home() {
        NodeInfo info = clnService.getInfo();
        long onchainSat = clnService.getOnchainBalanceSat();
        long lightningSat = clnService.getLightningBalanceSat();
        var channels = clnService.listChannels();
        var payments = clnService.listPayments();
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("  <meta charset=\"utf-8\" />\n")
            .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n")
            .append("  <meta name=\"format-detection\" content=\"telephone=no\" />\n")
            .append("  <title>CLN Dashboard</title>\n")
            .append("  <script src=\"/tailwind.js\"></script>\n")
            .append("  <style>\n")
            .append("    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; background:#0f1115; color:#e8e8e8; }\n")
            .append("    .card { background:#171a21; border:1px solid #242833; border-radius:12px; padding:20px; max-width:980px; }\n")
            .append("    h1 { margin:0 0 12px; font-size:24px; }\n")
            .append("    h2 { margin:20px 0 12px; font-size:18px; }\n")
            .append("    .kv { display:grid; grid-template-columns: 160px 1fr; gap:8px 16px; }\n")
            .append("    .k { color:#9aa4b2; }\n")
            .append("    .balances { display:grid; grid-template-columns: 1fr 1fr; gap:12px; margin-bottom:16px; }\n")
            .append("    .bal { background:#12151c; border:1px solid #242833; border-radius:10px; padding:12px; }\n")
            .append("    .bal .label { color:#9aa4b2; font-size:12px; text-transform:uppercase; letter-spacing:.04em; }\n")
            .append("    .bal .value { font-size:20px; font-weight:600; }\n")
            .append("    .actions { display:flex; flex-wrap:wrap; gap:10px; margin:12px 0 4px; }\n")
            .append("    .actions form { margin:0; }\n")
            .append("    .actions input { padding:8px 10px; border-radius:8px; border:1px solid #242833; background:#0f1115; color:#e8e8e8; }\n")
            .append("    .actions button { padding:8px 12px; border:0; border-radius:8px; background:#2d6cdf; color:white; font-weight:600; }\n")
            .append("    table { width:100%; border-collapse: collapse; margin-top:8px; display:block; overflow-x:auto; -webkit-overflow-scrolling:touch; }\n")
            .append("    th, td { text-align:left; padding:8px 10px; border-bottom:1px solid #242833; font-size:14px; white-space:nowrap; }\n")
            .append("    th { color:#9aa4b2; font-weight:600; }\n")
            .append("    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size:12px; }\n")
            .append("    @media (max-width: 640px) {\n")
            .append("      body { padding: 16px; }\n")
            .append("      .card { padding: 16px; }\n")
            .append("      h1 { font-size: 20px; }\n")
            .append("      h2 { font-size: 16px; }\n")
            .append("      .kv { grid-template-columns: 1fr; }\n")
            .append("      .balances { grid-template-columns: 1fr; }\n")
            .append("      .actions { flex-direction: column; }\n")
            .append("      .k { font-size: 12px; text-transform: uppercase; letter-spacing: .04em; }\n")
            .append("    }\n")
            .append("  </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("  <div class=\"card\">\n")
            .append("    <h1>Core Lightning Node Info</h1>\n")
            .append("    <div class=\"balances\">\n")
            .append("      <div class=\"bal\"><div class=\"label\">On-chain balance</div><div class=\"value\">").append(onchainSat).append(" sats</div></div>\n")
            .append("      <div class=\"bal\"><div class=\"label\">Lightning balance</div><div class=\"value\">").append(lightningSat).append(" sats</div></div>\n")
            .append("    </div>\n")
            .append("    <div class=\"actions\">\n")
            .append("      <form method=\"post\" action=\"/actions/onchain-address\">\n")
            .append("        <button type=\"submit\">New on-chain address</button>\n")
            .append("      </form>\n")
            .append("      <form method=\"post\" action=\"/actions/invoice\">\n")
            .append("        <input name=\"amount\" type=\"number\" min=\"1\" step=\"1\" placeholder=\"sats\" required />\n")
            .append("        <input name=\"description\" placeholder=\"description\" />\n")
            .append("        <button type=\"submit\">Create invoice</button>\n")
            .append("      </form>\n")
            .append("    </div>\n")
            .append("    <div class=\"kv\">\n")
            .append("      <div class=\"k\">Alias</div><div>").append(safe(info.alias())).append("</div>\n")
            .append("      <div class=\"k\">Node ID</div><div class=\"mono\">").append(safe(info.nodeId())).append("</div>\n")
            .append("      <div class=\"k\">Network</div><div>").append(safe(info.network())).append("</div>\n")
            .append("      <div class=\"k\">Block Height</div><div>").append(info.blockHeight()).append("</div>\n")
            .append("      <div class=\"k\">Gradle</div><div>").append(safe(getGradleVersion())).append("</div>\n")
            .append("    </div>\n")
            .append("    <p><a href=\"/channels/open\">Open a channel</a></p>\n")
            .append("    <h2>Channels</h2>\n")
            .append("    <table>\n")
            .append("      <thead><tr>\n")
            .append("        <th>Short ID</th><th>Peer ID</th><th>State</th><th>Capacity (msat)</th><th>Spendable (msat)</th><th>Receivable (msat)</th>\n")
            .append("      </tr></thead>\n")
            .append("      <tbody>\n");

        for (var ch : channels) {
            html.append("      <tr>")
                .append("<td class=\"mono\">").append(safe(ch.shortChannelId())).append("</td>")
                .append("<td class=\"mono\">").append(safe(ch.peerId())).append("</td>")
                .append("<td>").append(safe(ch.state())).append("</td>")
                .append("<td>").append(ch.capacityMsat()).append("</td>")
                .append("<td>").append(ch.spendableMsat()).append("</td>")
                .append("<td>").append(ch.receivableMsat()).append("</td>")
                .append("</tr>\n");
        }

        if (channels.isEmpty()) {
            html.append("      <tr><td colspan=\"6\" class=\"k\">No channels</td></tr>\n");
        }

        html.append("      </tbody>\n")
            .append("    </table>\n")
            .append("    <h2>Payments</h2>\n")
            .append("    <table>\n")
            .append("      <thead><tr>\n")
            .append("        <th>Created</th><th>Status</th><th>Amount (msat)</th><th>Sent (msat)</th><th>Label</th><th>Dest</th><th>Hash</th>\n")
            .append("      </tr></thead>\n")
            .append("      <tbody>\n");

        for (var p : payments) {
            html.append("      <tr>")
                .append("<td>").append(p.createdAt()).append("</td>")
                .append("<td>").append(safe(p.status())).append("</td>")
                .append("<td>").append(p.amountMsat()).append("</td>")
                .append("<td>").append(p.amountSentMsat()).append("</td>")
                .append("<td>").append(safe(p.label())).append("</td>")
                .append("<td class=\"mono\">").append(safe(p.destination())).append("</td>")
                .append("<td class=\"mono\"><span>").append(safe(p.paymentHash())).append("</span> ")
                .append("<a href=\"/payments/").append(safe(p.paymentHash())).append("\">view</a></td>")
                .append("</tr>\n");
        }

        if (payments.isEmpty()) {
            html.append("      <tr><td colspan=\"7\" class=\"k\">No payments</td></tr>\n");
        }

        html.append("      </tbody>\n")
            .append("    </table>\n")
            .append("  </div>\n")
            .append("</body>\n")
            .append("</html>\n");
        return html.toString();
    }

    private String getGradleVersion() {
        Path path = Path.of("gradle/wrapper/gradle-wrapper.properties");
        if (!Files.exists(path)) return "unknown";
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
            String dist = props.getProperty("distributionUrl", "");
            int idx = dist.indexOf("gradle-");
            if (idx >= 0) {
                String tail = dist.substring(idx + 7);
                int end = tail.indexOf("-bin");
                if (end > 0) {
                    return tail.substring(0, end);
                }
            }
        } catch (IOException ignored) {
        }
        return "unknown";
    }

    private String safe(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
