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
  <style>
    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; background:#0f1115; color:#e8e8e8; }
    .card { background:#171a21; border:1px solid #242833; border-radius:12px; padding:20px; max-width:720px; }
    label { display:block; margin:12px 0 6px; color:#9aa4b2; }
    input { width:100%; padding:10px; border-radius:8px; border:1px solid #242833; background:#0f1115; color:#e8e8e8; }
    button { margin-top:16px; padding:10px 16px; border:0; border-radius:8px; background:#2d6cdf; color:white; font-weight:600; }
    a { color:#8ab4f8; text-decoration:none; }
  </style>
</head>
<body>
  <div class="card">
    <h1>Open Channel</h1>
    <form method="post" action="/channels/open">
      <label>Connection string (nodeid@host:port)</label>
      <input name="connection" placeholder="02ab...@1.2.3.4:9735" required />
      <label>Capacity (sats)</label>
      <input name="capacity" type="number" min="1" step="1" required />
      <label style="display:flex; align-items:center; gap:8px; margin-top:12px;">
        <input type="checkbox" name="privateChannel" />
        Private channel (do not announce)
      </label>
      <button type="submit">Open Channel</button>
    </form>
    <p style="margin-top:16px;"><a href="/">← Back</a></p>
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
            .append("  <style>\n")
            .append("    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; background:#0f1115; color:#e8e8e8; }\n")
            .append("    .card { background:#171a21; border:1px solid #242833; border-radius:12px; padding:20px; max-width:720px; }\n")
            .append("    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size:12px; word-break: break-all; }\n")
            .append("    a { color:#8ab4f8; text-decoration:none; }\n")
            .append("  </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("  <div class=\"card\">\n")
            .append("    <h1>Channel Opened</h1>\n")
            .append("    <p>TXID:</p>\n")
            .append("    <div class=\"mono\">").append(result.txid()).append("</div>\n")
            .append("    <p>Channel ID:</p>\n")
            .append("    <div class=\"mono\">").append(result.channelId()).append("</div>\n")
            .append("    <p style=\"margin-top:16px;\"><a href=\"/\">← Back</a></p>\n")
            .append("  </div>\n")
            .append("</body>\n")
            .append("</html>\n");
        return html.toString();
    }
}
