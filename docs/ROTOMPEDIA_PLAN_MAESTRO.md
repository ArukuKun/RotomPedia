# RotomPedia — Plan maestro de desarrollo

**Marca / proyecto:** RotomPedia\
**Visión:** Pokédex y enciclopedia Pokémon en español, inspirada en la experiencia de PokeInfo de Bruno Sousa, con identidad propia, herramientas conectadas, mapas interactivos y un Rotom contextual.\
**Versión publicada actual:** **v3.5.0-rc1**, pre-release con A/B/C; validación manual y estabilización pendientes, no versión estable.\
**Propósito:** documento de continuidad para OpenCode; conservar el contexto de lo realizado y planificar lo pendiente **sin volver a empezar desde cero**.\
**Regla de alcance:** implementar solo fases autorizadas por el usuario. La política permanente de §8.1 autoriza commits, push, tags nuevos y pre-releases de esas fases sin pedir permiso adicional; no autoriza fases nuevas, versiones estables, cambios de firma ni operaciones destructivas. No modificar tags/releases anteriores.

**Documento principal del repositorio desde el 3 de noviembre de 2026.** Se adopta el adjunto `ROTOMPEDIA_PLAN_MAESTRO_v3.4_en_adelante.md`, conservando sus fases y principios. Los apartados 11–14 incorporan el contraste con el código y la propuesta para aprobación. Los documentos de v3.3 son históricos, no el plan vigente.

**Estado vigente:** v3.4-A/B/C terminadas. **Pre-release pública actual `v3.5.0-rc1`** con A/B/C integradas, rama `feature/v3.5-bc-encyclopedia`, tag en `a857f7e`; pruebas locales y CI correctas, validación del usuario en Android pendiente (§17). Política autorizada: pruebas/hitos como pre-release; base `X.Y.0` como release normal al cerrar la versión. No se autorizaron corrección de captura ni implementación de mapas/RI. Los bugs del apartado 13 no reabren v3.4 globalmente.

**Continuidad posterior a RC1:** este archivo es y seguirá siendo el **único Plan Maestro**, sin hoja de ruta paralela ni renumeración de fases. La mejora de Pokédex (§4.1) está implementada en `feature/v3.5-pokedex-polish`; se prepara su pre-release `v3.5.0-rc2` bajo la nueva política permanente de §8.1. Las otras observaciones siguen en §13, sin autorización de implementación conjunta. Conservar firma compatible y aumentar `versionCode` para cada APK de prueba; numeración `0.x` solo en evaluación. Las prohibiciones anteriores de push/tags/pre-releases de hitos autorizados quedan sustituidas por §8.1.

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

**Estado de ejecución:** implementación y verificación técnica completadas; preparación de RC2 para validación manual. No sustituir el APK de RC1. Backlog restante en §13; no iniciar otra mejora al publicar esta prueba.

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

---

## 5. v3.6 — Mapas interactivos inspirados en PokeInfo (PENDIENTE)

**Objetivo:** transformar los mapas actuales, que tienen algunos puntos interactivos, en una herramienta útil para explorar regiones, rutas, cuevas, ciudades y encuentros por juego.

### v3.6-A — Investigación y piloto

- [ ] Revisar videos/capturas del **PokeInfo original de Bruno Sousa**, paquete `com.brunosousa.pokeinfo`. No confundirlo con aplicaciones homónimas.
- [ ] Documentar navegación real: elegir región/juego → explorar mapa → tocar ruta/zona → consultar Pokémon, objetos, condiciones y acceder a ficha.
- [ ] Elegir **una región y un conjunto concreto de ediciones** como piloto. Hoenn puede ser candidata por contar ya con mapa, pero no imponerla sin evaluar datos y recursos.
- [ ] Verificar origen, permisos y calidad de los mapas; no copiar recursos privados de otra aplicación.
- [ ] Separar cartografía (imagen, dimensiones, puntos/polígonos normalizados), ubicaciones (región/juego/área) y encuentros (método, nivel, versión, horario, probabilidades, condiciones).
- [ ] Definir fuente y cobertura de objetos de cada zona; PokeAPI por sí sola no garantiza datos cartográficos completos ni objetos localizados.

### v3.6-B — Experiencia de mapa

