param(
    [switch]$SkipPdf
)

$ErrorActionPreference = 'Stop'

$reportPath = Join-Path $PSScriptRoot 'report.md'
$pdfPath = Join-Path $PSScriptRoot 'report.pdf'

if (-not (Test-Path $reportPath)) {
    throw "Could not find report file: $reportPath"
}

$content = Get-Content -Path $reportPath -Raw -Encoding UTF8
$pattern = '(?m)^\| \*\*Number of characters \(including spaces\)\*\* \| .* \|\r?$'

if ([regex]::IsMatch($content, $pattern)) {
    for ($i = 0; $i -lt 8; $i++) {
        $count = $content.Length
        $replacement = "| **Number of characters (including spaces)** | $count |"
        $newContent = [regex]::Replace(
            $content,
            $pattern,
            [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $replacement },
            1
        )

        if ($newContent -eq $content) {
            break
        }

        $content = $newContent
    }

    Set-Content -Path $reportPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "Updated character count: $count"
}
else {
    Write-Host 'Skipped character count update: row not found.'
}

if (-not $SkipPdf) {
    Push-Location $PSScriptRoot
    try {
        pandoc report.md -o report.pdf --pdf-engine tectonic
        if ($LASTEXITCODE -ne 0) {
            throw "pandoc failed with exit code $LASTEXITCODE"
        }
        Write-Host "Built PDF: $pdfPath"
    }
    finally {
        Pop-Location
    }
}
