# Export toàn bộ database booking_tour từ MySQL local → file SQL
# Chạy: .\scripts\export-local-db.ps1

param(
    # Avoid clobbering built-in read-only $Host variable
    [string]$DbHost = "127.0.0.1",
    [int]$Port = 3306,
    [string]$User = "root",
    # Leave empty to be prompted securely
    [string]$Password = "",
    [string]$Database = "booking_tour",
    [string]$OutFile = "scripts/exports/booking_tour_local.sql"
)

$mysqldump = @(
    "C:\Program Files\MySQL\MySQL Server 9.4\bin\mysqldump.exe",
    "C:\Program Files\MySQL\MySQL Workbench 8.0 CE\mysqldump.exe"
) | Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $mysqldump) {
    Write-Error "Không tìm thấy mysqldump.exe. Cài MySQL Server hoặc sửa đường dẫn trong script."
    exit 1
}

$outDir = Split-Path $OutFile -Parent
if ($outDir -and -not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
}

Write-Host "Đang export $Database từ ${DbHost}:${Port} ..."

if (-not $Password) {
    $sec = Read-Host "MySQL password for $User@${DbHost}:${Port}" -AsSecureString
    $Password = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec)
    )
}

$env:MYSQL_PWD = $Password
& $mysqldump `
    --host=$DbHost --port=$Port `
    -u$User `
    --single-transaction --routines --triggers `
    --set-gtid-purged=OFF `
    $Database 2>$outDir\dump.err | Out-File -FilePath $OutFile -Encoding utf8

Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue

if ($LASTEXITCODE -ne 0) {
    Get-Content "$outDir\dump.err" -ErrorAction SilentlyContinue
    Write-Error "Export thất bại."
    exit 1
}

$size = (Get-Item $OutFile).Length
Write-Host "OK: $OutFile ($([math]::Round($size/1KB, 1)) KB)"
Write-Host "Bước tiếp: xem scripts/SYNC-DB-TO-RAILWAY.md để import lên Railway."
