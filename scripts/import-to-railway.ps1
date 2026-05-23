# Import booking_tour_local.sql lên MySQL Railway (từ máy Windows)
# Chạy: .\scripts\import-to-railway.ps1
# Cần: Public Networking đã bật trên service MySQL Railway

param(
    [string]$SqlFile = "scripts/exports/booking_tour_local.sql",
    [string]$RailwayHost = "",
    [int]$RailwayPort = 0,
    [string]$RailwayUser = "",
    [string]$RailwayPassword = "",
    [string]$RailwayDatabase = ""
)

$mysql = @(
    "C:\Program Files\MySQL\MySQL Server 9.4\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Workbench 8.0 CE\mysql.exe"
) | Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $mysql) {
    Write-Error "Không tìm thấy mysql.exe. Cài MySQL Server hoặc dùng MySQL Workbench (xem SYNC-DB-TO-RAILWAY.md)."
    exit 1
}

if (-not (Test-Path $SqlFile)) {
    Write-Error "Không thấy file $SqlFile — chạy trước: .\scripts\export-local-db.ps1"
    exit 1
}

if (-not $RailwayHost) { $RailwayHost = Read-Host "MYSQLHOST (Railway, vd: monorail.proxy.rlwy.net)" }
if ($RailwayPort -eq 0) { $RailwayPort = [int](Read-Host "MYSQLPORT (Railway, vd: 12345)") }
if (-not $RailwayUser) { $RailwayUser = Read-Host "MYSQLUSER (Railway)" }
if (-not $RailwayPassword) { $RailwayPassword = Read-Host "MYSQLPASSWORD (Railway)" -AsSecureString; $RailwayPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($RailwayPassword)) }
if (-not $RailwayDatabase) { $RailwayDatabase = Read-Host "MYSQLDATABASE (Railway, vd: railway)" }

Write-Host ""
Write-Host "CANH BAO: Import se GHI DE toan bo du lieu tren Railway MySQL!" -ForegroundColor Yellow
Write-Host "Host: $RailwayHost`:$RailwayPort  DB: $RailwayDatabase  User: $RailwayUser"
$confirm = Read-Host "Go tiep? (y/N)"
if ($confirm -ne "y" -and $confirm -ne "Y") { exit 0 }

$env:MYSQL_PWD = $RailwayPassword
Write-Host "Dang import (co the mat 1-2 phut)..."
Get-Content $SqlFile -Raw | & $mysql --host=$RailwayHost --port=$RailwayPort -u$RailwayUser $RailwayDatabase 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Import thanh cong!" -ForegroundColor Green
    Write-Host "Buoc tiep: Railway -> service Spring Boot -> Deploy / Restart"
} else {
    Write-Error "Import that bai. Xem loi phia tren hoac dung MySQL Workbench."
}
Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
