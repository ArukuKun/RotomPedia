# RotomPedia 3.6.0-rc1: compilar, firmar con la clave ORIGINAL y preparar pre-release.
# Ejecutar desde PowerShell 7 o Windows PowerShell 5.1. No requiere abrir Android Studio.
# SIN -Publish: solo compila, firma y verifica localmente. No modifica GitHub.
# CON -Publish: crea rama y tag NUEVOS y deja una pre-release en draft para el CI.
# Nunca copia un keystore a GitHub ni cambia el certificado de las RC anteriores.
[CmdletBinding()]
param(
    [switch]$Publish,
    [string]$KeyStorePath = '',
    [string]$KeyAlias = 'androiddebugkey',
    [switch]$PromptPasswords
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$expectedCert = '5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa'
$tag = 'v3.6.0-rc1'
$branch = 'feature/v3.6-hoenn-atlas-rebuild'
$repo = 'ArukuKun/RotomPedia'
$baseBranch = 'feature/v3.5-main-games-hisui'
$projectRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $projectRoot 'RotomPedia'
$gradleBuildFile = Join-Path $androidRoot 'app\build.gradle.kts'
$defaultDebugKey = Join-Path $env:USERPROFILE '.android\debug.keystore'
if ([string]::IsNullOrWhiteSpace($KeyStorePath)) { $KeyStorePath = $defaultDebugKey }

function Assert-Exit([string]$name) {
    if ($LASTEXITCODE -ne 0) { throw "$name falló (código $LASTEXITCODE). No se publicó nada." }
}
function PlainFromSecure([System.Security.SecureString]$s) {
    $p=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($p) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($p) }
}
function Find-AndroidTool([string]$name, [string]$sdk) {
    $buildTools = Join-Path $sdk 'build-tools'
    if (-not (Test-Path $buildTools)) { throw "No encuentro Android SDK Build Tools en $buildTools" }
    $dirs=Get-ChildItem $buildTools -Directory | Sort-Object { try { [version]$_.Name } catch { [version]'0.0' } } -Descending
    foreach ($dir in $dirs) {
        foreach ($extension in @('.bat','.exe','')) {
            $candidate=Join-Path $dir.FullName ($name+$extension)
            if (Test-Path $candidate) { return $candidate }
        }
    }
    throw "No encuentro $name en Android SDK Build Tools. Instálalo con SDK Manager."
}

