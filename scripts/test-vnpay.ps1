# Test VNPay Sandbox — tạo URL thanh toán + gọi API query (querydr)
# Chạy: powershell -ExecutionPolicy Bypass -File scripts/test-vnpay.ps1

$ErrorActionPreference = "Stop"

$tmnCode    = "1HDJ5TF3"
$hashSecret = "MNB1G82H8LNODS95129LVVI4I4K081W8"
$payUrl     = "https://vnpayment.vn/paymentv2/vpcpay.html"
$apiUrl     = "https://vnpayment.vn/merchant_webapi/api/transaction"
$returnUrl  = "http://localhost:8080/payment/vnpay-callback"

function Encode-Vnp([string]$s) {
    [System.Uri]::EscapeDataString($s)
}

function Get-VnpSecureHash([hashtable]$fields, [string]$secret) {
    $sorted = $fields.GetEnumerator() | Sort-Object Name
    $parts = @()
    foreach ($kv in $sorted) {
        if ($null -ne $kv.Value -and "$($kv.Value)".Length -gt 0) {
            $parts += "$(Encode-Vnp $kv.Name)=$(Encode-Vnp ([string]$kv.Value))"
        }
    }
    $hashData = $parts -join "&"
    $keyBytes = [Text.Encoding]::UTF8.GetBytes($secret)
    $hmac = [System.Security.Cryptography.HMACSHA512]::new($keyBytes)
    $hash = $hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($hashData))
    $hmac.Dispose()
    -join ($hash | ForEach-Object { $_.ToString("x2") })
}

function New-VnpPayUrl([long]$amountVnd, [string]$txnRef, [string]$orderInfo) {
    $now = [DateTimeOffset]::Now.ToOffset([TimeSpan]::FromHours(7))
    $createDate = $now.ToString("yyyyMMddHHmmss")
    $expireDate = $now.AddMinutes(15).ToString("yyyyMMddHHmmss")

    $params = @{
        vnp_Version     = "2.1.0"
        vnp_Command     = "pay"
        vnp_TmnCode     = $tmnCode
        vnp_Amount      = [string]($amountVnd * 100)
        vnp_CurrCode    = "VND"
        vnp_TxnRef      = $txnRef
        vnp_OrderInfo   = $orderInfo
        vnp_OrderType   = "other"
        vnp_Locale      = "vn"
        vnp_ReturnUrl   = $returnUrl
        vnp_IpAddr      = "127.0.0.1"
        vnp_CreateDate  = $createDate
        vnp_ExpireDate  = $expireDate
    }

    $hash = Get-VnpSecureHash $params $hashSecret
    $query = ($params.GetEnumerator() | Sort-Object Name | ForEach-Object {
        "$(Encode-Vnp $_.Name)=$(Encode-Vnp ([string]$_.Value))"
    }) -join "&"
    return "$payUrl`?$query&vnp_SecureHash=$hash"
}

function Invoke-VnpQuery([string]$txnRef, [string]$transDate) {
    $now = [DateTimeOffset]::Now.ToOffset([TimeSpan]::FromHours(7))
    $createDate = $now.ToString("yyyyMMddHHmmss")
    $params = @{
        vnp_Request     = "2.1.0"
        vnp_Version     = "2.1.0"
        vnp_Command     = "querydr"
        vnp_TmnCode     = $tmnCode
        vnp_TxnRef      = $txnRef
        vnp_OrderInfo   = "Kiem tra don hang"
        vnp_TransDate   = $transDate
        vnp_CreateDate  = $createDate
        vnp_IpAddr      = "127.0.0.1"
    }
    $hash = Get-VnpSecureHash $params $hashSecret
    $body = ($params.GetEnumerator() | Sort-Object Name | ForEach-Object {
        "$(Encode-Vnp $_.Name)=$(Encode-Vnp ([string]$_.Value))"
    }) -join "&"
    $body += "&vnp_SecureHash=$(Encode-Vnp $hash)"

    Invoke-RestMethod -Uri $apiUrl -Method Post -ContentType "application/x-www-form-urlencoded" -Body $body
}

Write-Host "=== VNPay Sandbox Test ===" -ForegroundColor Cyan
Write-Host "TMN Code: $tmnCode"
Write-Host ""

$txnRef = "TEST" + (Get-Date -Format "HHmmss")
$url = New-VnpPayUrl -amountVnd 100000 -txnRef $txnRef -orderInfo "Test thanh toan ZakiBooking"
Write-Host "[1] Payment URL (mo tren browser de thanh toan sandbox):" -ForegroundColor Yellow
Write-Host $url
Write-Host ""

try {
    $resp = Invoke-WebRequest -Uri $url -Method Get -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Write-Host "[2] GET payment URL => HTTP $($resp.StatusCode) (OK neu 200)" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 302) {
        Write-Host "[2] GET payment URL => redirect (OK)" -ForegroundColor Green
    } else {
        Write-Host "[2] GET payment URL => $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "[3] Query API (querydr) — can co loi neu chua co giao dich:" -ForegroundColor Yellow
$today = (Get-Date).ToString("yyyyMMdd")
try {
    $q = Invoke-VnpQuery -txnRef $txnRef -transDate $today
    Write-Host ($q | ConvertTo-Json -Depth 5)
} catch {
    Write-Host $_.Exception.Message -ForegroundColor DarkYellow
}

Write-Host ""
Write-Host "Thẻ test sandbox VNPay:" -ForegroundColor Cyan
Write-Host "  NCB | 9704198526191432198 | NGUYEN VAN A | 07/15 | OTP: 123456"
Write-Host ""
Write-Host "Flow tren app: dat tour -> POST /booking/submit -> redirect VNPay -> callback /payment/vnpay-callback"
