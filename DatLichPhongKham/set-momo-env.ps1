# Script to set MoMo environment variables for Windows PowerShell
# Run this before starting the Spring Boot application

# Set MoMo environment variables
$env:MOMO_PARTNER_CODE = "MOMOD3WW20251129_TEST"
$env:MOMO_ACCESS_KEY = "WYyd3BeN9LE4S0W1"
$env:MOMO_SECRET_KEY = "OdYnJGOsh3s3ArEfMmKh5L81BvxY29Kk"

Write-Host "MoMo environment variables set!" -ForegroundColor Green

# Check if port 8080 is in use and kill the process if needed
$port8080 = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($port8080) {
    Write-Host "Port 8080 is already in use. Attempting to free it..." -ForegroundColor Yellow
    $processId = $port8080.OwningProcess
    if ($processId) {
        try {
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
            Write-Host "Process $processId on port 8080 has been terminated." -ForegroundColor Green
            Start-Sleep -Seconds 2
        } catch {
            Write-Host "Could not terminate process $processId. Please manually stop the process using port 8080." -ForegroundColor Red
            exit 1
        }
    }
} else {
    Write-Host "Port 8080 is available." -ForegroundColor Green
}

Write-Host "Starting Spring Boot application..." -ForegroundColor Yellow
./mvnw spring-boot:run

