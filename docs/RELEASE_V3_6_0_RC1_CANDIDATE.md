# RotomPedia v3.6.0-rc1 — PRE-RELEASE EXPERIMENTAL

**Estado:** compilada y firmada localmente el **10 nov 2026**; pre-release en preparación (tag `v3.6.0-rc1` + draft para que el CI verifique y publique). `versionCode=30601`, `versionName=3.6.0-rc1`, `applicationId=com.duoc.rotompedia`, minSdk 25.

## Evidencia de compilación y pruebas (10 nov 2026)

- **43 pruebas Python** (`unittest discover -s tools/tests`) correctas, más los cuatro generadores en `--verify-only` (pokémon 1025 especies/1351 variedades, ítems 2223, referencias 937 movimientos, ubicaciones Hoenn/Esmeralda 115 áreas con encuentros, 1644 filas, 155 variedades; hash del piloto `02b005f95763388ee6e6555d8c0df272127406bfbc1aa7793e3ac41b3b7b1219` idéntico al recurso de pruebas).
- **202 pruebas Kotlin** (`testDebugUnitTest`) correctas en suite completa (`--max-workers=1`). `ItemUiTest` sufrió un timeout de Compose puntual bajo carga en una ejecución; pasó 3/3 aislado y volvió a pasar en la suite completa siguiente.
- **`lintDebug`: 0 errores** (112 avisas/29 sugerencias preexistentes). Dos errores `ProduceStateDoesNotAssignValue` eran un falso positivo conocido de AGP 8.12.3 ([issuetracker 349411310](https://issuetracker.google.com/issues/349411310)): hasta `produceState(0) { value = 1 }` se marca. Supresión documentada en `VerifiedHoennMapScreen.kt` con la asignación `value = …` presente en ambos productores.
- **`assembleDebug` y `assembleDebugAndroidTest`** correctos; **`assembleRelease`** correcto (variante release real, no debuggable).
- **Corrección de recursos:** tres imágenes tenían extensión falsa respecto a su contenido real (`rotomfondo.png` era JPEG, `map_paldea.jpg` y `map_sinnoh.jpg` eran WebP) y AAPT2 rechaza eso en release. Renombradas a `rotomfondo.jpg`, `map_paldea.webp`, `map_sinnoh.webp` **sin cambiar el nombre de recurso** (`R.drawable.rotomfondo`, `getIdentifier("map_$region")` intactos). RC14 contenía el JPEG a trozo crudo porque era build debug sin crunch.
- **Corrección de compilación:** import de `rememberSaveable` mal paquetado en `MapScreen.kt` y `VerifiedHoennMapScreen.kt` (`androidx.compose.runtime.saveable`), y ámbito de `maxHeight` en el diálogo a pantalla completa.

## Firmas y checksum (10 nov 2026)

- APK release firmada: `release-candidate-output/RotomPedia-v3.6.0-rc1.apk` (+ `.sha256`).
- **Certificado firmante SHA-256:** `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa` — **idéntico al de RC14** (clave original `~/.android/debug.keystore`, alias `androiddebugkey`): actualización compatible sin desinstalar. No se creó ni reemplazó keystore alguno.
- **SHA-256 del APK:** `84c0a44da7d8c3cf0f8518a37ded837d7e7e6a105a750ba3b8d2675c43e1c34a`.

## Cambios desde v3.5.0-rc14

- Piloto funcional de Hoenn/Esmeralda con catálogo de encuentros auditados y vista de secciones; aún NO es un mapa ilustrado definitivo.
- Selector regional al entrar por primera vez a Mapas; Kanto primero y destacado.
- Memoria local de la última región visitada al volver a Mapas; botón Atrás devuelve al selector para cambiar de región.
- Ficha de ubicación organizada por Pokémon y Objetos/NPC; agrupación por método (hierba/cueva, pesca, Surf, Cabezazo, Golpe Roca y otros cuando estén documentados).
- Selección explícita de zonas superpuestas; sin pines inventados ni datos de objetos/otras ediciones simulados.
- Primera APK de la serie construida en **variante release** (las RC anteriores eran builds debug/no-crunch con `debuggable=true`).

## Limitaciones

- Los mapas ilustrados originales de Kanto y Hoenn todavía están pendientes. Otros mapas conservan recursos provisionales; no anunciar esta versión como la reconstrucción artística final.
- Objetos/NPC por ubicación y otras ediciones no tienen cobertura suficiente para mostrarse como completos. Sin afirmar inexistencia de datos.
- **Prueba en dispositivo pendiente:** no hay emulador ni teléfono conectado a este entorno; la validación visual y de interacción (selector, Hoenn/Ruta 102→Ralts, zoom) no está ejecutada. Las pruebas automatizadas no sustituyen la revisión manual.
- Se debe instalar **encima** de RC14 para validar conservación de cuentas, equipos y favoritos. **NO desinstalar** primero. No retroceder de versión después de abrir la base de datos.

## Controles obligatorios antes de publicar

1. `versionCode=30601`, `versionName=3.6.0-rc1`, `applicationId=com.duoc.rotompedia`. ✔ local
2. `python -m unittest discover -s tools/tests -v`, generadores `--verify-only`, Gradle `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease`. ✔ local (10 nov 2026); CI repite en el tag.
3. Certificado firmante SHA-256 **5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa**: tiene que coincidir con RC14. No crear/reemplazar la clave. ✔ verificado con `apksigner` contra RC14.
4. Generar APK y `.sha256`; subirlos en pre-release **draft**; CI de tag debe comprobar y publicar. No marcar estable o Latest.
5. Prueba manual: primera visita → selector con Kanto primero; elegir Hoenn → abrir Ruta 102 → encuentros por método → ficha Pokémon; salir, volver a Mapas → Hoenn; Atrás → selector. Comprobar Room/usuario/equipos/favoritos tras actualizar RC14. **Pendiente.**

**Nota:** la clave privada nunca se incrusta en el repositorio ni viaja en la APK. El script `tools/publish-v36-windows.ps1` compila, firma y valida en el equipo del propietario; su modo `-Publish` deja el draft para que el CI (`.github/scripts/publish-prerelease.sh`) verifique certificado, checksum y `versionName` antes de publicar.