Write-Host '==== ROTOMPEDIA: COMPILACIÓN Y FIRMA COMPATIBLE ====' -ForegroundColor Cyan
if (-not (Test-Path $gradleBuildFile)) { throw "Falta $gradleBuildFile. Extrae el ZIP completo." }
$buildScript = Get-Content $gradleBuildFile -Raw
if ($buildScript -notmatch 'versionCode\s*=\s*30601' -or $buildScript -notmatch 'versionName\s*=\s*"3\.6\.0-rc1"') {
    throw 'La versión del código no es 3.6.0-rc1 (30601). No publicar un APK con numeración antigua.'
}
if (-not (Test-Path $KeyStorePath)) {
    throw "No encontré la CLAVE PRIVADA ORIGINAL en '$KeyStorePath'. No generes otra clave. Busca el debug.keystore o el .jks usado para firmar RC14."
}
$gradlew=Join-Path $androidRoot 'gradlew.bat'
if (-not (Test-Path $gradlew)) { throw "Falta gradlew.bat en $androidRoot" }
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw 'Java/JDK no está instalado o configurado en PATH (se recomienda JDK 17).' }
$sdk=$env:ANDROID_HOME
if ([string]::IsNullOrWhiteSpace($sdk)) { $sdk=$env:ANDROID_SDK_ROOT }
if ([string]::IsNullOrWhiteSpace($sdk)) { $sdk=Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if (-not (Test-Path $sdk)) { throw "Android SDK no encontrado en '$sdk'. Ajusta ANDROID_HOME o instala el SDK." }
$env:ANDROID_HOME=$sdk
$apkSigner=Find-AndroidTool 'apksigner' $sdk
$aapt=Find-AndroidTool 'aapt' $sdk

# Validar los tests de generadores antes de compilar. Python 3 es requerido por CI.
# El alias de Microsoft Store devuelve 9009 sin ser Python: se valida la salida real.
function Find-Python {
    $candidates = @()
    if (-not [string]::IsNullOrWhiteSpace($env:ROTOM_PYTHON)) { $candidates += $env:ROTOM_PYTHON }
    foreach ($command in @('python3', 'py')) {
        $found = Get-Command $command -ErrorAction SilentlyContinue
        if ($found) { $candidates += $found.Source }
    }
    $candidates += 'python'
    $candidates += (Join-Path $env:LOCALAPPDATA 'Temp\opencode\python312\python.exe')
    foreach ($candidate in $candidates) {
        try {
            $version = & $candidate --version 2>&1
            if ($LASTEXITCODE -eq 0 -and ($version -join ' ') -match '^Python 3\.') { return $candidate }
        } catch { }
    }
    throw 'Falta un Python 3 funcional para los tests de datos (el alias de la Microsoft Store no sirve).'
}
$pythonExe = Find-Python
Write-Host "Python de trabajo: $pythonExe"
Push-Location $projectRoot
try {
    & $pythonExe -m unittest discover -s tools/tests -v
    Assert-Exit 'Pruebas Python'
    foreach ($indexScript in @('generate_pokemon_index.py','generate_item_index.py','generate_reference_index.py','generate_location_index.py')) {
        $arguments = @((Join-Path 'tools' $indexScript), '--verify-only')
        if ($indexScript -eq 'generate_location_index.py') { $arguments += 'RotomPedia/app/src/main/assets/hoenn-emerald-location-pilot.json' }
        & $pythonExe @arguments
        Assert-Exit "Verificación $indexScript"
    }
} finally { Pop-Location }

Push-Location $androidRoot
try {
    & $gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease --stacktrace --max-workers=1
    Assert-Exit 'Gradle/Android'
} finally { Pop-Location }

$unsigned=Join-Path $androidRoot 'app\build\outputs\apk\release\app-release-unsigned.apk'
if (-not (Test-Path $unsigned)) {
    throw "No se generó APK release sin firmar en '$unsigned'. Revisa la salida de Gradle."
}
$outputDir=Join-Path $projectRoot 'release-candidate-output'
New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
$signed=Join-Path $outputDir "RotomPedia-$tag.apk"

# La contraseña no se escribe en el script ni en un archivo. apksigner la lee de variables de entorno.
if (($KeyStorePath -eq $defaultDebugKey) -and ($KeyAlias -eq 'androiddebugkey') -and (-not $PromptPasswords)) {
    $storePassword='android'
    $keyPassword='android'
    Write-Host 'Probando credenciales estándar del keystore debug existente.'
} else {
    Write-Host 'Introduce las contraseñas de TU keystore original (no las compartas por chat).'
    $storePassword=PlainFromSecure (Read-Host 'Contraseña del keystore' -AsSecureString)
    $keyPassword=PlainFromSecure (Read-Host 'Contraseña de la clave (Enter si es la misma)' -AsSecureString)
    if ([string]::IsNullOrEmpty($keyPassword)) { $keyPassword=$storePassword }
}
try {
    $env:ROTOM_SIGN_STORE_PASS=$storePassword
    $env:ROTOM_SIGN_KEY_PASS=$keyPassword
    & $apkSigner sign --ks $KeyStorePath --ks-key-alias $KeyAlias --ks-pass 'env:ROTOM_SIGN_STORE_PASS' --key-pass 'env:ROTOM_SIGN_KEY_PASS' --out $signed $unsigned
    Assert-Exit 'Firma APK'
} finally {
    Remove-Item Env:ROTOM_SIGN_STORE_PASS -ErrorAction SilentlyContinue
    Remove-Item Env:ROTOM_SIGN_KEY_PASS -ErrorAction SilentlyContinue
    $storePassword=$null
    $keyPassword=$null
}

$certOutput = @(& $apkSigner verify --verbose --print-certs $signed 2>&1)
Assert-Exit 'Verificación de apksigner'
$certText=$certOutput -join "`n"
$m=[regex]::Match($certText,'Signer #1 certificate SHA-256 digest:\s*([a-fA-F0-9]{64})')
if (-not $m.Success) { throw 'No pude leer el certificado SHA-256 del APK. Publicación bloqueada.' }
$actualCert=$m.Groups[1].Value.ToLowerInvariant()
if ($actualCert -ne $expectedCert) {
    Remove-Item $signed -Force -ErrorAction SilentlyContinue
    throw "FIRMA INCOMPATIBLE: $actualCert. Se eliminó el APK resultante para impedir una actualización inválida. Debes usar la clave ORIGINAL de RC14."
}
$metadata=(& $aapt dump badging $signed) -join "`n"
Assert-Exit 'Lectura de metadatos'
if ($metadata -notmatch "package: name='com\.duoc\.rotompedia' versionCode='30601' versionName='3\.6\.0-rc1'") {
    throw 'Package/versión incorrectos. No se publicará el APK.'
}
$sha=(Get-FileHash -Path $signed -Algorithm SHA256).Hash.ToLowerInvariant()
$shaFile="$signed.sha256"
[IO.File]::WriteAllText($shaFile,"$sha  $(Split-Path $signed -Leaf)`n",[Text.UTF8Encoding]::new($false))
Write-Host "APK VERIFICADO: $signed" -ForegroundColor Green
Write-Host "Certificado compatible: $actualCert" -ForegroundColor Green
Write-Host "SHA-256: $sha"
if (-not $Publish) {
    Write-Host 'Listo: compilado y firmado LOCALMENTE. No se ha enviado a GitHub.' -ForegroundColor Cyan
    Write-Host 'Para intentar publicar, repite con -Publish. No borres tu APK RC14 ni desinstales la app.'
    exit 0
}

# Publicación: se requiere identidad GitHub autenticada, rama nueva, tag nuevo y confirmación.
foreach ($tool in @('git','gh','robocopy')) {
    if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) { throw "Falta $tool. El APK YA se creó localmente; no se publicó." }
}
# gh puede no estar en PATH: se añade su ubicación conocida de esta máquina sin tocar el sistema.
$ghDir = Join-Path $env:LOCALAPPDATA 'Temp\opencode\gh\bin'
if (-not (Get-Command gh -ErrorAction SilentlyContinue) -and (Test-Path (Join-Path $ghDir 'gh.exe'))) {
    $env:PATH = "$ghDir;$env:PATH"
}
& gh auth status
Assert-Exit 'Autenticación de GitHub CLI'
$gitUrl="https://github.com/$repo.git"
$branchExists=@(& git ls-remote --heads $gitUrl "refs/heads/$branch")
Assert-Exit 'Verificación rama remota'
if ($branchExists.Count -gt 0 -and $branchExists[0]) { throw "La rama $branch ya existe; no se sobrescribirá." }
$tagExists=@(& git ls-remote --tags $gitUrl "refs/tags/$tag")
Assert-Exit 'Verificación tag remoto'
if ($tagExists.Count -gt 0 -and $tagExists[0]) { throw "El tag $tag ya existe; no se sobrescribirá." }
Write-Host "Se creará $branch y el tag $tag en $repo y se subirá un draft de pre-release."
$confirmation=Read-Host 'Escribe PUBLICAR para confirmar'
if ($confirmation -cne 'PUBLICAR') { Write-Host 'Publicación cancelada. El APK local permanece intacto.';exit 0 }

