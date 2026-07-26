$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$ProjectGradleCache = Join-Path $Root ".gradle"
$Wrapper = Join-Path $Root "gradlew.bat"

Push-Location $Root
try {
    if (Test-Path $Wrapper) {
        & $Wrapper --stop
    }

    Remove-Item $ProjectGradleCache -Recurse -Force -ErrorAction SilentlyContinue

    if (-not (Test-Path $Wrapper)) {
        throw "gradlew.bat was not found. Run tools/bootstrap-gradle-wrapper.ps1 first."
    }

    & $Wrapper --no-configuration-cache clean check
    if ($LASTEXITCODE -ne 0) {
        throw "Recovery build failed with exit code $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}

Write-Host "Stale project-local Gradle metadata was removed and the no-cache check completed."
Write-Host "Subsequent builds may use the normal configuration-cache setting."
