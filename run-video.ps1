# Run SmartSportHub from Windows PowerShell without WSL.
[CmdletBinding()]
param(
    [ValidateSet("Help", "Build", "Api", "Web", "Open", "Test")]
    [string]$Role = "Help",
    [int]$ApiPort = 18083,
    [string]$DbPath = "db\video-demo.db"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

function Find-Tool([string]$name) {
    $command = Get-Command $name -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }
    throw "Tool not found in PATH: $name"
}

function Find-DotNet {
    $candidates = @(
        "D:\dev\dotnet\dotnet.exe",
        "$env:ProgramFiles\dotnet\dotnet.exe",
        "${env:ProgramFiles(x86)}\dotnet\dotnet.exe",
        "$env:LOCALAPPDATA\Microsoft\dotnet\dotnet.exe"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }
    $command = Get-Command dotnet -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }
    throw "dotnet SDK not found. Install the SDK or set its path in Find-DotNet."
}

function Invoke-Build {
    & (Join-Path $root "build.ps1")
    if ($LASTEXITCODE -ne 0) {
        throw "Java build failed."
    }
}

function Ensure-JavaBuild {
    $jar = Join-Path $root "lib\sqlite-jdbc-3.46.1.3.jar"
    $mainClass = Join-Path $root "out\smartsporthub\api\SmartSportHubApi.class"
    if (-not (Test-Path $jar) -or -not (Test-Path $mainClass)) {
        Invoke-Build
    }
}

function Show-Help {
    @"
SmartSportHub Windows runner

  .\run-video.ps1 -Role Build
      Compile Java and run 113 Java tests.

  .\run-video.ps1 -Role Api
      Start Java API at http://localhost:18083.
      Keep this PowerShell window open.

  .\run-video.ps1 -Role Web
      Start ASP.NET Core at http://localhost:5080.
      Open another PowerShell window after starting Api.

  .\run-video.ps1 -Role Open
      Open the web application in the default browser.

  .\run-video.ps1 -Role Test
      Run the C# test suite (43 tests) with D:\dev\dotnet\dotnet.exe.

No WSL distribution is required. The Api/Web/Test roles find dotnet.exe at
D:\dev\dotnet when dotnet is not available in PATH.
"@
}

switch ($Role) {
    "Build" {
        Invoke-Build
    }
    "Api" {
        Ensure-JavaBuild
        $java = Find-Tool "java"
        $jar = Join-Path $root "lib\sqlite-jdbc-3.46.1.3.jar"
        if ([System.IO.Path]::IsPathRooted($DbPath)) {
            $db = $DbPath
        } else {
            $db = Join-Path $root $DbPath
        }
        $db = [System.IO.Path]::GetFullPath($db)
        & $java "-Dfile.encoding=UTF-8" "-cp" "$(Join-Path $root 'out');$jar" `
            "smartsporthub.api.SmartSportHubApi" $ApiPort $db
    }
    "Web" {
        $dotnet = Find-DotNet
        $env:DOTNET_ROOT = Split-Path -Parent $dotnet
        $env:Path = "$($env:DOTNET_ROOT);$($env:Path)"
        $project = Join-Path $root "web\SmartSportHub.Web\SmartSportHub.Web.csproj"
        & $dotnet run --project $project --launch-profile "SmartSportHub.Web"
    }
    "Open" {
        Start-Process "http://localhost:5080/"
    }
    "Test" {
        $dotnet = Find-DotNet
        $env:DOTNET_ROOT = Split-Path -Parent $dotnet
        $env:Path = "$($env:DOTNET_ROOT);$($env:Path)"
        $tests = Join-Path $root "web\SmartSportHub.Web.Tests\SmartSportHub.Web.Tests.csproj"
        & $dotnet test $tests --no-restore
    }
    default {
        Show-Help
    }
}
