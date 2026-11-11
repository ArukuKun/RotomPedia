# RotomPedia v3.6.0-rc2 — pre-release para revisión en Android

Nueva entrega de prueba, **no estable**, `3.6.0-rc2` / `30602`.
Conserva `com.duoc.rotompedia` y el certificado original compatible con RC14/RC1.
Actualizar **sin desinstalar ni borrar datos**. No instalar versiones antiguas
sobre una base de datos más reciente para intentar volver atrás.

## Qué revisar

- Diseño global aprobado: Home con accesos reales, Más con herramientas,
  barra superior roja con la carita original de Rotom y navegación inferior
  fija Inicio / Pokédex / Mapas / Equipos / Más. Ilustración grande solo en Home.
- Hoenn muestra una recreación de Ztash declarada CC0, con zoom/pan acotados,
  restauración de vista y selección aproximada por sectores; ya no pinta toda
  la cuadrícula de rectángulos sobre el mapa. No es la cartografía final;
  conserva etiquetas inglesas y necesita revisión espacial en teléfono.
- Hoenn/Esmeralda conserva datos auditados, búsqueda por zona/Pokémon/número,
  filtros, sprites, niveles, condiciones y enlace a la ficha exacta.
  Datos: 1644 filas fuente, 1642 admitidas en el cliente, 114 áreas con registros;
  los dos registros de distribución de Colosseum siguen excluidos.
- Ficha Pokémon / Objetos-NPC con selector de edición individual. Solo
  Esmeralda tiene registros verificados en el paquete de Hoenn; otras ediciones
  indican pendientes, sin heredar encuentros ni crear botones de métodos vacíos.
- Primera visita a Mapas muestra el selector, Kanto primero. Al regresar se
  recupera la última región y siempre se puede volver al selector.
- Kanto y otras regiones conservan referencia histórica y visor mejorado,
  **no cartografía reconstruida ni cobertura certificada por edición**.
  Los datos históricos permanecen separados y explícitamente sin verificar.
  Hisui continúa no disponible hasta contar con cartografía.

## Pruebas y correcciones

- Corregidos imports, recursos duplicados con extensión incorrecta,
  alineación de cabecera, clipping y desplazamiento del explorador.
- Corregido cierre de Room antes del desmontaje de Compose en `ItemUiTest`;
  retirado avance manual del looper, con regresión de teardown. Timeout no
  aumentado; diagnóstico completo y XML, sin excluir tests en ejecuciones verdes.
- RC2 recompilada con JDK17: **214 tests Kotlin, 44 Python**, cuatro índices,
  lint sin errores, debug/instrumentación y **assembleRelease correctos**
  (`BUILD SUCCESSFUL in 5m18s`). La base anterior también tiene dos Verify
  consecutivos verdes (`43084556620`, `43086461990`). Actions del tag repite
  pruebas/build y valida los assets antes de publicarlos.
- Sin cambios Room/esquemas/migraciones, cuentas, favoritos o equipos.

## Limitaciones y recorrido manual pendiente

No hubo teléfono/emulador conectado: pruebas automatizadas **no son validación
visual ni una actualización física ejecutada**. Revisar:

1. Actualización sobre RC14 o RC1 sin desinstalar: cuentas, favoritos y equipos.
2. Home/Más y los cinco destinos; claro/oscuro, texto grande, teclado y horizontal.
3. Mapas → selector (si no hay preferencia) → Hoenn → salir a otro módulo →
   volver a Mapas → recuperar Hoenn → Atrás → selector.
4. Buscar Ruta 102/Ralts, métodos de Esmeralda, sprite y apertura de Pokédex.
5. Pellizcar/arrastrar/restablecer; Ruta 112/Senda Ígnea/Paso Desnivel:
   elección explícita de la zona, sin asumir precisión cartográfica final.
6. Cambiar a una edición pendiente: sin encuentros/métodos ficticios;
   volver a Esmeralda recupera los datos verificados.

Objetos/NPC por ubicación, nuevas ediciones, cartografía final de Kanto,
calibración fina/etiquetas españolas de Hoenn y expansión regional pendientes.
El modo offline completo queda para la versión final.

Licencia/procedencia del atlas: `tools/licenses/Hoenn-atlas-CC0.md` y aviso
empaquetado. No recurso del videojuego ni activo de PokeInfo añadido.
Documento de continuidad: §5.9–5.11 del único `ROTOMPEDIA_PLAN_MAESTRO.md`.

Descargar el APK y su `.sha256` adjuntos; comprobar el SHA-256 antes de instalar.
La firma debe coincidir con RC14: `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`.

## APK verificado localmente

- `RotomPedia-v3.6.0-rc2.apk`, **16.762.503 bytes**, variante release no debuggable.
- SHA-256: `b7d6250c8ee86c1d7de2a3fdfc3c903254f4a5510799957d929d197131fbe5be`.
- Firma original verificada con `apksigner`, alineación correcta y paquete/
  versión comprobados con `aapt`. No keystore nuevo ni cambios de datos.
