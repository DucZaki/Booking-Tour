Param(
    [switch]$ForceDownload
)

function Has-Java {
    try { & java -version > $null 2>&1; return $LASTEXITCODE -eq 0 } catch { return $false }
}

if (Has-Java -and -not $ForceDownload) {
    Write-Host "Java found on PATH - running mvnw.cmd..."
    & .\mvnw.cmd -DskipTests spring-boot:run
    exit $LASTEXITCODE
}

$JDK_DIR = Join-Path $PSScriptRoot ".jdk"
if (!(Test-Path $JDK_DIR)) { New-Item -ItemType Directory -Path $JDK_DIR | Out-Null }

$apiUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/adoptium"
$zipPath = Join-Path $JDK_DIR "openjdk.zip"

Write-Host "Java not found. Downloading Temurin JDK 17 to $JDK_DIR (may take a few minutes)..."
try {
    Invoke-WebRequest -Uri $apiUrl -OutFile $zipPath -TimeoutSec 300
} catch {
    Write-Error "Failed to download JDK: $_"
    exit 1
}

Write-Host "Extracting JDK..."
try {
    Expand-Archive -Path $zipPath -DestinationPath $JDK_DIR -Force
} catch {
    Write-Error "Extraction failed: $_"
    exit 1
}
Remove-Item $zipPath -Force

# Pick the first extracted folder as JAVA_HOME
$dir = Get-ChildItem -Path $JDK_DIR | Where-Object { $_.PSIsContainer } | Select-Object -First 1
if (-not $dir) { Write-Error "No JDK folder found after extraction."; exit 1 }
$javaHome = $dir.FullName

Write-Host "Setting JAVA_HOME to $javaHome for this session."
$env:JAVA_HOME = $javaHome
$env:Path = "$($javaHome)\bin;" + $env:Path

Write-Host "Running mvnw.cmd - this will start the Spring Boot app (Ctrl+C to stop)."
& .\mvnw.cmd -DskipTests spring-boot:run
