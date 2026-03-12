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
            .append("</head>\n")
            .append("<body class=\"bg-slate-950 text-slate-100\">\n")
            .append("  <div class=\"max-w-5xl mx-auto p-4 sm:p-6 space-y-6\">\n")
            .append("    <div class=\"bg-slate-900/60 border border-slate-800 rounded-xl p-4 sm:p-6\">\n")
            .append("      <h1 class=\"text-xl sm:text-2xl font-semibold\">Core Lightning Node</h1>\n")
            .append("      <div class=\"grid gap-3 sm:grid-cols-2 mt-4\">\n")
            .append("        <div class=\"bg-slate-900 border border-slate-800 rounded-lg p-3\">\n")
            .append("          <div class=\"text-xs uppercase tracking-wide text-slate-400\">On-chain balance</div>\n")
            .append("          <div class=\"text-lg font-semibold\">").append(onchainSat).append(" sats</div>\n")
            .append("        </div>\n")
            .append("        <div class=\"bg-slate-900 border border-slate-800 rounded-lg p-3\">\n")
            .append("          <div class=\"text-xs uppercase tracking-wide text-slate-400\">Lightning balance</div>\n")
            .append("          <div class=\"text-lg font-semibold\">").append(lightningSat).append(" sats</div>\n")
            .append("        </div>\n")
            .append("      </div>\n")

            .append("      <div class=\"flex flex-col gap-3 sm:flex-row sm:items-center mt-4\">\n")
            .append("        <form method=\"post\" action=\"/actions/onchain-address\" class=\"w-full sm:w-auto\">\n")
            .append("          <button class=\"w-full sm:w-auto px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 font-semibold\" type=\"submit\">New on-chain address</button>\n")
            .append("        </form>\n")
            .append("        <form method=\"post\" action=\"/actions/invoice\" class=\"flex flex-col sm:flex-row gap-2 w-full\">\n")
            .append("          <input name=\"amount\" type=\"number\" min=\"1\" step=\"1\" placeholder=\"sats\" required class=\"w-full sm:w-32 px-3 py-2 rounded-lg bg-slate-950 border border-slate-800\" />\n")
            .append("          <input name=\"description\" placeholder=\"description\" class=\"flex-1 px-3 py-2 rounded-lg bg-slate-950 border border-slate-800\" />\n")
            .append("          <button class=\"w-full sm:w-auto px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 font-semibold\" type=\"submit\">Create invoice</button>\n")
            .append("        </form>\n")
            .append("      </div>\n")

            .append("      <div class=\"grid gap-2 mt-5 text-sm\">\n")
            .append("        <div><span class=\"text-slate-400\">Alias:</span> ").append(safe(info.alias())).append("</div>\n")
            .append("        <div><span class=\"text-slate-400\">Node ID:</span> <span class=\"font-mono text-xs break-all\">\n").append(safe(info.nodeId())).append("</span></div>\n")
            .append("        <div><span class=\"text-slate-400\">Network:</span> ").append(safe(info.network())).append("</div>\n")
            .append("        <div><span class=\"text-slate-400\">Block Height:</span> ").append(info.blockHeight()).append("</div>\n")
            .append("        <div><span class=\"text-slate-400\">Gradle:</span> ").append(safe(getGradleVersion())).append("</div>\n")
            .append("      </div>\n")

            .append("      <div class=\"mt-4\"><a class=\"text-blue-400 hover:underline\" href=\"/channels/open\">Open a channel</a></div>\n")
            .append("    </div>\n")

            .append("    <div class=\"bg-slate-900/60 border border-slate-800 rounded-xl p-4 sm:p-6\">\n")
            .append("      <h2 class=\"text-lg font-semibold\">Channels</h2>\n")
            .append("      <div class=\"overflow-x-auto mt-3\">\n")
            .append("        <table class=\"min-w-full text-sm\">\n")
            .append("          <thead class=\"text-slate-400\"><tr>\n")
            .append("            <th class=\"text-left p-2\">Short ID</th><th class=\"text-left p-2\">Peer ID</th><th class=\"text-left p-2\">State</th><th class=\"text-left p-2\">Capacity</th><th class=\"text-left p-2\">Spendable</th><th class=\"text-left p-2\">Receivable</th>\n")
            .append("          </tr></thead>\n")
            .append("          <tbody class=\"divide-y divide-slate-800\">\n");

        for (var ch : channels) {
            html.append("          <tr>")
                .append("<td class=\"p-2 font-mono text-xs\">").append(safe(ch.shortChannelId())).append("</td>")
                .append("<td class=\"p-2 font-mono text-xs\">").append(safe(ch.peerId())).append("</td>")
                .append("<td class=\"p-2\">").append(safe(ch.state())).append("</td>")
                .append("<td class=\"p-2\">").append(ch.capacityMsat()).append("</td>")
                .append("<td class=\"p-2\">").append(ch.spendableMsat()).append("</td>")
                .append("<td class=\"p-2\">").append(ch.receivableMsat()).append("</td>")
                .append("</tr>\n");
        }

        if (channels.isEmpty()) {
            html.append("          <tr><td colspan=\"6\" class=\"p-2 text-slate-400\">No channels</td></tr>\n");
        }

        html.append("          </tbody>\n")
            .append("        </table>\n")
            .append("      </div>\n")
            .append("    </div>\n")

            .append("    <div class=\"bg-slate-900/60 border border-slate-800 rounded-xl p-4 sm:p-6\">\n")
            .append("      <h2 class=\"text-lg font-semibold\">Payments</h2>\n")
            .append("      <div class=\"overflow-x-auto mt-3\">\n")
            .append("        <table class=\"min-w-full text-sm\">\n")
            .append("          <thead class=\"text-slate-400\"><tr>\n")
            .append("            <th class=\"text-left p-2\">Created</th><th class=\"text-left p-2\">Status</th><th class=\"text-left p-2\">Amount</th><th class=\"text-left p-2\">Sent</th><th class=\"text-left p-2\">Label</th><th class=\"text-left p-2\">Dest</th><th class=\"text-left p-2\">Hash</th>\n")
            .append("          </tr></thead>\n")
            .append("          <tbody class=\"divide-y divide-slate-800\">\n");

        for (var p : payments) {
            html.append("          <tr>")
                .append("<td class=\"p-2\">").append(p.createdAt()).append("</td>")
                .append("<td class=\"p-2\">").append(safe(p.status())).append("</td>")
                .append("<td class=\"p-2\">").append(p.amountMsat()).append("</td>")
                .append("<td class=\"p-2\">").append(p.amountSentMsat()).append("</td>")
                .append("<td class=\"p-2\">").append(safe(p.label())).append("</td>")
                .append("<td class=\"p-2 font-mono text-xs\">").append(safe(p.destination())).append("</td>")
                .append("<td class=\"p-2 font-mono text-xs\"><span>").append(safe(p.paymentHash())).append("</span> ")
                .append("<a class=\"text-blue-400 hover:underline\" href=\"/payments/").append(safe(p.paymentHash())).append("\">view</a></td>")
                .append("</tr>\n");
        }

        if (payments.isEmpty()) {
            html.append("          <tr><td colspan=\"7\" class=\"p-2 text-slate-400\">No payments</td></tr>\n");
        }

        html.append("          </tbody>\n")
            .append("        </table>\n")
            .append("      </div>\n")
            .append("    </div>\n")
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
