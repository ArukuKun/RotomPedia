# RotomPedia — Plan maestro de desarrollo

**Marca / proyecto:** RotomPedia\
**Visión:** Pokédex y enciclopedia Pokémon en español, inspirada en la experiencia de PokeInfo de Bruno Sousa, con identidad propia, herramientas conectadas, mapas interactivos y un Rotom contextual.\
**Versión publicada actual:** **v3.6.0-rc1**, pre-release del 10 nov 2026 (tag en `a34b3d1`); RC14 sigue como referencia de compatibilidad, no como estable. Continuación local del diseño aprobado y atlas en §5.9.\
**Propósito:** documento de continuidad para OpenCode; conservar el contexto de lo realizado y planificar lo pendiente **sin volver a empezar desde cero**.\
**Regla de alcance:** implementar solo fases autorizadas por el usuario. La política permanente de §8.1 autoriza commits, push, tags nuevos y pre-releases de esas fases sin pedir permiso adicional; no autoriza fases nuevas, versiones estables, cambios de firma ni operaciones destructivas. No modificar tags/releases anteriores.

**Documento principal del repositorio desde el 3 de noviembre de 2026.** Se adopta el adjunto `ROTOMPEDIA_PLAN_MAESTRO_v3.4_en_adelante.md`, conservando sus fases y principios. Los apartados 11–14 incorporan el contraste con el código y la propuesta para aprobación. Los documentos de v3.3 son históricos, no el plan vigente.

**Estado vigente:** v3.4 terminada; v3.5-A/B/C entregadas en RC, **sin v3.5 estable publicada**. RC14 conserva su evidencia histórica en §4.16. v3.6.0-rc1 publicada (§5.8), con piloto de datos Esmeralda; diseño aprobado y atlas provisional en continuación local (§5.9). No dar por terminadas cartografía, regiones ni pruebas físicas. Offline completo para el final. **Estables con aprobación propia** (§8.1). Esta cabecera corrige el estado obsoleto recuperado al incorporar la carpeta del 11 nov; no anula el historial de hitos anteriores.

**Prioridad vigente tras probar RC11:** el usuario rechaza el ritmo de entregas de dos pantallas y la distancia entre la maqueta aprobada y el inicio real. **Solicita completar el rediseño coherente de toda la aplicación y P3 antes de otra revisión pública** (§4.12). V1/RC11 se conserva como entrega histórica, no aceptación visual. Mapas interactivos se reconstruirán en otro momento con ubicaciones verificables; no se amplían ni se inventan pines en esta tarea. «ZIP» significa el proyecto de esta carpeta y su historial.

**Continuidad posterior a RC1:** este archivo es y seguirá siendo el **único Plan Maestro**. Las cuatro observaciones manuales de RC2 están implementadas y publicadas como `v3.5.0-rc3` / `30503` (§4.2). No se autoriza implementar conjuntamente el resto de §13 ni cambiar mapas/RI. Conservar firma compatible y versiones anteriores; `0.x` solo en evaluación.

**Ampliación de planificación:** Rotom Intelligence Pipeline se registra en §16 como investigación/prototipo futuro después de v3.5, con integración progresiva en v3.7 y sin bloquear mapas de v3.6. No es autorización para implementarlo, contratar servicios ni enviar datos a terceros. El [README principal](../README.md) resume estados de todas las series (3.x, 4.x y posteriores) y debe actualizarse junto con este plan al cerrar cada hito/versión.

> **Cómo leer los estados:** `[x]` significa **reportado como completado por el usuario**, no necesariamente auditado de forma independiente en esta conversación. `[ ]` indica **pendiente o por validar**. OpenCode debe contrastar los estados con el repositorio actual, las pruebas y los tags, y actualizar el registro de evidencias sin desmarcar lo realizado solo por falta de contexto.

---

## 1. Visión del producto y principios

RotomPedia no es una Pokédex aislada más un conjunto de herramientas independientes. Es **una enciclopedia conectada**: la Pokédex nacional organiza la información; Team Builder, calculadoras, movimientos, objetos, localizaciones, mapas y Rotom consumen las mismas identidades y relaciones de Pokémon.

**Principios de diseño y desarrollo**

1. **Pokédex-first:** el orden nacional y las relaciones correctas de especie/variedad/forma/evolución son la base de todo.
2. **Experiencia PokeInfo, identidad RotomPedia:** recuperar la navegación fácil, las fichas completas y los mapas útiles de la aplicación de referencia; no copiar literalmente su diseño ni recursos privados.
3. **Español primero:** nombres, etiquetas, filtros, explicaciones y mensajes claros; preservar nombre técnico/API solo internamente o como información secundaria.
4. **Fuente compartida:** no crear catálogos paralelos por pantalla; consumir repositorios comunes con estado de interfaz independiente.
5. **Datos verificables:** PokeAPI y otras fuentes autorizadas para hechos; metadata local curada para traducciones, alias, categorías y textos editoriales, no para duplicar arbitrariamente IDs/estadísticas.
6. **Preservación:** nunca perder o reescribir silenciosamente cuentas, equipos, EV/IV, movimientos, favoritos ni preferencias.
7. **Rendimiento y usabilidad:** listas lazy, carga progresiva, caché, búsqueda local y estados correctos de carga/error/offline; no descargar cientos de fichas por cada pulsación.
8. **Cambios acotados:** iterar por fases, ejecutar pruebas y validar en móvil antes de publicar.
9. **Honestidad de cobertura:** si PokeAPI o un recurso no contiene un dato, informarlo sin inventarlo; distinguir juego/edición y procedencia.
10. **Seguridad de sesión:** repositorios personales filtran por usuario activo y validan propiedad en lectura y escritura.

### Identidad Pokémon — contrato transversal

- `speciesId`: identifica la especie de PokeAPI.
- `nationalNumber`: número visible en la Pokédex nacional; se obtiene de la relación oficial de Pokédex, no del ID de una variedad.
- `pokemonId`: ID técnico de la variedad, necesario para estadísticas, tipos, sprites, etc.
- `pokemonFormId`: identidad del recurso `pokemon-form` cuando existe.
- `slug`: identificador técnico para consultar APIs.
- `displayNameEs`: nombre mostrado en español, con alias de búsqueda.
- Especies evolucionadas (Vaporeon, Jolteon, Flareon) tienen números nacionales propios.
- Variedades de una misma especie (Lycanroc Mediodía, Medianoche, Crepúsculo) comparten número nacional; no se convierten en especies distintas ni se etiquetan indiscriminadamente como transformaciones especiales.
- Regionales (Ninetales de Alola), Megas (Mega Charizard X/Y) y formas de combate (Aegislash Filo) se clasifican de forma diferenciada y admiten etiquetas secundarias.
- IDs técnicos superiores a 10000 **no** se interpretan como número nacional ni como clasificación universal.

**Casos de referencia de numeración:** Ninetales de Alola `#038`, Mega Charizard X `#006`, Aegislash Filo `#681`, Lycanroc Medianoche `#745`, Mega Zygarde `#718`.

---

## 2. Historial y checklist de versiones

### v3.3.0-rc1 — Refactor inicial de formas y estabilidad

- [x] Estabilizar el selector respecto a claves/IDs inconsistentes y búsquedas de formas.
- [x] Mantener búsqueda parcial con nombres en español y alias.
- [x] Sustituir la dependencia anterior de `PokemonFormsData.kt` por catálogo de metadata más índice API en el proyecto de esa versión.
- [x] Mover la sección de formas al detalle inferior, después de la línea evolutiva.
- [x] Incorporar o mejorar movimientos y encuentros bajo demanda.
- [x] Publicar la pre-release v3.3.0-rc1 (referencia histórica).
- [x] Identificar deuda posterior: orden nacional, IDs visibles, clasificación e imágenes.

### v3.4-A — Persistencia, sesión e índice compartido

**Estado: COMPLETADA, según el usuario.**

- [x] Avanzar la migración de Room de esquema v6 a v7 de forma aditiva.
- [x] Establecer protecciones frente a migraciones destructivas.
- [x] Introducir repositorios compartidos y dependencias de aplicación (`AppContainer` / `PokemonRepository`, según implementación real).
- [x] Implementar o integrar índice nacional persistente de especies y variedades.
- [x] Preparar generación y validación de snapshot desde relaciones de PokeAPI.
- [x] Corregir acceso a favoritos/equipos entre usuarios y unificar contexto de sesión.
- [x] Mantener datos de equipos históricos: el usuario observó que volvieron a aparecer equipos creados en v2.5.
- [x] **Auditoría de código y evidencias automatizadas:** migraciones registradas, pruebas v6→v7→v8, aislamiento y esquemas exportados; véase apartado 11. No implica validación exhaustiva de todos los dispositivos.

### v3.4-B — Pokédex nacional, picker e identidades

**Estado: COMPLETADA, según el usuario.**

- [x] Integrar la Pokédex y el selector con el índice/repositorio compartido.
- [x] Ordenar especies por número nacional en lugar de mostrar Megas en bloque al inicio.
- [x] Relacionar formas y variedades con sus especies nacionales.
- [x] Separar número nacional visible de IDs internos de PokeAPI.
- [x] Mejorar clasificación entre evolución, variedades estables, regionales, Megas y combate.
- [x] Mantener búsqueda parcial por nombre, número, alias y formas.
- [x] Habilitar filtros que combinen región, tipo y categoría sin exclusiones arbitrarias.
- [x] **Auditoría de código y pruebas:** casos obligatorios, normalización, orden nacional y conservación de equipos respaldados por tests; véase apartado 11. El informe manual del usuario no detalla todos esos escenarios individualmente.

### v3.4-C — Recursos gráficos y pulido de formas

**Estado: COMPLETADA, según el usuario.**

- [x] Resolver imágenes de la variedad seleccionada mediante `pokemonId`, con metadata API validada y candidatos de sprite/artwork/HOME. `speciesId`, número nacional y `pokemonFormId` no se usan como sustitutos del ID de imagen. No implica selección exhaustiva de formas cosméticas.
- [x] Incorporar alternativas/fallbacks para sprites o artworks ausentes.
- [x] Trabajar el caso de Mega Zygarde, manteniendo la imagen correspondiente a su propia forma.
- [x] Mejorar imágenes del detalle y del picker sin cargar recursos pesados innecesariamente.
- [x] Evitar sustituir una forma por la imagen incorrecta de otra.
- [x] **Auditoría de código, HTTP y pruebas automatizadas:** identidad de los casos de referencia, Mega Zygarde, normal/shiny, recuperación y placeholder; registros en apartado 11.
- [ ] **Validación manual específica de imágenes:** completar en Android los escenarios de error/offline, normal/shiny y placeholders. Las pruebas del usuario no incluyen una matriz completa de estos casos; no se reabre C por ello.

### v3.4 integrada — Base completada

**Estado informado por el usuario: v3.4 terminada.**

- [x] Hitos A, B y C realizados.
- [x] El proyecto ya tiene una base de arquitectura, Pokédex nacional y recursos gráficos mejorada respecto a v3.3.
- [x] Registrar tag/commit/release exactos, versión, pruebas y límites de validación manual; véase apartado 11. RC1 está publicada como prerelease.
- [x] Registrar bugs residuales como incidencias específicas, sin reabrir A/B/C; véase apartado 13.

---

## 3. Arquitectura objetivo que debe mantenerse

```text
PokeAPI + metadata española/alias curados + recursos gráficos autorizados
                             |
                     Repositorios comunes
                             |
           +-----------------+-----------------+
           |                 |                 |
        Room/índice        Cachés        Sesión/propiedad
           |                 |                 |
    +------+---------+-------+--------+--------+-------+
    |                |                |                |
  Pokédex        Team Builder     Calculadoras      Favoritos
    |                |                |                |
 Detalles        Movimientos        Captura       Otros módulos
    |
 Objetos / Habilidades / Localizaciones / Mapas / Rotom contextual
```

Cada pantalla mantiene su navegación, filtros y estado visual; el repositorio administra identidades y datos compartidos. No duplicar DTOs de PokeAPI como modelos de negocio visibles ni usar `PokedexScreen.kt` como backend de otras pantallas.

Para actualizaciones del índice:

- Generador de mantenimiento independiente (por ejemplo `tools/generate_pokemon_index.py`) que consulta relaciones reales de especies y variedades.
- Snapshot inicial versionado, validado y empaquetado; no hace falta red para abrir el índice ya incluido.
- Comprobaciones de IDs/relaciones duplicadas, faltantes, integridad, hash, cobertura y formato.
- Detalles pesados se consultan bajo demanda y se cachean.
- Un nuevo snapshot se revisa antes de integrarlo; no hacer merge/release automático por el mero hecho de cambiar PokeAPI.

---

## 4. v3.5 — Enciclopedia enriquecida (A/B/C EN PRE-RELEASE; ESTABILIZACIÓN ABIERTA)

**Objetivo:** que objetos, movimientos y habilidades sean fichas útiles e interconectadas, no catálogos de emojis o nombres incompletos.

### v3.5-A — Objetos / Items

**Implementada y publicada dentro de v3.5.0-rc1; validación del usuario en Android pendiente.** Evidencias iniciales en §15 y publicación integrada en §17. No se crea otra entrega retrospectiva de A.

- [x] Auditar `CompetitiveItemData` y separar información factual de consejos editoriales (§15).
- [x] `ItemRepository` compartido: detalle REST `item`; identidades/categorías/atributos desde CSV oficiales fijados a revisión, sin consultas REST por cada filtro.
- [x] Sprite real mediante `item.sprites.default` cuando existe; candidatos de objetos y placeholder, sin imágenes Pokémon. Admite iconos compartidos declarados por API (p. ej. MT).
- [x] Nombre español cuando existe, categoría, descripción/efecto con idioma/procedencia y precio por edición cuando está documentado; ausencias explícitas.
- [x] Comprobar bayas e identidad; `shuca-berry` corregida a Baya Acardo / Tierra, con efecto revisado y origen identificado.
- [x] Usos separados por categoría/atributos documentados y portadores con contexto de juego; sin inventar disponibilidad/objetos de mapa.
- [x] Búsqueda y filtros locales; enlaces de evolución hacia la ficha y de portadores hacia Pokémon. Equipo sin campo de objeto y movimientos futuros no reciben enlaces ficticios ni nueva persistencia.
- [x] Caché persistente y detalles bajo demanda; sin descarga masiva en búsquedas.
- [x] Pruebas de traducción, identidad, categorías, sprite y conservación de datos (§15).
- [ ] Validación y aprobación del usuario en Android real antes de cerrar la puerta manual de A.

### v3.5-B — Movimientos completos

**Implementada y verificada técnicamente; validación manual pendiente (§17).** Aprendizaje por edición; valores base actuales, no históricos. Sin certificación de legalidad; tipos/categorías en texto, sin sprites individuales inventados. La lista original siguiente conserva los criterios de alcance; §17 registra su cumplimiento y límites.

**Pulido P3:** pantalla dedicada de aprendizaje y presentación compacta en §13.1, sobre `MoveRepository` ya implementado. Tras RC10 se solicita su diseño dentro de §4.10, aún sin implementación; no reabre los datos de v3.5-B ni autoriza MOV-003/Z automáticamente.

- [x] Crear/adaptar `MoveRepository` reutilizable en Pokédex, editor de miembros y enciclopedia.
- [x] Nombre español, tipo, categoría física/especial/estado, PP, potencia, precisión, prioridad y efecto con idioma identificado.
- [x] Mostrar nivel, método y edición/grupo de versiones de aprendizaje; no mezclar juegos de forma engañosa.
- [x] Preservar varias vías de aprendizaje de un mismo movimiento (nivel, MT/MO, huevo, tutor).
- [x] Paginación de 40 sin topes fijos que oculten datos.
- [x] No inventar sprites individuales de movimientos; tipos/categorías identificados en texto. Iconografía adicional queda como pulido visual, no dato factual.
- [x] Conservar movesets antiguos guardados como texto y resolver alias sin borrado silencioso.
- [x] No ofrecer certificación de legalidad competitiva ni atribuirla a mezclas de generaciones.

### v3.5-C — Habilidades y relaciones enciclopédicas

**Implementada y verificada técnicamente; validación manual pendiente (§17).** Relaciones actuales por variedad/slot, textos por edición. Se conserva la política de descripción de especie/variedad de v3.4. Sin enlaces de sinergia inventados entre habilidad/movimiento/objeto. Evidencias en §17.

- [x] Crear/adaptar `AbilityRepository` con nombres/efectos y fuentes por idioma.
- [x] Mostrar habilidades normales y ocultas, y Pokémon asociados por variedad/slot.
- [x] Navegación Pokémon ↔ habilidad y objeto MT/MO ↔ movimiento por relaciones verificadas; sin inventar sinergias.
- [x] Conservar descripción de especie/variedad correctamente etiquetada, sin atribuir texto específico no verificado.
- [x] Selector de edición en aprendizaje/textos/MT; advertencias de valores y relaciones actuales, no historial reconstruido.

**Criterio de salida v3.5:** pantallas en español con recursos correctos, datos de procedencia clara, búsqueda/reutilización compartida, tests de regresión y ninguna pérdida de equipos.

### 4.1 Estabilización posterior a v3.5.0-rc1 — Pokédex (alcance inmediato autorizado)

No se reabren v3.4 ni v3.5-A/B/C como proyectos nuevos. Se pule la Pokédex sobre sus repositorios, identidades y resolución de imágenes, sin crear otra línea funcional ni retrasar v3.6/v3.7 hasta implementar todo el backlog. RC2 es una entrega de prueba de esta misma estabilización.

**Rama:** `feature/v3.5-pokedex-polish`, desde `03466d1` (documentación de publicación RC1). La implementación no cambió Room ni firma. Preparación posterior autorizada: `3.5.0-rc2` / `30502`, sin modificar el tag ni APK de RC1.

**Commits de implementación:** `7515df6` (datos, filtros, nacional limpio, Lista/Cuadrícula y pruebas), `c52319d` (artwork original mediante resolutor compartido), `17ba815` (cierre documental). El cierre inicial fue local; después se autorizó su publicación como pre-release mediante §8.1.

**Referencia visual propia:** historial `2590bf5`, `PokedexScreen.kt::PokemonListItemCard`: sprite 72 dp, número 18 sp, nombre 20 sp, padding horizontal 16/vertical 8 dp, elevación 4 dp. El explorador de `248c922` pasó a sprite 44 dp dentro de 48 dp. Se recuperan las proporciones de RotomPedia con wrapping, tipos y controles accesibles; no se copian tamaños de PokeInfo ni se restaura el antiguo cargador de imágenes por URL arbitraria.

**Contrato de presentación:**

- Pokédex nacional: una variedad predeterminada por especie, número nacional oficial y nombre de especie, sin hijos ni desplegables. Buscar `#038` en este modo muestra Ninetales, no despliega Alola. Tipos/grupos huevo/favoritos no expanden variantes silenciosamente.
- Categorías explícitas u origen regional permiten explorar formas; resultados planos, ordenados por número nacional, con nombre de variedad e ID técnico intacto. No mostrar una especie base que no cumple los filtros como resultado seleccionable.
- Selector de equipos mantiene su explorador agrupado y acceso a variedades; detalle/formas existente permanece. No eliminar metadata ni identidades, cosméticos no seleccionables siguen sin inventarse como variedades.
- Lista predeterminada, filas amplias y sprite 72 dp. Cuadrícula `LazyVerticalGrid` de **tres columnas**, tantas filas como resultados, claves `pokemonId`. Misma consulta, filtros, favoritos y callbacks de navegación al cambiar de vista; scroll independiente por modo.
- Filtros intersectados: tipo, generación/origen regional existentes, categorías y grupo huevo. Gigamax por marcador canónico `-gmax` verificado tanto en variedad como en `pokemon-form`, separado de la categoría Combate; Mega por `isMega`. No derivar categorías del número de ID.
- Grupos huevo: ampliar el **mismo** `pokemon-index.json` con `egg_groups.csv`, `egg_group_prose.csv` y `pokemon_egg_groups.csv` de la revisión fijada `e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff`. Son metadatos de especie (no legalidad reproductiva histórica por juego), disponibles offline; no listas factuales manuales ni red por pulsación.

**Aceptación y evidencias:** marcar solo tras comprobar cada punto; validación visual en teléfono sigue siendo una puerta independiente.

- [x] Vista normal con 1025 especies, una por número nacional y orden #001…#1025; primeros seis Bulbasaur→Charizard.
- [x] Sin Megas/variedades desplegadas ni desplegables en la Pokédex nacional; picker y detalle mantienen formas.
- [x] Sprite 72 dp y proporciones de filas recuperadas del historial propio; prueba UI de 72×72 dp y tarjeta de al menos 96 dp.
- [x] Grupo huevo funcional, combinable y con procedencia; 15 grupos/1304 relaciones que cubren las 1025 especies.
- [x] Categorías Mega/Gigamax y regionales funcionales; 34 variedades Gigamax en el snapshot, número nacional separado de ID técnico.
- [x] Lista predeterminada y cuadrícula con tres columnas, navegación más allá del noveno resultado hasta el último.
- [x] Consulta/filtros/favoritos/navegación intactos al cambiar de vista; favoritos por variedad, no migrados a especie.
- [x] Pruebas de datos, UI, picker y regresión personal; compilación/lint y snapshot verificados. Sin migración ni borrado de cuentas/equipos/EV/IV/movimientos.
- [ ] Validación del usuario en Android: proporciones, sprites reales, pantalla estrecha/texto grande, navegación, favoritos y estado al volver.

**Estado de ejecución:** implementación verificada y publicada como **pre-release RC2** para validación manual. RC1 y sus assets no se modificaron. Backlog restante en §13; no iniciar otra mejora al publicar esta prueba.

#### Evidencia de implementación y regresión

- `PokemonExplorer.filter(..., nationalOnly=true)` aplica nacional limpio solo en Pokédex; el contrato predeterminado del picker conserva variedades. Se verifica `#038`→`38` en nacional y acceso a `10104` en picker/categoría regional. Mega y Gigamax se filtran sobre identidades existentes; `10196` muestra `#006` y navega a Charizard Gigamax, no a Charizard base.
- Grupos huevo añadidos al snapshot original con `--enrich-eggs-only`. Comparación estructurada con `03466d1`: **sin cambios** en especies, variedades, formas, tipos, nombres de formas ni revisión upstream; solo se añaden `eggGroups`, `speciesEggGroups` y `eggGroupsComplete`. Permanecen 1025 especies, 1351 variedades y 1579 formas.
- `PokemonIndexImporter` valida hash y relaciones antes de importar; publica metadatos de grupo huevo del mismo snapshot al repositorio, también si Room ya tenía el índice. Se rehidratan desde el asset en reapertura offline, sin otra tabla/catálogo ni migración. Se prueba rechazo de relación huérfana aunque el marcador de cobertura esté desactivado, sin reemplazar estado válido.
- `PokedexResults.kt`: Lista/LazyColumn y Cuadrícula/LazyVerticalGrid con `GridCells.Fixed(3)` y claves por `pokemonId`. Mantiene el resolutor de imágenes de v3.4 y carga lazy de 72 dp, con estilo `ARTWORK` como el componente original y fallbacks de identidad correctos; no restaura URLs arbitrarias. Para nombres de variedad duplicados de la fuente, muestra el slug secundario en vez de inventar traducciones.
- Panel de filtros desplazable en bottom sheet para no restar todo el alto de resultados en pantallas pequeñas. Etiquetas distinguen generación de especie, origen regional y grupo huevo. Los grupos huevo son relaciones actuales de especie, no reconstrucción por edición ni afirmación de legalidad reproductiva.
- **77 pruebas Kotlin correctas, 0 fallos/errores/omitidas; 21 pruebas Python correctas.** `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain` correcto. Lint sin errores: 110 advertencias y 30 sugerencias, sin incidencias nuevas en `PokedexResults.kt`.
- `PokemonExplorerTest.kt`: ocho casos de datos (tres previos + cinco nuevos de nacional/filtros/identidad/offline) y un caso adicional de integridad de grupos huevo; se mantienen los casos de referencia y verificación de cero interacciones API. `PokedexPolishUiTest.kt`: cinco casos nuevos de Lista, proporciones, cuadrícula, estado/favoritos/navegación, Gigamax y grupo huevo. Los dos casos previos de `PokemonExplorerUiTest.kt` siguen comprobando el picker agrupado.
- Evidencia de orden: `(1..1025)` exacto y los seis slugs iniciales; cuadrícula comprueba geometría de las tres primeras tarjetas y scroll a índices 24 y 1024. Filtros: Bulbasaur en Monstruo/Planta, Ditto en su grupo, Mewtwo en Desconocido; Alola × Hielo × Campo × favorito devuelve `10104`; combinación incompatible produce vacío. Número nacional sintético distinto de especie/variedad también probado.
- Incidencia de prueba resuelta: el texto `#038` aparecía tanto en búsqueda como en tarjeta; se acotó el selector al tag de la tarjeta, sin eliminar la aserción. No se ocultaron fallos ni se deshabilitaron tests. Otros límites encontrados: formas Gigamax comparten `isBattleOnly`; se diferencian usando su identidad canónica, no un flag inexistente.
- Evidencia del cierre técnico previo a la publicación: APK en `RotomPedia/app/build/outputs/apk/debug/app-debug.apk`; instrumentación compilada, no ejecutada en dispositivo (`adb devices` vacío). Entonces se conservaban `3.5.0-rc1` / `30501` y Room v10: diff de build, base, migraciones y esquemas contra `03466d1` vacío. `apksigner` confirmó certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, igual al de RC1. RC2 requiere recompilar con `30502` y verificar de nuevo; no reemplazar el asset anterior.

#### Archivos de esta intervención

Rutas Kotlin base: `RotomPedia/app/src/main/java/com/duoc/rotompedia/`.

- `data/explorer/PokemonExplorer.kt`; `data/index/PokemonIndexSnapshot.kt`; `data/index/PokemonIndexImporter.kt`; `data/repository/DefaultPokemonRepository.kt`.
- `viewmodel/PokemonExplorerController.kt`; `viewmodel/PokemonViewModel.kt`.
- `ui/screens/PokedexScreen.kt`; `ui/screens/PokemonExplorerContent.kt`; nuevo `ui/screens/PokedexResults.kt`.
- `RotomPedia/app/src/main/assets/pokemon-index.json`.
- `RotomPedia/app/src/test/java/com/duoc/rotompedia/PokemonExplorerTest.kt`; nuevo `RotomPedia/app/src/test/java/com/duoc/rotompedia/PokedexPolishUiTest.kt`.
- `tools/generate_pokemon_index.py`; `tools/tests/test_pokemon_index.py`; `README.md` y **este mismo Plan Maestro**.

#### Validación pendiente en el teléfono

- [ ] Confirmar que tamaño/espaciado recuperan la identidad visual deseada; texto grande y ancho estrecho sin controles inaccesibles.
- [ ] Nacional sin desplegables, Lista inicial; cambiar a Cuadrícula y recorrer más de nueve resultados. Confirmar imágenes, tipos y nombres largos, sin confundir número e ID.
- [ ] Mega/Gigamax/regionales/grupos huevo y combinaciones; búsqueda/vacíos/limpiar filtros, apertura/cierre del bottom sheet y teclado.
- [ ] Marcar/desmarcar favoritos y volver desde detalle; favoritos de variedades guardados siguen accesibles al elegir su categoría, no se borran por ocultarse en nacional.
- [ ] Selector de equipos conserva formas y selección exacta; revisar usuarios, equipos, EV/IV y movimientos antes/después de una futura actualización autorizada. Offline y placeholders gráficos.

No se inicia otra mejora al completar este bloque. La nueva política de §8.1 autoriza entregar RC2 por GitHub para esta validación; **RC1 no contiene este pulido** y se conserva sin cambios.

#### Publicación de Pokédex — v3.5.0-rc2

- [Pre-release pública](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc2), publicada por GitHub Actions el 4 de noviembre de 2026: `draft=false`, `prerelease=true`, sin marcar Latest ni estable. [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc2/RotomPedia-v3.5.0-rc2.apk).
- Tag anotado **nuevo**, no movido: `v3.5.0-rc2` → `c7010ac62db95e134d75bfea43404b2f315d3d71`. Incluye `7515df6`, `c52319d` y `17ba815`. Las correcciones posteriores de automatización no alteran el código Android ni el tag.
- APK: `RotomPedia-v3.5.0-rc2.apk`, **23899029 bytes**, SHA-256 **`a8dd7e0754bcc51a93bc7d5e914d93fe63e9f9c992663a9399c1cb4b20cc198d`**; asset `.apk.sha256` incluido. Digest GitHub coincidente y enlace público comprobado por HTTP HEAD **200**, MIME APK y longitud correctos.
- Versión `3.5.0-rc2`, código **30502 > 30501**, mismo paquete `com.duoc.rotompedia`, mínimo API 25 y Room v10. Sin nueva migración ni cambio de datos/configuraciones. No reinstalar APK antiguos sobre bases migradas ni desinstalar para forzar actualización.
- Firma compatible comprobada localmente contra el asset público de RC1 (hash anterior verificado) y de nuevo en Actions: certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`. No se subió la clave privada a GitHub. El APK instalable es el firmado localmente, no el de clave efímera del runner.
- **77 pruebas Kotlin/21 Python correctas**, lint sin errores y ambos APK compilados tras incrementar versión. Verificación remota del tag: tests/build correctos; [Verify por push](https://github.com/ArukuKun/RotomPedia/actions/runs/41883796984) `success`.
- Incidencia de distribución: el primer workflow de tag y su primer intento de recuperación detuvieron la publicación en el parser de certificados al seleccionar Build Tools **37.0.0** del runner; tests/compilación habían pasado y la release siguió en borrador. Diagnóstico y recuperación en `1d1176f`; fijar las mismas Build Tools **35.0.0** usadas localmente en `a0e2ee6` resolvió el bloqueo, sin cambiar firma ni debilitar su comprobación.
- **Workflow completo de verificación y publicación `success`:** [Actions 41887424591](https://github.com/ArukuKun/RotomPedia/actions/runs/41887424591). Su recuperación manual requiere primero repetir todas las verificaciones; compara el árbol Android/herramientas con el tag original (`git diff` vacío), verifica un único certificado exacto, SHA-256, paquete, `versionCode` y `versionName` contra el APK de CI, y solo después publica los assets preparados como pre-release. Nunca retaguea ni reemplaza una release ya pública.
- Reglas/automatización actualizadas: `.github/workflows/release.yml`, `.github/scripts/publish-prerelease.sh` y recuperación `workflow_dispatch` en `.github/workflows/verify-v34a.yml`. El script rechaza tags estables y drafts sin notas; prueba local del rechazo de `v3.5.0` correcta. Futuros tags usan ya el verificador fijado, no el parser incompatible del tag histórico.
- Notas públicas incluyen cambios, instrucciones para instalar sobre RC1, comparación de versiones, problemas conocidos y puerta manual. Captura y el resto de §13 siguen sin implementar; mapas/Rotom/RI no iniciados.

### 4.2 Ajustes posteriores a RC2 — comodidad, retorno y orden regional

**Autorización y evidencia:** el usuario reporta cuatro puntos concretos al probar Android: botón de filtros como texto amarillo poco reconocible; filtro de Kalos perdido al volver de Furfrou y scroll aplicado a mitad del nacional; orden de Hisui poco útil; categoría «Variedades estables» demasiado amplia y duplicados Tótem. Captura aportada: 16 resultados de Hisui ordenados nacionalmente. No se infieren modelo, versión Android ni otra validación no informada.

**Rama:** `feature/v3.5-pokedex-comfort`, desde `b2443c7`. Es continuación de estabilización de v3.5, no una fase funcional paralela. RC3 publicada: código 30503, sin nueva migración Room ni cambios de firma. Se conserva íntegra la evidencia histórica de RC2 (§4.1); el contrato siguiente actualiza su comportamiento en estos puntos.

- **Filtro visible:** botón `Filtrar` con borde, icono de tres barras y texto, alineado a la izquierda del resumen. Usa color de superficie legible, no texto amarillo suelto; conserva el panel inferior desplazable y sus filtros combinables.
- **Retorno cómodo:** filtro guardado por entrada de navegación mediante `rememberSaveable`/Saver antes de crear el controlador. Conserva consulta, categoría, generación, origen regional, tipo, grupo huevo y favoritos. Lista y Cuadrícula guardan modo, índice y offset; no montar sus scrollers mientras faltan índice/metadatos validados. Cambiar voluntariamente los filtros inicia en el primer resultado; volver de la ficha restaura la posición del listado original.
- **Causa reproducida:** RC2 creaba un controlador nuevo con filtro nacional, mientras Compose restauraba el scroll del listado filtrado; eran dos estados desacoplados. No se resuelve forzando siempre scroll a cero al volver, porque eso perdería la posición solicitada.
- **Orden regional verificado:** el mismo `pokemon-index.json` incorpora cuatro Pokédex de referencia y 1445 relaciones desde CSV de la revisión fijada `e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff`, sin consultas por búsqueda. Hisui usa Leyendas Arceus; Alola, Ultrasol/Ultraluna (`updated-alola`); Galar, Espada/Escudo; Paldea, Escarlata/Púrpura. Fuente `pokedexes`, `pokedex_prose`, `pokedex_version_groups`, `pokemon_dex_numbers`, `regions` y `version_groups`.
- **Hisui:** entre sus formas regionales, Decidueye `#003`, Typhlosion `#006`, Samurott `#009` quedan al inicio por el orden real de esa Pokédex. Sus números nacionales siguen visibles (`#724`, `#157`, `#503`); se añade referencia regional identificada y contexto de edición. Nacional normal y selector conservan el orden nacional. Elegir una categoría regional genérica sin origen concreto tampoco inventa un orden entre regiones.
- **Cobertura:** especies sin registro en la Pokédex base elegida —por ejemplo regionales de DLC de Galar— se colocan al final en orden nacional y se informa cuántas faltan. No se mezclan números de Isla de la Armadura/Corona con la Pokédex base ni se fabrica una numeración regional.
- **Formas útiles sin categoría cajón:** retirar el chip «Variedades estables» de los filtros generales (también del picker); mantener la clasificación interna y las variedades seleccionables por especie/búsqueda. Lycanroc diurno sigue como variedad predeterminada en nacional; sus otras formas, los genios y las formas Origen continúan disponibles en detalle/selector. No se borran identidades ni equipos.
- **Tótem:** las 12 variedades con marcador canónico `totem` no se duplican en los resultados de Pokédex, incluso al filtrar Alola; sí permanecen en el índice, detalle y picker para conservar datos históricos y permitir selección exacta. No se modifica ni reemplaza un favorito o integrante Tótem guardado.

**Pruebas de aceptación:**

- [x] `Filtrar` es un botón con icono y borde accesible, no texto suelto.
- [x] Kalos → Furfrou → Atrás conserva filtro y posición de Lista.
- [x] Hisui → Cuadrícula → ficha → Atrás conserva origen, vista y posición.
- [x] Consulta/tipo/categoría/grupo huevo/generación/favoritos también se restauran; guardar/restaurar estado no aplica scroll a una lista sin sus metadatos.
- [x] Limpiar/cambiar filtros lleva al comienzo correcto, no a mitad del nacional.
- [x] Hisui comienza por los tres iniciales regionales según Leyendas Arceus, manteniendo número nacional y variedad exacta.
- [x] Otras regiones muestran su contexto real y faltantes, sin numeraciones de DLC mezcladas.
- [x] Sin chip de variedades estables ni duplicados Tótem en Pokédex; formas útiles y las 12 identidades Tótem intactas en el picker/índice.
- [x] Pruebas completas, lint/build y snapshot correctos; APK con firma compatible y versión creciente, entrega por §8.1.
- [ ] Validar RC3 en el teléfono, especialmente el recorrido exacto reportado y la sensación del nuevo botón.

**Estado:** implementación verificada y publicada como pre-release; falta la validación de RC3 en el teléfono. Captura, biología, movimientos, mochila, entrenadores, Team Builder, mapas y RI no se implementan en esta intervención.

**Evidencias locales:**

