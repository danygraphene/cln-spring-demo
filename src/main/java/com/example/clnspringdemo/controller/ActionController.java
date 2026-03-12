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
            .append("  <script src=\"/tailwind.js\"></script>\n")
            .append("</head>\n")
            .append("<body class=\"bg-slate-950 text-slate-100\">\n")
            .append("  <div class=\"max-w-2xl mx-auto p-4 sm:p-6\">\n")
            .append("    <div class=\"bg-slate-900/60 border border-slate-800 rounded-xl p-4 sm:p-6\">\n")
            .append("      <h1 class=\"text-xl sm:text-2xl font-semibold\">New On-chain Address</h1>\n")
            .append("      <div class=\"mt-4 font-mono text-xs break-all\">\n").append(safe(address)).append("\n</div>\n")
            .append("      <div class=\"mt-4\"><a class=\"text-blue-400 hover:underline\" href=\"/\">← Back</a></div>\n")
            .append("    </div>\n")
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
            .append("  <script src=\"/tailwind.js\"></script>\n")
            .append("</head>\n")
            .append("<body class=\"bg-slate-950 text-slate-100\">\n")
            .append("  <div class=\"max-w-2xl mx-auto p-4 sm:p-6\">\n")
            .append("    <div class=\"bg-slate-900/60 border border-slate-800 rounded-xl p-4 sm:p-6\">\n")
            .append("      <h1 class=\"text-xl sm:text-2xl font-semibold\">Lightning Invoice</h1>\n")
            .append("      <div class=\"mt-4 font-mono text-xs break-all\">\n").append(safe(bolt11)).append("\n</div>\n")
            .append("      <div class=\"mt-4\"><a class=\"text-blue-400 hover:underline\" href=\"/\">← Back</a></div>\n")
            .append("    </div>\n")
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