- [ ] Zoom y pan suaves; imagen, marcadores y áreas táctiles usan la misma transformación de coordenadas.
- [ ] Puntos e hitboxes fieles a la ubicación, sin marcadores flotando al hacer zoom.
- [ ] Seleccionar región, juego/edición, ciudad, ruta, cueva y otras áreas disponibles.
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

1. **Ahora: entregar la Pokédex post-RC1 (§4.1)**, única implementación autorizada: implementación y pruebas completadas; preparar/publicar RC2 bajo §8.1, registrar evidencia y enlace de APK.
2. **Puerta manual:** presentar el resultado y completar/revisar observaciones de Android, sin declarar v3.5 estable solo por tests. Detenerse antes de otra mejora.
3. **Seleccionar la siguiente tarea de estabilización de §13 con autorización**, acotada por módulo; no ejecutar todo el backlog ni convertirlo en requisito ilimitado que posponga mapas. Matemática de captura y selector de Balls son trabajos distintos.
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
| v3.5 post-RC1 — Pokédex | Implementada y verificada; preparando RC2 como prueba, no fase nueva | `feature/v3.5-pokedex-polish`, `7515df6`, `c52319d` | 77 Kotlin/21 Python; publicación autorizada por §8.1, validación manual pendiente (§4.1) |
| v3.6 | ⬜ Pendiente | — | Mapas por juego/región |
| v3.7 | ⬜ Pendiente | — | Rotom contextual e interfaz |
| Rotom Intelligence Pipeline | ⬜ Investigación y prototipo pendientes; sin iniciar | Sin rama/código de RI | Después de v3.5; integración progresiva con v3.7, sin retrasar v3.6 (§16) |
| v4.x y posteriores | Sin alcance aprobado | — | Mantener el README y este plan al definir cada nueva serie; RI no tiene asignada una v4.x |

