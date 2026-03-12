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
        var channels = clnService.listChannels();
        var payments = clnService.listPayments();
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("  <meta charset=\"utf-8\" />\n")
            .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n")
            .append("  <title>CLN Dashboard</title>\n")
            .append("  <style>\n")
            .append("    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; background:#0f1115; color:#e8e8e8; }\n")
            .append("    .card { background:#171a21; border:1px solid #242833; border-radius:12px; padding:20px; max-width:980px; }\n")
            .append("    h1 { margin:0 0 12px; font-size:24px; }\n")
            .append("    h2 { margin:20px 0 12px; font-size:18px; }\n")
            .append("    .kv { display:grid; grid-template-columns: 160px 1fr; gap:8px 16px; }\n")
            .append("    .k { color:#9aa4b2; }\n")
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
            .append("      .k { font-size: 12px; text-transform: uppercase; letter-spacing: .04em; }\n")
            .append("    }\n")
            .append("  </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("  <div class=\"card\">\n")
            .append("    <h1>Core Lightning Node Info</h1>\n")
            .append("    <div class=\"kv\">\n")
            .append("      <div class=\"k\">Alias</div><div>").append(safe(info.alias())).append("</div>\n")
            .append("      <div class=\"k\">Node ID</div><div class=\"mono\">").append(safe(info.nodeId())).append("</div>\n")
            .append("      <div class=\"k\">Network</div><div>").append(safe(info.network())).append("</div>\n")
            .append("      <div class=\"k\">Block Height</div><div>").append(info.blockHeight()).append("</div>\n")
            .append("      <div class=\"k\">Gradle</div><div>").append(safe(getGradleVersion())).append("</div>\n")
            .append("    </div>\n")
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
                .append("<td class=\"mono\">").append(safe(p.paymentHash())).append("</td>")
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
