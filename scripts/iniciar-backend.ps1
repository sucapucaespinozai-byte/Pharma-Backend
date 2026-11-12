param(
    [ValidateSet('local', 'dev', 'prod')][string]$Perfil = 'dev',
    [switch]$Reiniciar,
    [ValidatePattern('^\d+(ms|s|m|h)$')][string]$CacheTtl = '10m'
)
$ErrorActionPreference = 'Stop'
$raizPractica = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$backendPractica = Join-Path $raizPractica 'PharmaBackend'
$frontendPractica = Join-Path $raizPractica 'PharmaSoft'
$javaPractica = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'C:/Users/sucap/.gradle/jdks/eclipse_adoptium-21-amd64-windows.2/bin/java.exe' }
$jarPractica = Join-Path $backendPractica 'target/tienda-0.0.1-SNAPSHOT.jar'
if (!(Test-Path -LiteralPath $javaPractica)) { throw 'No se encuentra el Java 21 configurado.' }
if (!(Test-Path -LiteralPath $jarPractica)) { throw 'Primero compila PharmaBackend: .\mvnw.cmd package -DskipTests' }
$localPractica = @{}
$archivoLocalPractica = Join-Path $backendPractica 'src/main/resources/application-local.properties'
if (Test-Path -LiteralPath $archivoLocalPractica) { Get-Content $archivoLocalPractica | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') { $localPractica[$matches[1]] = $matches[2] }
} }
if (!$env:FRONTEND_ORIGINS) { $env:FRONTEND_ORIGINS = 'http://localhost:4200,http://127.0.0.1:4200,http://localhost:8081' }
if ($Perfil -eq 'prod') {
    if (!$env:DB_URL) { $env:DB_URL = 'jdbc:oracle:thin:@//localhost:1525/FREEPDB1' }
    if (!$env:DB_USERNAME) { $env:DB_USERNAME = 'tienda' }
    if (!$env:DB_PASSWORD) { $env:DB_PASSWORD = $localPractica['spring.datasource.password'] }
    if (!$env:DB_PASSWORD) { throw 'Configura DB_PASSWORD por entorno o en application-local.properties (excluido de Git).' }
    $carpetaLocalPractica = Join-Path $backendPractica '.local'
    New-Item -ItemType Directory -Force $carpetaLocalPractica | Out-Null
    $claveLocalPractica = Join-Path $carpetaLocalPractica 'jwt-prod.txt'
    if (!(Test-Path -LiteralPath $claveLocalPractica)) {
        $bytesClavePractica = New-Object byte[] 48
        $generadorPractica = [Security.Cryptography.RandomNumberGenerator]::Create()
        try { $generadorPractica.GetBytes($bytesClavePractica) } finally { $generadorPractica.Dispose() }
        [Convert]::ToBase64String($bytesClavePractica) | Set-Content -LiteralPath $claveLocalPractica
    }
    if (!$env:JWT_SECRET) { $env:JWT_SECRET = (Get-Content -LiteralPath $claveLocalPractica -Raw).Trim() }
}
$escuchaPractica = Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -First 1
if ($escuchaPractica) {
    $procesoPractica = Get-CimInstance Win32_Process -Filter "ProcessId=$($escuchaPractica.OwningProcess)"
    if ($Reiniciar -and $procesoPractica.CommandLine -like '*PharmaBackend*' -and $procesoPractica.Name -eq 'java.exe') {
        Stop-Process -Id $procesoPractica.ProcessId
        Start-Sleep -Seconds 2
        $escuchaPractica = $null
    } elseif ($procesoPractica.CommandLine -notlike '*PharmaBackend*') {
        throw 'El puerto 8080 pertenece a otro programa. No se ha detenido.'
    } else { Write-Host 'El backend de farmacia ya esta encendido en 8080. No se inicia otra copia.' }
}
if (!$escuchaPractica) {
    New-Item -ItemType Directory -Force (Join-Path $backendPractica 'logs') | Out-Null
    Start-Process -FilePath $javaPractica -ArgumentList @('-jar', $jarPractica, '--server.port=8080', "--spring.profiles.active=$Perfil", "--spring.cache.redis.time-to-live=$CacheTtl") -WorkingDirectory $backendPractica -WindowStyle Hidden -RedirectStandardOutput (Join-Path $backendPractica 'logs/consola.log') -RedirectStandardError (Join-Path $backendPractica 'logs/errores-java.log') | Out-Null
    $backendListoPractica = $false
    for ($intentoPractica=0; $intentoPractica -lt 60; $intentoPractica++) {
        try { Invoke-WebRequest 'http://localhost:8080/api/v1/health' -UseBasicParsing -TimeoutSec 2 | Out-Null; $backendListoPractica=$true; break } catch { Start-Sleep -Seconds 1 }
    }
    if (!$backendListoPractica) { throw 'El backend no arranco. Revisa PharmaBackend/logs/consola.log.' }
}
Write-Host "Backend de farmacia listo: http://localhost:8080/api/v1"