**Última actualización del plan:** 4 de noviembre de 2026.\
**Próxima puerta:** entregar RC2 de Pokédex por GitHub (§4.1/§8.1) para validación de Android; después esperar autorización de otra tarea. Nunca promover a estable automáticamente.

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
| `DEX-001` — nacional limpio y proporciones propias; **autorizado ahora** | Observación Android: filas/sprites reducidos y variedades desplegables no deseadas en la Pokédex normal. Referencia propia `2590bf5` frente a `248c922`. | §4.1: 1025 especies ordenadas, sin hijos/desplegables; recuperar sprite 72 dp/nombre 20 sp, sin retirar formas del detalle/picker. Validación visual en teléfono pendiente. |
| `DEX-002` — filtros verificables; **autorizado ahora** | Tipos/origen/generación ya existen; faltan grupos huevo y categoría Gigamax diferenciada. | §4.1: ampliar el índice existente desde la revisión fijada; combinar grupo huevo, tipo, categoría, generación/origen y favoritos. Relación de especie explícita, cobertura real, sin catálogos factuales manuales. |
| `DEX-003` — Lista/Cuadrícula; **autorizado ahora** | Solicitud post-RC1; lista como defecto y cuadrícula de tres columnas sin máximo de nueve. | §4.1: componentes lazy, claves estables, sprite/número/nombre/tipos, resultados completos y estado compartido al alternar; navegación/favoritos intactos. |
| `BIO-001` — ficha biológica compacta; pendiente de autorización | Usuario solicita reorganizar bajo imagen/descripción; habilidades ya tienen repositorio/fichas. | Especie/categoría, altura y peso con unidades y procedencia; habilidades normales y `(oculta)` en bloque compacto, conservando fichas detalladas. No atribuir descripción de especie a variedad sin especificidad verificada. |
| `ABIL-001` — contadores y traducciones; pendiente de autorización | Ya existe habilidad → variedades asociadas en v3.5-C. Se reportan traducciones incorrectas sin aportar aún la lista exacta de nombres. | Añadir contadores **de variedades distintas (`pokemonId`)** normales y ocultas; deduplicar cada rol, indicar que una variedad podría pertenecer a ambos si la fuente lo registra. Si se ofrecen especies, conteo separado por `speciesId`, nunca mezclar unidades. Revisar muestras/traducciones con fuentes españolas verificables, sin reemplazos inventados ni reimplementar el enlace existente. |
| `MOV-002` — símbolos y presentación; pendiente de autorización | v3.5 ya expone tipo/categoría en texto y filtros por ambos en el catálogo; no son funciones inexistentes. | Pulir tipo elemental y símbolo Física/Especial/Estado, conservar y validar filtros actuales, fichas, métodos/niveles/ediciones y repositorio. No reconstruir el catálogo desde cero. |
| `MOV-003` — movimientos Z; pendiente de autorización e investigación de cobertura | Solicitud de filtro adicional, no nueva categoría de daño. | Relación/marcador de movimiento Z desde fuente verificable y contexto de juego; intersectar con tipo/categoría. Física, Especial y Estado siguen siendo las tres categorías de daño; no clasificar Z como cuarta. |
| `ITEM-002` — mochila por bolsillos; pendiente de autorización | `ItemRepository`, sprites y categorías oficiales ya existen. | Explorar Poké Balls, Medicina, Bayas, MT/MO, clave, equipables y otros bolsillos documentados. Usar relaciones de fuente/edición, no una organización universal inventada ni equipabilidad inferida solo del bolsillo; declarar cobertura. |
| `TRAIN-001` — líderes, Alto Mando, Campeón y revanchas; pendiente de autorización | Módulo de líderes y backend Spring Boot existentes, no sistema completo de entrenadores por edición. | Investigar/reutilizar backend y contratos; filtro principal **juego/edición**, región secundaria. No mezclar Rubí/Zafiro, Esmeralda y RO/ZA; verificar personajes, orden, equipos, estadísticas y revanchas por fuente/edición. No inventar ni asumir que PokeAPI cubre todo. Los filtros regionales de Pokédex no se reemplazan por este criterio. |
| `CAP-004` — selector visual de Poké Balls; pendiente de autorización | Tarjetas de Balls actuales; solicitud de botón → diálogo/bottom sheet. No prueba una causa matemática. | Reutilizar `ItemRepository`: sprite real, nombre español, buscador y listado con condiciones documentadas. Balls menos comunes solo con datos/mecánicas compatibles por edición; no hacer una tabla duplicada ni atribuirle `CAP-001`–003. |
| `TEAM-001` — acceso a editar movimientos; pendiente de autorización | Editor existente persiste cuatro movimientos; se solicita acceso por integrante desde pestaña Movimientos. | Añadir `Editar movimientos` por Pokémon abriendo **el mismo editor**, sin otro sistema; preservar cuatro textos, usuario activo, propiedad y variedad exacta. Pruebas de guardar/cancelar/reabrir/cambiar usuario. |
| `SEC-001` — distribución y seguridad; pendiente de revisión autorizada | APK debug con firma comprobada; solicitud de revisar avisos Play Protect, permisos y privacidad. No se ha probado una causa concreta de avisos. | Revisar procedencia/instalación y captura exacta del aviso, manifiesto/permisos necesarios, política de privacidad real y estrategia de firma/actualización. No prometer quitar advertencias, evadir controles ni fabricar documentos; no cambiar certificado ni comprometer datos. |
| `VER-001` — posible numeración 0.x; **solo propuesta** | Usuario considera expresar beta con 0.x, sin aprobar política definitiva. | Mantener 3.x y `versionCode` creciente hasta decidir; no renombrar tags/releases antiguas ni reducir código Android. Pruebas como pre-release y base como release normal solo con autorización aplicable; no asignar una nueva versión a estas tareas. |

**Orden y límites del backlog:** primero DEX-001–003, única programación autorizada; luego validación manual y selección con el usuario de una siguiente tarea acotada. BIO/ABIL/MOV/ITEM/TEAM son pulido de módulos existentes; CAP matemático y selector se planifican y validan por separado; TRAIN exige investigación de datos/backend; SEC/VER no permiten cambios de firma o numeración por inferencia. No ejecutar esta tabla de golpe ni crear una línea paralela que sustituya v3.6, v3.7 o RI.

Las incidencias se registran en este único plan; **no se han creado issues públicos**. ITEM-001 y MOV-001 están incluidos en la RC1 publicada, aún pendientes de aprobación manual. CAP-001–004 y las mejoras no DEX siguen pendientes, sin implementación autorizada ahora. Conservar las capturas de la conversación como evidencia del usuario; no afirmar que sus archivos se archivaron en el repositorio.

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
- Alcance de la revisión documental original: solo plan/README. Las implementaciones posteriores de Kotlin/Room/build y la publicación integrada se conservan en §15 y §17; el pulido actual autorizado y no publicado está en §4.1.

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