- **86 pruebas Kotlin, 0 fallos/errores/omitidas; 23 pruebas Python correctas.** `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain` correcto. Lint sin errores, 110 advertencias y 30 sugerencias preexistentes. APK de instrumentación compilado, no ejecutado en dispositivo (`adb devices` vacío).
- `PokedexNavigationUiTest.kt`, seis casos nuevos con un `NavHost` real en Robolectric: ida/vuelta de Lista Kalos/Furfrou y Cuadrícula Hisui, posición con offset de 18 px y comparación de coordenadas sin clipping, restauración de estado Compose, cambio intencional de filtros al inicio, botón/chips y persistencia combinada de los siete campos del filtro. No equivale a validar el teléfono.
- `PokemonExplorerTest.kt`: tres casos nuevos de Hisui (primeros regionales 3/6/9), contexto/faltantes Galar y preservación de Tótem/Lycanroc/genios/Origen. Se amplía la reapertura offline para no emitir entradas hasta que los metadatos regionales estén listos; conserva cero interacciones API al explorar. `PokedexPolishUiTest` mantiene sus cinco casos con el botón nuevo.
- `tools/tests/test_pokemon_index.py`: dos casos nuevos de orden/edición y rechazo de regionales huérfanos/duplicados/incompletos. Comparación del asset contra `b2443c7`: todos los campos anteriores intactos; solo añade `regionalDexes`, `regionalDexNumbers` y `regionalDexesComplete`. Sin cambios en esquemas, entidades/migraciones Room ni IDs personales.
- Los primeros tests de ida/vuelta esperaban el controlador viejo durante la transición; se corrigió la sincronización esperando el listado restaurado en el árbol UI, sin retirar comprobaciones. Búsqueda `totem` admite alias parciales, por lo que se verifica que conserva las 12 identidades, no que todo resultado textual de esa consulta sea exclusivamente Tótem.
- Archivos: `data/explorer/PokemonExplorer.kt`, `data/index/{PokemonIndexSnapshot,PokemonIndexImporter}.kt`, `data/repository/DefaultPokemonRepository.kt`, `viewmodel/{PokemonExplorerController,PokemonViewModel}.kt`, `ui/screens/{PokedexScreen,PokedexScreenState,PokedexResults,PokemonExplorerContent}.kt` (base Kotlin de §11). Nuevo test `PokedexNavigationUiTest.kt`, ampliaciones de `PokemonExplorerTest.kt`/`PokedexPolishUiTest.kt`, asset `pokemon-index.json`, generador/tests Python, build de versión, workflow de publicación, README y este mismo plan.
- El workflow de tags ahora asegura Build Tools 35.0.0 antes de verificar/publicar, igual que la recuperación de RC2; evita depender del conjunto de SDK preinstalado del runner sin cambiar la clave ni rebajar sus controles.

**Entrega pública RC3:**

