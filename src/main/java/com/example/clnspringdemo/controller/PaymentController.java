package com.example.clnspringdemo.controller;

import com.example.clnspringdemo.dto.PaymentInfo;
import com.example.clnspringdemo.service.ClnService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

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
            .append("  <style>\n")
            .append("    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; background:#0f1115; color:#e8e8e8; }\n")
            .append("    .card { background:#171a21; border:1px solid #242833; border-radius:12px; padding:20px; max-width:980px; }\n")
            .append("    h1 { margin:0 0 12px; font-size:24px; }\n")
            .append("    .kv { display:grid; grid-template-columns: 180px 1fr; gap:8px 16px; }\n")
            .append("    .k { color:#9aa4b2; }\n")
            .append("    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size:12px; word-break: break-all; }\n")
            .append("    a { color:#8ab4f8; text-decoration:none; }\n")
            .append("    @media (max-width: 640px) {\n")
            .append("      body { padding: 16px; }\n")
            .append("      .card { padding: 16px; }\n")
            .append("      h1 { font-size: 20px; }\n")
            .append("      .kv { grid-template-columns: 1fr; }\n")
            .append("      .k { font-size: 12px; text-transform: uppercase; letter-spacing: .04em; }\n")
            .append("    }\n")
            .append("  </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("  <div class=\"card\">\n")
            .append("    <h1>Payment Details</h1>\n")
            .append("    <div class=\"kv\">\n")
            .append("      <div class=\"k\">Status</div><div>").append(safe(p.status())).append("</div>\n")
            .append("      <div class=\"k\">Created</div><div>").append(p.createdAt()).append("</div>\n")
            .append("      <div class=\"k\">Completed</div><div>").append(p.completedAt()).append("</div>\n")
            .append("      <div class=\"k\">Amount (msat)</div><div>").append(p.amountMsat()).append("</div>\n")
            .append("      <div class=\"k\">Sent (msat)</div><div>").append(p.amountSentMsat()).append("</div>\n")
            .append("      <div class=\"k\">Label</div><div>").append(safe(p.label())).append("</div>\n")
            .append("      <div class=\"k\">Destination</div><div class=\"mono\">").append(safe(p.destination())).append("</div>\n")
            .append("      <div class=\"k\">Payment Hash</div><div class=\"mono\">").append(safe(p.paymentHash())).append("</div>\n")
            .append("      <div class=\"k\">Bolt11</div><div class=\"mono\">").append(safe(p.bolt11())).append("</div>\n")
            .append("      <div class=\"k\">Bolt12</div><div class=\"mono\">").append(safe(p.bolt12())).append("</div>\n")
            .append("    </div>\n")
            .append("    <p style=\"margin-top:16px;\"><a href=\"/\">← Back</a></p>\n")
            .append("  </div>\n")
            .append("</body>\n")
            .append("</html>\n");
        return html.toString();
    }

    private String safe(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
