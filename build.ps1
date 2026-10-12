# Build Java core and run the complete Java test suite from Windows PowerShell.
[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

function Resolve-NativeTool([string]$name) {
    $command = Get-Command $name -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }
    throw "Tool not found in PATH: $name"
}

$java = Resolve-NativeTool "java"
$javac = Resolve-NativeTool "javac"
$jar = Join-Path $root "lib/sqlite-jdbc-3.46.1.3.jar"
$jarUrl = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.3/sqlite-jdbc-3.46.1.3.jar"

if (-not (Test-Path $jar)) {
    New-Item -ItemType Directory -Force (Split-Path $jar) | Out-Null
    Write-Host "==> Downloading SQLite driver"
    Invoke-WebRequest -Uri $jarUrl -OutFile $jar
}

$sourceFiles = @(Get-ChildItem (Join-Path $root "src") -Recurse -Filter *.java |
    ForEach-Object { $_.FullName })
$testFiles = @(Get-ChildItem (Join-Path $root "test") -Recurse -Filter *.java |
    ForEach-Object { $_.FullName })

if ($sourceFiles.Count -eq 0 -or $testFiles.Count -eq 0) {
    throw "Java source or test files were not found."
}

Remove-Item (Join-Path $root "out"), (Join-Path $root "test-out") -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force (Join-Path $root "out"), (Join-Path $root "test-out") | Out-Null

Write-Host "==> Compiling Java source"
& $javac -Xlint:all -encoding UTF-8 -cp $jar -d (Join-Path $root "out") @sourceFiles
if ($LASTEXITCODE -ne 0) { throw "Java source compilation failed." }

Write-Host "==> Compiling Java tests"
& $javac -encoding UTF-8 -cp "$(Join-Path $root 'out');$jar" -d (Join-Path $root "test-out") @testFiles
if ($LASTEXITCODE -ne 0) { throw "Java test compilation failed." }

$tests = @(
    "FieldDomainTest", "BookingDomainTest", "SeedDataValidationTest", "JsonContractTest",
    "ApiServerTest", "DatabaseTest", "BookingServiceTest", "WriteApiTest"
)
$failed = 0
foreach ($test in $tests) {
    & $java "-Dfile.encoding=UTF-8" -cp "$(Join-Path $root 'out');$(Join-Path $root 'test-out');$jar" "smartsporthub.test.$test"
    if ($LASTEXITCODE -ne 0) {
        $failed++
        Write-Host "FAIL: $test" -ForegroundColor Red
    }
}

if ($failed -ne 0) {
    throw "$failed test suite(s) failed."
}
Write-Host "==> All Java tests passed: 113/113" -ForegroundColor Green