- [Pre-release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc3), `draft=false`, `prerelease=true`; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc3/RotomPedia-v3.5.0-rc3.apk). Actions verificó y publicó automáticamente la entrega preparada bajo §8.1, sin recuperación ni retagueo.
- Commit/tag **`4309dd8bf4efe2e03632da209dffb73bb31272f9` / `v3.5.0-rc3`**. Incluye código, pruebas, snapshot, versión y evidencias previas a publicación; la documentación de cierre se añade después sin cambiar tag ni APK.
- APK `RotomPedia-v3.5.0-rc3.apk`, **23922929 bytes**, SHA-256 **`eeb4b65ebd8491eee48b5c249dac9f08f6b2b6f30af7a080b0712a1df2a567aa`**, coincidente con el digest público; `.apk.sha256` incluido. Enlace público comprobado por HTTP HEAD **200**, MIME APK y longitud correctos.
- `versionName=3.5.0-rc3`, `versionCode=30503 > 30502`, mismo paquete y mínimo API 25. Certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, comprobado contra el APK público de RC2 (hash previo coincidente) y validado otra vez por Actions. Clave privada fuera de GitHub. Sin cambios Room/datos personales.
- Ambos workflows del mismo commit **success**: [Verify RotomPedia 41896235062](https://github.com/ArukuKun/RotomPedia/actions/runs/41896235062) y [Release APK 41896234934](https://github.com/ArukuKun/RotomPedia/actions/runs/41896234934). Conservados RC1/RC2 y sus tags/assets sin modificación.
- Notas incluyen los cuatro cambios, recorrido Kalos/Furfrou e Hisui, instalación sobre RC2 sin desinstalar, limitaciones regionales/Tótem y problemas conocidos. La calculadora de captura sigue pendiente y no se marca v3.5 estable por esta entrega. Detenerse aquí antes de otra mejora y recoger la validación manual.

---

### 4.3 Correcciones posteriores a RC3 — barra y filtros limpios

**Alcance autorizado:** atender el reporte del usuario sobre ubicación del filtro, exceso de texto, categorías Cosméticos/Combate, primigenias y falso «Pikachu de Alola». Rama `feature/v3.5-pokedex-toolbar`, preparación `v3.5.0-rc4` / `30504`. Este apartado actualiza el contrato visual de §4.2 sin borrar su evidencia histórica. No abre P3 ni otras fases.

- Icono blanco de filtros en la **barra superior original**, junto al título Pokédex; sin botón duplicado en el cuerpo. `PokedexExplorerScreen` comparte el Scaffold real entre producción y pruebas.
- Panel sin párrafos explicativos: categorías **Nacional, Megaevoluciones, Gigamax y Regionales**, más los controles combinables existentes con títulos breves. Cosméticos y Combate se retiran de los filtros generales (también del picker), no del modelo ni de la ficha.
- Pokédex no lista alternativas cosméticas/de combate ni Tótem; conserva las **1025 variedades predeterminadas**, incluso Aegislash. Las identidades completas siguen en índice, detalle y selector por especie/búsqueda; no se borran datos personales.
- `kyogre-primal`/`groudon-primal` se agrupan con Megaevoluciones, exigiendo el slug canónico de variedad y forma. Siguen llamándose **Regresión Primigenia**, sin alterar `isMega=false` del origen ni inventar «Mega Groudon/Kyogre».
- Pikachu con gorra/cosplay no tiene origen regional; `pikachu-alola-cap` conserva sus IDs y nombre **Pikachu (Gorra Alola)**, también en la ficha. Raichu de Alola y el orden regional de RC3 se conservan.
- Retorno de ficha mantiene los siete filtros, Lista/Cuadrícula y posición exacta de RC3. Restaurar categorías retiradas vuelve a Nacional, sin filtro invisible activo.
- Sin cambios de snapshot, Room, migraciones, cuentas, equipos, favoritos, EV/IV ni textos de movimientos guardados. P3 (§13.1), captura, mapas, Rotom contextual y RI **siguen pendientes**.

**Verificación local completa:** **91 pruebas Kotlin, cero fallos/errores/omitidas; 23 Python correctas** y los tres verificadores de snapshots correctos. `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain` correcto tras la revisión adicional de nombres. Lint: cero errores, 110 advertencias y 30 sugerencias. Tests de navegación usan ahora el Scaffold real: posición del icono, ausencia de botón/prosa/categorías retiradas, restauración, recorridos Kalos/Furfrou e Hisui y offset de 18 px. Regresiones de primigenias, gorras, alternativas y nombres de ficha añadidas. `git diff --check` correcto. Publicación y validación visual en teléfono todavía pendientes; no hay dispositivo conectado.

**APK preparado:** `RotomPedia-v3.5.0-rc4.apk`, 24028928 bytes; SHA-256 `daf2719958717888b7751b5a9ed9ac6e245f431d7f09cb4be7e12505f6e85c97`, archivo `.apk.sha256` separado. Paquete `com.duoc.rotompedia`, versión `3.5.0-rc4` / `30504 > 30503`, mínimo API 25. `apksigner verify --print-certs` correcto: certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, idéntico al APK RC3 cuyo hash se volvió a comprobar. Clave privada fuera de GitHub. Notas incluyen cambios, instalación sin desinstalar, recorridos manuales y problemas conocidos. Publicar solo tras repetir controles en Actions, sin modificar tags/assets previos.

**Entrega pública RC4:** tag inmutable `v3.5.0-rc4` en `2c0a9ce`; [pre-release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc4) con `draft=false`, `prerelease=true`, sin promoción a estable/Latest. Ambos workflows **success**: [Release APK 41949731254](https://github.com/ArukuKun/RotomPedia/actions/runs/41949731254) y [Verify 41949726956](https://github.com/ArukuKun/RotomPedia/actions/runs/41949726956). Actions repitió pruebas/build y verificó certificado único, checksum y paquete/versión antes de publicar automáticamente. Digest público idéntico al SHA-256 anterior; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc4/RotomPedia-v3.5.0-rc4.apk) comprobado con HTTP HEAD **200**, 24028928 bytes y MIME APK. RC1–RC3 y sus tags/assets intactos. La documentación de cierre no modifica el tag ni el APK.

**Puerta manual pendiente:** instalar sobre RC3 sin desinstalar, revisar barra/panel, ambas primigenias, Alola sin Pikachu con gorra, Kalos → Furfrou → Atrás e Hisui/Cuadrícula con posición conservada. Comprobar también equipos y favoritos previos. Pruebas automatizadas correctas no equivalen a validación visual en teléfono.

**Respuesta posterior del usuario:** «Todo correcto sigamos!». Se registra aprobación general de las correcciones de RC4; no se infieren dispositivo, versión Android ni ejecución individual de cada recorrido o comprobación de persistencia. Cierra las observaciones reportadas de Pokédex, no promueve v3.5 a estable ni autoriza automáticamente todos los módulos pendientes. Seleccionar el siguiente alcance de §13; P3 continúa diferida.

### 4.4 BIO-001 — ficha biológica compacta (autorizada tras RC4)

**Autorización:** tras aprobar RC4 («Todo correcto sigamos!»), el usuario acepta expresamente la propuesta BIO-001: «si continuemos con eso mismo». Alcance exclusivo: especie/categoría, altura, peso y habilidades normales/oculta bajo imagen y descripción, con acceso a fichas existentes. Rama `feature/v3.5-pokemon-biology`; preparación `v3.5.0-rc5` / `30505`, entrega de prueba bajo §8.1. No autoriza P3, captura, ABIL-001, mochila, mapas, Rotom ni RI.

**Implementación:**

- Tarjeta «Datos biológicos» inmediatamente después de la descripción y antes de estadísticas: especie, categoría de especie, altura en metros, peso en kilogramos y habilidades visibles sin desplegar. Distribución adaptable mediante FlowRow; nombres y controles admiten varias líneas, sin columnas de ancho fijo ni reducción de texto obligatoria.
- Categoría desde `pokemon-species.genera` en español, no una traducción inventada ni la categoría de una forma. Reutiliza la consulta de especie de descripción/evolución, sin petición adicional; se publica su estado antes de consultar la cadena evolutiva, de modo que un error posterior no descarte la categoría. Se comprueba el ID de respuesta cuando existe y la tarjeta no acepta categoría de otra especie.
- Altura/peso desde `PokemonData` de la **variedad exacta**, convirtiendo decímetros/hectogramos a m/kg (una cifra decimal, coma española). Ausentes/no positivos como «—», nunca sustituir por la especie base.
- Habilidades mediante el **AbilityRepository existente** e índice incluido, ordenadas por slot; marcador `(oculta)` y acceso a `AbilityReferenceSheet` con textos/procedencia/edición y enlaces ya existentes. No se duplican catálogos ni se implementan los contadores/traducciones de ABIL-001. Fallback inglés identificado si falta nombre español.
- Carga, error, reintento y cobertura vacía explícitos, sin párrafos técnicos en el resumen. Cambio de variedad cancela la carga anterior y reinicia selección/estado para no mostrar habilidades ajenas.
- Sin cambios Room/esquema/migraciones/snapshots ni escrituras de datos personales. **Límite explícito:** la caché persistente anterior no almacena medidas ni categoría; tras reapertura sin red pueden aparecer «—». Las habilidades sí proceden del índice local. No se promete cobertura biológica completa offline ni historial de medidas/habilidades por edición.

**Fuente cotejada:** PokeAPI `pokemon/6` (Charizard, altura 17/peso 905) y `pokemon/10034` (Mega Charizard X, altura 17/peso 1105), ambos enlazando especie 6; `pokemon-species/6.genera` español «Pokémon Llama». Confirma por qué no debe usarse peso de especie base para la Mega. No se altera la política de descripción específica de variedad de RC4.

**Pruebas:** `PokemonBiologyTest` cubre conversión, faltantes, género español y compatibilidad de JSON anterior; `PokemonBiologyUiTest` cubre tarjeta, texto grande/280 dp, rol oculto, apertura/cierre de las fichas reales, cambio de variedad con carga tardía, reintento, cobertura vacía y rechazo de enlaces ajenos. `PokemonRepositoryTest` conserva identidad offline y verifica que dimensiones ausentes en la caché vieja no se inventan. **Suite completa: 100 pruebas Kotlin, cero fallos/errores/omitidas; 23 Python y los tres verificadores de snapshots correctos.** `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain` correcto; lint sin errores, 110 advertencias/30 sugerencias. `git diff --check` correcto. APK de instrumentación compilado, no ejecutado; `adb devices` vacío. Validación visual en teléfono y publicación pendientes.

**APK preparado RC5:** `RotomPedia-v3.5.0-rc5.apk`, 23937709 bytes; SHA-256 `fee07a72bd25e714d469f115a145a41b0f7b00dd1668ef85d3a12baecb8517b0` y archivo `.apk.sha256`. Paquete `com.duoc.rotompedia`, versión `3.5.0-rc5` / `30505 > 30504`, mínimo API 25. Firma verificada con Build Tools 35.0.0: certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, idéntico al APK de RC4 cuyo hash también se volvió a comprobar. Clave privada fuera de GitHub. Publicar únicamente tras repetir controles de §8.1 en Actions, conservando todas las RC anteriores y sin promoción a estable.

**Entrega pública RC5:** tag inmutable `v3.5.0-rc5` en `2776a61`; [pre-release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc5), `draft=false`, `prerelease=true`, no estable/Latest. Ambos workflows **success**: [Release APK 41953561285](https://github.com/ArukuKun/RotomPedia/actions/runs/41953561285) y [Verify 41953557438](https://github.com/ArukuKun/RotomPedia/actions/runs/41953557438). Actions repitió pruebas/lint/build y verificó checksum, certificado único y paquete/versión antes de publicar automáticamente. Digest público idéntico al SHA-256 anterior; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc5/RotomPedia-v3.5.0-rc5.apk) comprobado con HTTP HEAD **200**, longitud 23937709 y MIME APK. RC1–RC4 y sus tags/assets intactos; documentación de cierre posterior no modifica el tag ni el APK.

**Puerta manual BIO-001:** instalar sobre RC4 sin desinstalar; comparar Charizard (1,7 m/90,5 kg) con Mega Charizard X (1,7 m/110,5 kg), categoría de especie y habilidades correspondientes. Abrir/cerrar fichas normales y ocultas, cambiar variedad y probar texto grande. Comprobar favoritos/equipos previos y retorno de Pokédex. El resultado visual y la aprobación del usuario siguen pendientes; no avanzar automáticamente a otra mejora.

**Respuesta posterior del usuario:** «por lo demas, todo bien sigamos al siguiente». Se registra aprobación general de BIO-001/RC5 sin inferir dispositivo ni una matriz exhaustiva de pruebas. Solicita para el **siguiente parche** ocultar la sección completa «Formas» cuando no haya otras variedades, incluido su título, espacio reservado y aviso de ausencia en PokeAPI; ejemplo reportado: Overqwil. Se registra como DEX-008, pendiente de implementación, sin publicar otro APK solo por esta anotación. Además reporta que la ficha de Overqwil no explica cómo evolucionarlo: EVO-001, pendiente de reproducción y revisión por edición. No confundir ausencia de alternativas con fallo/carga del servicio ni presentar evolución como corregida. La siguiente tarea funcional se selecciona aparte; P3 continúa diferida.

### 4.5 Sprites de portadores de habilidad y sección Formas limpia — RC6 publicada

**Autorización:** el usuario solicita mini sprites junto a los Pokémon asociados en la ficha de habilidad, tras aprobar lo demás. Se incorpora además DEX-008, expresamente pedido para el siguiente parche. Rama `feature/v3.5-ability-sprites`; preparación `v3.5.0-rc6` / `30506` bajo §8.1. Alcance visual acotado: no se implementan contadores/traducciones de ABIL-001, evolución Overqwil (EVO-001), P3 ni otros módulos.

- Cada fila de portador añade un sprite de **40 dp** junto al nombre, rol normal/oculta y slot existentes. Reutiliza `PokemonImage`/resolver/caché, siempre con **`link.pokemonId`**, nunca con número nacional ni especie base. Mantiene fallback de imagen de la variedad exacta y placeholder/reintento si falta; no inventa imágenes ni crea otro catálogo.
- Nombre y rol siguen visibles y legibles; texto adaptable con ancho restante y varias líneas, comprobado a 280 dp/texto 1,6×. Imagen decorativa para accesibilidad, sin repetir el nombre anunciado. Tocar la fila sigue abriendo el mismo Pokémon/variedad.
- La ficha compartida se usa tanto desde catálogo de habilidades como desde detalle Pokémon. Se conservan todas las relaciones y la carga progresiva de 40 en 40: no se impone un nuevo límite total ni se cambia el significado del conteo existente.
- `PokemonFormsSection` comparte producción/pruebas. Con carga terminada y sin otras variedades no compone título, tarjetas, aviso ni espaciado propio. Con alternativas conserva tarjetas, selección y retorno a la variedad predeterminada. Mientras carga no muestra datos anteriores; en error ofrece «Reintentar formas» sin mensaje técnico ni afirmar ausencia confirmada, conservando alternativas parcialmente cargadas si existen.
- No se cambian snapshots, repositorios, esquemas/migraciones Room, cuentas, equipos, favoritos, EV/IV ni movimientos guardados. Permanece la ficha biológica de RC5.

**Verificación local completa:** **105 pruebas Kotlin, cero fallos/errores/omitidas; 23 Python y tres verificadores de snapshots correctos.** Cinco casos nuevos en `AbilitySpritesAndFormsUiTest`: petición de imagen exacta `10104`, sprite 40 dp/texto grande y navegación; ficha real con 41 relaciones y carga adicional; Overqwil sin título/aviso/hueco; selección de alternativas; carga/error/reintento. `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain` correcto; lint sin errores, 110 advertencias/30 sugerencias. APK de instrumentación compilado, no ejecutado; `adb devices` vacío. Publicación y validación visual en teléfono pendientes.

**APK preparado RC6:** `RotomPedia-v3.5.0-rc6.apk`, 23943285 bytes; SHA-256 `58381f67c41670f96379d45530d439a3c68c2c021fc2f7e141c5a23b0478010d` y archivo `.apk.sha256`. Paquete `com.duoc.rotompedia`, versión `3.5.0-rc6` / `30506 > 30505`, mínimo API 25. Firma comprobada con Build Tools 35.0.0: certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, idéntico al APK RC5 cuyo hash se volvió a verificar. Clave privada fuera de GitHub; publicación supeditada a repetir controles de §8.1 en Actions, sin alterar tags/assets anteriores ni promover a estable.

**Entrega pública RC6:** tag inmutable `v3.5.0-rc6` en `7a892df`; [pre-release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc6), `draft=false`, `prerelease=true`, sin promoción a estable/Latest. Ambos workflows **success**: [Release APK 41958335159](https://github.com/ArukuKun/RotomPedia/actions/runs/41958335159) y [Verify 41958330768](https://github.com/ArukuKun/RotomPedia/actions/runs/41958330768). Actions repitió pruebas/lint/build y comprobó checksum, certificado único y paquete/versión antes de publicar automáticamente. Digest público idéntico al SHA-256 anterior; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc6/RotomPedia-v3.5.0-rc6.apk) verificado con HTTP HEAD **200**, longitud 23943285 y MIME APK. RC1–RC5 y sus tags/assets intactos; documentación de cierre posterior no cambia el tag ni el APK.

**Puerta manual RC6:** instalar sobre RC5 sin desinstalar; revisar sprites y nombres/roles de portadores desde catálogo y detalle, navegación a una variedad regional y carga adicional tras 40 relaciones. Comprobar Overqwil sin sección vacía, Ninetales/Charizard con alternativas reales, texto grande y datos guardados. Validación visual pendiente; EVO-001 y el resto del backlog no se presentan como resueltos.

**Aprobación posterior del usuario:** «Esta perfecta! quedo de pana, sigamos!». RC6, incluidos ABIL-002 y DEX-008, aprobada en términos generales. No se infiere dispositivo ni ejecución individual de toda la matriz manual. Proponer EVO-001 (requisito evolutivo de Overqwil por edición) como siguiente alcance y confirmar antes de implementarlo; P3 continúa diferida. Esta aprobación no promueve v3.5 a estable ni autoriza todo el backlog.

### 4.6 EVO-001 ampliada — requisitos evolutivos de Hisui (autorizada)

**Autorización:** tras aprobar RC6, el usuario amplía Overqwil a «todos los de Hisui que tienen esas restricciones, o requisitos» y confirma «si» al alcance: condiciones verificadas por juego, métodos locales frente a transferencia, sin mezclar ediciones. Rama `feature/v3.5-hisui-evolutions`; preparación `v3.5.0-rc7` / `30507`, entrega de prueba bajo §8.1. No autoriza P3, captura, mapas, RI ni el resto del backlog.

**Diagnóstico:** `parseEvolutionChainWithStages` consume solo `evolutionDetails.firstOrNull()` y una cadena de **especies**, no variedades; su texto genérico no modela estilos/contador de usos ni garantiza contexto de juego. No se atribuye el mismo método a todas las ediciones ni se usa el ID nacional para representar una variedad de Hisui.

**Diseño y límites:** referencia contextual local `HisuiEvolutions`, 22 relaciones verificadas cuyos slugs se resuelven en el **mismo índice Pokémon**. No introduce otro catálogo de identidades, peticiones por relación ni cambios de snapshot/Room. Bloque de evolución con edición explícita **Leyendas: Arceus / Escarlata-Púrpura**, requisitos, nombres/sprites y navegación a la variedad exacta; enlaces de objetos reutilizan las fichas existentes. La edición se guarda por variedad con `rememberSaveable`. Funciona aunque falle la cadena remota, una vez disponible el índice local.

La referencia genérica previa de la especie se conserva tras un botón, identificada como **no filtrada por edición** (otros ancestros/ramas, como Scizor); no certifica variedad ni sustituye las reglas verificadas visibles por defecto. En las variedades ordinarias Qwilfish, Basculin rojo/azul y Sneasel/Weavile se excluyen las ramas reservadas a sus contrapartes de Hisui/Raya Blanca. Luna Carmesí no se atribuye como evolución de Ursaring. No se cambia la persistencia personal ni la consulta de movimientos.

**Cobertura revisada (dos contextos, no afirmación universal para todos los juegos):**

| Relación | Leyendas: Arceus | Escarlata/Púrpura |
|---|---|---|
| Stantler → Wyrdeer | Asalto Barrera 20 veces, estilo rápido; menú Evolucionar | Método deshabilitado; evolucionar en Arceus y transferir por HOME |
| Scyther → Kleavor | Mineral Negro | Sin evolución local; alternativa válida: evolucionar en Arceus y transferir. No afirmar que sea la única obtención: también hay captura en DLC |
| Teddiursa → Ursaring → Ursaluna normal | Nivel 30; Bloque de Turba en noche de luna llena | Teddiursa sí evoluciona; Ursaring no produce Ursaluna aquí. Luna Carmesí no es evolución |
| Qwilfish de Hisui → Overqwil | Mil Púas Tóxicas 20 veces, estilo fuerte; menú Evolucionar | Subir de nivel con Mil Púas Tóxicas aprendido |
| Sneasel de Hisui → Sneasler | Usar Garra Afilada de día, sin equiparla | Subir de nivel de día con Garra Afilada equipada |
| Basculin Raya Blanca → Basculegion macho/hembra | ≥294 PS acumulados de retroceso sin debilitarse; menú Evolucionar, sin nivel adicional | Mismo daño acumulado y subir de nivel; conserva sexo e identidad |
| Growlithe de Hisui → Arcanine de Hisui | Piedra Fuego | Piedra Fuego |
| Voltorb de Hisui → Electrode de Hisui | Piedra Hoja, no Piedra Trueno ni nivel 30 | Piedra Hoja |
| Cyndaquil → Quilava → Typhlosion de Hisui | Nivel 17 y después 36 | Quilava al 14; no produce Typhlosion de Hisui localmente |
| Oshawott → Dewott → Samurott de Hisui | Nivel 17 y después 36 | Dewott al 17; no produce Samurott de Hisui localmente |
| Rowlet → Dartrix → Decidueye de Hisui | Nivel 17 y después 36, no 34 | Dartrix al 17; no produce Decidueye de Hisui localmente |
| Petilil → Lilligant de Hisui | Piedra Solar en Arceus | No produce esta forma localmente |
| Rufflet → Braviary de Hisui | Nivel 54 en Arceus | No produce esta forma localmente |
| Bergmite → Avalugg de Hisui | Nivel 37 en Arceus | No produce esta forma localmente |
| Zorua de Hisui → Zoroark de Hisui | Nivel 30 | Nivel 30 de la misma variedad regional |
| Goomy → Sliggoo de Hisui → Goodra de Hisui | Nivel 40 en Arceus; después ≥50 con lluvia natural | Goomy no produce Sliggoo de Hisui aquí; Sliggoo de Hisui sí evoluciona a Goodra de Hisui al subir desde nivel 50 con lluvia natural |

En Arceus las evoluciones se eligen en el menú tras cumplir la condición, no se presentan como automáticas. Para métodos sin evolución local, HOME es una ruta posible, no una afirmación de exclusividad frente a capturas/eventos. **Otras ediciones (incluidas Z-A, GO o generaciones anteriores) no están verificadas por esta referencia y no reciben reglas extrapoladas.** No se incluyen cambios de forma legendarios como evoluciones. Niebla descartada para Goodra de Hisui: la referencia consultada limita la alternativa de niebla a generación VII; no mezclarla con Hisui.

**Fuentes cotejadas el 05-11-2026:**

- [Serebii: nuevos métodos de Arceus](https://www.serebii.net/legendsarceus/evolution.shtml): objetos, estilos y retroceso; contrastar por nombre, no copiar números de tabla que contiene erratas.
- [Serebii: métodos de Escarlata/Púrpura](https://www.serebii.net/scarletviolet/evolution.shtml): Overqwil con movimiento aprendido.
- Pokémon Database, apartado Evolution chart de [Wyrdeer](https://pokemondb.net/pokedex/wyrdeer), [Kleavor](https://pokemondb.net/pokedex/kleavor), [Ursaluna](https://pokemondb.net/pokedex/ursaluna), [Basculegion](https://pokemondb.net/pokedex/basculegion), [Sneasler](https://pokemondb.net/pokedex/sneasler), [Overqwil](https://pokemondb.net/pokedex/overqwil), y páginas de Arcanine, Electrode, Typhlosion, Samurott, Lilligant, Zoroark, Braviary, Sliggoo, Goodra, Avalugg y Decidueye en el mismo sitio: niveles/formas/objetos y variantes de iniciales.
- Bulbapedia: [Hisuian form](https://bulbapedia.bulbagarden.net/wiki/Hisuian_form), [Methods of Evolution](https://bulbapedia.bulbagarden.net/wiki/Methods_of_Evolution), y Evolution data de [Sliggoo](https://bulbapedia.bulbagarden.net/wiki/Sliggoo_(Pok%C3%A9mon)), [Basculegion](https://bulbapedia.bulbagarden.net/wiki/Basculegion_(Pok%C3%A9mon)), [Sneasler](https://bulbapedia.bulbagarden.net/wiki/Sneasler_(Pok%C3%A9mon)), [Ursaluna](https://bulbapedia.bulbagarden.net/wiki/Ursaluna_(Pok%C3%A9mon)), [Wyrdeer](https://bulbapedia.bulbagarden.net/wiki/Wyrdeer_(Pok%C3%A9mon)) y Kleavor: evolución local frente a transferencia, nivel adicional, sexo y lluvia.
- Nombres españoles de movimientos/objetos contrastados con los índices compartidos incluidos: **Mil Púas Tóxicas**, Asalto Barrera, Mineral Negro, Bloque de Turba, Garra Afilada y piedras. No crear alias de movimientos ni reescribir textos guardados por esta corrección.

**Verificación:** primera pasada focalizada de 10 pruebas correcta; ampliada con la sección real de producción y estado de fallo remoto. Suite final Android en ejecución; 23 Python y los tres verificadores de snapshots correctos. Nuevos tests verifican las 22 relaciones contra identidades reales, exclusiones de formas, cambios de método por edición, navegación exacta, objetos, restauración de edición y ausencia de evoluciones inventadas. Publicación y validación visual pendientes.

**Incidencia local de verificación:** una repetición se solapó con Gradle aún activo y falló al escribir `classes.jar` por bloqueo de archivo de Windows. La ejecución anterior terminó correctamente, pero no basta como evidencia del árbol final tras ajustar el nombre español a «Mil Púas Tóxicas». Se repite toda la verificación sin solapamiento (`--max-workers=1`); no publicar sobre el intento fallido ni cambiar firma por este bloqueo.

**Verificación final local RC7:** repetición completa **correcta**, sin solapamiento: `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain --max-workers=1`. **117 Kotlin, cero fallos/errores/omitidas; 23 Python y los tres verificadores de snapshots correctos.** Lint sin errores, 110 advertencias/30 sugerencias. `git diff --check` correcto. Instrumentación compilada, no ejecutada; `adb devices` vacío. La incidencia de bloqueo anterior queda resuelta para esta entrega; validación visual pendiente.

**APK preparado RC7:** `RotomPedia-v3.5.0-rc7.apk`, 24071978 bytes, SHA-256 `f8857ff5659159ab1b4c8be6a2f26b3ce062109f1f08f0cec04a15f543d1dec5`, con `.apk.sha256`. Paquete `com.duoc.rotompedia`, `3.5.0-rc7` / `30507 > 30506`, mínimo API 25. Build Tools 35.0.0 verifica certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, idéntico al APK RC6 cuyo hash también se comprobó. Clave privada fuera de GitHub. Publicación supeditada a repetir controles de §8.1 en Actions; no modificar tags/assets anteriores ni promover a estable.

**Entrega pública RC7:** tag inmutable `v3.5.0-rc7` en `6027026`; [pre-release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc7), `draft=false`, `prerelease=true`, no estable/Latest. Ambos workflows **success**: [Release APK 41962875764](https://github.com/ArukuKun/RotomPedia/actions/runs/41962875764) y [Verify 41962871713](https://github.com/ArukuKun/RotomPedia/actions/runs/41962871713). Actions repitió pruebas/lint/build y comprobó checksum, certificado único y paquete/versión antes de publicar automáticamente. Digest público idéntico al SHA-256 anterior; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc7/RotomPedia-v3.5.0-rc7.apk) comprobado con HTTP HEAD **200**, longitud 24071978 y MIME APK. RC1–RC6 y sus tags/assets intactos; documentación de cierre posterior no cambia el tag ni el APK.

**Puerta manual RC7:** instalar sobre RC6 sin desinstalar; alternar edición en Overqwil, Wyrdeer y Sneasler, verificar retroceso/sexo en Basculegion, objetos/luna en Kleavor/Ursaluna y niveles/lluvia en iniciales y regionales. Comprobar navegación exacta, referencia general diferenciada y datos previos. Otras ediciones fuera de cobertura y P3 pendiente; esperar aprobación antes de otro alcance.

**Aprobación posterior del usuario:** «perfecto sigamos». RC7/EVO-001 aprobada en términos generales, sin inferir dispositivo ni ejecución exhaustiva de cada recorrido. Proponer ABIL-001 como siguiente alcance acotado: contadores de variedades distintas normales/ocultas y revisión de traducciones verificables, conservando sprites/fichas existentes. Confirmar antes de implementar; P3 y el resto del backlog no quedan autorizados por esta aprobación. No promover a estable.

### 4.7 ABIL-001 — contadores por rol y nombres revisados (autorizada)

**Autorización:** tras aprobar RC7, el usuario confirma «sip» a ABIL-001: contadores separados de variedades con habilidad normal/oculta, sin duplicados, y revisión de traducciones verificables, conservando sprites y fichas. Rama `feature/v3.5-ability-counts`; preparación `v3.5.0-rc8` / `30508`, entrega de prueba bajo §8.1. No autoriza P3, captura, mapas ni otros pendientes.

**Contadores y lista:**

- `AbilityPresentation.holders` agrupa relaciones del índice compartido por **`pokemonId`**, no por especie ni nombre. Total = unión de variedades; Normal/Oculta = conjuntos distintos por rol. Una variedad con ambos roles cuenta una vez en cada rol y una sola vez en total; aviso breve solo cuando exista solapamiento.
- Una fila por variedad, con sus slots normales/ocultos completos y ordenados, deduplicando slots idénticos sin modificar las relaciones de origen. Conserva sprites exactos de 40 dp, nombre, navegación y carga progresiva de 40 **variedades**, sin nuevo límite total.
- Unidad visible «Variedades asociadas», nunca mezclarla con especies o número de slots. Contadores se adaptan mediante FlowRow a 280 dp/texto 1,6×. Cobertura vacía indica total/roles a cero, sin inventar portadores.
- La edición sigue filtrando solo los textos de la ficha: las relaciones y contadores son referencia actual, no historial por juego. Fuente local: 374 habilidades/2943 relaciones, sin petición por fila. Ejemplos reales del índice: Pereza 3 variedades (2 normales/1 oculta), Viscosecreción 4 (4/0), Esclusa de Aire 1 (1/0), Mar Llamas 32 (30/2), Nevada 9 (5/4). El snapshot actual no tiene solapamiento por rol ni múltiples slots para una misma habilidad/variedad; esos casos se cubren con fixtures de regresión, no se anuncian como observados reales.

**Revisión de nombres (05-11-2026):**

- Los 311 nombres españoles incluidos coinciden exactamente con `ability_names.csv` de PokeAPI fijado en revisión `e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff`; no son un error de lectura del idioma. Comparación independiente contra [Lista de habilidades de WikiDex](https://www.wikidex.net/wiki/Lista_de_habilidades), enlazando por **nombre inglés/identidad**, no por ordinal de tabla (diverge de los IDs de PokeAPI en generación IX): **307 coinciden**, 3 conservan un nombre anterior y 1 requiere cotejo individual porque la lista omite su nombre inglés.
- Cotejo individual de [Mandíbula Dragón](https://www.wikidex.net/wiki/Mand%C3%ADbula_Drag%C3%B3n) confirma Dragon’s Maw/Mandíbula Dragón; no se cambia. Las 63 habilidades sin nombre español siguen con fallback inglés identificado en sus fichas/resumen; no inventar traducciones para completar cobertura.
- Tres revisiones documentadas **desde la novena generación**, verificadas además en sus páginas individuales: [Ausente → Pereza](https://www.wikidex.net/wiki/Pereza), [Lodo Líquido → Viscosecreción](https://www.wikidex.net/wiki/Viscosecreci%C3%B3n), [Bucle Aire → Esclusa de Aire](https://www.wikidex.net/wiki/Esclusa_de_Aire). Se conserva la nomenclatura española de España coherente con la referencia incluida; no confundir Viscosecreción con el nombre hispanoamericano Toxisecreción.
- `AbilityNames` contiene solo estas tres revisiones con ID+slug, nombre previo/actual y URL de procedencia. Se aplican únicamente a identidades y nombres de origen conocidos; no sobrescriben traducciones desconocidas. `ReferenceAbility.displayName` centraliza el nombre actual en catálogo, resumen Pokémon y ficha. La búsqueda acepta nombre actual, anterior, inglés, slug e ID; la ficha identifica el nombre anterior y su época. No se cambia el texto histórico de las descripciones por edición ni se afirma revisar integralmente sus efectos.
- Auditoría externa de consulta guardada en el temporal aprobado como `ability-name-audit-rc8.json` y script asociado (no como un segundo plan ni datos nuevos del catálogo). Resultados y fuentes durables quedan aquí; no se afirma que ese archivo temporal esté versionado.

**Preservación:** sin modificar snapshots, generadores, IDs/slugs, relaciones de origen, esquemas/migraciones Room ni cuentas/equipos/favoritos/EV/IV/movimientos guardados. Los nombres antiguos no se borran ni se reescriben en datos personales. Se conservan los avances de RC4–RC7.

**Verificación en curso:** pasada focalizada correcta de `AbilityPresentationTest`, `AbilityCountsUiTest`, `AbilitySpritesAndFormsUiTest` y `PokemonBiologyUiTest`. Tests nuevos: múltiples slots/duplicados/ambos roles/total unión y variantes regionales, faltantes, alias/nombres guardados, validación ID+slug, ficha real con 41 variedades y cambio de edición sin alterar contadores, sprites/navegación, texto grande y ceros. Se añade regresión sobre las **374 habilidades reales** y todos sus slots/roles. 23 Python y tres verificadores de snapshots correctos; suite completa Android en ejecución, publicación y validación visual pendientes.

**Verificación final local RC8:** `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain --max-workers=1` correcto, sin solapamiento. **125 Kotlin, cero fallos/errores/omitidas; 23 Python y tres snapshots correctos.** Lint sin errores, 110 advertencias/30 sugerencias; `git diff --check` correcto. Instrumentación compilada, no ejecutada; `adb devices` vacío. Validación visual pendiente.

**APK preparado RC8:** `RotomPedia-v3.5.0-rc8.apk`, 23968105 bytes; SHA-256 `94a53ce3d5974fce824335d6621d3e417ce6e92c9fa5e471b6eccc38e452fb30` y `.apk.sha256`. Paquete `com.duoc.rotompedia`, `3.5.0-rc8` / `30508 > 30507`, mínimo API 25. Build Tools 35.0.0 verifica certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, idéntico al APK RC7 cuyo hash también se comprobó. Clave privada fuera de GitHub; publicación supeditada a repetir controles de §8.1 en Actions, sin alterar releases anteriores ni promover a estable.

**Entrega pública RC8:** tag inmutable `v3.5.0-rc8` en `d93350c`; [pre-release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc8), `draft=false`, `prerelease=true`, no estable/Latest. Ambos workflows **success**: [Release APK 41967640326](https://github.com/ArukuKun/RotomPedia/actions/runs/41967640326) y [Verify 41967635287](https://github.com/ArukuKun/RotomPedia/actions/runs/41967635287). Actions repitió pruebas/lint/build y comprobó checksum, certificado único y paquete/versión antes de publicar automáticamente. Digest público idéntico al SHA-256 anterior; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc8/RotomPedia-v3.5.0-rc8.apk) comprobado con HTTP HEAD **200**, longitud 23968105 y MIME APK. RC1–RC7 y sus tags/assets intactos; documentación de cierre posterior no modifica el tag ni el APK.

**Puerta manual RC8:** instalar sobre RC7 sin desinstalar; revisar contadores/roles de Pereza, Viscosecreción, Esclusa de Aire, Mar Llamas y Nevada, nombres actuales/anteriores en búsqueda y resumen Pokémon. Cambiar edición sin alterar contadores, cargar más de 40 variedades, navegar a regionales y probar texto grande. Comprobar equipos/favoritos previos. Validación visual pendiente; no autoriza otro alcance ni promoción a estable.

### 4.8 ITEM-002 — objetos por bolsillos de referencia (autorizada)

**Autorización:** después de RC8 el usuario pide continuar y confirma «si» a ITEM-002: organizar objetos por bolsillos reutilizando catálogo/sprites. No se infiere una matriz visual exhaustiva de RC8 de esa continuidad. Rama `feature/v3.5-item-pockets`; preparación `v3.5.0-rc9` / `30509`, entrega de prueba bajo §8.1. No abre inventario personal, objetos equipados en equipos, captura, P3, mapas ni RI.

**Fuente y contrato de datos:**

- El mismo `item-index.json` se amplía con `pockets`, `categoryPockets` y `pocketsComplete`, desde CSV oficiales `item_pockets`, `item_pocket_names` y `item_categories.pocket_id` de **la misma revisión fijada** `e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff`. No se regenera el catálogo con datos más recientes ni se cambia una identidad.
- `tools/generate_item_index.py --add-pockets` enriquece el snapshot existente de forma reproducible. Comparación contra HEAD previa: campos **`items` y `revision` íntegros**, incluidos 2223 IDs, nombres, slugs duplicados, categorías y atributos. Solo cambian los metadatos nuevos y el checksum del sobre.
- Ocho bolsillos/54 relaciones de categoría cubren todos los objetos: Poké Balls **38**, Medicina **106**, Bayas **74**, MT/MO **338**, Objetos clave **391**, Objetos **1204**, Combate **36**, Correo **36**. Orden de navegación es UX, no una afirmación de orden universal en el juego.
- Se usa la relación de fuente, no coincidencias de palabras: categoría `medicine` → Bayas; `healing` → Medicina; `z-crystals` → Objetos clave; `held-items` → Objetos. No inventar un bolsillo universal de «Equipables» ni inferir equipabilidad desde el bolsillo; se conserva el filtro de atributo documentado «Se puede llevar».
- **Límite por edición:** los CSV no aportan una matriz de bolsillos por juego. La interfaz indica brevemente «Bolsillos de referencia · varían según el juego»; no inventa selector de edición que certifique disponibilidad o clasificación histórica. Las fichas existentes sí conservan selección de edición para textos/precios/MT-MO. Una mochila concreta de Escarlata/Púrpura puede tener otras divisiones.
- Bolsillo/categoría duplicados, relaciones huérfanas y cobertura completa declarada pero incompleta se rechazan antes de actualizar tablas. Snapshots antiguos sin metadatos siguen válidos: objetos accesibles en Todos/«Sin bolsillo verificado», sin asignaciones inventadas. Con cobertura parcial explícita las categorías desconocidas también quedan accesibles allí.

**Interfaz y arquitectura:** navegación horizontal por bolsillos con chips legibles, búsqueda y contador; filtros combinables de categoría/atributo/editorial en hoja abierta desde el icono de **barra superior**, sin ocupar permanentemente el listado. Categorías se acotan al bolsillo; al cambiarlo se limpia una categoría incompatible, conservando búsqueda y atributos. «Limpiar» vuelve al catálogo completo. Se conservan sprites, fichas y enlaces, seleccionando siempre por `itemId` aunque dos objetos tengan el mismo slug.

`ItemRepository.pocketIndex()` reutiliza el mismo asset validado y recarga sus metadatos incluso si el hash persistente ya está al día. No hay otro catálogo ni peticiones de bolsillo por búsqueda. Metadatos en memoria desde asset; sin columnas/tablas ni migración Room nuevas. Saver conserva bolsillo/consulta/categoría/atributo/editorial; la lista no se monta hasta que filtro y metadatos coinciden con el resultado, para no aplicar scroll guardado a otra lista. Abrir/cerrar una ficha mantiene la lista montada y su posición. Bolsillo de referencia visible también en detalle.

**Pruebas y estado:** compilación de código Android inicial correcta; primera pasada focalizada agotó su límite de 240 s durante KSP de tests sin resultado de pruebas (no se cuenta como éxito). Se comprobó que no quedaban procesos Gradle activos y se lanzó una única pasada focalizada con límite de 600 s. Nuevos tests: partición exacta de los 2223 objetos, Bayas/Medicina, filtros combinados, slugs duplicados, desconocidos, metadatos inválidos/legacy, recarga con hash persistido, rechazo atómico conservando favorito/índice, navegación real por bolsillos, restauración combinada y cierre de ficha con posición exacta. **26 Python correctas** y tres verificadores de snapshots correctos. Suite focalizada Android aún en ejecución; suite completa, firma final/publicación y validación visual pendientes.

**Resultado focalizado:** `ItemUiTest`, `ItemRepositoryTest`, `ItemPresentationTest`, `ItemPocketTest` y `ItemPocketsUiTest` **correctos** en la repetición. Se amplía la prueba UI a los ocho bolsillos y se inicia suite Android completa con un único worker, sin builds solapados. No se publica sobre el intento que agotó tiempo ni se infiere validación de teléfono.

**Verificación final local RC9:** `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain --max-workers=1` correcto. **134 Kotlin, cero fallos/errores/omitidas; 26 Python y tres snapshots correctos.** Lint sin errores, 110 advertencias/30 sugerencias; `git diff --check` correcto. Instrumentación compilada, no ejecutada; `adb devices` vacío. Validación visual pendiente.

**APK preparado RC9:** `RotomPedia-v3.5.0-rc9.apk`, 23993913 bytes; SHA-256 `b4686824d1445ed80438d52f1e402e9ea37ea243e97460438fba21a2b4489c12` y `.apk.sha256`. Paquete `com.duoc.rotompedia`, `3.5.0-rc9` / `30509 > 30508`, mínimo API 25. Build Tools 35.0.0 verifica certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, idéntico al APK RC8 cuyo hash también se comprobó. Clave privada fuera de GitHub; publicación supeditada a repetir controles de §8.1 en Actions, sin alterar releases previas ni promover a estable.

**Entrega pública RC9:** tag inmutable `v3.5.0-rc9` en `17faea3`; [pre-release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc9), `draft=false`, `prerelease=true`, no estable/Latest. Ambos workflows **success**: [Release APK 42000420294](https://github.com/ArukuKun/RotomPedia/actions/runs/42000420294) y [Verify 42000414303](https://github.com/ArukuKun/RotomPedia/actions/runs/42000414303). Actions repitió pruebas/lint/build y comprobó checksum, certificado único y paquete/versión antes de publicar automáticamente. Digest público idéntico al SHA-256 anterior; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc9/RotomPedia-v3.5.0-rc9.apk) comprobado con HTTP HEAD **200**, longitud 23993913 y MIME APK. RC1–RC8 y sus tags/assets intactos; documentación de cierre posterior no cambia el tag ni el APK.

**Puerta manual RC9:** instalar sobre RC8 sin desinstalar; recorrer los ocho bolsillos, combinar Bayas/búsqueda Acardo/categoría/atributo, abrir/cerrar ficha y comprobar posición, restaurar filtros y usar Limpiar. Revisar MT/MO por edición en sus fichas, sprites y datos previos de equipos/favoritos. Validación visual pendiente; no autoriza otro alcance ni promoción a estable.

**Fuentes:**
- `https://raw.githubusercontent.com/PokeAPI/pokeapi/e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff/data/v2/csv/item_pockets.csv`
- `https://raw.githubusercontent.com/PokeAPI/pokeapi/e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff/data/v2/csv/item_pocket_names.csv`
- `https://raw.githubusercontent.com/PokeAPI/pokeapi/e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff/data/v2/csv/item_categories.csv`

### 4.9 Corrección de interfaz de cliente — sin diagnóstico ni procedencia en layouts

**Autorización y corrección de criterio:** tras RC9, el usuario insiste en que los layouts de cliente no muestren mensajes sobre PokeAPI, caché, disponibilidad en la fuente o comentarios de desarrollador. Solicita además un acceso superior a filtros de objetos más intuitivo; acepta lo demás y **difiere expresamente los sprites faltantes**. Rama `feature/v3.5-clean-client-ui`, preparación `v3.5.0-rc10` / `30510`, entrega bajo §8.1. Este criterio sustituye las exigencias históricas de mostrar procedencia/limitaciones extensas en la interfaz corriente; **no elimina la procedencia de modelos, documentación o pruebas ni autoriza inventar datos**.

**Regla de UI vigente:**

- Nada de banners «datos de PokeAPI», «caché local», copias guardadas, revisión/hash/fecha de descarga, IDs técnicos o párrafos sobre ausencia en el proveedor. Es información interna, no contenido cotidiano de las fichas. La documentación y los tests mantienen trazabilidad, cobertura, incertidumbre y contratos de datos.
- Contenido de juego disponible se conserva. Secciones opcionales sin contenido se ocultan sin tarjeta/título de advertencia ni sustitución por datos de otra variedad. Descripción Pokémon solo donde corresponde a la variedad predeterminada; no se usa la descripción de especie como si fuera de otra forma.
- Errores operativos reales siguen visibles con texto breve orientado al usuario y reintento; nunca se imprime la excepción interna. Se conservan «Actualizar»/reintento y uso interno de cachés sin anunciar su implementación. Etiqueta breve «Inglés» cuando hace falta distinguir un texto no traducido, sin explicación del backend.
- No declarar que un Pokémon no existe/no puede obtenerse solo porque falten encuentros; estado vacío breve. No inventar precio, efecto, traducción o referencia de MT/MO al ocultar los avisos técnicos.

**Cambios:** limpieza en objetos, catálogo/fichas de movimientos y habilidades, biología, descripción Pokémon, encuentros y pies de evolución de Hisui. Los requisitos y selectores de juego, contadores, alias históricos, sprites y navegación siguen presentes. «Copiar ficha» copia contenido de cliente, sin fuente/slugs/IDs internos; la trazabilidad detallada permanece aquí. Mensaje sobre movimiento guardado no resuelto se reduce a una acción breve de revisión, conservando íntegro el texto persistido y su editor. No se implementa P3 ni se reorganiza su navegación.

**Filtros:** botón superior de **Objetos** con icono + texto «Filtros», superficie tonal, forma redondeada, área táctil mínima de 48 dp, contador de filtros adicionales y estado accesible. Mantiene los bolsillos bajo la barra como navegación, sin duplicar su selección en el panel. Categoría/característica y selectores existentes se presentan como campos con borde, valor y flecha, no texto tipo enlace. «Buscar objeto», resumen activo y Limpiar permanecen; estados y reglas de filtrado de RC9 intactos. El icono blanco de la barra roja de **Pokédex no se cambia**, respetando la corrección ya aprobada.

**Preservación:** sin modificar repositorios, índices/snapshots, identidades, schemas/migraciones Room ni datos personales. No se tocan resolución/descarga/recursos de sprites: sus faltantes quedan para después. No se autoriza captura, mapas, Rotom/RI ni otra fase.

**Verificación local terminada:** tests UI actualizados a los mismos controles reales y nuevas regresiones online/offline/error/reintento, ausencia de banners de diagnóstico, copia limpia, campos vacíos ocultos, contenido inglés y datos de movimientos, descripción correcta sin hueco, botón de filtros con texto/badge/estado accesible a 280 dp y fuente 1,5×. Las comprobaciones previas de filtros/restauración/scroll/contadores/navegación siguen activas. `tools/tests/test_client_copy.py` añade guardia sobre literales de UI y permite explícitamente URLs de recursos/comentarios internos. **140 Kotlin, sin fallos/errores/omitidos; 28 Python y tres verificadores de snapshots correctos**. `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain --max-workers=1` correcto; lint sin errores (110 warnings/30 hints). `git diff --check` correcto. Instrumentación compilada, no ejecutada: `adb devices` vacío. Validación visual pendiente.

**APK preparado RC10:** `RotomPedia-v3.5.0-rc10.apk`, **23995809 bytes**, SHA-256 `b8208f2a818b4b78609edaf61760be4419fee51c2ec4c9570672ac677ea45b94` y `.apk.sha256`. Paquete `com.duoc.rotompedia`, `3.5.0-rc10` / `30510 > 30509`, mínimo API 25. Build Tools 35.0.0 verifica certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, idéntico al APK RC9 cuyo hash también se comprobó. Clave privada fuera de GitHub. Publicación supeditada a repetir controles de §8.1 en Actions, sin alterar releases anteriores ni promover a estable.

**Entrega pública RC10:** tag inmutable `v3.5.0-rc10` en `9ff2c69`; [pre-release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc10), `draft=false`, `prerelease=true`, no estable/Latest. Ambos workflows **success**: [Release APK 42058665480, intento 2](https://github.com/ArukuKun/RotomPedia/actions/runs/42058665480) y [Verify 42058655832](https://github.com/ArukuKun/RotomPedia/actions/runs/42058655832). El primer intento de Release agotó el límite de cinco minutos de tests tras `AppNotIdleException` en cuatro pruebas Compose; Verify pasó con el mismo commit. Se repitió únicamente el job fallido, sin omitir controles ni cambiar código/tag/APK; el segundo intento terminó correctamente y Actions verificó checksum, certificado y paquete/versión antes de publicar. Digest público idéntico al SHA-256 anterior; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc10/RotomPedia-v3.5.0-rc10.apk) comprobado con HTTP HEAD **200**, longitud 23995809 y MIME APK. Un observador local perdió su proceso (`Shell.NotFoundError`), sin detener la ejecución remota; estado final comprobado directamente. RC1–RC9 y sus tags/assets intactos; cierre documental posterior no modifica el tag ni el APK. Validación visual en teléfono pendiente.

### 4.10 Recuperar la identidad de Rotom — auditoría y propuesta previa a implementación

**Solicitud vigente:** pausar la acumulación de funciones y revisar toda la app como un **Rotom que vive en el teléfono**, no una enciclopedia genérica negra. `MapFeatureScreen` es referencia interna (fondo `rotomfondo`, oscurecimiento, cara y tarjetas legibles), no plantilla para copiar sin adaptación. PokeInfo de Bruno Sousa es referencia de comodidad/organización, no de recursos ni copia literal. Este apartado es parte del **único Plan Maestro**: la maqueta enlazada es un artefacto visual, no una hoja de ruta adicional.

**Alcance AHORA:** auditoría estática por pantalla, investigación pública verificable, propuesta de sistema visual, diseño de navegación P3, maqueta representativa, secuencia progresiva y riesgos. No modificar código Android, Room, modelos persistidos, firma, versión ni fórmulas; no publicar estable ni otra RC por entregar un documento. Tras aprobar el alcance visual/técnico, cada hito aprobado podrá implementarse y entregarse como pre-release compatible bajo §8.1, sin permiso por archivo. La preferencia por esta maqueta no certifica todavía el comportamiento en teléfono. Captura matemática, sprites faltantes, TRAIN funcional, mapas completos v3.6 y RI no se implementan aquí.

#### 4.10.1 Material estudiado y trazabilidad

- Proyecto de la carpeta de trabajo, rama de auditoría `docs/rotom-visual-direction`, partiendo de `c3fc08f`; versión Android permanece `3.5.0-rc10` / `30510`. Remoto `https://github.com/ArukuKun/RotomPedia`, actualizado mediante `git fetch origin --tags`. Se revisaron historial de temas, rutas y etiquetas v3.0–v3.5.0-rc10; esto no equivale a instalar y probar cada APK histórico. `origin/Main` apunta a `4529650`, distinto de la rama de trabajo: no se mezclan ni descartan cambios del remoto.
- Aclaración posterior del usuario: «ZIP» significa este proyecto/carpeta. No se requiere ni se afirma haber descomprimido/comparado otro ZIP.
- Captura de **Rowlet Moves** aportada en esta conversación: observación directa de cuatro pestañas, tabla y acción superior. El uso concreto de esa acción como selector de juego está descrito por el usuario; su menú abierto no aparece en la imagen. El deslizamiento entre páginas es un requisito del usuario, no algo demostrable por una captura fija.
- Videos de Incineroar/capturas de conversaciones anteriores: referencia histórica descrita en §13.1; sus archivos reproducibles no están disponibles en esta etapa. No afirmar haber vuelto a verlos ni deducir gestos/transiciones no observados.
- Fuentes públicas consultadas el 5 de noviembre de 2026: [portfolio del autor](https://brunoportfolio.com/android_apps.php), que enlaza el paquete correcto; [AppRecs](https://apprecs.com/android/com.brunosousa.pokeinfo/pokeinfo), dos capturas vistas (menú y catálogo de movimientos); [Uptodown](https://pokeinfo.en.uptodown.com/android), ocho capturas vistas (captura, líderes, mapa Kalos, objetos, detalle Pikachu, movimientos, Pokédex y menú); [AndroidBlip](https://www.androidblip.com/android-apps/com.brunosousa.pokeinfo.html), descripción histórica con líderes/Alto Mando y calculadoras. Google Play directo devuelve 404 en esta consulta, aunque el buscador conserva texto indexado. Las fechas/versiones de listados son heterogéneas: no asumir que cada captura corresponde a 14.1 ni que su cobertura sigue vigente.
- Imágenes públicas examinadas como evidencia, **no incorporadas a los recursos de la app**. Galería Uptodown, identificadores reproducibles: `c0e43bdb54d5e7a70df7e0fe3b6fab2300bc90bc61e010e96a1f63231d0cfe60` (captura), `692a818e3e3a8a89911f831d70566d679608102c795cfafc3cdceebb206d9ca1` (líderes), `28882b79570e091537526338b843b4697a78b4e327b0760da2a974d492aa2b11` (Kalos), `f5edbae41ace6798f6caa6815c10cc60eaacb908ae70c4d5e24bc6000c9fa11d` (objetos), `414a9972aa52284a091f709e5901f2f3520e18d0c4f900734e12086d2af2f470` (detalle), `8ec9431da1a85571d5e58cbdd19c9bd4d6a34d46e72d29ccfdbf64d57f1b7c14` (movimientos), `df651d6afea770e3ed00188bee55bf83b0c00327297c0f39d3109c96e72641bc` (Pokédex), `50d28f259602d47811ea652e9ecd87039643e29e33e83935d87231b650fadad5` (menú). No se descarga/extrae APK de terceros ni se copian sus imágenes privadas.

#### 4.10.2 Auditoría de coherencia visual por pantalla

Rutas relativas a `RotomPedia/app/src/main/java/com/duoc/rotompedia/`. Auditoría de código y del recurso ilustrado; no sustituye capturas Android de ejecución. **La incoherencia no es solo negro:** Líderes fuerza blanco mientras otras pantallas fuerzan gris oscuro y otras usan MaterialTheme.

| Pantalla / archivos reales | Hallazgo verificable | Conservar / intervención propuesta |
|---|---|---|
| Tema — `ui/theme/Theme.kt:40–59`, `Color.kt`, `Type.kt` | `darkTheme` se recibe pero se ignora; siempre `darkColorScheme`, fondo `#121212`/superficie `#1E1E1E`. Escalas claras declaradas pero no usadas; tipografía parcialmente centralizada, tamaños locales abundantes. | Resolver apariencia y forma independientemente; roles de color/contraste y densidad comunes, sin sustituir datos por diseño. |
| Inicio — `MainActivity.kt`, `MenuPrincipalScreen:100`, `MenuButton:72` | `rotomfondo` con Crop, sin zona protegida para la cara; columnas centradas y botones fijos 165×56 dp pueden desbordar con teclado/texto/ancho pequeño. Diálogo «tema» cambia formas, no apariencia. | Conservar entradas y comentarios actuales; rejilla adaptable, cara despejada, ajustes separados. No añadir motor contextual RI. |
| Login — `ui/screens/LoginScreen.kt:43–72` | Ilustración + overlay 0,45, panel casi opaco y campos/etiquetas blancos hardcodeados. | Compartir estructura y roles de formulario; mantener biometría, sesión, validaciones y campos; probar IME y autocompletado. |
| Registro — `ui/screens/RegisterScreen.kt:71–103` | Repite imagen/overlay/panel con otros acentos y textos de estado. | Mismo sistema de formularios sin cambiar contratos de autenticación ni recopilar datos nuevos. |
| Pokédex — `PokedexScreen.kt:38`, `PokemonExplorerContent.kt:55–128`, `PokedexResults.kt:61–95`, `PokedexScreenState.kt` | Barra roja aprobada; cuerpos/cards Material sin fondo Rotom compartido. Lista amplia y sprite 72 dp, cuadrícula 3 columnas, filtros/estado ya resueltos. | Recuperar marco y paneles, **no reducir proporciones**, no reabrir categorías retiradas ni perder orden nacional/regional, filtros o scroll. |
| Detalle — `PokemonDetailScreen.kt:98–151`, `PokemonBiologyCard.kt`, `PokemonFormsSection.kt`, `PokemonSpeciesDescription.kt`, `HisuiEvolutionSection.kt` | Barra gris azul y fondo opaco; mezcla de roles Material y cyan/grises fijos. Contenido actual aceptado; varias secciones legítimamente densas. | Envolver contenido aprobado con identidad común; conservar artwork/shiny/rugido, variantes exactas, habilidades, evolución por edición y errores breves. No rellenar huecos con datos de otra forma. |
| Aprendizaje embebido — `PokemonReferenceSections.kt:15–22`, `ReferenceScreens.kt:219–294` | Título + botón expansor y pane de 560 dp; controles ocupan varias filas. Carga está vinculada a pokemonId/repositorio/intento, no a cada pestaña. Estado local con remember, no supervivencia completa a recreación. | Único botón «Movimientos» → página P3; extraer estado común y reutilizar aprendizaje y ficha, sin duplicar peticiones. |
| Catálogos y fichas movimientos/habilidades — `ReferenceScreens.kt` | Scaffold/cards/sheets Material; selector común `ItemChoice`. Contadores, alias y paginación existen; no marco ilustrado. | Misma barra/filtros/superficies. Catálogo general distinto de aprendizaje por Pokémon; respetar lista completa y enlaces de portadores. |
| Objetos — `CompetitiveItemsScreen.kt:98`, `ItemFilterAction.kt` | Bolsillos y botón tonal de RC10 funcionales; cuerpo sin lenguaje Rotom común y dos sheets Material. | Mantener controles intuitivos de RC10 y estado de RC9; revestir, no duplicar filtros. No hay mochila/inventario personal: no crearlo bajo el nombre «mochila». |
| Team Builder — `TeamBuilderScreen.kt:76–130,463–524` | Fuerza `#121212` y barra/cards oscuras; acentos naranja/cyan dispersos. Tiene `ScrollableTabRow` + `when`, **no HorizontalPager** en el código actual. | Mantener equipos, seis slots, matrices y edición. Usar sus pestañas como referencia de organización; swipe P3 será capacidad nueva, no afirmación sobre Team Builder. |
| Editor y picker — `MemberStatEditorDialog.kt:114–197`, `PokemonPickerDialog.kt:22`, `MovePickerDialog` en `ReferenceScreens.kt:287` | Diálogos con alturas fijas/porcentuales, negros locales y pestañas independientes; el picker Pokémon comparte explorador con variantes. | Paneles legibles y desplazables con IME/texto grande; conservar guardar/cancelar, cuatro movimientos, textos desconocidos y propietario del equipo. |
| Versus — `PokemonVersusScreen.kt:49–125` | Scaffold negro, comparadores y gradientes rojos/cyan propios. | Roles semánticos para dos bandos y resultado; no sustituirlos por el acento de forma ni alterar análisis. |
| IV/EV — `StatCalculatorScreen.kt:114–165` | Fondos `#0F0F0F`/`#141414`, controles oscuros, exportación y varias tablas. | Enmarcar y adaptar contraste; mantener fórmulas, naturalezas, números, sliders, identidad y Showdown. |
| Captura — `CatchRateCalculatorScreen.kt:109–169` | Barra roja pero cuerpo `#0F1115`, numerosas superficies y badges locales. | Cambiar apariencia sin alterar matemática ni selector funcional de Balls; CAP sigue siendo trabajo independiente. |
| Tabla de tipos — `TypeChartScreen.kt:41–65,97–126` | Barra roja y matriz con scroll bidimensional legítimo. `HeaderCell` elige negro por comparación de color, arriesga contraste oscuro. | Mantener tabla/fórmulas; centralizar roles por celda, texto multiplicador y encabezados. No aplicar la prohibición de scroll horizontal de una fila de movimientos a una matriz. |
| Líderes — `GymLeadersScreen.kt:59–119,174,265` | Drawer regional; fondo/cards blancos fijos frente a diálogo oscuro. Código actual consume `GymLeadersData` local, no asumir una pantalla completa por edición/Alto Mando solo porque exista backend. | Coherencia de tema y filtros; TRAIN-001 y ampliación factual por juego son otro alcance. |
| Selector mapas — `MapScreen.kt:46–155` | Referencia aprobada: imagen Crop, overlay negro 0,55, tarjetas 0,85 y cyan. Paleta fija y cara no protegida contra todos los tamaños. | Extraer su intención, **no replicar Crop y números mágicos**. Preservar la navegación regional existente. |
| Mapa y localización — `MapScreen.kt:159`, `LocationDetailBottomSheet.kt:42–44,101` | Mapa transformable con pines y ficha negra fija; contexto geográfico distinto al fondo decorativo. | Marco/cara fuera del lienzo cartográfico y sheets temáticos; no poner ilustración encima del mapa ni tocar coordenadas, encuentros o alcance v3.6. |
| Menús, alertas y sheets transversales | Formas en MainActivity, naturaleza/editor, filtros Pokémon/objetos, fichas y drawer líderes tienen estilos distintos. | Adaptadores visuales comunes sobre componentes Material accesibles; no un mega-componente que mezcle navegación, filtros y negocio. La cara queda visible en la pantalla base; un modal que necesite foco puede cubrirla temporalmente. |

#### 4.10.3 PokeInfo ↔ RotomPedia: evidencia frente a propuesta

| Área | Observado / documentado en PokeInfo | Aplicación propuesta, no copia |
|---|---|---|
| Menú | AppRecs 0 / Uptodown 7: logo propio, dos columnas y ocho accesos; Pokédex, Items, Moves, Locations, Abilities, Type Chart, Gym Leaders/Elite Four y Tools. | Accesos reconocibles y pocos pasos; nuestro dispositivo/cara, colores y funcionalidades existentes, sin copiar sus botones/gradientes. |
| Pokédex | Uptodown 6: filas con sprite, nombre, número nacional y tipos; búsqueda arriba, acciones de barra. La captura no demuestra qué hace cada icono ni restauración de scroll. | Conservar lista amplia, cuadrícula y orden/filtros ya aprobados; no recuperar desplegables retirados por imitar otra app. |
| Detalle | Uptodown 4: imagen, nacional, tipo, especie, medidas, habilidades normal/oculta y barras de stats con rangos visibles. | Datos conectados y jerarquía compacta; conservar detalles aceptados de RotomPedia. No inferir fórmula histórica de los rangos desde la imagen. |
| Aprendizaje | Captura Rowlet: Nivel/Huevo/MT-HM/Tutor, nivel, nombre, tipo, clase con icono, potencia y precisión. | P3 con cuatro páginas deslizables, selector superior legible y filas adaptativas. No copiar seis columnas minúsculas en cualquier ancho. |
| Juego/edición | Usuario describe filtro de juego en barra; captura muestra acción, no su contenido. Texto público indexado contiene opiniones sobre valores históricos, no pruebas funcionales. | Contrato propio: edición gobierna aprendizaje/textos, no convierte automáticamente potencia/precisión actuales en valores históricos. Revisar menú real si se aportan video/capturas. |
| Objetos | Uptodown 3: sprite, nombre y categoría, búsqueda y acción de barra. | Mantener bolsillos RC9, ficha y filtro RC10. No inferir disponibilidad por juego ni inventario personal. |
| Habilidades | Botón observado en menú; descripciones públicas/reseñas refieren fichas, pero no se vio su pantalla ni navegación completa en esta etapa. | Conservar nuestros contadores, alias, portadores/sprites y navegación ya implementados; no atribuir esos detalles a PokeInfo sin evidencia. |
| Líderes/Alto Mando | Menú explícito y Uptodown 1: retratos/nombre, número de gimnasio y ciudad, búsqueda/acción. No se vio ficha de Alto Mando ni selector abierto. | Revestir líderes actuales; investigación funcional por juego/entrenador continúa en TRAIN-001. |
| Mapas/localizaciones | Uptodown 2: Kalos, rótulos, rutas numeradas, botones +/−, selector de ruta, botón Pokémon y flechas. Una imagen no demuestra precisión de hitboxes, pan, offline o cobertura. | Conservar intención de navegar zona→Pokémon; cartografía verificable y piloto v3.6 siguen aparte. |
| Calculadoras | Uptodown 0: Tools con pestañas, Pokémon, PS restantes, nivel, Ball, estado, resultado y Calculate. Stat/IV aparecen en descripción pública. | Controles agrupados y resultado claro sin cambiar nuestras fórmulas. No usar su 24,8 % como caso esperado sin todas las condiciones/fórmula. |

**Conclusión de investigación:** se recupera navegación enciclopédica directa, no estética ajena. Investigación pública suficiente para proponer organización; **no es una auditoría ejecutable exhaustiva de toda PokeInfo**. Faltan evidencia directa de habilidades, filtros abiertos, Alto Mando, fichas individuales de objetos/movimientos y reproducción de los videos previos. No se bloquea la propuesta por ello ni se inventa su comportamiento.

#### 4.10.4 Claro/oscuro: comprobación histórica y persistencia propuesta

- `f6fb780` (ruta histórica `app/src/.../ui/theme/Theme.kt`) y `2590bf5` (ruta actual bajo `RotomPedia/`) sí seleccionaban light/dark con `isSystemInDarkTheme()` y color dinámico en Android 12+. **Existía soporte de apariencia del sistema**.
- `d77be19` reemplazó esa selección por `darkColorScheme(...)` al añadir formas de Rotom. Conservó el parámetro `darkTheme`, pero dejó de usarlo. Esto explica la regresión de tema sin atribuirla a las RC recientes.
- Búsqueda en historial disponible (`git log --all -G` para dark/isDark/Switch/tema/oscuro y revisión de MainActivity/Pokédex/tema): **no se confirmó un selector manual claro/oscuro persistente**. No negar que pudiera existir en material externo o ramas ausentes; no confundir el switch de favoritos ni «Forma de Rotom» con apariencia. No hace falta restaurar exactamente una implementación inexistente para cumplir el nuevo requisito.
- Propuesta: `AppearanceMode = SYSTEM | LIGHT | DARK`, inicial SYSTEM cuando no exista preferencia. Resolver SYSTEM reactivamente con el sistema. Selección guardada localmente fuera de Room, mediante un pequeño almacén de preferencias de presentación; reutilizar SharedPreferences existente con clave nueva y StateFlow en vez de introducir otra base/migración. No escribir `rotom_form`, sesiones, cuentas ni datos de equipos al elegir apariencia. Forma sigue usando su clave existente en `rotom_theme_prefs`.
- Ajustes separados: **Apariencia** (Claro/Oscuro/Sistema) y **Forma de Rotom** (las seis actuales). Preferencia de dispositivo, también visible antes de iniciar sesión; no hacerla depender de una cuenta o red. Reinicio, recreación, cambio de sistema y selección de forma no deben resetearse mutuamente. Tema resuelto antes del primer contenido para evitar destello negro/blanco.
- Colores dinámicos del wallpaper no se reactivan por defecto: pueden borrar la identidad. Formas conservan acentos; no cambiar organización, navegación, colores elementales ni semántica de errores/éxito. Acentos adaptados a contraste, especialmente amarillo Ventilador sobre claro. La ilustración actual no representa seis formas: no teñirla ni fabricar seis artes automáticamente.

#### 4.10.5 Sistema visual reutilizable propuesto

**Dirección:** dispositivo naranja/rojo cálido, ojos y energía cyan, panel de lectura azul grisáceo o claro frío. Evitar tanto negro puro uniforme como cristal/transparencia sobre cada línea de texto. Tarjetas moderadamente redondeadas, bordes perceptibles y controles con presencia táctil, sin convertirlo en una app genérica minimalista. Mantener español, errores útiles y política de §4.9: nada de PokeAPI/caché/procedencia técnica en la UI.

| Pieza orientativa | Responsabilidad / límite |
|---|---|
| `RotompediaTheme` + tokens | Resolver apariencia×forma. Roles `deviceFrame`, `readingSurface`, `raisedSurface`, `onReading`, `mutedText`, `outline`, `accent`, `scrim`, error/éxito; escalas de espacio/tipografía/densidad. No convertir roles en una paleta duplicada por pantalla. |
| `RotomBackground` | Una ilustración decorativa local por pantalla raíz, semántica nula, oscurecimiento y anclaje adaptativo; sin red ni carga por fila. Mantener proporción, no Crop arbitrario ni estiramiento de cara. |
| `RotomScaffold` | Orquestar insets, barra, zona protegida de cara y panel de contenido con slots; no poseer repositorios ni navegación de negocio. Fondos de Scaffold/Surface interiores transparentes donde corresponda, paneles de lectura con contraste estable. |
| `RotomTopBar` | Atrás/título/acciones coherentes, 48 dp mínimo táctil, títulos largos adaptables y selector de edición accesible arriba. No mover filtros aprobados de Pokédex bajo la barra ni duplicarlos. |
| `RotomCard` / panel | Variantes de lectura/destacada/selección y formularios. Opacidad suficiente para texto, bordes por estado, tamaños de contenido flexibles. No obligar una card a envolver cada elemento de una tabla. |
| `RotomTabBar` | Apariencia y semántica de pestañas, indicador, estado seleccionado y desplazamiento. Sin acoplar datos de Team Builder al aprendizaje. Pager en P3 conectado a su propio estado. |
| `RotomFilterSheet` | Contenedor/cabecera/acciones/scroll/IME comunes; cada módulo conserva filtros y lógica propios. Fichas largas y alertas usan variantes accesibles, no fondo ilustrado repetido dentro del modal. |

**Tokens de partida, no constantes definitivas:** espacios 4/8/12/16/24 dp; esquinas 8/12/16 dp; cuerpo 16 sp, datos densos 14 sp, etiquetas auxiliares 12 sp y títulos 20–24 sp. Mantener nombres Pokédex 20 sp, artwork 72 dp y controles táctiles 48 dp como referencias ya aprobadas; no achicarlos para que quepa la decoración. Paleta conceptual de maqueta: marco normal `#B63C18`, lectura oscura `#18303C`, texto `#F3FBFF`, cyan `#8BDEF2`; claro `#F7FDFE`, texto `#142E3C`, acento `#075C75`. Se validarán pares reales compuestos y seis formas, no solo colores sueltos. Color de tipos/categorías separado de apariencia.

**Cara protegida y recursos:** `rotomfondo.png` existente mide 827×1477; la cara ocupa aproximadamente los primeros 322 px de alto. En retrato, reservar altura proporcional a ancho (~0,40×ancho útil), mantener cara por encima del scroll y situar el contenido en el área de pantalla ilustrada. Usar anclajes/constraints, no márgenes fijos por modelo. En landscape/tablet, variante lateral con cara íntegra y panel al costado; con IME o altura extrema, cabecera compacta propia sin aplastar/cortar el rostro. Estos dos casos requieren prototipo/prueba Android, no están resueltos por la maqueta de cuatro móviles. Se podrá extraer visualmente cabeza/marco del mismo recurso solo tras revisar calidad/procedencia y aprobar el resultado; **no modificar el asset original**. Es una imagen existente con firma gráfica: confirmar derechos antes de distribución de producción, no borrar firma ni asumir autoría/licencia por estar en Git.

#### 4.10.6 Navegación de movimientos — P3 propuesto, aún no implementado

1. Detalle Pokémon conserva el contenido aceptado; sustituir únicamente su sección embebida por un botón **«Movimientos»**. Sin título duplicado, expandir/ocultar ni lista de 560 dp dentro de la ficha.
2. Propuesta de ruta `pokemon_moves/{pokemonId}` en `MainActivity.kt`. Enviar **el ID de la variedad actualmente seleccionada en el detalle**, no el argumento inicial de la ruta ni speciesId: hoy cambiar forma modifica `selectedPokemonId` localmente. Resolver nombre/nacional/formas por repositorio existente, no por texto enviado desde UI.
3. Nueva pantalla con atrás, identidad y edición visible en zona superior; selector etiquetado/chevron o acción de barra con estado legible, sin icono misterioso como único indicio del juego. Mantener cuatro páginas principales **Nivel / Huevo / MT/MO / Tutor**, con `HorizontalPager` y tab row sincronizados por click/swipe. Listas verticales independientes, sin gesto horizontal obligatorio para acceder a una pestaña.
4. Estado de presentación compartido en ViewModel de esa entrada de navegación + SavedStateHandle: pokemonId validado, versionGroup, página, búsqueda, tipo, categoría y scroll por página. No guardar catálogos/respuestas completas en Bundle; cache/repositorios retienen datos. Volver de ficha mantiene contexto; Atrás retorna al mismo Pokémon/variedad; cambiar forma no arrastra scroll de otra identidad. Si no hay edición previa válida, solicitar selección, no mezclar todas ni elegir una histórica arbitraria. Cambio explícito de edición conserva filtros pero reinicia los scrolls de sus resultados; cambio de pestaña conserva su scroll.
5. Reutilizar `MoveRepository` y `ReferenceStore` de `data/reference/ReferenceRepositories.kt:43–87`, AppContainer y ficha `MoveReferenceSheet`. Extraer de `MoveLearnsetPane` el estado/filtrado/fila reutilizable: **no montar cuatro panes que cada uno llame learnset**. Una carga por pokemonId/actualización, luego particiones locales por método/edición. Swipes, filtros, tema y forma de Rotom no descargan catálogo ni aprendizaje. El picker del editor mantiene onSelect y sus cuatro slots, sin verse obligado a navegar a una ruta nueva.
6. `level-up`, `egg`, `machine`, `tutor` ocupan las cuatro páginas. Métodos adicionales (`light-ball-egg`, `form-change`, `zygarde-cube` y futuros) se muestran en un acceso **«Otros métodos»** fuera del paginador, solo si existen para la edición, con lista agrupada por método y misma ficha/navegación. No reasignarlos a Tutor/Huevo ni perderlos para mantener exactamente cuatro pestañas. Unknowns conservan identidad y etiqueta revisable, no se borran.
7. Filas: nombre español/fallback identificado, tipo con nombre/color, categoría con icono **propio** y descripción accesible Física/Especial/Estado, potencia y precisión, nivel cuando corresponde. Segunda línea en ancho reducido/texto grande, no seis columnas cortadas. Valores nulos «—», nunca cero ficticio. Nivel ordenado ascendente y desempate por nombre/ID; conservar varios niveles/métodos de un movimiento, no deduplicar relaciones. Sin límite arbitrario al total; lista lazy y paginación progresiva si hace falta.
8. Máquinas: `MachineLink` actual contiene itemId/moveId/versionGroup, **no número MT/MO**. Se puede enlazar al objeto del índice y mostrar su nombre verificado en esa edición cuando esté resuelto; nunca deducir «MTxxx» de itemId ni prometer resolverlo donde el modelo no lo permite. Si requiere nuevos datos o cambio de modelo, registrar la necesidad y separar investigación, no migrar Room por diseño.
9. La edición filtra aprendizaje y texto; potencia/precisión actuales no se transforman en historial por juego sin soporte factual. Límite explicado en documentación y pruebas; no volver a llenar el layout con descargos de proveedor. Ficha conserva PP/prioridad/efectos/enlaces. MOV-003/Z no entra automáticamente en esta aprobación visual: investigación y filtro general separado, sin nueva categoría de daño ni quinta pestaña.

#### 4.10.7 Maqueta para validar la dirección

Artefacto: [`docs/prototypes/rotom-identity.html`](prototypes/rotom-identity.html). HTML/CSS/JS independiente, **no implementación Android**, sin Room/repositorios/red. Referencia el `rotomfondo` local actual, no copia imágenes en cada pantalla ni incluye imágenes de PokeInfo. Cuatro vistas: **Inicio, Pokédex, Detalle Rowlet y Movimientos**. Controles globales Claro/Oscuro/Sistema y seis formas, tabs por click/teclado y gesto táctil de muestra, selector de edición ilustrativo, búsqueda en tres filas de ejemplo y diálogos explicativos. No hay movimientos reales cargados por edición, ni datos personales, ni simulación de cobertura factual; las limitaciones están escritas fuera de la UI conceptual. Los números reemplazan sprites en la maqueta, no una propuesta de quitarlos de la app. La apariencia elegida en el HTML no se persiste ni demuestra persistencia Android.

**Respuesta del usuario durante la revisión:** «se ve bien lo que hiciste de propuesta, me gusta mucho»; se acepta la **dirección estética** y se aclara que el proyecto es la carpeta, no un ZIP. No se infiere aprobación exhaustiva de accesibilidad, modo landscape, nuevas decisiones de datos o todos los hitos. Antes de programar, presentar el conjunto técnico/visual y confirmar el primer hito con este alcance.

#### 4.10.8 Implementación progresiva propuesta y criterios de salida

Esta secuencia vive aquí; no cambia la numeración de v3.6/v3.7 ni asigna RC/fecha por adelantado. Tras aprobación, entregar cortes verificables bajo §8.1; no una reescritura gigante.

| Hito propuesto | Alcance acotado | Puerta de aceptación |
|---|---|---|
| V0 — esta intervención | Auditoría, comparación, maqueta y dirección en el Plan Maestro. | Usuario revisa cuatro vistas, claro/oscuro/formas y propuesta técnica; ningún cambio Android. |
| V1 — base visual + piloto | Tokens/tema, preferencias de apariencia separadas de formas, fondo/scaffold/barra/panel reutilizables. Piloto en selector de mapas y Pokédex; un filtro representativo. | Cara íntegra, contraste y estado correcto en light/dark/system y seis formas; orden/filtros/scroll Pokédex preservados; actualización no destructiva. No liberar cambio global de tema con pantallas blancas sobre blanco: adaptador legacy explícito y temporal por destino no migrado, también sus modales. |
| V2 — detalle + P3 | Revestir detalle sin reordenar contenido aceptado; botón Movimientos, pantalla independiente y cuatro páginas; reutilizar pane/repositorio/ficha/editor. | Identidad y edición exactas; métodos adicionales accesibles; vuelta/restauración; cero peticiones por swipe; prueba del editor y datos guardados. |
| V3 — superficies enciclopédicas | Objetos/bolsillos, catálogo general de movimientos/habilidades y sus sheets/dialogs/pickers. | Mismo sistema, filtros y contadores íntegros; no inventario nuevo, Z ni otro repositorio. |
| V4 — extensión transversal | Inicio/login/registro, Team Builder/editor, Versus, calculadoras/tabla tipos, líderes y superficies del mapa/localizaciones. Revisar por módulo, no un único diff masivo. | Apariencia coherente incluyendo IME/drawers/modales; sin cambiar cálculos, autenticación, ownership, cartografía ni datos. |
| V5 — cierre visual | Retirar adaptadores legacy, revisar pantallas pendientes y manual/instrumentación disponible; notas/known issues. | Sin superficies hardcodeadas de modo incorrecto; revisión real de actualización y datos; aprobación explícita propia antes de estable. |

#### 4.10.9 Riesgos y verificaciones requeridas antes de cada entrega Android

- **Rendimiento:** bitmap 827×1477 ocupa ~4,7 MiB decodificado RGBA, aunque pese menos en disco. Reutilizar recurso/painter; nada de blur por fila, bitmaps recreados al recomponer, animaciones perpetuas o listas no lazy. Medir overdraw y fluidez en teléfono modesto; comparar carga/red antes/después, no prometer 60 fps desde HTML. Evitar recomponer consultas al cambiar tema; valores estables y scopes adecuados.
- **Compatibilidad:** mantener mínimo API 25/target actual, insets/status/navigation bars y teclado. Diseño portrait/landscape/tablet, sistema claro/oscuro y seis formas, nombres largos, tamaño de fuente 1×/1,5×/2×, 280/320/360 dp de ancho lógico. No forzar orientación ni ocultar acciones con título largo. Rotom visual no debe comerse la mitad del contenido útil en horizontal.
- **Accesibilidad:** texto normal ≥4,5:1 y controles/iconos significativos ≥3:1, medidos sobre composición real; foco visible, objetivos ≥48 dp, nombres TalkBack, orden de lectura y categoría/tipo no solo por color. Background decorativo sin anuncio. Pestañas seleccionadas y accesibles sin swipe; reducción de movimiento y no secuestro del scroll vertical. Pantallas densas ofrecen adaptación sin letra diminuta.
- **Persistencia:** claves de apariencia/forma independientes, default SYSTEM, cambios de sistema sin sobrescribir elección manual; reinicio y recreación de Activity/proceso. Room/schema/migraciones/sesiones sin cambios; snapshots e IDs intactos; equipos/favoritos por propietario, EV/IV y los cuatro textos de movimientos sin reescrituras. Edición/variedad/scroll no se comparten accidentalmente entre back stack entries ni usuarios.
- **Regresión funcional:** conservar las pruebas existentes de índices, filtros, Hisui, alias/contadores, navegación y actualización. Añadir UI tests con componentes de producción y repositorios fake para métodos múltiples, offline/error/retry, texto grande y restauración; contador de llamadas asegura no fetch por pestaña. Pruebas de tema/formas/prefs y checks de contraste; screenshots solo si no se confunden con comportamiento validado.
- **Distribución:** misma firma y paquete, versionCode creciente por APK futuro, checksum/notas/known issues, ambos CI y enlaces verificados. No retaguear RC10 ni subir claves. No declarar estable por aprobar una maqueta; no arreglar avisos Play Protect cambiando firma.
- **Recursos/derechos y evidencia:** recurso ilustrado existente no implica licencia de producción verificada. No copiar símbolos/mapas/arte de PokeInfo ni descargar APKs para extraerlos. Videos/capturas faltantes limitan la comparación, no autorizan inventar hechos. No enviar datos de cuentas a servicios externos para diseñar.

**Comprobación de la maqueta:** abierta en navegador local; 18 combinaciones de tres modos/seis formas ejercitadas, selección de tabs por click/teclado, búsqueda de Rowlet y apertura/cierre de diálogo correctas; sin overflow horizontal del documento a 800 px. Esto no verifica contraste completo, swipe en dispositivo, landscape ni persistencia Android. La captura automatizada del navegador no se pudo obtener por falta de ventana visible, aunque el DOM interactivo sí fue comprobado; no se adjunta una captura simulada como si fuera Android. `git diff --check` y 28 pruebas Python correctos en esta etapa; sin cambios Android ni nueva ejecución de Gradle. README enlaza la propuesta, no constituye otro plan.

**Estado final de esta etapa:** auditoría estática y propuesta documentadas; dirección estética recibida favorablemente. Android permanece en RC10, sin implementación de tema nuevo/P3 ni nueva compilación necesaria por documentación/maqueta. Validación de la maqueta web no equivale a validación Android. Siguiente acción: recoger la segunda observación anunciada por el usuario y cerrar aprobación del alcance V1 antes de programar.

### 4.11 V1 autorizado — base visual y piloto Pokédex / selector de mapas

**Autorización posterior a la propuesta:** el usuario aclara que la segunda observación era precisamente que le gustaba la propuesta y dice «empecemos con ello». Confirma además que las correcciones de RC10 estaban bien implementadas. Se aprueba ejecutar el rediseño progresivo de §4.10, sin pedir permiso por archivo, con pruebas y pre-releases compatibles bajo §8.1. No es aprobación estable ni autorización para RI, nuevos datos/mapas completos o reescritura masiva. La espera de aprobación registrada al entregar V0 es histórica y queda superada por esta respuesta.

**Primer corte V1:** rama `feature/v3.5-rotom-visual-foundation`, preparación `3.5.0-rc11` / `30511`. Base reutilizable y piloto real en **Pokédex, sus filtros y selector de mapas**, no todas las pantallas a la vez ni P3. El detalle y la página de movimientos se abordarán en V2, conservando lo aprobado en RC10. La aprobación de esta dirección cubre los hitos descritos; una ampliación factual o fuera de ellos sigue necesitando su propio alcance.

**Código y decisiones:**

- `util/RotomAppearanceManager.kt`: preferencia de dispositivo `AppearanceMode.SYSTEM/LIGHT/DARK`, default SYSTEM, StateFlow y SharedPreferences nuevos de presentación (`rotom_appearance_prefs/appearance_mode`). No usa Room/cuentas ni modifica la clave de forma `rotom_theme_prefs/rotom_form`. Valor desconocido vuelve a SYSTEM sin borrar otros datos.
- `ui/theme/Theme.kt` resuelve apariencia efectiva y forma de manera independiente. `RotomPalette.kt` centraliza roles y dimensiones: marco de dispositivo por forma, lectura clara/azul oscura, acentos contrastados, textos y bordes. Los tipos Pokémon y fórmulas no se alteran. Las seis formas conservan el mismo layout y su ilustración actual, sin recolorearla artificialmente.
- `ui/components/RotomScaffold.kt`: `RotomBackground`, `RotomTopBar`, `RotomScaffold`, `RotomCard` y `RotomFilterSheet`. Una imagen local por raíz, proporción del recurso intacta, zona de cara de 330/827 del ancho en retrato; contenido no se coloca encima. En horizontal se reserva columna lateral y la imagen conserva proporción. Insets consumidos y soporte IME; nada de blur/red/bitmaps por fila. No se cambia `rotomfondo.png` ni se generan recursos nuevos.
- `RotomAppearanceAction.kt`: ajuste accesible desde barras de los pilotos, inicio y antes de login. Selector Claro/Oscuro/Sistema con radios y estado, guardado inmediato; no reemplaza el selector existente de forma de Rotom. Login añade entrada de apariencia y scroll del formulario para evitar recorte, **no cambia su autenticación ni rediseña aún toda la pantalla**.
- Límite temporal explícito `RotompediaTheme(legacy = true)` en raíz de `MainActivity`: destinos aún no migrados conservan el esquema anterior para evitar texto blanco sobre superficies claras. `RotomScaffold` y diálogo de apariencia aplican el nuevo esquema dentro de su scope. No duplicar estados/ajustes para legacy; se retirará esta frontera al completar V4/V5. **Claro/Oscuro no transforma todavía todas las pantallas**, conocido y deliberado en este piloto.
- Pokédex conserva barra con filtro blanco a la derecha, búsqueda, Lista/Cuadrícula de tres columnas, artwork de 72 dp/nombres, nacional/regional, favoritos y estado de navegación. Se añade acción de apariencia separada; cards y panel/filtro usan roles comunes. El explorador compartido conserva su lógica y modelos; el contenedor de filtros respeta el tema ambiente también en el picker legacy.
- `MapFeatureScreen` reutiliza Scaffold/Card en vez de duplicar imagen/overlay; selección de las nueve regiones y ruta `map_viewer/{region}` intactas. El visor cartográfico, pines y encuentros no se modifican. La altura mínima de cards permite crecer el texto en vez de fijar 72 dp rígidos.
- Actions de Release/Verify ejecutarán la misma verificación con `--max-workers=1`, como en local, para reducir competencia entre tests Compose, lint y compilación en runners. No se eliminan pruebas ni se aumentan/omiten sus límites por ello; no se garantiza que eso elimine toda flakiness histórica.

**Pruebas focalizadas:** 22 correctas en `RotomVisualFoundationTest`, `PokedexNavigationUiTest` y `ClientFacingUiTest`. Ocho casos nuevos de base visual: preferencias/default/valor desconocido, reinicialización e independencia de forma, contraste de textos/acento ≥4,5:1 y borde ≥3:1 en lectura/elevada para seis formas×dos apariencias, blanco de barra, reacción a sistema/override manual, frontera legacy, proporción/cara fuera del panel a 280 dp y fuente 1,5×, cara lateral en horizontal y navegación real selector→Kanto al cambiar apariencia. Un caso adicional conserva exactamente filtro Kalos y posición Furfrou al cambiar apariencia. Se corrigió área real de acción a 48 dp, no se rebajó la prueba. Esto no acredita contraste de cada badge heredado, rendimiento físico ni reinicio real de proceso en teléfono.

**Verificación local terminada:** **149 Kotlin, cero fallos/errores/omitidos; 28 Python y tres índices correctos**. `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain --max-workers=1` correcto en 4m23s. Lint sin errores: 111 warnings/30 hints (un nuevo consejo UseKtx para edición de preferencias, consistente con el gestor anterior). `git diff --check` correcto; sin diff en datos/repositorios/schemas/assets. Instrumentación compilada, no ejecutada: `adb devices` vacío. Validación visual Android pendiente; la maqueta HTML no es evidencia de render Android.

**APK preparado RC11:** `RotomPedia-v3.5.0-rc11.apk`, **24023297 bytes**, SHA-256 `a141fda7446f087468db752a0ac057d845252b1cdf8f7b32430796b455e39edf`, con `.apk.sha256`. Paquete `com.duoc.rotompedia`, `3.5.0-rc11` / `30511 > 30510`, mínimo API 25. Build Tools 35.0.0 verifica certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, igual al APK RC10 cuyo hash se volvió a comprobar. Clave privada fuera de GitHub. Publicación pendiente de repetir controles en Actions, sin alterar releases anteriores ni promover a estable.

**Entrega pública RC11:** tag inmutable `v3.5.0-rc11` en `06cd73c`; [pre-release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc11), `draft=false`, `prerelease=true`, no estable/Latest. Ambos workflows **success**: [Release APK 42085791772, intento 2](https://github.com/ArukuKun/RotomPedia/actions/runs/42085791772) y [Verify 42085781411](https://github.com/ArukuKun/RotomPedia/actions/runs/42085781411). Primer intento Release: cuatro `AppNotIdleException` en las mismas pruebas ItemUiTest/PokedexNavigationUiTest que en RC10 y timeout de cinco minutos, pese a ejecución serial. Verify pasó con el mismo commit; reintento único del job de Release pasó sin cambiar fuente/tag/APK ni saltar controles. La flakiness sigue siendo deuda de infraestructura/pruebas, no se declara eliminada por `--max-workers=1`. Actions volvió a comprobar firma, paquete/versión y checksum antes de publicar. Digest público idéntico al hash anterior; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc11/RotomPedia-v3.5.0-rc11.apk) comprobado con HTTP HEAD **200**, 24023297 bytes y MIME APK. RC1–RC10 y sus assets/tags intactos; documentación posterior no altera el APK. Validación visual de V1 pendiente.

**Recorrido manual para V1:** actualizar sin desinstalar, abrir Pokédex y Mapas; alternar Claro/Oscuro/Sistema, ir y volver de ajustes/filtros/ficha y comprobar Kalos→Furfrou y listas. Cambiar forma desde inicio y verificar acentos, sin alterar apariencia. Cerrar/reabrir, rotar, teclado y fuente grande; comprobar datos de equipos/favoritos anteriores. Otras pantallas pueden seguir oscuras por frontera legacy y P3 aún usa su panel anterior. Sprites faltantes siguen para después. No promover a estable.

### 4.12 Corrección de rumbo tras RC11 — entrega visual completa, sin micro-RC

**Reporte real del usuario:** instaló RC11 y aporta captura del inicio prácticamente anterior, frente a la maqueta aprobada. Considera insuficiente cambiar solo Pokédex y rechaza cómo quedó Mapas. Pide cambios mayores: completar lo solicitado y después revisar mejoras por punto, no entregas de dos pantallas. Confirma «sigue» durante la implementación.

**Autorización y puerta actual:** rama `feature/v3.5-complete-rotom-interface`. Integrar **Inicio, Login/Registro, Pokédex/filtros, detalle, P3 completo, catálogos/fichas de movimientos/habilidades/objetos, Team Builder/editor, Versus, calculadoras/tabla de tipos, líderes y ventanas** con la base común y Claro/Oscuro/Sistema/formas. No publicar otra RC parcial ni pedir otra validación por cada módulo. Los pasos V2–V4 de §4.10 se usan como unidades internas, no como una publicación por paso. La entrega pública siguiente exige completar y verificar el conjunto; no se asigna tag/APK por código todavía parcial.

**Mapas:** el usuario quiere una reconstrucción interactiva posterior desde un diseño nuevo, **sin inventar dónde está cada cosa**. Se difiere expresamente; mantener accesos y datos actuales sin añadir puntos/cobertura ni presentar el mapa existente como validado. No rehacer ahora cartografía, coordenadas o encuentros. Recursos y relaciones previas se conservan para evaluación, no se borran por el desagrado visual. v3.6 sigue siendo el lugar para definir piloto, fuentes y precisión verificable.

**Implementación integrada preparada:** `3.5.0-rc12` / `30512`, rama `feature/v3.5-complete-rotom-interface`. `RotomHomeScreen.kt` sustituye el inicio real con accesos/iconos de todos los destinos anteriores, formas y consejos existentes; `MainActivity.kt` retira el menú duplicado y mantiene factories, sesión/biometría y navegación. `LoginScreen.kt`/`RegisterScreen.kt` comparten frame y formularios desplazables, conservando campos y acciones de autenticación. `RotomPageScaffold`/`RotomPageTopBar` cubren detalle, objetos, catálogos, equipos, Versus, calculadoras, tabla de tipos y líderes; `RotomColors` centraliza roles para tarjetas/editores/ventanas, con claro/oscuro efectivo en todo el conjunto migrado. Se conserva la preferencia y las seis formas de V1. La única frontera legacy queda explícitamente en el visor cartográfico diferido; no sigue envolviendo silenciosamente el inicio y todos los demás destinos.

**P3 implementado técnicamente:** `PokemonMovesViewModel.kt` con `SavedStateHandle`; `PokemonMovesRoute.kt` conecta el contenedor/repositorios existentes y resuelve nombre/nacional por ID; `PokemonMovesScreen.kt` implementa cuatro páginas con `HorizontalPager`, edición superior compacta, filtros y ficha contextual. Un único botón «Movimientos» sustituye la sección embebida; `PokemonDetailScreen` pasa el `pokemon.id` de la variedad actualmente cargada, no el argumento inicial. Una carga por entrada/actualización, nunca por swipe; filtrado local conserva niveles múltiples y métodos adicionales. «Otros métodos» permanece accesible mediante acción etiquetada de tres puntos, sin convertirlos en Tutor; referencias fuera del índice permanecen visibles en inglés y con ficha no disponible, no se descartan. MT/MO se vincula por objeto/movimiento/edición; no se deducen números de IDs. El picker/editor conserva su contrato y cuatro slots. `MoveReferenceSheet` recibe edición inicial y conserva estado al cerrar. Iconos de categoría Material + etiqueta, nombres/tipos, potencia/precisión, nivel y acceso a ficha; no copia símbolos privados de PokeInfo.

**Legibilidad y alcance factual:** el selector de edición P3 se compactó después de que una prueba a 320×470 detectara que los controles dejaban solo 7 px de lista; no se arregló rebajando el test ni reduciendo toda la letra. Filtros largos permiten scroll, barras tienen títulos elidibles y tipos conservan su paleta con tinta contrastada. Se elimina de la barra de captura la afirmación no comprobada «Fórmulas Oficiales Gen 8/9», sin cambiar su matemática ni presentarla como corregida. Las estadísticas actuales del catálogo no se convierten en valores históricos al seleccionar juego. El fondo original no se modifica ni se repite por fila. MainActivity mantiene los mismos repositorios/sesiones y callbacks de biometría; simplificar su código visual no autoriza cambiar las reglas de acceso.

**Protección:** mismo paquete/firma y política §8.1; preservación de usuarios, equipos, favoritos, EV/IV, cuatro movimientos, identidades y snapshots. Sprites pendientes no se arreglan ahora. No incorporar RI, nuevos entrenadores por edición, inventario personal o cambios matemáticos de captura. Diagnósticos de fuentes/caché siguen fuera de los layouts de cliente.

**Verificación local final:** **162 Kotlin, sin fallos/errores/omitidos; 31 Python y tres verificadores de snapshots correctos**. `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain --max-workers=1` correcto (última pasada 2m20s); lint sin errores, 111 warnings/29 hints. Se corrigió import duplicado de TextOverflow. El detector de lint de creación de corrutinas falló internamente con UParameter nulo al analizar el click anidado del pager; se extrajo esa acción a una función normal invocada por el click y se repitió lint/suite, **sin desactivar reglas ni omitir tareas**. `git diff --check` correcto. Sin diff en datos/repositorios/schemas/assets ni `MapScreen.kt`/`LocationDetailBottomSheet.kt`.

**Evidencias de pruebas:** `PokemonMovesUiTest` añade siete casos: botón único/variedad exacta, cuatro pestañas/swipe y ficha con misma edición usando una sola llamada, métodos adicionales/referencia desconocida, particiones por edición y filtros, restauración de parámetros de SavedStateHandle, recuperación por reintento y desplazamiento hasta resultado 65 sin tope de 40. `RotomInterfaceCoverageTest` añade seis: todos los destinos del inicio, contratos de login/registro y aviso de correo, frames reales de stats/captura/tipos en ambas apariencias, crear equipo en su diálogo, Versus/líderes. `tools/tests/test_rotom_visual_coverage.py` añade tres guardias **estructurales**, no validación visual: ruta de inicio nueva, legacy solo en visor diferido, raíces compartidas y ausencia de fondos neutrales oscuros fijos en superficies migradas. Se mantienen todas las regresiones previas, incluidas cuentas/Room/identidades, navegación/filtros, habilidades/Hisui y copia de cliente.

**APK publicado RC12:** [RotomPedia-v3.5.0-rc12.apk](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc12/RotomPedia-v3.5.0-rc12.apk), **24064405 bytes**, SHA-256 `08329440dd56a08ff7ec8bd81c58028144fc01b6ee0c461ea3ce51d84ca9a6fd`, con `.apk.sha256`. Paquete `com.duoc.rotompedia`, `3.5.0-rc12` / `30512 > 30511`, mínimo API 25. Certificado comprobado con Build Tools 35.0.0: SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, igual a RC11 cuyo hash se volvió a verificar. Clave privada fuera de GitHub. Actualizar sin desinstalar ni borrar datos; no estable/Latest ni reemplazo de RC11.

**Cierre de publicación verificado:** commit/tag `a3109167acf06d1c2699c49944599670e113ac49` / `v3.5.0-rc12`, rama `feature/v3.5-complete-rotom-interface`. [Release APK](https://github.com/ArukuKun/RotomPedia/actions/runs/42148685271) y [Verify RotomPedia](https://github.com/ArukuKun/RotomPedia/actions/runs/42148680638) terminaron `success`, ambos **intento 1** y sobre ese SHA exacto. [Release pública](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc12) comprobada con `isDraft=false`, `isPrerelease=true`; digest del asset idéntico al checksum local. HEAD del enlace directo devuelve **HTTP 200**, longitud `24064405` y MIME `application/vnd.android.package-archive`. APK y checksum publicados intactos; este cierre documental no mueve el tag ni reemplaza assets. La verificación técnica no implica aceptación visual del usuario.

**Puerta manual y límites:** no hay emulador/teléfono conectado; instrumentación compilada, no ejecutada. No afirmar que la maqueta HTML o los tests son capturas Android. La entrega integrada está publicada y verificada en CI, **no se presenta el resultado como aprobado visualmente por el usuario**. Revisar desde inicio real y formularios, claro/oscuro/sistema y formas, Pokémon→Movimientos→edición→pestañas/swipe→ficha→Atrás, filtros/scroll, equipos y favoritos previos, editores/calculadoras/líderes y texto grande/horizontal/teclado. Mapas, sprites faltantes y fases excluidas siguen fuera de esta entrega. Corregir después los puntos concretos de mejora que reporte el usuario; no volver al ritmo de una RC por dos pantallas.

### 4.13 Devolución del usuario sobre RC12 — aceptación general e ideas para la versión final

**Reporte posterior a la entrega:** el usuario confirma «Está todo bien», «se ven bien» y «lo que actualizaste ahora mismo está genial», y adjunta capturas de Pokédex, equipo y tabla de tipos. Se registra **aceptación visual general de lo actualizado**, no una matriz exhaustiva de pantallas/dispositivos ni certificación de ausencia de errores. Las capturas son evidencia aportada por el usuario; no se atribuye a todas la misma versión, tema o sesión sin comprobarlo. Los sprites sin cargar visibles no se diagnostican automáticamente como fallo de API, red o caché.

**Pendientes solicitados expresamente para la versión final, no para otra RC inmediata:**

1. **Funcionamiento sin internet — prioridad principal.** Actualmente hay índices locales y cachés parciales junto a consultas de API/recursos remotos; eso **no equivale a que toda la aplicación funcione offline**. Planificar cobertura real de datos, fichas e imágenes y dependencias de autenticación/backend, reutilizando repositorios e identidades. Antes de implementar, definir qué viene incluido y qué requiere descarga previa, actualización, espacio y comportamiento ante datos ausentes. Verificar inicio en modo avión, reapertura/proceso reiniciado, navegación y consulta sin red, conservación de equipos/favoritos y recuperación al reconectar. No prometer funcionamiento completo por haber visitado una ficha o tener un índice local. Este objetivo para la final actualiza la prioridad del backlog; no autoriza ahora descargas masivas ni otro catálogo paralelo.
2. **Tabla de tipos con encabezados fijos en ambos ejes.** Al bajar, mantener visible la fila superior de tipos defensivos; al desplazarse a la derecha, mantener visible la primera columna de tipos atacantes. Mantener esquina Ataque/Defensa, alineación y sincronización con las celdas para saber qué relación se consulta. No cambiar multiplicadores por este ajuste. Comprobar ambos desplazamientos combinados, temas y texto grande.
3. **Pokédex: continuidad entre Lista y Cuadrícula.** Cambiar de vista después de desplazarse no debe llevar al principio: conservar como ancla la identidad del Pokémon visible y convertir su posición entre fila y cuadrícula, sin confundir índice de fila con índice de entrada. Mantener búsqueda, filtros y orden; comprobar cambios en ambos sentidos, posición avanzada y retorno de detalle.

**Alcance de esta devolución:** registrar y priorizar en este único Plan Maestro, sin implementar todavía estas ideas ni generar APK/tag/pre-release/estable por el comentario. La versión final requiere su verificación y autorización de publicación estable independiente (§8.1). Mapas interactivos y sprites mantienen sus pendientes previos; no se convierten en tareas inmediatas por las capturas.

### 4.14 Devolución RC12 — idioma de habilidades y lectura visual de movimientos

**Reporte del usuario:** ya revisó la nueva versión y confirma el resto del resultado; señala que las funciones de habilidades aparecen en inglés. Para movimientos pide presentación más cercana a su captura de PokeInfo: tipo claramente visible, categoría diferenciada y más color para reconocer cada movimiento sin esfuerzo. **Filtros y resto de controles están bien:** conservarlos. La captura guía jerarquía visual, no autoriza copiar iconos/recursos privados ni usar sus cifras como fuente de datos.

**Diagnóstico comprobado en el snapshot actual:** 374 habilidades, **0 efectos extensos en español**, **267 con descripciones españolas por edición** y **107 sin descripción española**. La ficha arrancaba sin edición y omitía todas las descripciones localizadas, pasando directamente al efecto extenso inglés. Las 107 ausencias incluyen habilidades recientes y de juegos derivados; no todas son casos de la serie principal. El código de movimientos ya incluía el tipo, pero como texto discreto en el catálogo y badge pequeño en P3: el reporte se trata como fallo de legibilidad, no se rechaza por existir el campo.

**Corrección en código, aún no distribuida en APK:** la habilidad abre con la edición más reciente que tenga descripción española verificada, identificada en el selector; si no existe, usa la edición disponible sin inventar traducción. No se extrapola el texto de esa edición como una reconstrucción del efecto de otra. El inglés original y el efecto extenso quedan accesibles mediante «Consultar texto en inglés», sin perder información; cuando falta español se indica expresamente. **Esto recupera español disponible, no traduce los 107 faltantes ni convierte la descripción breve en efecto detallado traducido.** Completar esa cobertura exige otra revisión de fuentes/traducciones trazables; no declarar el problema de idioma totalmente resuelto por plegar el inglés.

**Presentación de movimientos:** `MoveTypeBadge` compartido y de lectura más amplia, con color propio del tipo, nombre español y tinta contrastada; categoría con icono y etiqueta más visibles, físico rojo, especial azul y estado gris. Se usa en catálogo, ficha, picker/editor a través de `MoveCard`, y P3/otros métodos. Conserva potencia, precisión, PP/prioridad donde existían, niveles/objetos y callbacks. Las etiquetas no dependen solo del color. Sin modificaciones de filtros, repositorios de aprendizaje, identidades, snapshots, Room, fórmulas, equipo o firma; no incorpora offline/scroll de §4.13 ni nuevos sprites.

**Verificación local terminada:** **166 Kotlin, cero fallos/errores/omitidos; 31 Python**, índice de referencias íntegro (937 movimientos/374 habilidades/2943 relaciones), lint sin errores (111 warnings/29 hints) y `testDebugUnitTest lintDebug assembleDebug --console=plain --max-workers=1` correcto. Cuatro nuevas regresiones verifican español inicial/edición explícita/inglés opcional, ausencia de español sin traducción inventada, tipo/categoría/estadísticas de tarjeta y selección de la última edición española sin tomar texto de otro juego. Se amplía P3 para comprobar tipo/categoría visibles. El test de contadores se adapta a la edición inicial nueva y distingue el elemento del menú del campo con la misma etiqueta; se repite la suite completa tras corregir esas expectativas, sin omitir casos. Sin revisión visual física de estos ajustes. RC12 pública/tag/APK permanecen intactos; código de trabajo con versión sin incrementar y compilación local no distribuible como actualización. No nueva pre-release hasta preparar expresamente una entrega verificable; traducción completa sigue abierta.

### 4.15 Encabezados fijos y continuidad Lista/Cuadrícula — autorizados tras RC12

**Instrucción vigente:** el usuario adelanta únicamente los encabezados de tabla y la continuidad del scroll de §4.13. **Offline queda para el final**, cuando se clasificará qué funciones requieren conexión y cuáles no; no iniciar ahora paquetes de descarga ni prometer cobertura offline global. Autoriza continuar después con v3.6. Esta instrucción sustituye el aplazamiento de esos dos ajustes, no el del modo sin internet.

**Balance real de las RC:** la interfaz integrada y P3 están publicados en RC12 y reciben aprobación general. El ajuste §4.14 está en `da49eee`, verificado con 166 Kotlin/31 Python, pero todavía no está en el APK público. Faltan descripciones españolas para 107 habilidades y efectos extensos traducidos; plegar el inglés no cierra su traducción. Sprites pendientes y validaciones específicas de dispositivo conservan sus límites; no se declara todo el backlog resuelto ni se publica estable por «sigue». Integrar §4.14 con estos dos ajustes en una siguiente pre-release verificable, sin reemplazar RC12.

**Implementación en curso:** `TypeChartContent` separa esquina, fila superior, columna izquierda y matriz en cuatro paneles, con offsets horizontal/vertical compartidos; encabezados y celdas mantienen ancho/alto y escalan juntos con tamaño de fuente. Misma matriz de multiplicadores, sin cambiar reglas de combate. `PokedexResults` conserva identidad del primer Pokémon visible entre vistas y convierte a la fila correspondiente de tres columnas; no traslada píxeles entre tarjetas de tamaños distintos. Al volver sin mover la cuadrícula conserva el ancla original, aunque no sea el primer elemento de su fila; si se desplaza, usa la nueva fila visible. Estados guardables y filtro/orden intactos; cambiar filtros continúa empezando en el primer resultado como antes. No afecta al navegador agrupado del picker de equipos.

**Verificación local terminada:** **172 Kotlin, sin fallos/errores/omitidos; 31 Python** y los tres verificadores de snapshots correctos. `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain --max-workers=1` correcto; última pasada 3m41s. Lint sin errores, 111 warnings/29 hints tras corregir orden del parámetro Modifier de la nueva celda. Se corrigió uso de `DpRect.width` inexistente en el test y se esperó a que la ruta/repo estuviera lista al volver de detalle, igual que las regresiones previas. No se omiten casos ni desactivan reglas. Tres nuevas regresiones de Pokédex cubren posición nacional avanzada y vuelta, cuadrícula desplazada/navegación a detalle y filtro/restauración; tres de tabla comprueban encabezados/esquina fijos, alineación en ambos ejes, gestos sobre matriz, restauración y fuente grande. Sin dispositivo conectado; instrumentación compilada, no ejecutada. Sin diff en snapshots, esquemas/repositorios o cartografía.

**Preparación de entrega RC13:** `3.5.0-rc13` / `30513`, rama `feature/v3.5-sticky-headers-scroll`. APK final **25013984 bytes**, SHA-256 **`cb68ca410f0196251565a973d62a102c6658537d33233dfd507716446df55383`**, con `.apk.sha256`. Paquete `com.duoc.rotompedia`, mínimo API 25, misma firma SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`; APK RC12 y hash revalidados antes de comparar. Se reemplazó únicamente el candidato local propio aún no publicado después de corregir el parámetro Modifier, comprobando su hash anterior; no se toca ninguna release/tag/asset histórico. Actualizar sobre RC12 sin desinstalar ni borrar datos. Notas incluyen §4.14, ambos ajustes, traducciones/sprites pendientes, offline al final y v3.6 solo en investigación. CI/publicación y revisión visual en teléfono pendientes, no estable.

**Publicación en proceso, todavía no entregada:** commit/tag `966dee0cbde42582c195cd7084ea3b8461e155df` / `v3.5.0-rc13`, push y borrador de pre-release con APK/checksum/notas creados. [Release APK](https://github.com/ArukuKun/RotomPedia/actions/runs/42156593562) y [Verify](https://github.com/ArukuKun/RotomPedia/actions/runs/42156589312) fallaron en intento 1: `AppNotIdleException` en ItemUiTest y tres casos previos de PokedexNavigationUiTest; tarea de tests agota límite de cinco minutos, 65 casos alcanzados (4 fallos/1 omitido por interrupción). No confundirlo con una suite CI completa aprobada. Borrador comprobado `isDraft=true`, `isPrerelease=true`; sin enlace de descarga público verificado. Se relanzan únicamente los jobs fallidos, **mismo SHA/tag/APK**, sin suprimir tests, aumentar límites ni cambiar código. Observación del intento 2 pasa a segundo plano a solicitud del usuario; resultado pendiente de notificación. Si persiste, investigar y mantener el borrador, no publicar manualmente eludiendo controles. RC12 continúa siendo la entrega pública vigente hasta comprobar éxito y assets públicos.

**Resultado posterior que sustituye el estado de borrador anterior:** Release APK **success, intento 2**, SHA exacto `966dee0cbde42582c195cd7084ea3b8461e155df`; ejecuta la suite completa, lint/build y verifica assets/firma/paquete/checksum antes de publicar automáticamente. **Verify independiente falla también en intento 2** con los mismos cuatro casos existentes y límite de cinco minutos; no se atribuye la causa raíz exacta al runner sin diagnóstico adicional. Ambos workflows usan el mismo comando Gradle serial/JDK17; no se cambiaron tests/configuración/tag/APK entre intentos. Intento de descargar reportes de Verify no encontró artefactos disponibles; el timeout no aporta informe HTML completo para atribuir una causa más específica. No se hizo una publicación manual saltando controles: el job de Release aprobó y publicó por su política existente, aunque no depende del otro workflow. Esa independencia es una limitación de la puerta de publicación a revisar, no evidencia de que Verify haya pasado.

**Entrega pública verificada RC13:** [release](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc13), `isDraft=false`, `isPrerelease=true`; [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc13/RotomPedia-v3.5.0-rc13.apk) HTTP HEAD **200**, **25013984 bytes**, MIME `application/vnd.android.package-archive`. Digest público `sha256:cb68ca410f0196251565a973d62a102c6658537d33233dfd507716446df55383`, igual al APK/checksum local preparado. Assets/tag intactos; actualización sobre RC12 sin desinstalar. Revisión física de RC13 y estabilización CI independientes quedan abiertas.

**Comprobación adicional en segundo plano:** el push documental `11cdc0b0bda27aa9a412d31d97ace449586fdf77` activa [Verify 42158459741](https://github.com/ArukuKun/RotomPedia/actions/runs/42158459741), todavía en ejecución; solo cambian README/Plan, no app/tools respecto al tag. Se observa en segundo plano, sin tercer relanzamiento ciego del job fallido ni retag/reemplazo de APK. Registrar su resultado al recibir notificación; un resultado posterior no borra los dos fallos históricos ni sustituye su diagnóstico.

**Resultado final de esa comprobación:** [Verify 42158459741](https://github.com/ArukuKun/RotomPedia/actions/runs/42158459741) terminó **success, intento 1**, sobre `11cdc0b0bda27aa9a412d31d97ace449586fdf77` (job 9m04s). Ejecutó Python, tres índices y la suite completa/SQLite/lint/APK/instrumentación compilada; artefactos subidos. Se volvió a comprobar `git diff <tag> <SHA> --exit-code -- RotomPedia tools`: sin cambios. El observador local previo perdió su proceso (`Shell.NotFoundError`), no detuvo Actions; se restableció seguimiento y se confirmó estado final directamente en GitHub. RC13 tiene Release aprobado en el tag y Verify posterior aprobado para **idéntico código de aplicación**, aunque el Verify original del tag conserva sus dos fallos. No retag, recompilación de entrega o sustitución de assets; revisión visual de RC13 y diagnóstico preventivo de estabilidad CI siguen pendientes. Este resultado sustituye el estado «en segundo plano» anterior.

### 4.16 Exclusión de contenido Colosseum/XD y filtro Hisui ampliado — autorizados

**Reporte/autorización:** el usuario detecta «Shadow» en la ficha Hielo Oscuro, pide quitar el contenido de Pokémon XD y propone incluir en Hisui las nuevas especies Wyrdeer→Enamorus que aparecen bajo generación VIII/Galar. Se atienden ambos puntos como continuación acotada de v3.5; no abre offline, cartografía ni todo el backlog. **No llamar «formas de Hisui» a especies nuevas.**

**Identificación y alcance no destructivo:** [Hielo oscuro (WikiDex)](https://www.wikidex.net/wiki/Hielo_oscuro) identifica Shadow Chill como movimiento exclusivo de Articuno oscuro en XD; [Movimiento oscuro](https://www.wikidex.net/wiki/Movimiento_oscuro) distingue esta mecánica. El snapshot fijado contiene **18 movimientos tipo `shadow`**, IDs 10001–10018, y grupos `colosseum`/`xd` (12/13). `GameContentScope` excluye esos movimientos, ediciones, textos/relaciones de máquina y aprendizaje de los catálogos/controles de cliente. `ReferenceStore` sigue validando y conservando el snapshot completo, sin cambiar IDs ni hashes; repositorios de referencia proyectan el alcance solicitado y registro legacy no ofrece Shadow como movimiento seleccionable. En P3/picker se sanea el contenido y filtros restaurados de XD/Shadow, sin otra petición por pestaña. **937 referencias originales, 919 movimientos visibles**; los datos de usuario/cachés no se borran ni se sustituyen movimientos guardados. Nombres legacy que el usuario hubiera guardado permanecen como texto no seleccionable para evitar pérdida silenciosa. Bola Sombra/Garra Umbría/Golpe Umbrío, habilidad Sombra Trampa y Calyrex Espectral no se eliminan por contener `shadow` en inglés: no son esta mecánica. Contenido de otros juegos derivados no se elimina por inferencia.

**Objetos:** auditoría del índice no encontró objetos exclusivos reconocibles de XD por sus identificadores (aromas, perfumes, fragmentos sol/luna, memorias Krane, CDs de combate, máquina cepo), por lo que no se inventa una exclusión por categorías generales. Las ediciones Colosseum/XD se excluyen también de elecciones de texto/precio y relaciones de portadores en fichas de objetos. No eliminar objetos normales reutilizados en esos juegos ni convertir su presencia en disponibilidad por edición certificada.

**Hisui — fuente exacta:** `pokemon_forms.csv` y `version_groups.csv` de la revisión ya fijada **`e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff`** contrastados por `introduced_in_version_group_id`. Formas/variedades: Wyrdeer 899/899, Kleavor 900/900, Ursaluna 901/901, Basculegion macho 902/902, Sneasler 903/903, Overqwil 904/904 y Enamorus Avatar 905/905; Basculegion hembra forma 10417/variedad 10248 y Enamorus Tótem forma 10418/variedad 10249: **`legends-arceus`**. Ursaluna Luna Carmesí forma 10441/variedad 10272 corresponde a **`the-teal-mask`**, queda fuera de esta asociación Hisui. Se cotejan IDs+slugs, no un rango nacional como única regla.

**Implementación:** asociación de origen adicional, sin cambiar `regionalOrigin` de una forma biológica, `VarietyCategory` o generación de especie. Filtro Hisui muestra **25 variedades**: 16 formas regionales anteriores + 9 variedades de las siete especies nuevas. Conserva orden del mismo Pokédex de Hisui y números nacionales, sin duplicar catálogo ni descargar fichas. Se combinan tipo/favoritos/grupo huevo/búsqueda y categorías; «Regionales» + Hisui admite también las nuevas especies como agrupación solicitada, rotuladas **«Especie de Hisui»** frente a **«Forma de Hisui»**. Generación VIII se presenta «Galar / Hisui», sin reasignar especies a otra generación; sección «Origen regional» y resumen «Hisui · formas y especies». Catálogo nacional sigue con 1025 especies; Galar/Alola y sus formas no se mezclan con Hisui. Fuente de origen no significa disponibilidad completa por edición.

**Verificación en curso:** regresiones de snapshot completo frente a proyección sin Shadow/XD, movimientos/habilidades normales cuyo nombre contiene Shadow, conservación de desconocidos/filas originales, filtros antiguos saneados y una sola carga de P3; nuevas especies/variedades de Hisui, exclusión Luna Carmesí, generación/identidad/orden, filtros combinados y recorrido Wyrdeer→cuadrícula→ficha→Atrás. Se actualizan las expectativas de 16→25 y 937→919 solo donde cambió el alcance de cliente; no se reduce la integridad de snapshots. Publicación/revisión física aún pendientes.

**Preparación RC14:** `3.5.0-rc14` / `30514`, rama `feature/v3.5-main-games-hisui`. Pruebas focalizadas de explorador/navegación/repositorios/referencias/P3 correctas (1m40s), 31 Python y tres índices íntegros. La validación de identidad de aprendizaje usa el **índice original completo**, no la proyección sin Shadow; se añade regresión para impedir que un ID oculto se reasigne y contamine caché. Suite completa/lint/APK/instrumentación en ejecución en segundo plano; no declarar aún aprobación ni publicación. RC13 pública/tag/assets intactos; misma política de firma/compatibilidad y pre-release, nunca estable automática.

**Verificación local final:** **179 Kotlin, cero fallos/errores/omitidos; 31 Python**, tres verificadores de snapshots correctos. Última pasada serial `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain --max-workers=1` correcta en **3m30s**, después de incorporar la validación de IDs ocultos y su regresión. Lint sin errores, 111 warnings/29 hints. Sin cambios en schemas/assets/cartografía ni datos personales; instrumentación compilada, no ejecutada (`adb devices` vacío). No se confunde con aprobación visual en teléfono. Siete casos nuevos respecto a RC13: dos de Hisui/datos/filtros, navegación UI de Wyrdeer, tres de referencia/alcance/identidad oculta y uno de filtros guardados/aprendizaje P3. Las regresiones de encabezados/scroll anteriores siguen pasando.

**APK preparado RC14:** **24189633 bytes**, SHA-256 **`d601eb1b2982b72cdecbd3960c2a510b4cf78473fa320a492ab9732cf96f2328`**, con `.apk.sha256`. Paquete `com.duoc.rotompedia`, versión `3.5.0-rc14` / `30514 > 30513`, mínimo API 25. Certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa` verificado con Build Tools 35.0.0; se vuelve a comprobar el hash/firma del APK RC13. Misma clave local, no subida a GitHub. Notas especifican exclusión de cliente no destructiva, Hisui/25 variedades, movimientos normales preservados, traducciones/sprites pendientes, offline al final y mapas sin cambiar. CI/publicación pendientes; tag/assets históricos no se reemplazan.

**Cierre de entrega pública RC14 (sustituye la preparación anterior):** commit/tag **`88c94e60208782720a7223477ad4debcfb9c167c` / `v3.5.0-rc14`**. [Release APK 42161749576](https://github.com/ArukuKun/RotomPedia/actions/runs/42161749576) y [Verify 42161746379](https://github.com/ArukuKun/RotomPedia/actions/runs/42161746379) **success, ambos intento 1**, mismo SHA exacto. [Pre-release pública](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc14) comprobada `isDraft=false`, `isPrerelease=true`; asset digest idéntico al SHA-256 preparado. [APK directo](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc14/RotomPedia-v3.5.0-rc14.apk) verificado mediante HTTP HEAD **200**, longitud **24189633**, MIME `application/vnd.android.package-archive`. No se reemplazan APK/tag/firmas previos; cierre documental no mueve tag ni recompila entrega. Instalar sobre RC13 sin desinstalar. Validación física de Shadow ausente/Hisui y datos previos sigue pendiente; tests/CI no equivalen a aceptación del usuario.

**Aprobación posterior de RC14:** el usuario confirma «ok todo perfecto». Se registra aceptación general de las correcciones Shadow/XD e Hisui, sin inferir una matriz exhaustiva de dispositivos/recorridos ni autorización para publicar estable. No reabrir esos ajustes como incidencias pendientes. v3.6-A conserva su continuidad autorizada; offline global permanece para el final y traducciones/sprites mantienen sus pendientes.

## 5. v3.6 — Mapas interactivos inspirados en PokeInfo (PILOTO DE DATOS PUBLICADO; CARTOGRAFÍA EN DESARROLLO)

**Objetivo:** transformar los mapas actuales, que tienen algunos puntos interactivos, en una herramienta útil para explorar regiones, rutas, cuevas, ciudades y encuentros por juego.

**Continuación autorizada tras RC12:** después de §4.15, el usuario permite avanzar a v3.6 «cuando puedas». Se inicia auditoría/fuentes de A, no se presenta todavía un mapa reconstruido ni se mezclan cambios cartográficos en la siguiente RC de v3.5. Offline global sigue expresamente para el final.

**Auditoría inicial comprobada (6 de noviembre de 2026):** `MapScreen.kt` ofrece nueve regiones y carga nueve imágenes (`kantomap`/`map_*`), pero `RegionLocationData.kt` solo contiene **36 ubicaciones**: Kanto 21, Johto 6, Hoenn 4, Sinnoh 3 y Paldea 2, con **116 filas de encuentros**. Alola, Galar, Kalos y Teselia no tienen ubicaciones en ese catálogo. `RouteLocation` mezcla coordenadas relativas, textos/objetos y encuentros; `WildEncounter` no incluye versión, condiciones ni referencia de fuente. Por tanto, sus porcentajes/niveles y coordenadas son datos históricos **sin verificación suficiente**, no cobertura certificada. No borrarlos ni migrarlos como hechos actuales. El visor ya transforma imagen y pines en un mismo contenedor, pero falta verificar límites de pan, hitboxes, restauración, selección de edición y correspondencia real con cada imagen. La hoja presenta vacío como «zona urbana protegida», afirmación no deducible de la ausencia de datos: corregir ese contrato al reconstruir, no darlo por cierto.

**Recursos:** Kanto PNG 200×176 (`b617aa8883aea891fb056cb584a88c0109c0ac1e7f232a73aa50669bacfc3b28`), Hoenn PNG 260×171 (`cdd83dd4615c71658bdf990e98ff2b858136893bead639006070cb67823537ba`); incorporados en el commit histórico `2590bf5`. Tener la imagen local no verifica edición, precisión, autoría o permiso. No se ha localizado licencia/procedencia en README para esos mapas. Ningún recurso nuevo descargado o añadido a la app por esta investigación.

**Fuentes revisadas y candidato, no elección cerrada:** [documentación PokeAPI](https://pokeapi.co/docs/v2#locations-section) distingue `location`/`location-area`, encuentros por **versión individual**, método, niveles, probabilidad y condiciones; no proporciona por ese contrato una cartografía gráfica completa ni objetos colocados en cada zona. No sumar probabilidades de condiciones/versiones incompatibles ni confundir `max_chance` con un slot. [pret/pokeemerald](https://github.com/pret/pokeemerald/tree/731ad5bfd6e6f265508d0efcca0ba42f9dcf5881), revisión `731ad5bfd6e6f265508d0efcca0ba42f9dcf5881`, contiene `src/data/region_map/region_map_sections.json` con coordenadas y dimensiones de secciones, y `region_map_layout.h`; su README identifica una decompilación de Esmeralda. **Hoenn/Esmeralda es candidato para evaluar**, no extrapolar a Rubí/Zafiro/ORAS ni copiar gráficos/código bajo una licencia no comprobada. El primer URL probado `region_map_entries.h` devolvió 404; se localizó la ruta real mediante el árbol, no se afirmó leer el archivo inexistente. Fuente consultable no equivale a permiso de redistribución: revisión de derechos, enlace de IDs, cobertura de encuentros/objetos y comparación con Kanto siguen pendientes antes de escoger piloto. Las referencias visuales ya observadas del PokeInfo correcto siguen en §4.10; no se atribuyen nuevas interacciones a vídeos no vistos.

**Siguiente paso de A:** comparar candidatos Kanto/ediciones concretas frente a Hoenn/Esmeralda por precisión y derechos de cartografía, cobertura real de áreas/encuentros/objetos y coste; definir un manifiesto con revisión/hash/fuentes, IDs y transformaciones verificables. Sin fuente o permiso suficiente, dejar ese recurso/campo fuera del piloto en vez de inventarlo. La selección región/juego y demostración visual se cerrarán con evidencia, no por contar con más pines antiguos.

**Comparación inicial de candidatos, no cobertura regional exhaustiva:** consultas HTTP 200 a `location/hoenn-route-101` (**449**) → `location-area/393` y `location/kanto-route-1` (**88**) → `location-area/295`. Hoenn 101 contiene relaciones para Rubí, Zafiro, Esmeralda, Rubí Omega y Zafiro Alfa; Kanto 1 contiene ediciones de varias generaciones, incluidos Rojo Fuego/Verde Hoja. Esto confirma que hay que filtrar **edición individual** y no unir encuentros por nombre de región. Hoenn 101 también contiene Treecko/Esmeralda con método **`gift`** y probabilidad 100: no interpretarlo como encuentro salvaje garantizado en hierba; conservar método y condiciones, y distinguir regalos/eventos/encuentros. `pret/pokefirered`, revisión `ce6406d8c2eaf46d543c8a2606de0c3cb1951982`, ofrece `src/data/region_map/region_map_layout_kanto.h` y `region_map_sections.json`; el JSON incluye IDs de Hoenn sin geometría, por lo que ni siquiera todos sus registros representan áreas navegables de Kanto. No importar secciones a ciegas. Ningún árbol consultado identifica licencia general en raíz en la búsqueda de `LICENSE`/`COPYING`: no equivale a una conclusión legal sobre todo el repositorio; permiso de recursos y derivación de cartografía siguen sin verificar. Ambas candidaturas tienen fuentes técnicas para investigar; ninguna tiene aún aprobado el paquete cartográfico redistribuible.

### 5.1 Ejecución v3.6-A tras aprobar RC14 — datos y geometría internos

**Autorización vigente:** tras «ok todo perfecto» sobre RC14, el usuario confirma «dale» a continuar v3.6. Rama `feature/v3.6-verified-map-pilot`. Se inicia implementación del piloto, **sin otra publicación parcial del mapa**. RC14 sigue siendo la entrega pública; no se mueve su tag ni se reemplaza el APK. Offline global permanece para el final y no se amplía RI, inventario o entrenadores.

**Decisión de piloto tras comparar datos:** **Hoenn / Esmeralda**, región 3, versión individual 9, grupo `emerald` (6). No Rubí/Zafiro/ORAS agregados. Se elige una sola edición con volumen menor de registros/condiciones que los candidatos Kanto y posibilidad de cotejar la geometría propia de Esmeralda; no por los cuatro pines antiguos ni por disponer de una imagen sin procedencia. Fuentes CSV de **`e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff`**, la misma revisión del índice nacional actual, descargadas con caché y hashes, sin scraping por ficha.

| Candidato | Ubicaciones declaradas en la región (todas las ediciones) | Áreas declaradas en la región | Áreas con filas de la edición elegida | Filas de encuentro | Variedades con IDs conocidos |
|---|---:|---:|---:|---:|---:|
| Hoenn / Esmeralda | 110 | 171 | 115 | 1644 | 155 |
| Kanto / Rojo Fuego | 96 | 159 | 139 | 2117 | 140 |
| Kanto / Verde Hoja | 96 | 159 | 139 | 2117 | 140 |

**Qué significan esos números:** son inventario de la fuente, **no porcentaje de cobertura geográfica ni prueba de que todas las áreas declaradas existan en la edición elegida**. Hoenn tiene 79 ubicaciones respaldadas por esas filas de Esmeralda. Las otras declaraciones pueden pertenecer a otros juegos o carecer de datos; no etiquetar vacíos como «zona urbana protegida» ni «sin Pokémon». Objetos por zona y cartografía siguen explícitamente **sin fuente aprobada**, no listas vacías que se presenten como completas. Hay 93/110 nombres de ubicación españoles, ningún nombre de subárea español en esas 171 declaraciones; no inventar traducciones de pisos/áreas. Nueve valores de condiciones y 272 tasas de método se conservan; dos textos ingleses de TV contienen caracteres de sustitución en el propio upstream y requieren revisión de presentación, no una reparación silenciosa de los hechos/hashes.

**Ámbito de cliente RC14 aplicado también al piloto:** la fuente de Esmeralda incluye dos filas de distribución `colosseum-bonus-disc-jpn` (encuentros 70694/70697, área 1210). `GameContentScope.allowsEncounterMethod` las oculta sin borrar el candidato auditado ni tocar Pokémon normales. La proyección utilizable queda en **1642 filas / 114 áreas / 154 variedades**; se guardan contadores de fuente y de cliente por separado. Esto evita reintroducir contenido de Colosseum por la puerta de encuentros de otro juego. No sumar regalos, intercambios, estáticos o distribuciones como encuentros salvajes normales.

**Generador reproducible:** `tools/generate_location_index.py` produce solo un candidato con `--output` explícito, **rechaza escribir en assets de la app**, usa red únicamente con `--download`, fija revisión y valida caché con manifiesto/hash por archivo. No sobrescribe una fuente cacheada incompatible ni relabela cachés de otra revisión. `--compare` reproduce la tabla; `--verify-only` funciona sin red. Se corrige la lectura de `contentSha256` del envelope nacional (no el nombre usado por otros índices), con regresión CLI. Candidato generado en `RotomPedia/app/src/test/resources/hoenn-emerald-location-candidate.json`, **330940 bytes**, hash del payload **`02b005f95763388ee6e6555d8c0df272127406bfbc1aa7793e3ac41b3b7b1219`**. Es recurso de pruebas, **no se instala ni se carga en navegación**. No se modifica ningún snapshot de producción ni se crea otro catálogo de Pokémon: solo referencias a los IDs existentes y al hash del índice nacional.

**Fuente/atribución:** manifiesto de 17 CSV: `regions`, `versions`, `version_groups`, `version_group_regions`, `locations`, `location_names`, `location_areas`, `location_area_prose`, `encounters`, `encounter_slots`, `encounter_methods`, `encounter_method_prose`, `encounter_conditions`, `encounter_condition_values`, `encounter_condition_value_prose`, `encounter_condition_value_map` y `location_area_encounter_rates`. URLs `https://raw.githubusercontent.com/PokeAPI/pokeapi/<revisión>/data/v2/csv/<archivo>.csv`; cada SHA-256 está en el candidato. [LICENSE.md de la revisión](https://github.com/PokeAPI/pokeapi/blob/e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff/LICENSE.md) declara BSD de tres cláusulas y marcas Pokémon; se conserva aviso completo en `tools/licenses/PokeAPI-BSD-3-Clause.txt` y en la procedencia del candidato. Esto **no licencia automáticamente imágenes de juegos o gráficos de otra app**.

**Modelos/lector:** `data/maps/LocationPilot.kt` y `LocationPilotStore.kt` separan ubicaciones, subáreas, edición, método, slots, niveles, condiciones, probabilidades y tasa de aparición del método. Se valida envelope/hash/revisión/atribución, vínculo al hash nacional, IDs conocidos, unicidad, relaciones y cobertura antes de exponer un catálogo; carga compartida con Mutex sin API por pestaña. `AVAILABLE`, `NO_SOURCE_ROWS` y `NOT_IN_PACKAGE` distinguen datos disponibles de falta de fuente/edición no cubierta. No se suman rarezas de slots/condiciones ni se confunde `rate` del método con la probabilidad de una especie. Ejemplo comprobado: Ruta 101/área 393 incluye hierba y tres elecciones iniciales con método `gift`, no tres encuentros salvajes al 100 %.

**Geometría interna:** `ui/maps/MapViewport.kt` usa una sola transformación en píxeles para imagen, puntos y hit-testing inverso: ajuste con márgenes, zoom 1–5, pan limitado por eje, foco de zoom y límites normalizados finitos. Pruebas usan rectángulos **sintéticos claramente identificados**, no coordenadas de ciudades inventadas. No se sustituye todavía el visor existente ni se afirma que su zoom esté corregido por tener esta clase nueva.

**Nuevo candidato gráfico localizado:** [File:Hoenn Map.png, Wikimedia Commons](https://commons.wikimedia.org/w/index.php?title=File:Hoenn_Map.png&oldid=883826344), recreación de **Ztash**, 28/12/2023, 1507×990; la página declara obra propia y **CC0 1.0** y la describe como Hoenn en Esmeralda. La versión vigente figura a las 20:45 y su historial declara haber quitado una ruta falsa de la primera versión: **no usar la versión anterior ni inferir precisión por la licencia**. La página/atribución/licencia se pudieron leer; la descarga directa de `upload.wikimedia.org/wikipedia/commons/a/a4/Hoenn_Map.png` devolvió **HTTP 429**. No se insistió, no se sorteó el límite, no se obtuvo un hash de imagen ni se incorporó archivo alguno. Falta inspección visual, correspondencia con geometría/áreas y evaluación de legibilidad/idioma antes de aprobarlo como recurso del piloto. No se presenta como imagen ya auditada ni se copia PokeInfo.

**Verificación en curso:** 40 Python correctas (incluidas nueve nuevas de generación/CLI/procedencia/relaciones/candidato real). Primera pasada Kotlin focalizada de lector/geometría correcta; la suite ampliada detectó una expectativa que todavía confundía las 115 áreas de fuente con las 114 de cliente tras excluir Colosseum: se corrige comprobando ambos valores, no retirando la exclusión. Se repite suite completa/lint/APK/instrumentación. Se añaden diez pruebas Kotlin (cuatro de lector/candidato real y seis de geometría). CI admite ahora `feature/v3.6-*` con los mismos controles, sin publicar por un push de rama. **Sin nuevo tag/APK ni cambios de firma/Room/datos personales.** Revisión visual y piloto navegable siguen pendientes.

**Siguiente unidad interna:** verificar el recurso gráfico/licencia declarada y ajustar una geometría con fuente y hash; enlazar explícitamente zona/subárea/edición, validar hitboxes y solo después integrar selector/visor/ficha reales con el frame Rotom y el repositorio compartido. Revisar traducciones de métodos/condiciones y cobertura de objetos por separado. La puerta para otra pre-release es un piloto utilizable y coherente, no solo este generador o dos pantallas nuevas.

### 5.2 Continuación local de v3.6-A/B — piloto consultable, sin georreferenciación

**Estado en el ZIP de trabajo (7 de noviembre de 2026):** avance implementado en copia local; **no se ha compilado Android ni publicado una nueva pre-release**. No es cierre global de v3.6. El último tag público sigue siendo `v3.5.0-rc14` según el estado base del ZIP.

- [x] Se incorpora el candidato auditado y firmado por hash `hoenn-emerald-location-pilot.json` como asset de lectura, sin cambiar el índice Pokémon ni datos personales. El `LocationPilotStore` valida integridad, revisión de los 17 CSV y los IDs de variedades contra el índice nacional incluido. Se mantienen **1644 filas fuente, 1642 admitidas, 114 áreas con registros utilizables**; los dos registros de distribución de Colosseum continúan excluidos del cliente.
- [x] `AppContainer` ofrece el catálogo de mapas de Hoenn/Esmeralda sin descargar datos al buscar. `MapViewerScreen` se reemplaza **solo para Hoenn** por un explorador consultable de rutas, ciudades y subáreas: búsqueda, filtro de zonas con registros, detalle por método, niveles, pesos de slot, condiciones, frecuencia de método y navegación a la variedad exacta de Pokémon. Número nacional visible y ID técnico separado.
- [x] La imagen `map_hoenn` preexistente solo se presenta como **referencia histórica no georreferenciada**; admite zoom/pan acotados con `MapViewport`, pero no incorpora marcadores falsos ni afirma cartografía validada. Las pantallas legadas pasan a identificar sus puntos como históricos sin verificar, y ausencia de filas no se confunde con zonas urbanas protegidas.
- [x] Se corrigió una prueba Python que usaba `root` fuera de su ámbito; **41 pruebas Python** del proyecto pasan. El snapshot instalado se valida en modo `--verify-only`; se añadió prueba Kotlin de integración del asset y numeración.
- [ ] **Compilación y ejecución Android:** no verificadas en este entorno porque carece de Android SDK y de distribución Gradle utilizable. Compilar con `testDebugUnitTest lintDebug assembleDebug` en el entorno de OpenCode o Android Studio y revisar el resultado en teléfono.
- [ ] **Cartografía verificable:** investigar permisos y correspondencia real entre imagen, áreas y coordenadas antes de activar pines; el PNG antiguo no tiene procedencia aprobada. Es obligatorio un mapeo verificable para cerrar la navegación espacial de v3.6-B.
- [ ] **v3.6-C:** ampliar regiones y ediciones solo mediante paquetes y cartografía verificables, probar accesibilidad, geometría y estabilidad en móvil, y conservar el requisito de offline completo para el final. No atribuir objetos a zonas sin fuente.

**Próxima puerta:** validar compilación/UX del explorador de Esmeralda y resolver la cartografía de un piloto con trazabilidad. No presentar esta copia como v3.6 estable o completa, ni publicar sin revisión de su alcance.

**Extensión local del piloto (7 nov 2026, aún sin publicación):** navegación por catálogo Esmeralda con buscador de zonas **y Pokémon por nombre/número nacional**, chips de métodos (hierba/cueva, agua, pesca, otros), contadores por variedad y acceso a la ficha exacta. `HoennAreaSearch` consume únicamente `LocationCatalogue.pilot`, ya filtrado por `GameContentScope`; evita otra API o catálogo de Pokémon. Nueva comprobación de asset empaquetado idéntico al candidato auditado, con exclusión de Colosseum y verificador en ambos workflows CI. Los pines antiguos de otras regiones se ocultan por defecto para no insinuar posiciones validadas. **42 tests Python verificados, `HoennAreaSearch` compilado aisladamente**, sin equivaler a la suite Android. **No certifica georreferenciación, legalidad cartográfica, build Android ni pre-release; faltan pruebas CI y validación en teléfono.**

### 5.3 Avance local — plano de secciones Hoenn/Esmeralda (7 de noviembre de 2026)

**Estado: implementado en copia local; no publicada ni compilada como APK Android.** Continúa v3.6-A/B sin dar por terminada toda la v3.6. Se preserva `v3.5.0-rc14`, la misma versión Android/Room y la política de firma. **No sustituye una cartografía artística autorizada.**

- [x] `HoennSections.kt` identifica **68 secciones** mediante sus coordenadas de cuadrícula del mapa de Esmeralda (rutas, ciudades y puntos de interés) auditadas con [`pret/pokeemerald` fijado al commit `0d3100185e0...`](https://github.com/pret/pokeemerald/blob/0d3100185e0b13faabfc589fc402dd46f83c1d6a/src/data/region_map/region_map_sections.json). Todas las 68 enlazan con slugs de ubicaciones reales del paquete Esmeralda. Son coordenadas de **secciones dentro del juego**, NO GPS, ni posiciones inferidas de la ilustración histórica ni coordenadas de un mapa de otra edición.
- [x] `HoennSectionMap.kt`: plano esquemático interactivo propio en Compose, ajustable, con zoom/pan táctil y botones +/-/centrar, y selección por intersección; en zonas solapadas se prioriza la sección específica. No se reutiliza arte de terceras partes ni se afirman encuentros por la sola presencia de una sección.
- [x] Al tocar un punto vinculado se abre la subárea directamente o un selector de subáreas si el lugar tiene varias. El detalle enlaza Pokémon por ID técnico exacto. Filtro por método se conserva dentro del detalle, no solo en los contadores externos. La ilustración antigua queda explícitamente opcional, **sin pines históricos**.
- [x] `HoennSectionsTest.kt` y `tools/tests/test_hoenn_sections.py` comprueban geometría, vínculo con fuente existente, casos de ruta y ciudad y elección de sección solapada. **43 pruebas Python** correctas y fuente Kotlin de geometría compilada de forma independiente con `kotlinc`; `git diff --check` sin errores.
- [ ] **Validación Kotlin/Android completa:** ejecutar `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` con SDK/Gradle compatible; no afirmar que los tests Compose ni el proyecto completo pasaron en este entorno. Pruebas instrumentadas en móvil y rendimiento/zoom sobre dispositivos pendientes.
- [ ] **Cartografía final:** revisar procedencia y licencias de cualquier mapa ilustrado definitivo, relacionar sus zonas con el sistema validado, y contrastar manualmente la selección por juego. Continuar cobertura verificada de regiones/ediciones para v3.6-C, sin incluir objetos ni offline global antes de sus propias fases.
- [ ] **Distribución:** no crear nueva release/etiqueta hasta contar con APK nueva compilada y firmada con certificado compatible, verificaciones CI y revisión de los permisos del recurso de mapa. No reutilizar APK anterior como si incorporara estos cambios.

### 5.4 Avance local de usabilidad del plano (7 de noviembre de 2026)

**Estado: código modificado en la rama local; NO equivale a APK verificada ni a release publicada.** Continúa la misma v3.6-A/B, sin introducir nuevas fases o repositorios y sin cambiar el catálogo Hoenn/Esmeralda, Room, firma ni versión Android.

- [x] **Mapa ampliado:** `VerifiedHoennMapScreen` abre un visor grande en `Dialog`, adecuado para tocar secciones diminutas en teléfonos; cerrar vuelve al explorador sin perder consulta/filtros. Al escoger una sección el diálogo se cierra antes de abrir el selector de áreas o ficha.
- [x] **Centrado sobre zona real:** `MapViewport.focusOn(bounds)` lleva la sección a la zona visible con zoom mínimo, pan acotado y respeto a las bandas vacías de la imagen. `HoennSectionMap` permite «Centrar zona», recupera su zoom/pan mediante `rememberSaveable` y sigue disponiendo de botones de zoom/restablecer.
- [x] **Filtros visibles sobre el plano:** las secciones que contienen áreas coincidentes con búsqueda/método se destacan; las no coincidentes se atenúan, sin eliminar su geometría ni fingir que carecen de Pokémon. Seleccionar una ubicación con una sola subárea abre su ficha y limpia la selección modal anterior.
- [x] **Lectura y accesibilidad:** controles de mapa distribuidos para pantalla estrecha y descripción del plano para servicios de accesibilidad; la lista textual de áreas continúa como ruta alternativa sin depender de gestos sobre celdas pequeñas.
- [x] **Verificación que SÍ se ejecutó:** 43 pruebas Python correctas, `git diff --check` correcto, `MapViewport.kt` compilado con `kotlinc` y pruebas de ejecución aisladas de foco/inversión/tap en orientación vertical y horizontal. Dos tests JUnit adicionales en `MapViewportTest.kt` quedan escritos, pero sin ejecutar en este entorno.
- [ ] **Puerta Android:** pasar `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` y probar visualmente en teléfono, especialmente vista ampliada, cambio de filtros, selección, mapa en orientación apaisada, texto grande y retorno. No se certifica compilación del Composable ni se publica una nueva RC sin APK nueva con firma compatible.
- [ ] **Cartografía/cobertura v3.6-C:** mapa artístico definitivo con derechos y correspondencia verificados, expansión por región/edición y revisión de objetos localizada solo con fuente. Se mantiene offline global para el final.

**Distribución:** el Git local no puede resolver `github.com` en este entorno, por lo que no se realizó push, tag ni pre-release; esto no altera la política §8.1 para entregas autorizadas. `RotomPedia/app/rotompedia_key` aparece rastreado en el historial de origen: excluir esa clave y todo `.git` de copias de entrega y revisar su exposición con el propietario antes de nuevas publicaciones. Nunca insertar la clave en APK/logs ni cambiar la firma sin autorización.

### 5.5 Continuidad local — búsquedas de Pokémon coherentes con el detalle (7 de noviembre de 2026)

**Alcance:** mejora de usabilidad del mismo piloto de Hoenn/Esmeralda; no cambia datos de encuentros, mapa de secciones ni versión Android. Se mantiene RC14 como última APK pública. No se adelantan otros juegos, objetos o modo offline completo.

- [x] `HoennAreaSearch.encountersForArea` reutiliza el filtro existente de método, pero si la consulta corresponde a un Pokémon (nombre o número nacional `#280`), reduce las filas mostradas en el detalle de zona a los encuentros de ese Pokémon. Buscar por nombre de ubicación (p. ej. «Ruta 102» o «Road 102») conserva los encuentros completos del lugar. Los contadores de resultados y variedades usan las mismas filas que verá el usuario; no representan tasas globales de captura.
- [x] El mapa y el selector de subáreas tienen en cuenta las áreas coincidentes con los filtros activos, en lugar de abrir una lista completa sin relación con la búsqueda. Una ubicación atenuada puede abrir una ficha vacía con explicación; atenuación no significa que no haya Pokémon.
- [x] Se agrega un botón de **limpiar búsqueda y filtros** sin cambiar la edición verificada. El detalle distingue ausencia de coincidencias frente a ausencia de datos en la fuente. No se alteran `pokemonId`, `speciesId`, niveles, métodos, condiciones ni registros de Colosseum ya excluidos.
- [x] Regresiones añadidas a `LocationPilotIntegrationTest`: Ralts y `#280` (área 394), sin resultados para pesca, preservación de los demás registros al buscar «Road 102», área desconocida. **43/43 pruebas Python correctas**; compilación aislada de `HoennAreaSearch.kt` con `kotlinc` y cinco verificaciones sintéticas correctas. La suite Kotlin Android no se ha ejecutado, ni se verificó APK o dispositivos reales.
- [ ] Compilación Android (`testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest`) y prueba en teléfono pendientes. No publicar nueva pre-release ni cambiar firma/versión únicamente por esta prueba local.

**Criterio para continuar:** verificar en Android que buscar Ralts muestra un registro concreto, que buscar Ruta 102 abre el catálogo completo, que los controles de filtro y el mapa coinciden y que tocar Pokémon abre la variedad exacta. Seguir con v3.6-B/C sin otra hoja de ruta.

### 5.6 Ajuste funcional aprobado tras vídeo de PokeInfo — atlas regional primero (8 nov 2026)

**Evidencia visual del usuario:** grabación de 39 s de PokeInfo original. Se aprecia selector de regiones, mapa de Kanto, selección de Ruta 1 y ficha `Location Detail` con pestañas `Pokémon` y `Items`; la ficha agrupa por generación y método (`Walking` en la muestra), distingue versión del juego y condiciones horarias mediante iconos, y muestra niveles. En `Items` aparecen `Black Apricorn` (HeartGold/SoulSilver) y `Potion` (FireRed/LeafGreen). El vídeo no demuestra que todos los métodos estén disponibles en esa ruta ni que todos los ítems sean recogibles del suelo; no copiar el arte ni los recursos privados de PokeInfo.

**Decisión del producto:** **UN mapa por región y variantes cartográficas únicamente si cambia físicamente la geografía**. NO duplicar mapas por edición. Al tocar ruta/ciudad/cueva se abre ficha con `Pokémon` y `Objetos / NPC`; las ediciones pertenecen al contenido de esa ficha. Los métodos (`hierba/cueva`, `Cabezazo`, `Golpe Roca`, `Surf`, cada caña, estáticos, regalo/intercambio, errantes y condiciones) se presentan separados. Objetos recogidos, ocultos y entregados por NPC son categorías diferentes, siempre verificadas por edición. Mapa limpio sin etiquetas masivas; búsqueda/listado accesible opcional.

**Implementación local de esta continuación, sin APK compilada ni push:**

- [x] La selección de regiones sigue siendo punto de entrada; se añade Hisui como entrada no interactiva hasta disponer de cartografía. Hoenn figura como piloto, las otras regiones no se anuncian como reconstruidas.
- [x] `VerifiedHoennMapScreen` pasa a ser **mapa primero**, sin imagen histórica antigua ni listado de resultados ocupando la pantalla de inicio; la búsqueda/filtrado se abre bajo demanda. El `Dialog` de ampliación permanece.
- [x] Al tocar una ubicación se abre su subárea o su selector. El mapa no queda sujeto a filtros ocultos cuando está cerrado el explorador. En intersecciones de secciones, `HoennSections.hits` devuelve candidatos y `HoennSectionMap` muestra una elección explícita; evita abrir una cueva cuando el usuario pretendía la ruta.
- [x] Corregida la **normalización vertical del plano a 28 × 15** (se usaba 28 × 18), y `Fiery Path` a un cuadro de anchura como en la fuente de `pret/pokeemerald`. Sin inventar un mapa ilustrado definitivo.
- [x] La ficha de área incluye pestañas `Pokémon` y `Objetos / NPC`. Los Pokémon están separados por familia de encuentro y método mediante `AtlasEncounterMethods.kt`, con nivel, peso de slot, condiciones y vínculo a Pokédex. Los filtros ahora distinguen `Cabezazo`, `Golpe Roca`, `Estáticos`, `Surf`, `Pesca` y demás; los tipos de método no disponibles en Esmeralda NO se simulan.
- [x] `AtlasItemAcquisition` y `AtlasSourcedItem` diferencian entrega de NPC, objeto visible/oculto, evento y Golpe Roca, exigiendo fuente explícita. La pestaña de objetos **no inventa registros**: indica que aún carece de catálogo auditado; se evita concluir «no hay objetos».
- [x] Conservados `LocationPilotStore`, IDs de Pokémon/áreas, fuente Esmeralda, Room, firma, número de versión y datos personales; 43 pruebas Python pasan; modelos de métodos/búsqueda compilan y pasan pruebas Kotlin aisladas. Añadidas pruebas JUnit, aún no ejecutadas con Android/Gradle.
- [ ] **Pendiente obligatorio:** compilar `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` con SDK, revisar GUI en teléfono y verificar actualización sobre RC14 sin pérdida de datos. CI, firma compatible, push, pre-release y APK NO verificados ni publicados.
- [ ] **Pendientes de producto:** cartografía ilustrada original con zonas alineadas y derechos comprobados; datos de nuevas ediciones por versión dentro del mismo atlas; objetos/NPC con fuentes y modos de adquisición; eventual caso de Cabezazo de HGSS y horarios si la fuente lo respalda; accesibilidad final.

**Criterio de aceptación específico para el flujo del vídeo:** abrir `Mapas` → `Hoenn` (sin seleccionar Esmeralda como mapa) → tocar Ruta 102 → ver ficha `Pokémon / Objetos-NPC` → consultar métodos y niveles de Esmeralda → abrir ficha de Ralts. Otras ediciones permanecen explícitamente sin datos hasta ser verificadas. Probar el solapamiento Ruta 112 / Senda Ígnea y el retorno sin perder búsqueda.

### 5.7 Navegación regional y memoria de último mapa (8 nov 2026)

**Decisión de producto:** al entrar en `Mapas` por primera vez aparece **el selector regional**, con Kanto en primer lugar y destacado. No se abre Hoenn ni se interpreta Kanto como elegido automáticamente. Tras seleccionar una región disponible, se guarda el identificador regional en preferencias locales del dispositivo. Al salir a otro módulo y regresar a `Mapas` (o volver a iniciar la aplicación) se reabre esa **última región**, no una edición específica del juego. En el visor, Atrás físico o de la barra vuelve al selector; el siguiente Atrás sale al menú. El selector nunca auto-redirige tras volver desde un mapa, evitando bucles. El usuario puede cambiar de región sin borrar su memoria anterior y la nueva selección reemplaza la guardada.

**Implementación local de continuidad (sin push ni APK nueva):**

- [x] `MapFeatureScreen` administra en una sola entrada el selector y el visor. Al iniciarse lee `LastMapRegionStore`; si no hay preferencia válida abre el selector; si existe reabre el mapa guardado. Al volver del visor usa estado local para mostrar el selector sin autorredirigir.
- [x] `MapRegionCatalogue` mantiene Kanto primero, Hoenn como piloto verificado, Hisui bloqueado mientras no exista mapa y el resto marcado como cartografía anterior aún sin reconstruir. No se inventan nuevos mapas ni se duplican por edición.
- [x] Persistencia mínima en `SharedPreferences` separadas de Room, autenticación, favoritos y equipos. Se descartan automáticamente regiones desconocidas o actualmente no disponibles. Reusar la selección de mapa no cambia la versión del juego ni datos de encuentros.
- [x] Se adaptó la prueba visual de selector existente y se añadieron pruebas de primera entrada, persistencia, regiones no disponibles y separación de preferencias. **Requieren ejecución con Android SDK/Gradle**; no se declara CI ni APK verificados.
- [ ] Comprobar en teléfono: primera entrada al selector; Kanto primero; elegir Hoenn, salir a Pokédex, volver a Mapas y recuperar Hoenn; cerrar y reabrir aplicación; Atrás al selector sin bucle; elegir Kanto y comprobar que es la nueva preferencia; fichas de zonas y regreso desde Pokédex.

**Seguimiento:** crear el mapa ilustrado original de Kanto antes de considerarlo reconstruido; por ahora su cartografía es la anterior. Mantener Hoenn como piloto de datos y no modificar claves de firma o bases existentes.

### 5.8 Candidata técnica v3.6.0-rc1 — compilación, firma y puerta de publicación (10 nov 2026)

- [x] Preparado el código local de la candidata `versionCode=30601`, `versionName=3.6.0-rc1`, con selector regional y memoria de §5.7; no implica APK ni estabilidad.
- [x] 43 pruebas Python pasan en la fuente de v3.6.
- [x] Añadido script Windows para compilar, **firmar usando la clave original del usuario** y comprobar SHA-256 del certificado antes de permitir publicación. La clave privada nunca se incrusta en el repositorio ni en el ZIP.
- [x] CI de comprobación acepta la rama `feature/v3.6-*` además de las anteriores.
- [x] **Entrega histórica del 10 nov:** [v3.6.0-rc1](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.6.0-rc1), pre-release, tag `a8d310e` → `a34b3d1`. APK release 16.672.223 bytes, SHA-256 `84c0a44da7d8c3cf0f8518a37ded837d7e7e6a105a750ba3b8d2675c43e1c34a`, digest remoto coincidente; certificado `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`, igual a RC14. 202 Kotlin/43 Python; lint 0 errores/build completo. Release `42844833432` y Verify `42852020780` intento 2/`42858423227` verdes. Commits `d1dba4e`, `1122283`, `a34b3d1`, `155f44d`, `6c76916`. No mover el tag ni reemplazar assets.
- [x] Límite de tests ampliado a 15 min (`155f44d`): 5 min insuficientes en CI. `AppNotIdleException` intermitente también presente en RC13; un retry verde no prueba que su causa se haya eliminado. La copia incorporada el 11 nov reintrodujo el límite antiguo y afirmaciones obsoletas de «sin compilar/no publicada»; se corrigen en §5.9.
- [ ] Validación manual Android sobre RC14 sin desinstalar, conservando cuentas, favoritos y equipos. Pruebas automatizadas no equivalen a actualización física ejecutada.

**Notas y procedimiento:** `docs/RELEASE_V3_6_0_RC1_CANDIDATE.md` y `tools/publish-v36-windows.ps1`. Mantener las limitaciones cartográficas explícitas.

### 5.9 Continuación real del diseño aprobado y atlas (11 nov 2026)

**Autorización:** desarrollar sobre la carpeta actualizada, sin buscar ZIP ni reiniciar arquitectura. Rama `feature/v3.6-regional-atlas-ui` sobre `6c76916`, con los cambios locales incorporados conservados. **No existe v3.5 estable publicada**; RC14 sigue como referencia, sin renumerarla ni promoverla. El código continúa `30601`/`3.6.0-rc1`: **desarrollo local, no nueva release**, no reemplazar APK/tag RC1.

**Interfaz y preservación:** Home/Más y cinco pestañas aprobados conservados. Barra roja con carita original en cabeceras compartidas, título alineado con acciones; tarjetas blancas en claro y superficies semánticas en oscuro. Ilustración grande solo en Home. Ningún módulo sustituido por placeholders. Corregidos import ausente de `Modifier`, imports inválidos de `rememberSaveable`, ámbito de `maxHeight` y falso positivo de lint `ProduceStateDoesNotAssignValue` (AGP 8.12.3, issue 349411310); límite de tests a 15 min. Recuperadas guardia CI de tag `v…-…`, Python real/cuarto verificador y protección del script histórico para no republicar RC1 ni sobrescribir su APK. Duplicados binarios con extensión falsa incorporados (`rotomfondo.png`, `map_paldea.jpg`, `map_sinnoh.jpg`) tenían hashes idénticos a los recursos correctos: movidos a respaldo externo, sin borrar contenido. `.git`, configuraciones locales y claves intactos. Sin cambios Room, esquemas, migraciones ni datos personales.

**Hoenn:** reemplazado el renderizado de cuadrícula/rectángulos por recreación de **Ztash declarada CC0**, corregida 28 dic 2023 20:45, 1507×990. SHA-256 `bb7ff006a8625d44bc8ef7b4e10a668e777a1efa1eeb9e3b538bce8fe80cd255`; página/licencia e imagen inspeccionadas el 11 nov. Procedencia en `tools/licenses/Hoenn-atlas-CC0.md`, aviso empaquetado en `res/raw/hoenn_atlas_license.txt`. No recurso del videojuego/PokeInfo añadido. `HoennAtlasProjection` documenta **calibración aproximada**, con anclas inspeccionadas de ciudades; conserva las 68 secciones/IDs y selección explícita Ruta 112/Senda Ígnea/Paso Desnivel. No cartografía final: etiquetas inglesas, polígonos finos/costas y QA físico pendientes. Imagen y selección comparten `MapViewport`, ajuste proporcional, clipping, zoom/pan acotados y restauración. Sin nube permanente de pines ni sectores rellenos.

Explorador en una sola `LazyColumn` (mapa/filtros/lista desplazables); diálogo ampliado desplazable, sin cortar controles en pantallas bajas. Búsqueda y datos Esmeralda intactos: 1644 filas fuente / 1642 de cliente / 114 áreas / 154 variedades. Ficha con selector de **versión individual**: Esmeralda verificada; las otras ediciones muestran pendientes, sin heredar filas. Métodos derivados de registros de área/edición/filtros activos, no booleanos manuales. Sprites con `PokemonImage`, niveles, peso de slot, condiciones y enlace a `pokemonId` exacto. Objetos/NPC siguen sin fuente auditada, no se inventan.

**Kanto:** primero/destacado, referencia histórica y catálogo buscable; **sin cartografía reconstruida ni encuentros certificados por edición**. Visor con fit proporcional/clipping/zoom/pan acotado. Ficha selecciona Rojo/Azul/Amarillo/Rojo Fuego/Verde Hoja/HG/SS individualmente y distingue pendientes de inexistencia. Contenido anterior conservado y accesible solo como histórico no verificado; no genera métodos ni objetos falsos para Ruta 1. Johto/Sinnoh/Teselia/Kalos/Alola/Galar/Paldea conservan recursos anteriores con el visor corregido/estado pendiente; Hisui sigue no disponible. CC0 de Hoenn no resuelve permisos de las imágenes antiguas.

**Navegación:** primera visita al selector, última región en preferencias separadas, retorno al selector sin bucle; reentrada con pestaña restaurada recupera la región. Pulsar la pestaña ya abierta no duplica destinos. Pokémon abierto desde un mapa/herramienta mantiene la pestaña de origen. Home/Más conservan rutas reales a todos los módulos.

**Pruebas finales locales:** **213 Kotlin, 0 fallos/errores/omitidas; 44 Python**, cuatro verificadores de índices y `git diff --check` correctos. Comando completo `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --stacktrace --max-workers=1` → **BUILD SUCCESSFUL in 3m55s**. Lint **0 errores / 113 warnings / 29 hints**. La base incorporada corregida había pasado previamente 204 Kotlin; se añadieron nueve casos de proyección/solapamiento, métodos/edición, ausencia de Surf ficticio, enlace exacto a Ralts, controles/pestañas y navegación real con selector → Kanto → Atrás → Más → Mapas (recupera Kanto). El primer test de ficha intentó localizar un elemento todavía no compuesto por `LazyColumn`: corregido usando `performScrollToNode`, sin retirar aserciones. `adb devices` vacío: **sin pruebas físicas, instrumentación ejecutada ni capturas Android**. Geometría probada no certifica alineación cartográfica completa.

**APK local de desarrollo:** `release-candidate-output/development-20261111/RotomPedia-v3.6.0-rc1-development-debug.apk` y `.sha256`, **26.165.430 bytes**, SHA-256 `bf29e28835d844941e57f790ba45cffec6802e5fe535feca317c25a7f98d79a4`. Es variante **debug**, no una nueva candidata release ni un asset público. `aapt`: `com.duoc.rotompedia`, `30601`/`3.6.0-rc1`, minSdk25. `apksigner verify` correcto, certificado `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa` igual a RC14. **Igual certificado no sustituye la versión creciente**: conserva el código de RC1 y no se publica como una actualización nueva. Ningún keystore creado/cambiado, APK público anterior intacto.

**Git y CI:** implementación `673d68e` y documentación `175081c` subidas en `feature/v3.6-regional-atlas-ui`. **Ambos Verify fallaron**: [43072080207](https://github.com/ArukuKun/RotomPedia/actions/runs/43072080207), 147 tests completados/14 fallos/1 omitido, y [43072511109](https://github.com/ArukuKun/RotomPedia/actions/runs/43072511109), 152/14/1. Python/cuatro índices correctos en ambos. Cascada `AppNotIdleException` iniciada en `ItemUiTest` (enlace de objeto evolutivo), seguida de Pokédex/biología/imágenes; la tarea termina por el límite de **15 min**. Patrón también observado antes de v3.6, pero **causa no resuelta**: no atribuir automáticamente todos los fallos a infraestructura ni considerar suficiente ampliar timeout. Los informes HTML no se generaron antes de interrumpirse la tarea, por lo que no había artefacto descargable. Los 213 tests locales verdes no sustituyen CI. **Entrega remota bloqueada** hasta diagnosticar y corregir el estado/idling compartido de las pruebas con evidencia; no más reintentos ciegos ni aumento del límite. Sin nuevo tag, estable o pre-release. `git fsck --connectivity-only` sin corrupción (solo dos blobs colgantes, no eliminados). Cambio del IDE excluido: `RotomPedia/.idea/vcs.xml`. Este resultado posterior se registra localmente, sin disparar otra ejecución por un push documental.

**Archivos principales** (base Kotlin `RotomPedia/app/src/main/java/com/duoc/rotompedia/`): `MainActivity.kt`, `ui/components/{RotomScaffold,RotomPageTopBar,RotomGlobalNavigationBar,RotomNavigationShell}.kt`, `ui/screens/{RotomHomeScreen,MapScreen,VerifiedHoennMapScreen,HoennSectionMap,RegionalReferenceMapScreen,AtlasEditionSelector}.kt`, `ui/maps/HoennAtlasProjection.kt`, `ui/theme/RotomPalette.kt`, `data/maps/AtlasEncounterSelection.kt`; `app/build.gradle.kts`, carita/atlas/licencia en recursos, pruebas `AtlasUiTest`, `AtlasEncounterSelectionTest`, `HoennAtlasProjectionTest`, `RotomGlobalNavigationTest` y adaptación de cobertura visual existente; `tools/tests/{test_atlas_art,test_rotom_visual_coverage}.py`, licencia CC0, script histórico de publicación, `.gitignore`, README y documentos de continuidad existentes.

**Pendiente:** cartografía propia/permitida y trazable de Kanto, paquetes por edición, calibración fina/etiquetas españolas de Hoenn, objetos/NPC con fuentes, QA en móvil/actualización sobre RC14 o RC1. Una futura distribución exige versión/código creciente, firma original, Actions/digest y **otro tag**; nunca sustituir RC1. Offline completo para el final.

### 5.10 Corrección del ciclo de vida de ItemUiTest y diagnóstico CI (11 nov 2026)

**Autorización:** «dale» a corregir aislamiento/idling tras los dos Verify fallidos de §5.9. Misma rama `feature/v3.6-regional-atlas-ui`. Sin cambios en pantallas/datos de producción, versión, firma, Room ni nuevas publicaciones.

- `ItemUiTest` cerraba su base en `@After`, que JUnit ejecuta **antes** del desmontaje de `createComposeRule`. Se sustituye por `RuleChain.outerRule(databaseRule).around(compose)`: abre Room antes de montar la UI y la cierra **después** de cancelar/desmontar Compose. Se añade regresión que comprueba `db.isOpen` dentro de `DisposableEffect.onDispose`, no solo al retornar el cuerpo del test.
- Retirado `shadowOf(mainLooper).idle()` dentro del predicado de `compose.waitUntil`: Compose administra su reloj/idling; no mezclar ambos mecanismos. Conservadas las aserciones de búsqueda, cero consultas al escribir, identidad de baya/efecto, petición única, error/reintento y enlace de objeto evolutivo.
- Test task mantiene **15 min**; no otro aumento. `failFast=true` detiene un run fallido tras su primer fallo para evitar 14 timeouts en cascada y permitir finalizar informes; **no excluye pruebas en ejecuciones correctas**. Excepciones completas en consola y XML de `test-results/testDebugUnitTest` incluidos en el artefacto de Verify. No retirar comprobaciones ni tratar un run parcial como suite verde.
- Temurin **17.0.21+9** descargado del repositorio oficial de Adoptium a la carpeta temporal aprobada (ZIP SHA-256 `98518ecf554465057ee5509462631deeee374bc368c12a411d225cd29381f84b`, verificado). Solo `JAVA_HOME`/PATH del comando; no cambio del JDK global, SDK, claves ni configuraciones del proyecto. Se reduce la diferencia de JDK respecto a Actions; Windows local no equivale a Ubuntu remoto.
- **Verificación local:** cuatro clases focalizadas (Item/Atlas/PokedexNavigation/PokedexPolish) correctas con JDK21; luego suite completa y lint/debug/instrumentación compilada correctos con JDK17 (7m02s, y repetición tras añadir la regresión de teardown en **2m01s**). **214 pruebas Kotlin, cero fallos/errores/omitidas; 44 Python correctas**, `git diff --check` correcto. Conservadas las pruebas de persistencia y propiedad existentes. No instrumentación física ejecutada.
- **Confirmación remota:** corrección `9fb5509` subida; [Verify 43084556620](https://github.com/ArukuKun/RotomPedia/actions/runs/43084556620) **success al primer intento**, `BUILD SUCCESSFUL in 10m06s`. Descargado el artefacto: **41 suites / 214 tests / 0 fallos / 0 errores / 0 omitidas**; Python/índices/lint/debug/instrumentación compilada también correctos. `failFast` no ha ocultado una suite parcial. XML/HTML retenidos como evidencia. Sin retry del commit fallido, nuevo tag o APK pública.
- **Límite de diagnóstico:** el orden incorrecto de cierre es un defecto comprobable del fixture; el CI verde tras corregirlo y retirar el avance manual del looper **no demuestra que sea la única causa** posible de toda flakiness histórica. Conservar diagnóstico completo y regresión de teardown. Bloqueo de esta ejecución CI resuelto; una publicación nueva sigue requiriendo versión creciente y el resto de controles físicos/distribución. No modificar assets RC1 por estos cambios de pruebas.

### v3.6-A — Investigación y piloto

- [ ] Revisar videos/capturas del **PokeInfo original de Bruno Sousa**, paquete `com.brunosousa.pokeinfo`. No confundirlo con aplicaciones homónimas.
- [ ] Documentar navegación real: elegir región/juego → explorar mapa → tocar ruta/zona → consultar Pokémon, objetos, condiciones y acceder a ficha.
- [ ] Elegir **una región y un conjunto concreto de ediciones** como piloto. Hoenn puede ser candidata por contar ya con mapa, pero no imponerla sin evaluar datos y recursos.
- [ ] Verificar origen, permisos y calidad de los mapas; no copiar recursos privados de otra aplicación.
- [ ] Separar cartografía (región o variante física, imagen, zonas/polígonos), ubicaciones (región/área), y contenido **por edición** (encuentros por método, nivel, horario, condiciones, objetos y NPC).
- [ ] Definir fuente y cobertura de objetos de cada zona; PokeAPI por sí sola no garantiza datos cartográficos completos ni objetos localizados.

### v3.6-B — Experiencia de mapa

- [ ] Zoom y pan suaves; imagen, marcadores y áreas táctiles usan la misma transformación de coordenadas.
- [ ] Puntos e hitboxes fieles a la ubicación, sin marcadores flotando al hacer zoom.
- [ ] Seleccionar región → mapa regional único → zona. Escoger **juego/edición en la ficha** para encuentros y objetos, sin duplicar cartografía cuando la geografía no cambia.
- [ ] Ficha de área con Pokémon, nivel, método (hierba, agua, pesca, etc.), horario/clima/condición cuando estén documentados.
- [ ] Mostrar objetos y formas de obtención cuando haya datos verificables.
- [ ] Enlazar mapa ↔ localización ↔ Pokédex, manteniendo filtro de edición.
- [ ] Buscar áreas/ubicaciones; mostrar cobertura y faltantes explícitamente.
- [ ] Optimizar recursos gráficos para teléfonos modestos y ofrecer caché adecuada.

### v3.6-C — Expansión progresiva

- [ ] Validar el piloto manualmente en dispositivo: precisión de pines, legibilidad, zoom, datos por edición.
- [ ] Añadir más regiones y juegos por paquetes de datos verificados, sin prometer cobertura inexistente.
- [ ] Incluir pruebas de coordenadas, filtros de versión, integridad de enlaces y modo offline.

**Criterio de salida v3.6:** el usuario puede encontrar un Pokémon en una zona comprobada, conocer contexto de edición y pasar a su ficha; no solo ver un mapa decorativo.

---

## 6. v3.7 — Rotom interactivo contextual y coherencia UI (PENDIENTE)

**Objetivo:** convertir Rotom en un acompañante discreto con personalidad que aporte consejos basados en la pantalla y en datos reales. **No un chatbot genérico.**

### v3.7-A — Rotom como personaje/interacción

- [ ] Diseñar una identidad visual propia y coherente con RotomPedia (expresiones/estados, tamaños y animaciones moderadas).
- [ ] Definir cuándo aparece, cómo se activa y cómo se oculta; no tapar botones o información importante.
- [ ] Permitir silenciar, reducir frecuencia y desactivar animaciones/comentarios.
- [ ] Mejorar coherencia de navegación, jerarquía visual, colores, tipografía y accesibilidad sin borrar la personalidad de la app.

### v3.7-B — Motor contextual sin IA obligatoria

Complementar este hito con RI-2/RI-5 de §16; son partes de una misma dirección arquitectónica, no dos motores contextuales independientes. Reutilizar `RotomCommentaryEngine` y los datos/repositorios existentes.

- [ ] Crear un motor `Contexto + Acción + Datos verificados → Reglas → Prioridad → Mensaje breve`.
- [ ] Pokédex: curiosidades, tipos, debilidades o variantes, con contexto correcto.
- [ ] Team Builder: advertir debilidades comunes, huecos de tipos o errores evidentes **sin afirmar resultados absolutos de combate**.
- [ ] Movimientos: explicar valores, categorías o mecánicas con reglas de edición cuando aplique.
- [ ] Mapas: comentar rutas, encuentros o datos disponibles, sin inventar información.
- [ ] Calculadoras: explicar resultados en frases breves.
- [ ] Limitar repeticiones, interrupciones y frecuencia; incluir cooldown e historial reciente.
- [ ] Mantener mensajes editoriales y datos factuales separados; pruebas de reglas.

### v3.7-C — IA opcional, solo si aporta valor real

RI-4 (§16) concreta la investigación experimental de esta opción; no es requisito para cerrar el modo local de v3.7 ni autoriza un chatbot.

- [ ] Evaluar después de estabilizar reglas: costo, privacidad, modo offline y calidad.
- [ ] Si se autoriza, IA restringida a redactar/variar comentarios basados en hechos verificados, no a inventar datos ni convertir Rotom en chat abierto.
- [ ] Debe existir un modo completamente funcional sin servicios externos de IA.

**Criterio de salida v3.7:** Rotom añade utilidad y encanto sin distraer, consumir excesivos recursos ni reemplazar herramientas existentes.

**Coordinación posterior a RC10:** §4.10 adelanta la auditoría/propuesta de coherencia visual por solicitud expresa, sin adelantar el personaje interactivo, motor contextual o RI de esta sección. Reutilizar esos tokens/componentes cuando corresponda; no construir dos sistemas visuales paralelos.

---

## 7. Investigación transversal de PokeInfo (ABIERTA)

- [x] Tomar PokeInfo de Bruno Sousa (`com.brunosousa.pokeinfo`) como referencia conceptual y revisar las capturas/videos aportados en conversaciones previas.
- [ ] Consolidar una ficha de investigación con **evidencias visuales concretas** sobre orden nacional, evoluciones ramificadas, formas, objetos, movimientos y mapas.
- [ ] Comparar flujos PokeInfo ↔ RotomPedia: pasos, información accesible, claridad, navegación y descubrimiento.
- [ ] Separar hechos verificados de ideas nuevas para RotomPedia; no atribuirle a PokeInfo comportamientos no observados.
- [ ] Registrar mejoras propias que no tenía PokeInfo: equipo más completo, calculadoras integradas, filtros, Rotom contextual, etc.
- [ ] Usar fuentes y activos con licencia compatible; no copiar APK, código ni gráficos privados sin permiso.

---

## 8. Calidad, compatibilidad y reglas de entrega (TODAS LAS VERSIONES)

### Datos y seguridad

- [ ] Probar migraciones Room desde esquemas realmente disponibles; nunca recuperar una excepción borrando la base.
- [ ] Pruebas de dos usuarios: favoritos/equipos aislados, propiedad validada también por ID, sesiones pendientes y cambio de cuenta.
- [ ] Preservar identidades técnicas y nombres originales de equipos guardados cuando haya conflictos de ID; no transformar un Pokémon en otro.
- [ ] Distinguir «sin datos», «aún no consultado», «sin red» y «resultado vacío».
- [ ] No exponer información personal ni secretos en logs; asegurar credenciales y firma fuera del repositorio.

### Experiencia y regresión

- [ ] Orden nacional en Pokédex, picker, calculadoras y búsqueda cuando corresponda.
- [ ] Todas las variedades mantienen su número nacional visible y su identidad interna de API.
- [ ] Pruebas de ejemplo: Venusaur/Mega, Charizard X/Y, Ninetales de Alola, Lycanroc, Aegislash, Zygarde/Mega Zygarde, `mega`, `hisui`, `#038`.
- [ ] Probar scroll, búsqueda rápida, cancelación de peticiones, imágenes faltantes, red lenta, offline, pantallas pequeñas y cambios de configuración.
- [ ] Textos en español y etiquetas de origen/versión para datos que cambian entre juegos.
- [ ] No declarar estabilidad exclusivamente por compilación o tests unitarios; completar pruebas manuales/instrumentadas disponibles.

**Matriz adicional post-RC1 (aplica a §4.1):**

- [ ] Nacional: una entrada por especie, #001–#006 y extremos #001/#1025, sin desplegables; comprobar búsquedas por número y ausencia de variantes implícitas.
- [ ] Formas: Mega Venusaur/Charizard, Gigamax Charizard, Ninetales de Alola y formas estables; elegir variedad conserva `pokemonId` y muestra número nacional, no ID interno.
- [ ] Filtros: grupo huevo × tipo × categoría × generación/origen × favoritos, vacíos reales y limpieza de consulta/filtros. Grupo huevo de especie no se anuncia como historial por juego.
- [ ] Lista/Cuadrícula: lista inicial, sprite 72 dp, tres columnas reales, scroll más allá de nueve y hasta el final, sin límite de resultados. Claves estables y mismo resolutor gráfico, sin llamadas de datos al escribir o cambiar vista.
- [ ] Alternar vista sin perder búsqueda, filtros, favoritos ni identidad de navegación; selector de equipos sigue ofreciendo variedades.
- [ ] Regresión de cuentas, equipos, EV/IV, cuatro movimientos y cachés; no cambiar esquema ni certificado. Pruebas automatizadas separadas de validación manual de actualización.
- [ ] Android real: texto grande, pantallas pequeñas, teclado, filtros desplazables, imágenes ausentes/offline y retorno desde detalle; no afirmar que Robolectric mide todos los dispositivos.

### Disciplina de desarrollo

- [ ] Usar rama de trabajo, revisar `git status` antes de tocar archivos y evitar sobrescribir cambios del usuario.
- [ ] Ejecutar pruebas/compilación relevantes; como referencia de Android: `./gradlew testDebugUnitTest lintDebug assembleDebug` desde el módulo/proyecto adecuado.
- [ ] Registrar archivos modificados, decisiones de arquitectura, pruebas ejecutadas, limitaciones y pendiente conocido.
- [ ] Actualizar este plan y el README principal al cerrar cada hito/versión (3.x, 4.x y posteriores): estado público frente a local, commits/tags reales, pruebas, límites y migraciones. No crear un README nuevo que sustituya al principal en cada versión ni automatizar publicaciones por actualizar documentación.
- [ ] No mezclar todas las mejoras en una sola PR o release gigante.
- [ ] Publicar pre-releases de fases autorizadas según §8.1; no publicar estables, cambiar firmas, borrar datos ni avanzar a otra fase sin aprobación independiente.

### 8.1 Política permanente de desarrollo y publicación

**Instrucción vigente del usuario:** una fase autorizada para implementar también está autorizada para su entrega de prueba en GitHub. No pedir otra aprobación para commits, push, etiqueta nueva, GitHub Actions y publicación **Pre-release**, con enlace directo de APK para instalación desde Android. Esta política sustituye las restricciones históricas de publicación de esos hitos, pero no amplía su alcance funcional.

1. Ejecutar y aprobar las verificaciones obligatorias, corregir errores y no publicar builds fallidos. Registrar resultados y problemas conocidos en este mismo plan y en las notas de la pre-release.
2. `versionCode` estrictamente creciente, mismo `applicationId` y certificado compatible con la instalación anterior. Si no puede verificarse la firma, **detener la publicación e informar**, no cambiarla ni sugerir desinstalar para forzar la instalación.
3. Mantener datos, equipos, favoritos, configuraciones y migraciones no destructivas. No modificar ni sobrescribir tags/assets de releases anteriores; conservarlos para comparar.
4. Todas las entregas automáticas de fases autorizadas son **pre-releases**; no promover a estable por tests o por ser una versión base. Las versiones estables requieren aprobación explícita propia, al igual que cambios de firma y acciones destructivas.
5. GitHub Actions compila/verifica el código y solo publica el APK preparado después de verificar checksum, certificado y metadatos de paquete/versión contra su build. No distribuir la APK firmada con la clave efímera del runner. La clave privada compatible permanece fuera de GitHub y del repositorio.
6. Flujo de esta entrega: build y firma locales comprobados → push de rama/tag nuevos → draft de pre-release con APK y `.sha256` → Actions ejecuta tests/build/verificaciones de assets → publica como pre-release, nunca Latest/estable → comprobar URL, estado público, digest y CI, actualizar evidencias y entregar enlace directo.
7. Las fases no autorizadas siguen pendientes aunque exista esta autorización de distribución. No empezar mapas, RI ni el resto del backlog al terminar una pre-release.

---

## 9. Orden recomendado desde hoy

**Punto de partida vigente:** v3.4 terminada; v3.5-A/B/C implementadas y publicadas como **v3.5.0-rc1**, pendientes de validación manual. La preparación/implementación inicial de v3.5 de este apartado ya se cumplió; su historial técnico permanece en §11–§17.

1. **Ahora: §4.15**, encabezados fijos y continuidad Lista/Cuadrícula, expresamente adelantados por el usuario. Integrar el ajuste de idioma/legibilidad §4.14 ya probado y aún no distribuido. RC12 integrada/P3 ya publicada y aceptada en general, no repetirla como tarea pendiente. Offline global se reserva para el final y su división de funciones con/sin conexión.
2. **Puerta manual:** presentar el resultado y completar/revisar observaciones de Android, sin declarar v3.5 estable solo por tests. Detenerse antes de otra mejora.
3. **Cerrar v3.5, no acumular funciones sin límite:** A/B/C y P3 están implementadas; corregir observaciones autorizadas, documentar traducciones/sprites y demás aplazamientos, solicitar aprobación explícita antes de publicar `3.5.0` estable. Mantener firma de prueba compatible. No hay un número de RC exigido: una nueva RC solo si hay cambios que validar. §13 es backlog, no una lista íntegra obligatoria para cerrar esta versión. Matemática de captura y selector de Balls son trabajos distintos.
4. **v3.6: mapas**, piloto por región/juego y expansión posterior, reutilizando componentes actuales (§5). Sigue siendo la siguiente versión funcional prevista, no se renumera por el pulido de v3.5.
5. **v3.7: Rotom contextual e interfaz**, reglas locales, personalidad y accesibilidad (§6), coordinado con RI (§16). RI-1 se puede proponer tras v3.5, con autorización de investigación propia y sin bloquear v3.6; IA/servicios externos no forman parte de esta mejora.

**Instrucción a OpenCode:** utilizar este único documento como contexto maestro. No reabrir v3.4 globalmente; concentrarse en lo pendiente. Una fase autorizada permite programar, investigar, refactorizar, probar y publicar su pre-release según §8.1 sin pedir permiso por archivo ni por entrega de prueba. Detenerse ante cambios de alcance, publicación estable, cambio de firma u operaciones destructivas.

---

## 10. Registro de control (completar con evidencias del repositorio)

| Versión / hito | Estado comunicado | Rama / commit / tag | Evidencia pendiente |
|---|---|---|---|
| v3.3.0-rc1 | Completada y pre-release publicada | `v3.3.0-rc1` (histórico) | Referencia histórica |
| v3.4-A | ✅ Completada | `feature/v3.4-a-implementation`, tag `v3.4.0-a1`, `0a67ea4` | Evidencias registradas en §11 |
| v3.4-B | ✅ Completada | `feature/v3.4-b-implementation`, `4c4e9c3`, `248c922`, `c5ccc77` | Evidencias registradas en §11 |
| v3.4-C | ✅ Completada | `feature/v3.4-c-implementation`, `e41bda3`, `6c8e9c2`, `6a0f2ac` | Evidencias registradas; matriz manual de imágenes pendiente |
| v3.4 integrada | ✅ Terminada; prerelease publicada | tag `v3.4.0-rc1`, commit `5d67ea0` | Bugs concretos en §13; no promoción a estable |
| v3.5 | A/B/C publicadas como pre-release integrada; verificación local y CI correctas | `feature/v3.5-bc-encyclopedia`, `v3.5.0-rc1`, `a857f7e` | Validación manual Android pendiente (§17); base 3.5.0 aún no publicada |
| v3.5 post-RC1 — Pokédex | Publicada como pre-release `v3.5.0-rc2`; no versión estable | `feature/v3.5-pokedex-polish`, tag en `c7010ac`, publicación Actions `41887424591` | 77 Kotlin/21 Python, firma/checksum/versión verificados; validación manual pendiente (§4.1) |
| v3.5 post-RC2 — comodidad Pokédex | Publicada como pre-release `v3.5.0-rc3`; nueva validación pendiente | `feature/v3.5-pokedex-comfort`, tag en `4309dd8` | 86 Kotlin/23 Python, ambos CI correctos; retorno Kalos/Furfrou, vistas, Hisui y Tótem (§4.2) |
| v3.5 post-RC3 — barra y filtros | Pre-release `v3.5.0-rc4`; correcciones aprobadas por el usuario | `feature/v3.5-pokedex-toolbar`, tag en `2c0a9ce` | 91 Kotlin/23 Python y ambos CI correctos; aprobación general sin inferir matriz manual exhaustiva (§4.3) |
| BIO-001 — ficha biológica | Pre-release `v3.5.0-rc5`; validación visual pendiente | `feature/v3.5-pokemon-biology`, tag en `2776a61` | 100 Kotlin/23 Python, ambos CI correctos, firma compatible y enlace público verificado (§4.4) |
| ABIL-002 / DEX-008 — sprites de portadores y Formas vacías | Pre-release `v3.5.0-rc6`; aprobación general del usuario | `feature/v3.5-ability-sprites`, tag en `7a892df` | 105 Kotlin/23 Python, ambos CI correctos; evidencia y límites en §4.5 |
| EVO-001 — requisitos de Hisui por edición | Pre-release `v3.5.0-rc7`; aprobación general del usuario | `feature/v3.5-hisui-evolutions`, tag en `6027026` | 22 relaciones, 117 Kotlin/23 Python y ambos CI correctos (§4.6) |
| ABIL-001 — contadores y nombres | Pre-release `v3.5.0-rc8`; continuidad aceptada, sin inferir matriz exhaustiva | `feature/v3.5-ability-counts`, tag `v3.5.0-rc8` | 125 Kotlin/23 Python y ambos CI correctos; contadores por variedad y alias (§4.7) |
| ITEM-002 — objetos por bolsillos | Pre-release `v3.5.0-rc9`; correcciones de UI posteriores en RC10 | `feature/v3.5-item-pockets`, tag en `17faea3` | 2223 objetos conservados, 134 Kotlin/26 Python y ambos CI correctos (§4.8) |
| Interfaz de cliente y filtros de Objetos | Pre-release `v3.5.0-rc10`; correcciones aceptadas en términos generales | `feature/v3.5-clean-client-ui`, tag en `9ff2c69` | 140 Kotlin/28 Python, firma/checksum/enlace y ambos CI correctos; publicación al segundo intento. Evidencia histórica §4.9; estable no autorizada. |
| Recuperación de identidad Rotom | Auditoría y propuesta visual entregadas; implementación progresiva aprobada después | `docs/rotom-visual-direction`, maqueta `docs/prototypes/rotom-identity.html`, `2d9164b` | §4.10: referencia de diseño, no APK Android. Autorización y primer corte V1 posterior en §4.11; revisión visual del corte pendiente. |
| V1 — base visual y piloto | RC11 histórica; instalada y considerada visualmente insuficiente | `feature/v3.5-rotom-visual-foundation`, tag `v3.5.0-rc11` en `06cd73c`, `30511` | §4.11 conserva evidencia de 149 Kotlin/28 Python y CI. Piloto superado por entrega integrada §4.12. |
| Interfaz Rotom integrada + P3 | RC12 publicada; aceptación visual general §4.13 | `feature/v3.5-complete-rotom-interface`, tag `v3.5.0-rc12` en `a310916`, `30512` | §4.12: 162 Kotlin/31 Python, índices/lint/build/firma y ambos CI correctos al intento 1; APK público intacto. |
| Encabezados fijos, continuidad y legibilidad | RC13 publicada, histórica | `feature/v3.5-sticky-headers-scroll`, tag `v3.5.0-rc13` en `966dee0`, `30513` | §4.15: 172 Kotlin/31 Python, Release correcto al intento 2; Verify posterior correcto para idéntica app (`11cdc0b`), fallos del tag conservados. APK intacto. |
| Exclusión de Shadow/XD e Hisui ampliado | **Pre-release actual RC14 publicada**, revisión física pendiente | `feature/v3.5-main-games-hisui`, tag `v3.5.0-rc14` en `88c94e6`, `30514` | §4.16: 179 Kotlin/31 Python, índices/lint/build/firma y ambos CI correctos al intento 1; digest/HTTP verificados. Sin cambios de snapshots ni borrado de datos personales. |
| v3.6 | ⬜ Pendiente | — | Mapas por juego/región |
| v3.7 | ⬜ Pendiente | — | Rotom contextual e interfaz |
| Rotom Intelligence Pipeline | ⬜ Investigación y prototipo pendientes; sin iniciar | Sin rama/código de RI | Después de v3.5; integración progresiva con v3.7, sin retrasar v3.6 (§16) |
| v4.x y posteriores | Sin alcance aprobado | — | Mantener el README y este plan al definir cada nueva serie; RI no tiene asignada una v4.x |

**Última actualización del plan:** 5 de noviembre de 2026.\
**Próxima puerta:** revisar RC14 en teléfono (§4.16): Shadow/XD ausentes y Hisui con especies nuevas/formas diferenciadas, filtros/navegación y datos previos conservados. Ambos CI de RC14 correctos; historial de RC13 no se borra. Offline para el final; continuar v3.6-A con fuentes/piloto verificable según autorización anterior, sin inventar mapas. Traducciones/sprites/RI mantienen sus pendientes. Nunca promover a estable automáticamente.

---

## 11. Evidencias de continuidad de v3.4

### 11.1 Entrega exacta y validación

- Base revisada: `5d67ea046d9ef56c5bd94ebe64150b2e8f0567ab`, tag anotado `v3.4.0-rc1`.
- [Release pública](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.4.0-rc1), comprobada mediante API: `draft=false`, `prerelease=true`, APK y `.sha256` subidos.
- `RotomPedia/app/build.gradle.kts`: `versionName=3.4.0-rc1`, `versionCode=30402`, `applicationId=com.duoc.rotompedia`, mínimo API 25.
- APK: `RotomPedia-v3.4.0-rc1.apk`; SHA-256 `66c192cda3f64d4e696ced4c16cd176442bce1b2a7c6731ca062b843b27f9ac0`, coincidente con el digest publicado.
- Firma de depuración, no de producción. Durante la entrega se verificó el mismo certificado que A1; no afirmar compatibilidad de firma con todas las versiones históricas.
- Evidencia local existente de la preparación de RC1: **39 pruebas Kotlin, 0 fallos, 0 errores; lint sin errores**, con advertencias. Se ensamblaron APK de aplicación y de instrumentación. Ensamblar el APK de pruebas no equivale a ejecutar instrumentación en un móvil.
- CI del mismo commit, comprobada como `success`: [Verify v3.4](https://github.com/ArukuKun/RotomPedia/actions/runs/41579334890) y [Release APK](https://github.com/ArukuKun/RotomPedia/actions/runs/41579356963).
- En esta revisión documental se reejecutaron **12 pruebas Python** y `tools/generate_pokemon_index.py --verify-only`, correctas. No se volvió a ejecutar Gradle ni se modificó código de aplicación.
- El usuario instaló y probó la entrega: reportó movimientos asociados al Pokémon, descripciones en inglés y cálculo de captura incorrecto. Las capturas muestran Virizion y el editor de Typhlosion de Hisui. No se deducen modelo de móvil, versión Android, condiciones de Ocaso Ball ni validación exhaustiva de A/B/C.

### 11.2 Matriz de implementación comprobada

Las rutas abreviadas `data/`, `ui/`, `viewmodel/`, `di/` y `util/` de las tablas parten de `RotomPedia/app/src/main/java/com/duoc/rotompedia/`. Las pruebas citadas están en `RotomPedia/app/src/test/java/com/duoc/rotompedia/`.

| Área | Código/artefacto real | Evidencia y límite |
|---|---|---|
| Room y preservación (A/B) | `data/local/AppDatabase.kt`, `data/local/migrations/DatabaseMigrations.kt`; esquemas `RotomPedia/app/schemas/com.duoc.rotompedia.data.local.AppDatabase/{6,7,8}.json` | Versión 8 en RC1 publicada (desarrollo local v10, §17); migraciones 4→5→6→7→8 registradas, sin fallback destructivo. `RoomPersistenceTest.kt` cubre datos personales, IDs contradictorios, versiones sin soporte y propiedad de equipos. No prometer migración desde esquemas anteriores no verificados. |
| Sesión y repositorios (A) | `di/AppContainer.kt`; `data/repository/{SessionRepository,FavoritesRepository,TeamRepository,PokemonRepository,DefaultPokemonRepository}.kt` | Dependencias compartidas y capturas de sesión/propiedad. `SessionRepositoryTest.kt` y `RoomPersistenceTest.kt`: cambio de cuenta, cierre de sesión y operaciones pendientes. |
| Índice local (A/B) | `data/index/{PokemonIndexSnapshot,PokemonIndexImporter}.kt`; `tools/generate_pokemon_index.py`; `RotomPedia/app/src/main/assets/pokemon-index.json` | Snapshot con hash y revisión upstream: **1025 especies, 1351 variedades, 1579 identidades de forma**, tipos y nombres. `PokemonRepositoryTest.kt` prueba importación, reapertura offline, rollback y conflictos sin reescribir IDs. Las 1579 formas no son 1579 entradas seleccionables. |
| Explorador compartido (B) | `data/explorer/PokemonExplorer.kt`; `viewmodel/PokemonExplorerController.kt`; `ui/screens/{PokedexScreen,PokemonPickerDialog,PokemonExplorerContent}.kt` | Una fuente para Pokédex y picker; filtros por pantalla. `PokemonExplorerTest.kt` usa el snapshot real y verifica ausencia de llamadas API al explorar. |
| Orden, identidad y búsquedas (B) | `PokemonExplorerTest.kt`, `PokemonExplorerUiTest.kt`, `PokemonFormCatalogTest.kt` | Bulbasaur primero; `mega`, `hisui`, `lycanroc`, `sceptile`, `#038`; normalización de tildes/guiones; cinco numeraciones de referencia; filtros intersectados y favoritos. Seleccionar Alola conserva `10104`, no lo cambia por `38`. |
| Imágenes (C) | `data/images/{PokemonImageResolver,ImageDiagnostics}.kt`; `ui/screens/PokemonImage.kt` | `pokemonId` exacto, fuentes STANDARD/ARTWORK/HOME; URLs API/guardadas se aceptan si coinciden con identidad/fuente/color. Prioriza fuentes declaradas válidas; aún construye candidatos convencionales seguros. Coil carga progresivamente y cachea; 404/410 tienen caché negativa temporal, no los errores transitorios. No es un resolutor arbitrario de todos los recursos `pokemon-form`. |
| Pruebas y auditoría gráfica (C) | `PokemonImageResolverTest.kt`, `PokemonImageUiTest.kt`; `tools/data/{explorer-sprite-audit,image-resolution-audit}.json` | Identidad sin fallback a especie; normal/shiny; placeholder/reintento. La auditoría B comprobó 1351 URLs estándar y halló nueve ausentes; C comprobó por GET seis candidatos por cada uno de esos nueve casos, incluida firma PNG. Cinco recuperan artwork normal y shiny, incluido Mega Zygarde. |
| Cobertura gráfica restante | `tools/data/image-resolution-audit.json` | Sin imagen en las tres fuentes comprobadas: `koraidon-limited-build`, `koraidon-swimming-build`, `miraidon-low-power-mode`, `miraidon-aquatic-mode`. Son faltantes de recursos, no un motivo para sustituirlos por otro Pokémon o reabrir C globalmente. |
| Descripciones por variedad | `ui/screens/PokemonDetailScreen.kt` | Los textos de especie se etiquetan; para variedades no predeterminadas no se muestra una descripción sin especificidad verificada. No se ha creado una base nueva de textos específicos para todas las formas. |

---

## 12. v3.5: diagnóstico acotado y alcance propuesto para aprobación

**Estado histórico: diagnóstico y propuesta previos a la implementación.** Se mantienen v3.5-A/B/C del apartado 4; no se rehace la Pokédex ni se crea un catálogo paralelo de Pokémon.

> Esta sección conserva el diagnóstico y la propuesta originales. Se autorizó primero A mediante «avanzalo» y después continuar los restantes hitos mediante «sigue lo que tenemos que terminar para la 3.5», «sigue» y «siguele bro». Implementación real en §15 y §17; las decisiones pendientes descritas debajo son históricas, no bloqueos vigentes.

### 12.1 Qué existe y qué falta

| Módulo | Base aprovechable | Carencia concreta para v3.5 |
|---|---|---|
| Objetos | `data/CompetitiveItemData.kt`: objetos, categorías tácticas, nombres y consejos; `ui/screens/CompetitiveItemsScreen.kt`: búsqueda, filtros y tarjetas | Es catálogo local de enfoque competitivo con emojis, no una enciclopedia de PokeAPI con sprites, identidad y procedencia. `PokeApiService.kt` no declara endpoints de objetos/categorías/atributos; `AppContainer.kt` no ofrece `ItemRepository`. Se mezclan hechos y consejos editoriales. |
| Movimientos | `getPokemonWithMoves`, `getMoveDetail` en `data/remote/PokeApiService.kt`; DTOs de `data/model/PokemonData.kt`; `viewmodel/PokemonViewModel.kt::loadPokemonMoves`; `data/PokemonMoveData.kt`; `MovePickerDialog` en `ui/screens/MemberStatEditorDialog.kt` | No hay `MoveRepository`. Hay caché en memoria por ViewModel y catálogo estático/dinámico. Los detalles no incluyen prioridad, flavor text ni cambios históricos. La carga conserva varios métodos pero aplana edición/nivel; escoge el detalle con mayor ID de grupo, no un juego elegido. Topes de 60 por método y 30 de tutor; no es un learnset exhaustivo. |
| Texto de movimientos | `PokemonMoveData.fromMoveDetail`, líneas 48–74 | Busca efecto español y, si falta, inglés. Los nombres pueden estar en español mientras la descripción queda en inglés. No modela `flavor_text_entries` y elimina `$effect_chance%` en vez de resolver esa información. Reutilizar nombres/alias útiles sin conservar una descripción engañosa. |
| Habilidades | Repositorios e interfaz API actuales; campo opcional `ability` en exportador de `util/StatCalculator.kt` | No se encontró `AbilityRepository`, endpoint/DTO de habilidad ni relación de habilidades en `PokemonData`; tampoco una ficha enciclopédica implementada. El texto opcional de exportación no constituye ese sistema. |
| Equipos | `data/local/MiembroEquipo.kt` conserva EV/IV/naturaleza/nivel y `move1`–`move4` como texto | No contiene un campo persistido de objeto equipado ni de habilidad. Añadir esos campos sería una ampliación funcional a aprobar, no algo ya implementado ni una migración automática de textos antiguos. |

### 12.2 Comprobación de fuentes (3 de noviembre de 2026)

Consultas de lectura a PokeAPI, sin integrar datos nuevos en la app:

- [`item/shuca-berry`](https://pokeapi.co/api/v2/item/shuca-berry/): ID `168`, slug `shuca-berry`, nombre español **Baya Acardo**, categoría `type-protection`, sprite `sprites.default`; efecto inglés: reduce daño supereficaz de **Tierra**. El flavor text español de `sun-moon` también indica Tierra. El catálogo actual (`CompetitiveItemData.kt:296–303`) dice «Baya Pasio» y Agua: discrepancia factual confirmada, pendiente de v3.5-A, no corregida en esta revisión.
- [`move/flamethrower`](https://pokeapi.co/api/v2/move/flamethrower/): ID `53`, nombre **Lanzallamas**, `priority=0`; efectos consultados en francés/inglés, pero flavor text español por `version_group`. Confirma que «efecto detallado español ausente» no significa «todo texto español ausente».
- [`ability/blaze`](https://pokeapi.co/api/v2/ability/blaze/): ID `66`, **Mar Llamas**; efectos consultados en francés/alemán/inglés, flavor text español por grupo de versiones y relación `pokemon`. Las habilidades ocultas se deben leer de la relación de la variedad, no inferirlas del nombre.
- Son tres muestras de esquema/cobertura, no una auditoría de todos los objetos, movimientos o habilidades. `flavor_text` es una descripción de juego, no siempre un efecto mecánico completo ni libre de errores históricos. Guardar idioma, procedencia y edición; no elegir simplemente el primer texto disponible.

### 12.3 Contratos comunes propuestos (sin crear código todavía)

1. `ItemRepository`, `MoveRepository`, `AbilityRepository` como dependencias compartidas en el `AppContainer` existente. Mantener `PokemonRepository` como autoridad de identidad Pokémon, sin convertirlo en un repositorio monolítico de todo.
2. Identidad factual por ID/slug de cada recurso; presentación española, alias y notas editoriales separados. Categorías oficiales de objetos separadas de etiquetas tácticas propias.
3. Índices ligeros locales para explorar/buscar; detalles y relaciones con carga progresiva, caché y cancelación. Evaluar snapshots de mantenimiento al estilo del índice actual para disponer de nombres españoles sin descargar cientos de fichas al escribir. No alterar el snapshot Pokémon innecesariamente.
4. Definir para cada ficha estados «no consultado», «cargando», «sin datos», «offline», «error» y «vacío». Distinguir «índice disponible offline» de «detalle aún no descargado»; no prometer toda la enciclopedia offline de inicio.
5. Política editorial de español: mostrar descripción oficial española de la edición seleccionada cuando exista; ofrecer efecto español curado solo si está verificado y etiquetado como tal. Si falta traducción, aviso explícito y original identificado como inglés, no mezcla silenciosa ni traducción automática presentada como dato oficial.
6. `versionGroup`/juego explícito en aprendizaje y textos; conservar `(pokemonId, moveId, versionGroup, método, nivel)`. Un `moveId` puede tener varios métodos y niveles; no colapsar esas relaciones en una sola entrada ni usar orden numérico de IDs como sustituto de edición.
7. Mantener sin cambios los cuatro textos de movimientos guardados. Resolver referencias/alias de forma no destructiva; conflictos o textos desconocidos siguen visibles. Cualquier futura migración Room deberá ser aditiva, probada desde v8 y sin pérdida de cuentas/equipos.
8. Los sprites de objetos requieren un cargador propio por identidad de objeto y `item.sprites.default`; no pasar un `itemId` al resolutor de imágenes Pokémon ni inventar sprites individuales para movimientos/habilidades. Reutilizar la estrategia de estados/caché/placeholder, no mezclar identidades.

### 12.4 Hitos y puertas de salida propuestos

**v3.5-A — Objetos (primera implementación recomendada, pendiente de aprobación)**

- Extender el acceso API y crear repositorio/modelo/índice/caché de objetos, categorías y atributos según datos disponibles; definir la revisión de datos antes de actualizar un snapshot.
- Adaptar la pantalla actual a listado y ficha con sprite, nombre español, categoría oficial, atributos/usos, descripción y efecto con fuente/idioma. Mantener los consejos competitivos como bloque editorial identificable, revisado y no «estrategia oficial».
- Registrar precio/obtención únicamente cuando la fuente y edición lo sostengan; si faltan, mostrarlo. No inventar ubicaciones para completar la ficha ni confundir categoría/atributo con prueba de que se puede equipar o consumir en cualquier juego.
- Búsqueda local con normalización; filtros coherentes; navegación desde el catálogo y desde objetos de evolución ya identificados. Enlaces desde movimientos/equipo cuando exista una relación real, sin agregar automáticamente persistencia de objetos equipados.
- **Aceptación:** `shuca-berry` no puede aparecer como Agua/Baya Pasio; comprobar bayas de protección, objetos de elección, evolutivos, consumibles y MT/MO con fixtures revisadas. Probar sprite ausente, sin traducción, sin precio, filtros, caché/reapertura offline y enlace que conserva identidad Pokémon/equipos.
- **Salida:** revisión del usuario en móvil + pruebas de repositorio/UI y regresión de datos. Detenerse antes de B si solo se autoriza A.

**v3.5-B — Movimientos completos**

- Extraer la carga actual a `MoveRepository` común para enciclopedia, detalle y editor; incorporar prioridad, textos localizados, efectos y contexto histórico disponible.
- Preservar métodos/nivel/edición; elegir un grupo de versiones para consultar aprendizaje, sin exigir que un equipo histórico adopte ese contexto ni afirmar legalidad competitiva por ello.
- Reemplazar los límites invisibles por carga progresiva con conteo/estado de cobertura explícitos; conservar todos los métodos de un mismo movimiento. No anunciar «todos» si la descarga es parcial o falló.
- Integrar nombres y texto español con su procedencia; resolver o informar placeholders como `$effect_chance`, respetar potencia/precisión no aplicables y usar iconos de tipo/categoría.
- **Aceptación:** Typhlosion de Hisui conserva su `pokemonId`; Lanzallamas, Bola Sombra y Nitrocarga muestran texto con política de idioma definida; un movimiento con nivel y MT conserva ambas relaciones; cambiar edición cambia el learnset, no los textos guardados del equipo. Pruebas de paginación, fallos parciales, alias y texto legado desconocido.

**v3.5-C — Habilidades y enlaces**

- Crear acceso API/repositorio/modelos de habilidad y relación por variedad (`slot`, `is_hidden`, identidad), con índice/caché y ficha propia.
- Pokémon → habilidades normales/ocultas → Pokémon asociados; conectar movimientos/objetos únicamente por relaciones verificables y con cobertura explícita. No inferir sinergias como si fueran enlaces factuales del API.
- Revisar la política ya existente de descripción de especie/variedad; conservarla si no se dispone de textos específicos, sin iniciar de nuevo el hito v3.4-C.
- **Aceptación:** Mar Llamas y casos de habilidad oculta/regional muestran nombres/relaciones/textos correctos; efectos sin español se identifican; retorno a Pokémon conserva variedad; ninguna pérdida de movesets ni datos personales.

**Fuera del alcance propuesto de v3.5:** mapas/polígonos y cobertura cartográfica (v3.6), Rotom contextual transversal/IA (v3.7), simulador de combate o certificación de legalidad, selección exhaustiva de cosméticos, nuevo sistema de equipar objetos/habilidades sin aprobación adicional, reescritura de v3.4, corrección inmediata de captura y publicación automática.

**Decisiones que requieren aprobación antes de programar:** autorizar primero A o toda v3.5 por entregas; confirmar política de idiomas/fallback; definir grupo(s) de versiones inicial(es) para aprendizaje/textos sin imponer Hoenn/Kanto ni un juego de mapa; aceptar qué detalles estarán disponibles offline. Recomendación: aprobar primero A y validar antes de B; el juego de aprendizaje puede concretarse al preparar B. No bloquear la investigación de A por una decisión exclusiva de B.

---

## 13. Backlog residual: incidencias específicas, no reapertura de v3.4

| ID / destino propuesto | Evidencia | Trabajo pendiente / criterio de cierre |
|---|---|---|
| `CAP-001` — corrección matemática de captura; pendiente, no autorizada ahora | Captura histórica de Virizion `#640`, ratio 3, salud 1%, Ocaso Ball: 0.1%, ~99.0 bolas y acumuladas redondeadas a 0%. Nuevo reporte del usuario menciona legendarios como Mewtwo, sin parámetros suficientes para una reproducción nueva. | Reproducir por edición con mismos PS/estado/entorno/turno, comparar fórmulas y referencias verificables, tasas/bonificaciones/redondeos; tests de legendarios y especies comunes. No limitar artificialmente el resultado a 0.1%, ni dar una causa por confirmada con el reporte. Independiente del selector visual `CAP-004`. |
| `CAP-002` — coherencia numérica y condiciones, mismo mantenimiento | `CatchRateCalculatorScreen.kt:89–105` incluye `selectedBall` en `remember` y la pasa a `calculate`; `CatchRateCalculator.kt:83–124` sí aplica multiplicadores, pero limita probabilidad visible a ≥0.1% y esperadas a ≤99. Usa potencia 0.75 y luego cuatro comprobaciones. `DIVE_BALL` aplica 3.5 sin condición y `HEAVY_BALL` no tiene entrada de peso. | Auditar fórmula/condiciones por edición antes de prometer «oficial Gen 8/9»; separar cálculo real de presentación/redondeo, eliminar mensajes engañosos y verificar monotonicidad/acumuladas. Los límites explican parte de la presentación, no prueban por sí solos la causa completa. |
| `CAP-003` — datos de captura y PS, mismo mantenimiento | La pantalla usa `resolveBaseCatchRate` local; además existe `CatchRateCalculator.getDefaultCatchRate`, otra tabla. Se pasa `maxHp=100` y porcentaje redondeado como PS; el botón «1 PS» representa 1%, no 1 PS real. | Obtener tasa por especie del repositorio/datos verificables y distinguir porcentaje/PS reales; probar variedades, tasas ausentes y estimaciones. No mezclar esta corrección funcional con el repositorio enciclopédico de objetos sin ampliar alcance. |
| `MOV-001` — traducción/procedencia, v3.5-B | Captura del editor de Typhlosion de Hisui: nombres españoles y efectos ingleses. Código y muestra de PokeAPI confirmados en §12.1–12.2. | Implementado localmente: texto español por edición y fallback inglés identificado, sin inventar efectos detallados. Pruebas correctas; validación manual pendiente (§17). |
| `UI-001` — pantallas estrechas / texto grande | En las capturas se parte verticalmente «255 (Común)» y «Estado»; hay controles horizontales parcialmente visibles. | Revisar anchuras mínimas, wrapping, scroll y accesibilidad sin borrar filtros; decidir en su hito funcional o pulido v3.7, y probar con tamaños de fuente grandes. Observación visual, no causa confirmada de `CAP-001`. |
| `ITEM-001` — datos de bayas, v3.5-A | Discrepancia histórica de `shuca_berry`, verificada en §12.2. | Corregida en la implementación local de A: índice oficial, Baya Acardo/Tierra y efecto revisado con fuente. Pruebas de bayas/identidad; pendiente de validación manual. |
| `DEX-001` — nacional limpio y proporciones propias; **entregada en RC2** | Observación Android: filas/sprites reducidos y variedades desplegables no deseadas en la Pokédex normal. Referencia propia `2590bf5` frente a `248c922`. | §4.1: 1025 especies ordenadas, sin hijos/desplegables, sprite 72 dp/nombre 20 sp. La nueva captura confirma regionales en cuadrícula, no una validación visual exhaustiva; nuevas observaciones en DEX-004–007. |
| `DEX-002` — filtros verificables; **entregada en RC2** | Grupos huevo/Gigamax implementados en §4.1 sobre tipos/origen/generación existentes. | Seguir validando combinaciones y cobertura en Android; orden regional y memoria de filtros se corrigen en §4.2, sin reimplementar el catálogo. |
| `DEX-003` — Lista/Cuadrícula; **entregada en RC2** | Tres columnas y scroll completo ya probados; captura manual muestra Hisui en cuadrícula. | Conservar claves/identidades y vistas; DEX-005 aborda específicamente regreso desde detalle, distinto de alternar Lista/Cuadrícula. |
| `DEX-004` — botón de filtros; autorizado post-RC2 | Captura: «Filtros» como texto amarillo, sin presencia de botón. | §4.2: botón con borde e icono de tres barras a la izquierda; panel/filtros intactos y validación manual de comodidad. |
| `DEX-005` — memoria al volver de detalle; autorizado post-RC2 | Recorrido Kalos → Furfrou → Atrás pierde filtro y conserva un scroll incompatible. | §4.2: restaurar conjuntamente filtros, vista e índice/offset, cargar metadatos antes del scroller, probar navegación real y estado guardado. Cambiar filtro intencionalmente sí reinicia al comienzo. |
| `DEX-006` — orden regional; autorizado post-RC2 | Captura de Hisui con orden nacional; el usuario pide iniciales al comienzo según su Pokédex. | §4.2: numeración regional de fuente fijada/contexto de juego, sin IDs inferidos; faltantes de Galar/DLC explícitos. Nacional normal mantiene orden nacional. |
| `DEX-007` — categoría alternativa y Tótem; autorizado post-RC2 | Se cuestiona el cajón «Variedades estables» y Raticate Tótem duplicado. | §4.2: retirar chip general, ocultar Tótem en Pokédex sin borrar datos; conservar formas de Lycanroc, genios y Origen en fichas/picker y preservar equipos/favoritos por ID. |
| `DEX-008` — sección Formas vacía; **publicada en RC6** | Tras aprobar RC5, el usuario reporta una sección sin alternativas y un aviso sobre PokeAPI en Overqwil. | §4.5: sección y espaciado propio ocultos sin alternativas; selección/identidades y reintento conservados. 105 Kotlin/23 Python y ambos CI correctos; validación visual pendiente. |
| `ABIL-002` — mini sprites de portadores; **publicada en RC6** | El usuario solicita acompañar con sprite cada Pokémon de la lista de portadores en la ficha de habilidad. | §4.5: sprite de 40 dp por `pokemonId`, nombre/rol legibles, lista completa y navegación exacta. 105 Kotlin/23 Python y ambos CI correctos; validación visual pendiente. Contadores/traducciones de ABIL-001 siguen aparte. |
| `EVO-001` — requisitos evolutivos de Hisui; **publicada en RC7** | Reporte inicial Overqwil en RC5; después el usuario pide todos los casos de Hisui y confirma el alcance por edición. | §4.6: 22 relaciones en Arceus y Escarlata/Púrpura, identidad exacta, condiciones y transferencia diferenciadas; 117 Kotlin/23 Python y ambos CI correctos. Validación visual pendiente; otras ediciones y P3 fuera de cobertura. |
| `BIO-001` — ficha biológica compacta; **publicada en RC5** | Usuario acepta expresamente continuar con BIO-001 tras aprobar RC4; habilidades reutilizan repositorio/fichas. | §4.4: 100 Kotlin/23 Python y ambos CI correctos; especie/categoría, medidas de variedad y habilidades normales/`(oculta)`. Validación visual pendiente; sin atribuir descripción de especie a variedad ni inventar medidas offline. |
| `ABIL-001` — contadores y nombres; **publicada en RC8** | Usuario confirma el alcance; relación habilidad → variedades y sprites ya existentes. Revisión de 311 nombres españoles identifica tres nombres anteriores a generación IX. | §4.7: total unión y contadores normales/ocultas por `pokemonId`, filas sin duplicados conservando slots y alias históricos; 125 Kotlin/23 Python y ambos CI correctos. Validación visual pendiente. Efectos sin traducción española no se inventan ni se certifican como revisados integralmente. |
| `MOV-002` — P3, símbolos y presentación; publicado en RC12 | Referencia PokeInfo y propuesta aprobada; no se copian recursos privados. | §4.12/§13.1: tipo/color, icono y etiqueta Física/Especial/Estado en catálogo/ficha y aprendizaje. Filas adaptables sin otro repositorio; CI correcto, aceptación visual pendiente. |
| `MOV-003` — P3, movimientos Z; pendiente de autorización e investigación de cobertura | Solicitud de filtro adicional, no nueva categoría de daño. | §13.1: conservar filtro general Z solo con relación/marcador verificable y contexto de juego; intersectar con tipo/categoría. Física, Especial y Estado siguen siendo las tres categorías de daño; Z no es una cuarta ni una pestaña de aprendizaje. |
| `MOV-004` — P3, pantalla de aprendizaje por Pokémon; publicada en RC12 | Usuario exige completar el conjunto tras RC11 insuficiente; botón único y cuatro páginas. | §4.12/§13.1: ruta/VM propios, edición/variedad exactas, swipe/tabs, otros métodos y ficha contextual, sin otra descarga por pestaña ni topes a 40. CI correcto, aceptación visual pendiente. |
| `ITEM-002` — objetos por bolsillos; **publicada en RC9** | Usuario acepta organizar el catálogo existente por bolsillos. La fuente fijada aporta 8 bolsillos/54 relaciones, pero no clasificación por edición. | §4.8: mismo catálogo, IDs intactos, grupos de referencia, filtros, sprites/fichas y estado; 134 Kotlin/26 Python y ambos CI correctos. No inferir equipabilidad ni disponibilidad por juego. Validación visual pendiente. |
| `TRAIN-001` — líderes, Alto Mando, Campeón y revanchas; pendiente de autorización | Módulo de líderes y backend Spring Boot existentes, no sistema completo de entrenadores por edición. | Investigar/reutilizar backend y contratos; filtro principal **juego/edición**, región secundaria. No mezclar Rubí/Zafiro, Esmeralda y RO/ZA; verificar personajes, orden, equipos, estadísticas y revanchas por fuente/edición. No inventar ni asumir que PokeAPI cubre todo. Los filtros regionales de Pokédex no se reemplazan por este criterio. |
| `CAP-004` — selector visual de Poké Balls; pendiente de autorización | Tarjetas de Balls actuales; solicitud de botón → diálogo/bottom sheet. No prueba una causa matemática. | Reutilizar `ItemRepository`: sprite real, nombre español, buscador y listado con condiciones documentadas. Balls menos comunes solo con datos/mecánicas compatibles por edición; no hacer una tabla duplicada ni atribuirle `CAP-001`–003. |
| `TEAM-001` — acceso a editar movimientos; pendiente de autorización | Editor existente persiste cuatro movimientos; se solicita acceso por integrante desde pestaña Movimientos. | Añadir `Editar movimientos` por Pokémon abriendo **el mismo editor**, sin otro sistema; preservar cuatro textos, usuario activo, propiedad y variedad exacta. Pruebas de guardar/cancelar/reabrir/cambiar usuario. |
| `SEC-001` — distribución y seguridad; pendiente de revisión autorizada | APK debug con firma comprobada; solicitud de revisar avisos Play Protect, permisos y privacidad. No se ha probado una causa concreta de avisos. | Revisar procedencia/instalación y captura exacta del aviso, manifiesto/permisos necesarios, política de privacidad real y estrategia de firma/actualización. No prometer quitar advertencias, evadir controles ni fabricar documentos; no cambiar certificado ni comprometer datos. |
| `VER-001` — posible numeración 0.x; **solo propuesta** | Usuario considera expresar beta con 0.x, sin aprobar política definitiva. | Mantener 3.x y `versionCode` creciente hasta decidir; no renombrar tags/releases antiguas ni reducir código Android. Pruebas como pre-release y base como release normal solo con autorización aplicable; no asignar una nueva versión a estas tareas. |

**Orden y límites del backlog actualizados tras RC10:** DEX-001–008, BIO-001, ABIL-001/002, EVO-001 e ITEM-002 tienen entregas registradas en §4.1–§4.9; no tratarlas como pendientes de implementar por notas históricas. La prioridad ahora es auditoría/propuesta visual §4.10 y diseño P3, no ejecución conjunta del resto de esta tabla. TEAM, CAP matemático/selector, TRAIN, SEC/VER conservan sus puertas; no cambiar datos/firma/numeración por inferencia ni sustituir v3.6/v3.7/RI. El rediseño de una pantalla no autoriza completar sus nuevas funciones factuales.

Las incidencias se registran en este único plan; **no se han creado issues públicos**. ITEM-001 y MOV-001 están incluidos en la RC1 publicada, aún pendientes de aprobación manual. CAP-001–004 y las mejoras no DEX siguen pendientes, sin implementación autorizada ahora. Conservar las capturas de la conversación como evidencia del usuario; no afirmar que sus archivos se archivaron en el repositorio.

### 13.1 P3 — Interfaz de movimientos inspirada en PokeInfo (DISEÑO EN §4.10)

**Estado actualizado tras el reporte de RC11 (§4.12):** P3 **implementado, verificado técnicamente y publicado en RC12** con ambos CI correctos; aceptación visual pendiente. Se supera la microsecuencia pública V1→V2 por solicitud del usuario, sin volver a esperar permiso por módulo. P3 no es otra versión, catálogo ni hoja de ruta. MOV-003/Z sigue condicionado a investigación factual y no se implementa automáticamente por el rediseño. Mapas completos y RI permanecen fuera de alcance.

**Referencia indicada por el usuario:** video de PokeInfo mostrando la consulta de movimientos de Incineroar. Registrar el flujo descrito sin afirmar que se inspeccionó o archivó el video en esta intervención; cotejar el recurso al preparar el diseño. Recuperar la navegación conceptual, conservando identidad visual de RotomPedia, sin copiar una tabla que resulte ilegible en Android.

#### Pantalla dedicada y métodos de aprendizaje

- Desde el botón único **«Movimientos»** en la ficha Pokémon, abrir una pantalla independiente para el **mismo `pokemonId`/variedad actualmente seleccionado**, no un catálogo general ni la especie base por sustitución. Cuatro páginas deslizadas por gesto horizontal y pestañas táctiles; edición en zona superior (§4.10.6).
- Cuatro pestañas principales: **Por nivel**, **Por huevo**, **MT/MO** y **Tutor**. Nivel muestra el nivel aprendido; crianza/tutores solo donde estén documentados; máquinas muestran su identificación real en la edición seleccionada cuando exista. No derivar números MT/MO de IDs internos.
- Selector de edición/grupo de versiones explícito para no mezclar aprendizaje de juegos distintos. Cambiar de pestaña mantiene Pokémon y edición, sin regresar al detalle.
- Desplazamiento vertical por todos los resultados y acceso a la ficha completa al tocar un movimiento. Al volver, mantener contexto; regreso al Pokémon original con su identidad intacta.
- Un movimiento aprendido por nivel y por MT debe aparecer en ambos métodos de esa edición; conservar los niveles y relaciones originales. Los métodos adicionales de la fuente no se eliminan: concretar su presentación/cobertura al preparar P3, sin ocultar datos ni imponer un máximo arbitrario de resultados.

#### Presentación compacta y adaptada a Android

- Fila/tarjeta compacta con **nombre español verificado**, tipo elemental identificado por color, **icono de categoría Física/Especial/Estado**, potencia y precisión.
- Añadir nivel o referencia MT/MO cuando corresponda; mantener PP y otros datos extensos en la ficha individual, sin borrarlos del modelo/repositorio.
- Valores no aplicables como **«—»**, no como cero. Distinguir ausencias de datos de valores reales; no inventar traducciones, efectos ni referencias de máquinas.
- Encabezados y distribución responsiva para pantallas estrechas/texto grande, sin columnas cortadas, tipografía diminuta ni scroll horizontal obligatorio para entender cada fila. Inspiración conceptual de la tabla del video, no copia literal.
- La selección de edición filtra aprendizaje y textos. No presentar los valores base actuales como estadísticas históricas reconstruidas si la fuente no las sostiene; conservar procedencia y límites existentes de v3.5.

#### Arquitectura y catálogo general

- Reutilizar **`MoveRepository`**, índice de referencia, caché de aprendizaje, alias/traducciones, fichas y relaciones de v3.5-B. No crear otro catálogo ni duplicar las peticiones por movimiento.
- Preservar el Team Builder, el editor existente y sus cuatro textos de movimientos guardados; referencias ambiguas/desconocidas siguen visibles sin reescritura silenciosa.
- El catálogo general conserva/mejora filtros por **tipo elemental**, **categoría de daño** e iconos de categoría (MOV-002), y filtro de **movimientos Z cuando haya cobertura verificable** (MOV-003).
- Esos filtros son distintos de las cuatro pestañas de aprendizaje de un Pokémon. **Z no es una cuarta categoría de daño** ni certifica que el Pokémon lo aprende en cualquier edición.

#### Criterios de aceptación de P3 — revisión en teléfono pendiente

§4.12 registra implementación y pruebas de pantalla/páginas/edición/variedad/ficha/repositorio/editor. Se conservan estas casillas abiertas para la aceptación manual del usuario, no como afirmación de que el código no existe. El filtro Z sigue excluido y pendiente de cobertura verificable; no marcarlo completado por tener iconos de las tres categorías.

- [ ] Pantalla dedicada a movimientos por Pokémon y variedad exactos.
- [ ] Pestañas Nivel, Huevo, MT/MO y Tutor, conservando varios métodos del mismo movimiento.
- [ ] Tipo con color y categoría con icono visualmente identificables.
- [ ] Potencia y precisión visibles; no aplicables como «—».
- [ ] Nivel y referencia de máquina cuando estén documentados para esa edición.
- [ ] Aprendizaje/textos diferenciados por edición, sin mezcla de juegos ni valores históricos inventados.
- [ ] Nombres españoles cuando exista traducción verificada; fallback identificado donde falte.
- [ ] Acceso a ficha completa con PP y otros datos; regreso conserva Pokémon, pestaña y edición.
- [ ] Diseño cómodo en diferentes tamaños de pantalla y texto, con scroll por todos los resultados.
- [ ] Integración con `MoveRepository`, cachés y relaciones existentes, sin segundo catálogo ni topes arbitrarios.
- [ ] Filtros generales de tipo/categoría/Z e iconos preservados/mejorados, separados de los métodos de aprendizaje.
- [ ] Sin regresiones en Team Builder, usuario activo, identidades ni los cuatro movimientos guardados; pruebas de navegación, métodos/edición y persistencia.

**Puerta de inicio actual:** propuesta aprobada en §4.11; mantener secuencia progresiva V1→V2 y sus verificaciones. La maqueta HTML no es código Android ni entrega de P3; no marcarlo implementado hasta probar y publicar su corte correspondiente. No autoriza MOV-003/Z ni ampliar datos por inferencia.

---

## 14. Continuación y límites de esta revisión

- [x] Adoptar el adjunto como plan principal y conservar A/B/C terminadas.
- [x] Vincular entrega y evidencias reales de v3.4, sin una auditoría extensa para rehacerla.
- [x] Diagnóstico acotado de objetos, movimientos y habilidades; registrar reporte manual y deudas concretas.
- [x] Preparar propuesta de v3.5 con fuentes, reutilización, aceptación y exclusiones.
- [x] Obtener aprobación para A y posteriormente B/C. Captura sigue diferida.
- [x] Implementar y verificar técnicamente A/B/C autorizadas; actualizar este documento (§15 y §17).
- [ ] Validación manual de v3.5 en Android por el usuario.
- [ ] v3.6: reutilizar `MapScreen.kt` (ya tiene zoom/pan y pines en un mismo contenedor transformado), `RegionLocationData.kt` y `LocationDetailBottomSheet.kt`; no empezar de cero. Falta contexto de edición en el modelo local de encuentros, cobertura cartográfica comprobada y validación de hitboxes. Elegir piloto y permisos de activos antes de expandir.
- [ ] v3.7: reutilizar `RotomCommentaryEngine.kt` y `RotomThemeManager.kt`; ya existen comentarios en detalle y temas. Falta el motor contextual transversal, prioridades/cooldown, controles de frecuencia y accesibilidad. No describirlo como inexistente ni como completo. IA opcional sigue sin autorizar.
- Alcance de la revisión documental original: solo plan/README. Las implementaciones posteriores de Kotlin/Room/build y la publicación integrada se conservan en §15 y §17; el pulido de Pokédex publicado como RC2 y la política permanente están en §4.1 y §8.1.

---

## 15. Ejecución autorizada: v3.5-A — Objetos

Esta sección es posterior a la revisión documental de §14; no cambia retroactivamente su alcance ni el código/tag publicado de v3.4.

**Registro histórico de la entrega inicial:** rama `feature/v3.5-a-items`, entonces solo desarrollo local sin push/tag/release ni incremento de versión. Más tarde se incluyó A en la pre-release integrada de §17. En ese hito no se modificaron movimientos, habilidades ni cálculo de captura; los cambios en detalle/evoluciones son enlaces a objetos oficiales.

**Commits de implementación:** `66f6c95` (índice, repositorio, caché y migración), `3f589cd` (enciclopedia, enlaces y pruebas de interfaz). Esta rama también conserva el commit documental previo `47df886`; la release pública sigue apuntando a `5d67ea0`.

### Entrega de A

- Índice de **2223 objetos**, **2218 con nombre español**, generado por `tools/generate_item_index.py` desde CSV oficiales fijados a revisión Git `e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff`; cinco nombres sin español tienen fallback identificado. Las 54 categorías oficiales tienen etiquetas españolas de UX; sin duplicar IDs o estadísticas.
- `data/items/{ItemModels,ItemDao,ItemRepository,ItemPresentation}.kt` (rutas base de §11): `ItemRepository` compartido en `AppContainer`, búsqueda/filtros locales, detalles PokeAPI bajo demanda con validación de identidad y caché persistente. Fichas frescas se reutilizan durante 24 h; actualización manual y copia guardada cuando falla la consulta. Detalles nunca descargados requieren red.
- Room **8→9** añade únicamente `item_index`, `item_detail_cache`, `item_index_state`. Todas las definiciones de tablas existentes de v8 son idénticas en v9; no se agrega equipamiento o habilidad a miembros, ni se reescriben `move1`–`move4`, usuarios/favoritos/EV/IV.
- Se adapta `CompetitiveItemsScreen.kt`, conservando la ruta de entrada, a enciclopedia con listado lazy y ficha: sprite del objeto (no del Pokémon), nombre/categoría, atributos, texto con idioma/grupo de versiones, efecto y precio por edición cuando la API los proporciona. Ausencias no equivalen a precio cero, incompatibilidad para llevarlo o ausencia total de uso.
- Nuevo enlace desde objetos de evolución y retorno desde Pokémon que llevan objetos en encuentros. Las identidades/nombres nacionales se obtienen del `PokemonRepository` existente; los porcentajes de portadores son por juego y no una lista de compatibilidad para equipar.
- `CompetitiveItemData.kt` se sustituye por el índice factual y `ItemEditorial`: se conservan cuatro consejos revisados y etiquetados (Cinta/Gafas Elección, Restos, Baya Acardo). No se trasladan como hechos oficiales los efectos y recomendaciones antiguas sin revisión. El resto del catálogo anterior permanece en el historial Git, no como catálogo paralelo activo.
- Corrección de `ITEM-001`: Baya Acardo es `shuca-berry`, ID 168, protección ante Tierra. Efecto español traducido y revisado, con procedencia explícita; no se presenta como texto oficial español del efecto. Se oculta el flavor text español inconsistente de X/Y, manteniendo el aviso de por qué falta.
- Upstream contiene dos IDs con slug `roseli-berry` (723 y 2279), verificados mediante API. Se conservan ambos en el índice; enlaces por slug ambiguo se resuelven contra el recurso canónico, no el primer ID arbitrario. Sin resolución disponible se informa y se permite buscar por ID.
- La API actual usa `prices` con compra/venta/moneda/grupo de versiones; puede estar vacío. No se transforma el `cost` genérico del CSV o de esquemas antiguos en precio universal. Los textos sin español tienen fallback inglés identificado; no se promete traducción integral de todos los efectos.

### Verificación y cierre

- Snapshot verificado: 2223 objetos; **15 pruebas Python correctas** (12 anteriores + 3 de objetos).
- Comparación automática de esquemas: todas las tablas de v8 sin cambios, solo tres tablas nuevas.
- Pruebas nuevas: índice/búsquedas sin API, bayas y categorías, caché/reapertura offline, concurrencia, identidades erróneas, snapshot reasignado, slugs duplicados, idioma/edición/precio, sprite, migración v8 y navegación de objetos.
- Validación: **52 pruebas Kotlin correctas** (39 previas + 13 nuevas, incluidas tres de interfaz); `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` correcto, lint sin errores y con advertencias. Se probó además el paso carga→detalle de la ficha asíncrona, evitando que la ventana quedase mostrando carga tras recibir los datos.
- Evidencias nuevas: `ItemRepositoryTest.kt`, `ItemPresentationTest.kt`, `ItemUiTest.kt`, extensión de `RoomPersistenceTest.kt`, `tools/tests/test_item_index.py` y esquema exportado `9.json`. El workflow de verificación se amplía a ramas v3.5 y valida ambos snapshots. No se ha ejecutado CI remoto porque no se autorizó push.
- La aprobación en Android real permanece pendiente; no convertir compilación o Robolectric en validación de dispositivo. No instalar versiones antiguas sobre una base migrada a v9.
- Puerta histórica: presentar A y esperar autorización de B. Después se autorizó B/C y su pre-release integrada (§17). La política posterior de §8.1 ya permite tags/pre-releases de fases autorizadas, incluida Pokédex (§4.1), sin otra aprobación; estables siguen requiriéndola.

---

## 16. Proyecto futuro — Rotom Intelligence Pipeline

**Estado:** [ ] Pendiente de investigación y prototipo; **sin iniciar ni autorizar implementación**.\
**Momento propuesto:** después de finalizar v3.5, con integración progresiva en la etapa de Rotom contextual (v3.7).\
**Prioridad:** no interferir con v3.5 ni retrasar innecesariamente los mapas de v3.6. No constituye una nueva versión numerada ni se asigna automáticamente a v4.x.

### 16.1 Objetivo

Convertir a Rotom en un acompañante inteligente y contextual dentro de RotomPedia, capaz de compartir curiosidades verificadas, comentarios espontáneos y pequeños consejos relacionados con las acciones del usuario.

**Rotom NO será un chatbot convencional.** Su propósito es enriquecer la experiencia de exploración sin exigir conversaciones directas.

### 16.2 Arquitectura híbrida propuesta

1. **Knowledge Pipeline:** proceso de búsqueda, extracción, normalización y validación de información Pokémon. Investigar fuentes autorizadas, permisos, cobertura y revisión de datos; la descarga por sí sola no convierte una afirmación en un hecho validado.
2. **Knowledge Base:** almacenamiento de curiosidades y fuentes, ligado a las identidades existentes de especie/variedad/forma y al contexto de juego. Registrar referencias, fechas de consulta/revisión, idioma y procedencia de cada dato. No sustituir ni duplicar arbitrariamente el índice nacional/repositorios existentes.
3. **Context Engine:** relaciona información disponible con pantalla, Pokémon y acciones del usuario; selección local de comentarios basada en contexto verificable, prioridad y frecuencia.
4. **Personality Engine:** expresiones, estilo de habla, animaciones y comportamiento de Rotom, separado de los hechos y de su validación. Reutilizar lo que ya aportan `RotomCommentaryEngine.kt` y `RotomThemeManager.kt` cuando sea útil.
5. **IA generativa opcional:** reformulación de comentarios a partir de información previamente verificada, sin crear hechos nuevos ni ser una dependencia obligatoria.

Flujo conceptual, aún no implementado:

```text
Fuentes autorizadas → extracción/normalización → validación → Knowledge Base
                                                               ↓
Contexto local + acción + identidades/juego → selección de hechos verificados
                                                               ↓
Personalidad + controles de frecuencia → comentario breve en español
                       ↘ IA opcional para reformular, si se autoriza ↗
```

Los prototipos futuros deben tener contratos y métricas acotados, no una reescritura de v3.4/v3.5. Registrar origen/edición también cuando dos fuentes discrepen; no resolver contradicciones con una redacción convincente de IA.

### 16.3 Funcionalidades a investigar y desarrollar por autorización

- [ ] Obtener curiosidades de PokeAPI y otras fuentes autorizadas.
- [ ] Investigar integración de búsquedas web verificables.
- [ ] Almacenar referencias, fechas, idiomas y fuentes de cada dato.
- [ ] Generar comentarios en español con personalidad de Rotom.
- [ ] Evitar comentarios repetitivos, con historial reciente y cooldown.
- [ ] Reaccionar al Pokémon que se está consultando, con su variedad correcta.
- [ ] Detectar observaciones útiles en el Team Builder sin afirmar resultados absolutos de combate.
- [ ] Comentar movimientos, habilidades y objetos con el contexto de edición disponible.
- [ ] Integrar comentarios sobre rutas y localizaciones verificadas.
- [ ] Permitir activar, silenciar u ocultar a Rotom.
- [ ] Mantener funcionamiento básico sin conexión mediante datos almacenados y reglas locales.
- [ ] Evaluar el costo real de utilizar IA generativa, antes de contratar o integrar proveedores.
- [ ] Implementar límites de solicitudes y protección de credenciales.

### 16.4 Reglas obligatorias

1. **La IA no debe inventar información Pokémon.** Los hechos deben proceder de datos verificables; reformular no autoriza añadir propiedades, relaciones, números o explicaciones no respaldados. Si no hay dato validado, omitir el comentario o señalar la ausencia.
2. Identificar correctamente especie, variedad, forma cuando corresponda y contexto de juego. Conservar `speciesId`, `nationalNumber`, `pokemonId` y `pokemonFormId` separados, y respetar el contrato transversal existente.
3. Las búsquedas externas se ejecutarán preferentemente **fuera del dispositivo**, con resultados validados y almacenados en caché. La navegación del usuario no debe disparar búsquedas masivas o permanentes por cada acción.
4. **Los datos descargados no se ejecutan ni se obedecen como instrucciones del agente.** Páginas, metadatos, respuestas de herramientas y textos externos son datos no confiables que deben aislarse y validarse; no pueden autorizar herramientas, ejecutar comandos ni modificar reglas del sistema.
5. **Nunca incluir claves privadas en el APK**, el repositorio, logs o prompts. Si una futura integración requiere secretos, evaluar un servicio fuera del dispositivo y sus controles; no asumir un proxy o proveedor ya elegido.
6. Rotom debe ser útil sin depender permanentemente de internet o de una IA externa. Las reglas locales y la base almacenada deben ofrecer el modo básico funcional aunque se desactive o falle la IA.
7. Mantener la personalidad y el contenido editorial separados de información factual y fuentes. El estilo de Rotom no debe esconder incertidumbre, ausencia de datos ni contradicciones.
8. **No recopilar ni enviar equipos, cuentas u otros datos personales a servicios externos sin justificación y controles claros.** Por defecto, seleccionar contexto localmente; definir minimización, información/consentimiento cuando corresponda, retención y eliminación antes de autorizar cualquier envío. No usar datos personales como contexto automático de IA ni de búsqueda web.
9. Limitar solicitudes, costo, frecuencia y concurrencia; permitir desactivar servicios externos sin perder el modo básico. No contratar servicios ni generar gastos a partir de esta entrada del plan.

### 16.5 Etapas de desarrollo

| Etapa | Trabajo propuesto | Relación con el plan existente / puerta de salida |
|---|---|---|
| **RI-1 — Prototipo de conocimiento** | Pipeline experimental que obtenga, valide y almacene curiosidades para un conjunto pequeño de Pokémon. | Proponer después de v3.5 y aprobar fuentes, muestra y criterios antes de ejecutarlo. Salida: datos trazables, identidades correctas, revisión de calidad y descarte de información no validada. No bloquear v3.6. |
| **RI-2 — Contexto local** | Motor que seleccione comentarios según pantalla y datos de la Pokédex, sin IA en tiempo real. | Complementa v3.7-B. Salida: reglas probadas, contexto de variedad/juego, control de repetición y funcionamiento offline. No crear un segundo motor incompatible. |
| **RI-3 — Personalidad** | Expresiones, animaciones, frecuencia de comentarios y preferencias del usuario. | Complementa v3.7-A. Salida: activar/silenciar/ocultar, animaciones moderadas y navegación no obstruida. |
| **RI-4 — IA experimental** | Evaluar un modelo de lenguaje de bajo costo para variar redacción, solo con hechos verificados y formato de respuesta limitado. | Complementa v3.7-C; aprobación independiente de proveedor, presupuesto y privacidad. Medir costo/calidad y rechazar respuestas que añadan hechos o incumplan formato. Si falla, volver al comentario local, no inventar. No bloquea el modo local ni su entrega. |
| **RI-5 — Integración completa** | Conectar Rotom con Pokédex, Team Builder, movimientos, objetos y mapas manteniendo una experiencia discreta. | Integración progresiva sobre módulos disponibles y validados; no exige IA. Salida: pruebas de contexto, rendimiento, privacidad, offline y control del usuario en cada pantalla incorporada. |

El orden de presentación RI-1…RI-5 no hace obligatoria RI-4: se puede integrar conocimiento/contexto/persona sin IA generativa. Cada etapa necesita autorización propia o un alcance conjunto explícitamente aprobado.

### 16.6 Criterios de aceptación

- [ ] Rotom comenta información correcta y contextual, comprobable contra hechos/fuentes almacenados.
- [ ] No necesita un chat para interactuar con el usuario.
- [ ] No interrumpe constantemente la navegación ni tapa acciones importantes.
- [ ] Los comentarios tienen procedencia verificable; se conserva trazabilidad aunque la IA varíe la redacción.
- [ ] Puede funcionar sin conexión mediante datos almacenados; desactivar la IA no rompe el modo básico.
- [ ] El usuario controla su nivel de actividad, incluida la posibilidad de silenciarlo u ocultarlo.
- [ ] El sistema no compromete rendimiento ni privacidad; probar solicitudes/costos acotados, límites de frecuencia y ausencia de envíos personales no autorizados.

### 16.7 Alcance de esta incorporación

Se registra la propuesta del usuario y se alinea con v3.7; **no se ha implementado pipeline, base de curiosidades, búsqueda web, proveedor de IA ni motor nuevo**. No se aprobaron presupuesto, credenciales, recopilación de datos ni despliegue de un backend adicional. v3.4 permanece terminada; v3.5-A/B/C están publicadas en RC1 y pendientes de validación manual (§17). La Pokédex post-RC1 (§4.1) no incluye RI, captura ni mapas y no mueve sus prioridades.

---

## 17. Ejecución autorizada: v3.5-B/C — Movimientos y habilidades

> **Actualización de autorización posterior al cierre técnico:** el usuario indicó publicar siempre las entregas de prueba como pre-release y las versiones base `X.Y.0` como release normal para poder probar desde el teléfono. Se autoriza push de esta entrega, tag y pre-release `v3.5.0-rc1` con A/B/C integradas. Esto sustituye las restricciones anteriores de «sin publicación autorizada» para esta entrega, pero no autoriza cerrar v3.5 estable, implementar captura/mapas/RI ni publicar pruebas como estables. Mantener APK firmado con certificado comprobado, código de versión creciente, checksum y notas de migración; no publicar automáticamente APK con una clave efímera de CI.

**Fecha:** 4 de noviembre de 2026. **Rama:** `feature/v3.5-bc-encyclopedia`, incluye A y la planificación de RI. Autorización: continuar los hitos restantes de v3.5 mediante «sigue lo que tenemos que terminar para la 3.5», «sigue» y «siguele bro». Sin push/tag/release, cambios de firma ni incremento de versión Android.

**Commit de implementación y pruebas:** `e382d67`. Incluye repositorios, snapshot, migración, interfaz y correcciones finales de las seis pruebas UI; esta documentación se conserva en un commit local posterior.

### Implementación y cobertura

- `data/reference/ReferenceModels.kt`, `ReferenceRepositories.kt`, `ReferencePresentation.kt`: `ReferenceStore` compartido con validación SHA-256, `MoveRepository` y `AbilityRepository` en `AppContainer`. No se crea otro catálogo Pokémon.
- `reference-index.json`: **937 movimientos** (937 nombres españoles), **374 habilidades** (311 nombres españoles), **2943 relaciones de habilidad por variedad** y **2372 relaciones MT/MO–movimiento–objeto–edición**. Revisión PokeAPI fijada `e2710d30d14b3f4dc6a02a4a71e8619cd835b7ff`; generador de mantenimiento `tools/generate_reference_index.py`, sin descarga durante builds normales ni una petición por movimiento/búsqueda.
- Aprendizaje completo bajo demanda desde `pokemon/{pokemonId}`, conserva `(pokemonId, moveId, versionGroup, método, nivel)` y valida IDs/slugs. Caché Room y fallback guardado ante fallo de actualización. Los datos no descargados requieren conexión; no se ocultan fallos bajo una lista parcial.
- `ui/screens/ReferenceScreens.kt`: enciclopedias, fichas, aprendizaje y selector compartidos. Edición obligatoria para aprendizaje normal; opción general explícita «no acredita aprendizaje». Paginación de 40 con contador, sin límites 60/30. Cabecera y resultados usan una sola lista desplazable para no agotar el espacio con errores/filtros.
- `PokemonMoveData` queda como compatibilidad de alias español/inglés/slug del snapshot. El editor y equipo reaccionan a su carga; texto desconocido o ambiguo sigue visible y no se reescribe. Se retiran catálogo estático y carga remota por movimiento del ViewModel.
- Pokémon ↔ habilidad conserva identidad de variedad, normal/oculta y slot; Ninetales de Alola usa `10104`, no `38`. MT/MO ↔ movimiento ↔ objeto conserva IDs exactos y edición. Sin equipar objetos/habilidades automáticamente ni fabricar relaciones de sinergia.
- Nombres y descripciones españoles cuando existen; fallback inglés identificado. La edición filtra textos, aprendizaje y MT/MO, **no reconstruye estadísticas históricas de movimientos ni historial de habilidades**. Sin legalidad competitiva certificada ni sprites individuales inventados. Se conserva la política de especie/variedad de v3.4.

### Preservación y verificación

- **Room v10:** migración aditiva `9→10`, solo `pokemon_learnset_cache`; comparación automática de `9.json` y `10.json` confirma todas las tablas anteriores idénticas. `RoomPersistenceTest` verifica cuentas/equipos, ID técnico, nivel, EV/IV, texto legado, favoritos y caché de objetos. Las migraciones anteriores siguen registradas, sin fallback destructivo.
- **66 pruebas Kotlin, 0 fallos, 0 errores, 0 omitidas; 19 pruebas Python correctas.** `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain` correcto. Lint: sin errores, 110 advertencias y 30 sugerencias; no se afirma ausencia de deuda previa.
- `ReferenceRepositoryTest`: identidades, más de 60 movimientos, varios métodos/niveles/ediciones, concurrencia, reapertura offline, alias, habilidad regional y corrupción. `ReferenceUiTest`: seis casos, incluidos paginación sin refetch, texto legado, texto español por edición y navegación por ID de MT/variedad.
- Los dos fallos finales se corrigieron sin omitir pruebas: lista sin altura al aparecer errores y selección de una edición fuera del viewport del menú. Las seis pruebas UI pasan; ejecución completa correcta en aproximadamente minuto y medio. Tareas unitarias limitadas a cinco minutos para evitar otra espera indefinida.
- `tools/tests/test_reference_index.py` y `--verify-only` verifican snapshot y enlaces con los índices Pokémon/objetos. Workflow local actualizado para validar el snapshot; sin CI remoto porque no hay push autorizado.
- APK de app: `RotomPedia/app/build/outputs/apk/debug/app-debug.apk`; APK de instrumentación ensamblado, **no ejecutado en dispositivo**. Conservan metadatos `3.4.0-rc1` / `30402`; el APK público sigue siendo RC1 con Room v8, no estos cambios locales.

### Puerta manual pendiente

- [ ] Validar en Android el recorrido catálogo → ficha, filtros/paginación y aprendizaje por edición en Typhlosion de Hisui y otras variedades; comparar métodos y niveles.
- [ ] Validar Mar Llamas, habilidad oculta de Ninetales de Alola y retorno a la variedad exacta; MT/MO por edición y navegación al objeto correcto.
- [ ] Reabrir offline tras descargar aprendizaje/detalles; verificar error/reintento donde no exista copia y texto grande/pantalla estrecha.
- [ ] Comprobar cuentas, equipos, favoritos, EV/IV y cuatro textos de movimientos preexistentes tras actualización. No desinstalar ni instalar APK antiguo sobre Room v10.

No hay dispositivo/emulador conectado para esta validación. **Cierre técnico, no cierre manual ni versión base estable de v3.5.** CAP-001–003, v3.6, v3.7 y RI continúan pendientes; no se han implementado en este trabajo.

### Publicación posterior autorizada — v3.5.0-rc1

- Publicada el 4 de noviembre de 2026 como **pre-release**, `draft=false`, `prerelease=true`, sin marcarla Latest: [descarga y notas](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc1). Incluye A/B/C; no se fabrican entregas retrospectivas A/B separadas.
- Rama subida y tag anotado `v3.5.0-rc1` en `a857f7e681c290c71a9aa5605bf3e00d507d9cc3`. El commit documental posterior no cambia el APK/tag.
- APK `RotomPedia-v3.5.0-rc1.apk`, 23857077 bytes, SHA-256 **`5335f6413d3a0045eb01a28bb1e002d5668b5574fcdd1dccf75ac15227b59e25`**, coincidente con el digest del asset GitHub; archivo `.apk.sha256` incluido.
- `versionName=3.5.0-rc1`, `versionCode=30501`, aplicación `com.duoc.rotompedia`, mínimo API 25. Firma debug verificada por `apksigner`, mismo certificado que el APK publicado v3.4.0-rc1 (certificado SHA-256 `5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa`). El APK anterior se contrastó con su digest público antes de comparar firmas.
- Migraciones v8→9→10 aditivas; no desinstalar ni volver a un APK con esquema menor. Compatibilidad de firma comprobada con RC1 anterior, no con todas las versiones históricas.
- Reejecutado `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest`: correcto. CI del tag/commit: [Verify RotomPedia](https://github.com/ArukuKun/RotomPedia/actions/runs/41773940435) y [Release APK](https://github.com/ArukuKun/RotomPedia/actions/runs/41773940114), ambos **success**.
- Workflow de tags verifica y conserva artefacto CI, pero no reemplaza assets instalables con APK de clave efímera. Publicación explícita del APK comprobado; pruebas futuras van como pre-release y bases `X.Y.0` como release normal una vez cerradas, conforme a la instrucción permanente del usuario.

Las menciones anteriores de «sin push/tag/release» describen el cierre técnico previo a esta autorización y publicación; no son el estado actual. La puerta manual sigue pendiente para que el usuario pruebe esta descarga en su teléfono.


### v3.6 — Dirección visual global aprobada (10 de noviembre de 2026)

**Aprobación del usuario:** usar el dashboard de fondo claro con tipografía de alto contraste, barra superior con carita Rotom fiel a `rotomfondo.png`, y navegación inferior de cinco accesos: Inicio, Pokédex, Mapas, Equipos y Más. No convertir módulos en pantallas ficticias: Más agrupa Líderes y Alto Mando, Objetos, Habilidades, Movimientos, Tabla de tipos, comparador y calculadoras. La ilustración grande de Rotom no debe tapar la lectura fuera de Home. Conservar el acceso por región, Kanto destacado y la memoria del último mapa, y permitir seleccionar ediciones dentro de las zonas. El usuario aprobó un diseño visual, no validó aún una APK.

**Implementación local inicial (sin APK ni publicación):** `RotomHomeScreen`, `RotomGlobalNavigationBar`, `RotomFaceAction`, `RotomPageScaffold` y navegación raíz; la carita se recorta del recurso original y no se inventan imágenes externas. Pendiente verificar compilación real, pruebas UI, layout a gran escala, contraste en oscuro y compatibilidad con páginas legacy; no tocar Room, firma, versiones ni releases anteriores.