$tmp=Join-Path $env:TEMP ("rotompedia-v36-"+[guid]::NewGuid().ToString('N'))
& git clone --quiet --branch $baseBranch --single-branch $gitUrl $tmp
Assert-Exit 'Clonado del repositorio'
if (-not (Test-Path (Join-Path $tmp '.git'))) { throw 'Clonado incompleto. No se hará push.' }
# Solo se limpia una copia temporal creada por este script; jamás el proyecto fuente ni el directorio del usuario.
Get-ChildItem $tmp -Force | Where-Object { $_.Name -ne '.git' } | Remove-Item -Force -Recurse
& robocopy $projectRoot $tmp /E /XD '.git' '.gradle' 'build' '.idea' '.vscode' 'release-candidate-output' /XF 'local.properties' '*.jks' '*.keystore' '*.p12' '*.pfx' '*.apk' '*.aab' '*.key' '.env' '*.log' /NFL /NDL /NJH /NJS /NP | Out-Null
$robocopyCode=$LASTEXITCODE
if ($robocopyCode -gt 7) { throw "Copia al repositorio temporal falló: robocopy $robocopyCode" }
& git -C $tmp checkout -b $branch
Assert-Exit 'Nueva rama local'
& git -C $tmp add -A
Assert-Exit 'git add'
$staged=@(& git -C $tmp diff --cached --name-only)
if (-not $staged -or $staged.Count -eq 0) { throw 'No hay cambios para publicar.' }
if ($staged | Where-Object { $_ -match '(?i)(\.apk$|\.aab$|\.jks$|\.keystore$|\.p12$|\.pfx$|\.env$|local\.properties$)' }) {
    throw 'Se detectaron archivos sensibles/binaries en git add. Push detenido.'
}
& git -C $tmp commit -m 'feat(v3.6): Hoenn atlas and regional selector memory release candidate'
Assert-Exit 'git commit'
& git -C $tmp push -u origin $branch
Assert-Exit 'Push rama'
& git -C $tmp tag -a $tag -m 'RotomPedia v3.6.0-rc1 candidate'
Assert-Exit 'Creación de tag'
& git -C $tmp push origin $tag
Assert-Exit 'Push tag'
$notes=Join-Path $tmp 'docs\RELEASE_V3_6_0_RC1_CANDIDATE.md'
& gh release create $tag --repo $repo --verify-tag --draft --prerelease --title "RotomPedia $tag (pruebas)" --notes-file $notes $signed $shaFile
Assert-Exit 'Creación de draft de pre-release'
Write-Host "Draft creado: https://github.com/$repo/releases/tag/$tag" -ForegroundColor Green
Write-Host 'CI del tag verificará APK, fuente y certificado antes de publicar la pre-release.'
Write-Host 'Si el workflow de tag termina antes de que exista el draft, vuelve a lanzarlo manualmente desde Actions para este tag.'
Write-Host 'No desinstales RC14; valida esta actualización y tus datos personales en el dispositivo.'
