package com.example.clnspringdemo.controller;

import com.example.clnspringdemo.service.ClnService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ActionController {

    private final ClnService clnService;

    public ActionController(ClnService clnService) {
        this.clnService = clnService;
    }

    @PostMapping("/actions/onchain-address")
    @ResponseBody
    public String newAddress() {
        String address = clnService.newOnchainAddress();
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("  <meta charset=\"utf-8\" />\n")
            .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n")
            .append("  <title>New Address</title>\n")
            .append("  <script src=\"https://cdn.tailwindcss.com\"></script>\n")
            .append("  <style>\n")
            .append("    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; background:#0f1115; color:#e8e8e8; }\n")
            .append("    .card { background:#171a21; border:1px solid #242833; border-radius:12px; padding:20px; max-width:720px; }\n")
            .append("    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size:12px; word-break: break-all; }\n")
            .append("    a { color:#8ab4f8; text-decoration:none; }\n")
            .append("  </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("  <div class=\"card\">\n")
            .append("    <h1>New On-chain Address</h1>\n")
            .append("    <div class=\"mono\">\n").append(safe(address)).append("\n</div>\n")
            .append("    <p style=\"margin-top:16px;\"><a href=\"/\">← Back</a></p>\n")
            .append("  </div>\n")
            .append("</body>\n")
            .append("</html>\n");
        return html.toString();
    }

    @PostMapping("/actions/invoice")
    @ResponseBody
    public String invoice(@RequestParam("amount") long amountSat,
                          @RequestParam(value = "description", required = false) String description) {
        String bolt11 = clnService.createInvoice(amountSat, description);
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("  <meta charset=\"utf-8\" />\n")
            .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n")
            .append("  <title>Invoice</title>\n")
            .append("  <script src=\"https://cdn.tailwindcss.com\"></script>\n")
            .append("  <style>\n")
            .append("    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; background:#0f1115; color:#e8e8e8; }\n")
            .append("    .card { background:#171a21; border:1px solid #242833; border-radius:12px; padding:20px; max-width:720px; }\n")
            .append("    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size:12px; word-break: break-all; }\n")
            .append("    a { color:#8ab4f8; text-decoration:none; }\n")
            .append("  </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("  <div class=\"card\">\n")
            .append("    <h1>Lightning Invoice</h1>\n")
            .append("    <div class=\"mono\">\n").append(safe(bolt11)).append("\n</div>\n")
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
