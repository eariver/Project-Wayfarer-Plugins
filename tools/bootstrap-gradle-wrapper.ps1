$ErrorActionPreference = "Stop"

$Version = "9.6.1"
$DistributionUrl = "https://services.gradle.org/distributions/gradle-$Version-bin.zip"
$ExpectedSha256 = "9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14"
$ExpectedWrapperSha256 = "497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7"

$Root = Split-Path -Parent $PSScriptRoot
$Temp = Join-Path ([System.IO.Path]::GetTempPath()) "wayfarer-gradle-$Version"
$Zip = Join-Path $Temp "gradle-$Version-bin.zip"
$Extract = Join-Path $Temp "extract"
$BootstrapGradleUserHome = Join-Path $Temp "gradle-user-home"
$ProjectGradleCache = Join-Path $Root ".gradle"

$PreviousGradleUserHome = $env:GRADLE_USER_HOME

Remove-Item $Temp -Recurse -Force -ErrorAction SilentlyContinue
New-Item $Temp -ItemType Directory | Out-Null
New-Item $BootstrapGradleUserHome -ItemType Directory | Out-Null

try {
    Invoke-WebRequest -Uri $DistributionUrl -OutFile $Zip
    $Actual = (Get-FileHash $Zip -Algorithm SHA256).Hash.ToLowerInvariant()

    if ($Actual -ne $ExpectedSha256) {
        throw "Gradle distribution checksum mismatch: $Actual"
    }

    Expand-Archive -Path $Zip -DestinationPath $Extract
    $Gradle = Join-Path $Extract "gradle-$Version/bin/gradle.bat"

    # Keep bootstrap-only Kotlin DSL and daemon state out of the user's normal
    # Gradle home, and never serialize the temporary Gradle installation path
    # into the project's Configuration Cache.
    $env:GRADLE_USER_HOME = $BootstrapGradleUserHome

    Push-Location $Root
    try {
        & $Gradle `
            --no-configuration-cache `
            wrapper `
            --gradle-version $Version `
            --distribution-type bin `
            --gradle-distribution-sha256-sum $ExpectedSha256

        if ($LASTEXITCODE -ne 0) {
            throw "Gradle wrapper generation failed with exit code $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }

    # The bootstrap distribution lives under %TEMP% and is deleted below.
    # Remove project-local bootstrap metadata so no absolute path to that
    # temporary installation can be reused by a later wrapper build.
    Remove-Item $ProjectGradleCache -Recurse -Force -ErrorAction SilentlyContinue
}
finally {
    if ($null -eq $PreviousGradleUserHome) {
        Remove-Item Env:GRADLE_USER_HOME -ErrorAction SilentlyContinue
    }
    else {
        $env:GRADLE_USER_HOME = $PreviousGradleUserHome
    }

    Remove-Item $Temp -Recurse -Force -ErrorAction SilentlyContinue
}

$WrapperJar = Join-Path $Root "gradle/wrapper/gradle-wrapper.jar"
if (-not (Test-Path $WrapperJar)) {
    throw "Generated Gradle Wrapper JAR was not found: $WrapperJar"
}

$ActualWrapperSha256 = (Get-FileHash $WrapperJar -Algorithm SHA256).Hash.ToLowerInvariant()
if ($ActualWrapperSha256 -ne $ExpectedWrapperSha256) {
    throw "Gradle Wrapper JAR checksum mismatch: $ActualWrapperSha256"
}

Write-Host "Gradle Wrapper generated and verified."
Write-Host "The bootstrap Configuration Cache was disabled and project-local bootstrap metadata was removed."
Write-Host "Commit gradlew, gradlew.bat and gradle/wrapper/."
