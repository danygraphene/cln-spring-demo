package com.example.clnspringdemo.controller;

import com.example.clnspringdemo.dto.PaymentInfo;
import com.example.clnspringdemo.service.ClnService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
public class PaymentController {

    private final ClnService clnService;

    public PaymentController(ClnService clnService) {
        this.clnService = clnService;
    }

    @GetMapping("/payments/{hash}")
    @ResponseBody
    public String payment(@PathVariable("hash") String hash) {
        PaymentInfo p = clnService.getPayment(hash);
        if (p == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("  <meta charset=\"utf-8\" />\n")
            .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n")
            .append("  <title>Payment Details</title>\n")
            .append("  <script src=\"/tailwind.js\"></script>\n")
            .append("</head>\n")
            .append("<body class=\"bg-slate-950 text-slate-100\">\n")
            .append("  <div class=\"max-w-3xl mx-auto p-4 sm:p-6\">\n")
            .append("    <div class=\"bg-slate-900/60 border border-slate-800 rounded-xl p-4 sm:p-6\">\n")
            .append("      <h1 class=\"text-xl sm:text-2xl font-semibold\">Payment Details</h1>\n")
            .append("      <div class=\"mt-4 grid gap-3 text-sm\">\n")
            .append("        <div><span class=\"text-slate-400\">Status:</span> ").append(safe(p.status())).append("</div>\n")
            .append("        <div><span class=\"text-slate-400\">Created:</span> ").append(formatTime(p.createdAt())).append("</div>\n")
            .append("        <div><span class=\"text-slate-400\">Completed:</span> ").append(p.completedAt() > 0 ? formatTime(p.completedAt()) : "-").append("</div>\n")
            .append("        <div><span class=\"text-slate-400\">Amount (msat):</span> ").append(p.amountMsat()).append("</div>\n")
            .append("        <div><span class=\"text-slate-400\">Sent (msat):</span> ").append(p.amountSentMsat()).append("</div>\n")
            .append("        <div><span class=\"text-slate-400\">Label:</span> ").append(safe(p.label())).append("</div>\n")
            .append("        <div><span class=\"text-slate-400\">Destination:</span> <span class=\"font-mono text-xs break-all\">").append(abbrev(safe(p.destination()))).append("</span></div>\n")
            .append("        <div><span class=\"text-slate-400\">Payment Hash:</span> <span class=\"font-mono text-xs break-all\">").append(abbrev(safe(p.paymentHash()))).append("</span></div>\n")
            .append("        <div><span class=\"text-slate-400\">Bolt11:</span> <span class=\"font-mono text-xs break-all\">").append(abbrev(safe(p.bolt11()))).append("</span></div>\n")
            .append("        <div><span class=\"text-slate-400\">Bolt12:</span> <span class=\"font-mono text-xs break-all\">").append(abbrev(safe(p.bolt12()))).append("</span></div>\n")
            .append("      </div>\n")
            .append("      <div class=\"mt-4\"><a class=\"text-blue-400 hover:underline\" href=\"/\">← Back</a></div>\n")
            .append("    </div>\n")
            .append("  </div>\n")
            .append("</body>\n")
            .append("</html>\n");
        return html.toString();
    }

    private String formatTime(long epochSeconds) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(epochSeconds));
    }

    private String abbrev(String v) {
        if (v == null) return "";
        if (v.length() <= 12) return v;
        return v.substring(0, 4) + "…" + v.substring(v.length() - 4);
    }

    private String safe(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
