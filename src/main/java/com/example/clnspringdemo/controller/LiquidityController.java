package com.example.clnspringdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LiquidityController {

    @GetMapping("/liquidity")
    @ResponseBody
    public String liquidity() {
        return """
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Liquidity</title>
  <script src="/tailwind.js"></script>
</head>
<body class="bg-slate-950 text-slate-100">
  <div class="max-w-3xl mx-auto p-4 sm:p-6">
    <div class="bg-slate-900/60 border border-slate-800 rounded-xl p-4 sm:p-6 space-y-6">
      <div>
        <h1 class="text-xl sm:text-2xl font-semibold">Liquidity</h1>
        <p class="text-sm text-slate-400 mt-1">Manage channels, on-chain addresses, and invoices.</p>
      </div>

      <div class="space-y-3">
        <h2 class="text-lg font-semibold">Open Channel</h2>
        <a class="inline-flex items-center px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 font-semibold" href="/channels/open">Open a channel</a>
      </div>

      <div class="space-y-3">
        <h2 class="text-lg font-semibold">On-chain Address</h2>
        <form method="post" action="/actions/onchain-address">
          <button class="px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 font-semibold" type="submit">New on-chain address</button>
        </form>
      </div>

      <div class="space-y-3">
        <h2 class="text-lg font-semibold">Lightning Invoice</h2>
        <form method="post" action="/actions/invoice" class="flex flex-col sm:flex-row gap-2">
          <input name="amount" type="number" min="1" step="1" placeholder="sats" required class="w-full sm:w-32 px-3 py-2 rounded-lg bg-slate-950 border border-slate-800" />
          <input name="description" placeholder="description" class="flex-1 px-3 py-2 rounded-lg bg-slate-950 border border-slate-800" />
          <button class="w-full sm:w-auto px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 font-semibold" type="submit">Create invoice</button>
        </form>
      </div>

      <div>
        <a class="text-blue-400 hover:underline" href="/">← Back</a>
      </div>
    </div>
  </div>
</body>
</html>
""";
    }
}
