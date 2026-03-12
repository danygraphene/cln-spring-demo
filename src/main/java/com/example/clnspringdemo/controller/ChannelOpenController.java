package com.example.clnspringdemo.controller;

import com.example.clnspringdemo.dto.OpenChannelResult;
import com.example.clnspringdemo.service.ClnService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ChannelOpenController {

    private final ClnService clnService;

    public ChannelOpenController(ClnService clnService) {
        this.clnService = clnService;
    }

    @GetMapping("/channels/open")
    @ResponseBody
    public String form() {
        return """
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Open Channel</title>
  <script src="/tailwind.js"></script>
</head>
<body class="bg-slate-950 text-slate-100">
  <div class="max-w-2xl mx-auto p-4 sm:p-6">
    <div class="bg-slate-900/60 border border-slate-800 rounded-xl p-4 sm:p-6">
      <h1 class="text-xl sm:text-2xl font-semibold">Open Channel</h1>
      <form method="post" action="/channels/open" class="mt-4 space-y-3">
        <div>
          <label class="block text-sm text-slate-400 mb-1">Connection string (nodeid@host:port)</label>
          <input name="connection" placeholder="02ab...@1.2.3.4:9735" required class="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800" />
        </div>
        <div>
          <label class="block text-sm text-slate-400 mb-1">Capacity (sats)</label>
          <input name="capacity" type="number" min="1" step="1" required class="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800" />
        </div>
        <label class="flex items-center gap-2 text-sm text-slate-300">
          <input type="checkbox" name="privateChannel" class="rounded border-slate-700" />
          Private channel (do not announce)
        </label>
        <button type="submit" class="w-full sm:w-auto px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 font-semibold">Open Channel</button>
      </form>
      <div class="mt-4"><a class="text-blue-400 hover:underline" href="/">← Back</a></div>
    </div>
  </div>
</body>
</html>
""";
    }

    @PostMapping("/channels/open")
    @ResponseBody
    public String open(@RequestParam("connection") String connection,
                       @RequestParam("capacity") long capacitySat,
                       @RequestParam(value = "privateChannel", required = false) String privateChannel) {
        boolean isPrivate = privateChannel != null;
        OpenChannelResult result = clnService.openChannel(connection, capacitySat, isPrivate);
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("  <meta charset=\"utf-8\" />\n")
            .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n")
            .append("  <title>Channel Opened</title>\n")
            .append("  <script src=\"/tailwind.js\"></script>\n")
            .append("</head>\n")
            .append("<body class=\"bg-slate-950 text-slate-100\">\n")
            .append("  <div class=\"max-w-2xl mx-auto p-4 sm:p-6\">\n")
            .append("    <div class=\"bg-slate-900/60 border border-slate-800 rounded-xl p-4 sm:p-6\">\n")
            .append("      <h1 class=\"text-xl sm:text-2xl font-semibold\">Channel Opened</h1>\n")
            .append("      <div class=\"mt-4 space-y-3 text-sm\">\n")
            .append("        <div><span class=\"text-slate-400\">TXID:</span> <span class=\"font-mono text-xs break-all\">\n").append(result.txid()).append("</span></div>\n")
            .append("        <div><span class=\"text-slate-400\">Channel ID:</span> <span class=\"font-mono text-xs break-all\">\n").append(result.channelId()).append("</span></div>\n")
            .append("      </div>\n")
            .append("      <div class=\"mt-4\"><a class=\"text-blue-400 hover:underline\" href=\"/\">← Back</a></div>\n")
            .append("    </div>\n")
            .append("  </div>\n")
            .append("</body>\n")
            .append("</html>\n");
        return html.toString();
    }
}
