# Start Spring Boot Backend in local mode (H2 in-memory database)
$mavenBin = "C:\Users\mallu\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin"
if (Test-Path $mavenBin) {
    $env:PATH = "$mavenBin;$env:PATH"
}
Set-Location -Path "$PSScriptRoot\backend"
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Starting AI Recruitment Backend Server... " -ForegroundColor Green
Write-Host " API Docs: http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
mvn clean spring-boot:run
