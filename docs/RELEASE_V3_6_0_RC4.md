# RotomPedia v3.6.0-rc4 — navegación, filtros y equipos

**Pre-release para revisión, no estable.** `3.6.0-rc4` / `30604`.
Actualizar encima de RC3 **sin desinstalar ni borrar datos**. Se mantiene el
paquete `com.duoc.rotompedia` y se exige la firma original de RC14/RC1/RC3.

## Cambios

- **Inicio funciona al regresar desde Pokédex**, tanto entrando por tarjeta
  como por navbar. Los destinos principales comparten navegación; las fichas
  conservan la pertenencia al módulo desde el que se abrieron.
- Se conservan búsqueda, filtros, vista y posición de Pokédex al cambiar de
  pestaña, y la última región de Mapas. La sesión recordada espera al grafo
  de navegación y no fuerza Home sobre una pantalla restaurada al recrear la actividad.
- **Filtros por dos niveles:** resumen compacto y opciones de un solo grupo.
  Confirmar una selección vuelve al resumen; Aplicar confirma el conjunto una
  vez. Cancelar/Atrás descartan el borrador; Restablecer no altera lo aplicado
  hasta confirmar. Tipo sigue siendo selección única.
- Generación significa **generación de la especie**, no de su forma. El índice
  tiene 20 formas de Galar, ninguna de especie Gen8; esa intersección vacía
  no se corrigió inventando generaciones o cambiando identidades.
- **Equipos:** selector en el título, renombrado no vacío y papelera de alto
  contraste junto a Exportar Showdown. La confirmación muestra el nombre;
  el borrado usa ID y propietario, no el texto del nombre.
- Tras eliminar el seleccionado, permanece en **Equipos → selector**, nunca
  vuelve a Inicio ni abre otro automáticamente. Si era el último, muestra vacío.
- **Movimientos de Equipos:** el selector reutiliza la pantalla aprobada de
  Pokémon, con edición arriba y Nivel/Huevo/MT-MO/Tutor, no el antiguo listado
  centrado en buscador. Elegir asigna al slot correspondiente; Atrás conserva
  el editor y Dejar vacío sigue disponible. Se mantienen variedad, otros
  métodos, valores y cuatro movimientos guardados; no hay otro catálogo ni
  peticiones por pestaña. El P3 de la Pokédex no se sustituye por otro diseño.

## Conservación y límites

Sin cambios de Room/esquemas/migraciones, cuentas, datos de encuentros,
cartografía ni nuevas herramientas. Hoenn sigue provisional, Esmeralda es la
edición verificada del piloto, y otras regiones conservan sus límites previos.
Offline completo sigue fuera de alcance.

**RC5 no implementada.** La captura del usuario con fondo oscuro y tarjetas/
navbar blancas queda registrada como un problema de coherencia del tema para
RC5. RC4 no anuncia un modo oscuro rediseñado ni nuevas paletas por forma.

## Qué revisar

1. Home → tarjeta Pokédex → Inicio; repetir por navbar y desde Más.
2. Filtrar por grupos: cambiar sin aplicar, Atrás/Cancelar, reabrir,
   Restablecer y Aplicar; lista/cuadrícula, scroll y retorno desde una ficha.
3. Mapas → Hoenn → Ruta 102/Ralts → ficha → Atrás; cambiar de pestaña y volver.
4. Equipos: cambiar desde título, renombrar, cancelar un borrado y confirmarlo;
   comprobar selector, equipo restante y caso del último equipo.
5. Editar un miembro → Movimientos → elegir slot y edición → pestañas → elegir
   movimiento → Guardar. Cerrar/reabrir y comprobar exportación/estadísticas.

## Verificación de esta candidata

- **222 pruebas Kotlin**, cero fallos/errores/omitidas; **44 Python**, cuatro
  índices, lint y builds debug/instrumentación/release correctos localmente
  (JDK21 real). Verify de rama `46102307509` correcto también en JDK17.
- **7 instrumentadas correctas** en el emulador API37, incluidos NavHost real,
  renombrado/recreación, borrado al selector y elección/persistencia de movimiento.
  Otras **2 pruebas** correctas en oscuro/fuente 150%; comprobaciones adicionales
  de filtros en horizontal real y teclado, con capturas en `docs/evidence/rc4`.
  Esto no sustituye la revisión de tu teléfono ni certifica todos los tamaños.
- Actualización **release RC3 → release RC4 ejecutada sin desinstalar**:
  cuenta, equipo, Bulbasaur/508 EV/Solar Beam y favorito conservados.
- Incompatibilidad inicial de Espresso con API37 resuelta actualizando solo
  dependencias de instrumentación. Fallos anteriores conservados en el Plan.
- APK **release no debuggable**, **16.778.887 bytes**, firma/alineación/versión
  verificadas. Certificado SHA-256 original:
  `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`.
- SHA-256 de `RotomPedia-v3.6.0-rc4.apk`:
  `78be60f95484ec4eb20e0300273acf80c7ce2137c1942bd5cd80f65e8a4b144c`.

Actions del tag valida de nuevo antes de publicar; APK y checksum adjuntos.
Seguimiento acumulativo en informe técnico 002; el único Plan Maestro sigue
siendo `docs/ROTOMPEDIA_PLAN_MAESTRO.md` (§5.13). **Tras entregar RC4 se espera
tu revisión antes de implementar RC5.**
