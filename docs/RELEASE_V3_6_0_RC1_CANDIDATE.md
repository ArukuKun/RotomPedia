# RotomPedia v3.6.0-rc1 — PRE-RELEASE EXPERIMENTAL

**Estado histórico corregido:** [RC1 publicada como pre-release el 10 nov 2026](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.6.0-rc1), `30601`/`3.6.0-rc1`, tag en `a34b3d1`. APK release SHA-256 `84c0a44da7d8c3cf0f8518a37ded837d7e7e6a105a750ba3b8d2675c43e1c34a`, certificado `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa` idéntico a RC14; Release `42844833432` y Verify `42858423227` verdes. La carpeta incorporada el 11 nov había recuperado el texto anterior de «no publicada».

**Importante:** los cambios del diseño aprobado y atlas CC0 del 11 nov son continuación local en `feature/v3.6-regional-atlas-ui` (§5.9 del único Plan Maestro), **no están en el APK RC1 público**. No reemplazar este tag/assets ni ejecutar `-Publish` del script histórico. Una nueva entrega requiere código de versión creciente y otro tag. No hay versión estable de v3.5 ni se crea retrospectivamente.

## Cambios desde v3.5.0-rc14

- Piloto funcional de Hoenn/Esmeralda con catálogo de encuentros auditados y vista de secciones; aún NO es un mapa ilustrado definitivo.
- Selector regional al entrar por primera vez a Mapas; Kanto primero y destacado.
- Memoria local de la última región visitada al volver a Mapas; botón Atrás devuelve al selector para cambiar de región.
- Ficha de ubicación organizada por Pokémon y Objetos/NPC; agrupación por método (hierba/cueva, pesca, Surf, Cabezazo, Golpe Roca y otros cuando estén documentados).
- Selección explícita de zonas superpuestas; sin pines inventados ni datos de objetos/otras ediciones simulados.

## Limitaciones

- Los mapas ilustrados originales de Kanto y Hoenn todavía están pendientes. Otros mapas conservan recursos provisionales; no anunciar esta versión como la reconstrucción artística final.
- Objetos/NPC por ubicación y otras ediciones no tienen cobertura suficiente para mostrarse como completos. Sin afirmar inexistencia de datos.
- Requiere compilar y ejecutar pruebas Android (SDK, Gradle y dispositivo) antes de pre-release.
- Se debe instalar **encima** de RC14 para validar conservación de cuentas, equipos y favoritos. **NO desinstalar** primero. No retroceder de versión después de abrir la base de datos.

## Controles obligatorios antes de publicar

1. `versionCode=30601`, `versionName=3.6.0-rc1`, `applicationId=com.duoc.rotompedia`.
2. `python -m unittest discover -s tools/tests -v`, generadores `--verify-only`, Gradle `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease`.
3. Certificado firmante SHA-256 **5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa**: tiene que coincidir con RC14. No crear/reemplazar la clave.
4. Generar APK y `.sha256`; subirlos en pre-release **draft**; CI de tag debe comprobar y publicar. No marcar estable o Latest.
5. Prueba manual: primera visita → selector con Kanto primero; elegir Hoenn → abrir Ruta 102 → encuentros por método → ficha Pokémon; salir, volver a Mapas → Hoenn; Atrás → selector. Comprobar Room/usuario/equipos/favoritos tras actualizar RC14.

**Nota vigente:** el script histórico bloquea republicar RC1 y reemplazar su APK local. Para nuevas entregas, preparar otra versión y comprobar build, firma original, Actions y digest; nunca crear otra clave ni borrar datos para forzar actualización. Las limitaciones y controles anteriores describen el hito RC1; §5.9 distingue la continuación local actual y las pruebas pendientes.
