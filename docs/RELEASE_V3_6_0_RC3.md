# RotomPedia v3.6.0-rc3 — revisión del diseño y atlas provisional

**Pre-release, no estable.** Versión `3.6.0-rc3` / `30603`, paquete
`com.duoc.rotompedia`, firma original compatible con RC14 y RC1.
Instalar encima **sin desinstalar ni borrar datos**. No retroceder de versión
sobre bases de datos más recientes.

## Cambios para revisar desde RC1

- Home y Más con módulos reales; barra roja con carita original y navegación
  fija Inicio/Pokédex/Mapas/Equipos/Más. Ilustración grande únicamente en Home.
- Hoenn sustituye la cuadrícula visible por recreación de Ztash declarada CC0.
  Ajuste proporcional, zoom/pan acotados, restauración, lista buscable y elección
  explícita de sectores solapados. Selección **aproximada**, no cartografía final;
  etiquetas inglesas y calibración fina pendientes. Licencia/procedencia incluidas.
- Catálogo Esmeralda intacto: 1644 filas fuente, 1642 de cliente, 114 áreas con
  registros, exclusión de distribuciones de Colosseum. Encuentros con sprites,
  niveles, métodos, peso de slot, condiciones y acceso al Pokémon exacto.
- Edición individual en la ficha; solo Esmeralda verificada para Hoenn.
  Métodos calculados desde los registros disponibles, sin botones vacíos de
  Surf/Pesca y sin trasladar datos a ediciones pendientes.
- Mapas abre selector en primera visita (Kanto primero), recuerda la última
  región y permite volver al selector. Visores históricos con fit/clipping y
  pan acotado. Kanto **todavía no está reconstruido** ni tiene encuentros
  certificados por edición. Otras regiones incompletas; Hisui no disponible.
- Sin cambios Room, migraciones, cuentas, favoritos o equipos.

## Estabilización de las pruebas

RC2 quedó **en borrador**, sin publicación, por fallos de idling en Actions.
Se conservan su tag y APK, no se reemplazan. RC3 amplía el cierre correcto de
fixtures a bolsillos/Pokédex e imágenes: bases y loaders se cierran después de
desmontar Compose. Aserciones conservadas, con regresiones del orden de cierre
tanto en éxito como ante fallo. Sin ampliar timeout ni omitir tests en verde.
Esta corrección de fixtures no implica descartar toda flakiness histórica.

## Recorrido de revisión en teléfono

1. Actualizar sobre RC14/RC1 conservando cuentas, equipos y favoritos.
2. Home/Más y todas las pestañas, claro/oscuro, texto grande y horizontal.
3. Mapas → Hoenn → otro módulo → volver a Mapas → Hoenn → Atrás al selector.
4. Buscar Ruta 102/Ralts → encuentros Esmeralda → ficha real en Pokédex.
5. Zoom/pan/restablecer y selección explícita Ruta 112/Senda Ígnea/Paso Desnivel.
6. Edición pendiente sin datos ficticios; volver a Esmeralda restaura sus filas.

No hay validación física/capturas Android desde este entorno, sin dispositivo
conectado. Objetos/NPC localizados, nuevas ediciones, Kanto y cartografía final
siguen pendientes, **no se presentan como inexistentes**. Offline completo para
la versión final. Datos históricos de otras regiones separados como no verificados.

APK y `.sha256` adjuntos tras aprobar controles. Certificado requerido:
`5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`.
Los resultados finales de build, firma, CI y checksum se registran en §5.12 del
único `docs/ROTOMPEDIA_PLAN_MAESTRO.md`.

**Verificación local RC3:** 216 tests Kotlin, 44 Python, cuatro índices, lint y
build debug/instrumentación/release correctos con JDK17 (`BUILD SUCCESSFUL in
8m12s`). APK release no debuggable, 16.762.503 bytes, firma/alineación/metadatos
verificados. SHA-256:
`849620ff54b5f111767efd0056190afc42152cddf55a99e7b0e6f65330fad8b2`.
Actions del tag repite controles y valida assets antes de publicar.
