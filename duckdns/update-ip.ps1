# ============================================================
#  DuckDNS Auto-Updater for Swordie
#  1. Go to https://www.duckdns.org and log in
#  2. Create a subdomain (e.g. "myswordie")
#  3. Copy your token from the top of the page
#  4. Fill in SUBDOMAIN and TOKEN below
# ============================================================

$SUBDOMAIN = "YOUR_SUBDOMAIN_HERE"   # e.g. "myswordie"  (without .duckdns.org)
$TOKEN      = "YOUR_DUCKDNS_TOKEN_HERE"       # from duckdns.org top of page

# ============================================================

$url = "https://www.duckdns.org/update?domains=$SUBDOMAIN&token=$TOKEN&ip="
$logFile = "$PSScriptRoot\duckdns.log"

try {
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing
    $raw = $response.Content
    $result = if ($raw -is [byte[]]) { [System.Text.Encoding]::UTF8.GetString($raw).Trim() } else { "$raw".Trim() }
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    if ($result -eq "OK") {
        $ipRaw = (Invoke-WebRequest -Uri "https://api.ipify.org" -UseBasicParsing).Content
        $currentIp = if ($ipRaw -is [byte[]]) { [System.Text.Encoding]::UTF8.GetString($ipRaw).Trim() } else { "$ipRaw".Trim() }
        $msg = "[$timestamp] Updated: $SUBDOMAIN.duckdns.org -> $currentIp"
    } else {
        $msg = "[$timestamp] FAILED: DuckDNS returned '$result'"
    }
} catch {
    $msg = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] ERROR: $($_.Exception.Message)"
}

Write-Host $msg
Add-Content -Path $logFile -Value $msg
