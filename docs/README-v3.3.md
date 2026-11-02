# RotomPedia v3.3 — Guía de cambios y continuación

Este documento explica **qué cambió respecto a v3.2, cómo funciona ahora y dónde
intervenir para seguir mejorando la app**. No describe como nuevas las funciones
que ya existían antes de esta actualización.

## 1. Estado de la entrega

| Aspecto | Estado |
| --- | --- |
| Versión publicada | `3.3.0-rc1` |
| Código de versión Android | `30300` |
| Commit de implementación | [`fdf30cf`](https://github.com/ArukuKun/RotomPedia/commit/fdf30cfd796dac35df9689ac239d03879e202eca) |
| Pruebas automáticas | 12 correctas; lint sin errores, con advertencias |
| Compilación en GitHub | Correcta; APK adjunto a la pre-release |
| Validación manual | El usuario reportó haber validado todo después de instalar/probar la entrega |
| Validación independiente del agente en Android | No realizada: no había dispositivo/emulador disponible |
| Firma del APK | Debug/de desarrollo; no firma privada de producción |
| Promoción a estable | No realizada; requiere decidir versión y distribución |

El reporte del usuario no incluye modelo de dispositivo, versión de Android ni
resultados individuales. No se inventan esos datos ni se considera una garantía
de compatibilidad con todos los dispositivos.

### Enlaces útiles

- [Descargar el APK publicado](https://github.com/ArukuKun/RotomPedia/releases/download/v3.3.0-rc1/RotomPedia-v3.3.0-rc1.apk).
- [Release y novedades resumidas](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.3.0-rc1).
- [Comparación completa v3.2 → v3.3.0-rc1](https://github.com/ArukuKun/RotomPedia/compare/v3.2...v3.3.0-rc1).
- [Diagnóstico y escenarios de regresión](v3.3-validacion.md).

## 2. Objetivo de producto

Reorientar RotomPedia hacia una **Pokédex en español como experiencia principal**,
inspirada en la claridad de PokeInfo y manteniendo la identidad de RotomPedia.
El Team Builder y las calculadoras siguen siendo complementos importantes.

La decisión central no fue «quitar todo el hardcode»: fue **conservar la metadata
útil para explorar y dejar que la API proporcione los datos del Pokémon**.

## 3. Cambios visibles para el usuario

### 3.1 Selector de Pokémon: estabilidad y exploración

**Problemas anteriores:** había IDs repetidos/incorrectos en el catálogo de formas;
las claves de las listas dependían de esos IDs. Además, el selector modificaba
filtros compartidos con la Pokédex y podía añadir un Pokémon con estadísticas cero
al fallar una consulta.

**Comportamiento actual:**

- Se conserva **⚡ Formas / Megas**, además de Todos y las regiones/generaciones.
- La búsqueda es parcial: no exige escribir un slug exacto de PokeAPI.
- Se comparan nombres españoles, slugs y etiquetas; se normalizan mayúsculas,
  tildes y guiones. En las formas, los términos pueden aparecer en distinto orden.
- Admite búsqueda por ID resuelto y reconocimiento de tipos en español/inglés.
- El índice de API amplía las formas más allá de las 68 entradas curadas locales.
- Las claves de formas usan el slug; se eliminan entradas repetidas por ese slug.
- Las formas predeterminadas con nombre propio, como Lycanroc Mediodía, se
  conservan; se evita repetir su especie base cuando su ID está resuelto.
- El filtro de tipo se obtiene de PokeAPI, no de tipos numéricos/locales obsoletos.
- Búsqueda, región y tipo pertenecen al diálogo: no cambian filtros ni favoritos
  de la pantalla principal de Pokédex.
- Al seleccionar se consulta la ficha real; se reutiliza caché cuando está disponible.
- Si faltan datos, se muestra un error en lugar de añadir un miembro ficticio.
- Se bloquea la selección repetida mientras se procesa una elección.
- Las miniaturas tienen tamaño limitado y se muestran en listas lazy.

Ejemplos de exploración:

| Entrada | Resultado esperado |
| --- | --- |
| `hisui` | Varias formas de Hisui |
| `mega` | Varias megaevoluciones |
| `lycanroc` | Mediodía, medianoche y crepúsculo |
| `aegislash` | Escudo y filo |
| `crepusculo` | Coincide con «Crepúsculo» sin exigir tilde |
| `alola golem` | Coincide con la metadata de Golem de Alola |

**Matiz:** las regiones del selector siguen filtrando las especies base por rango
nacional. Para descubrir variantes regionales se usa Todos o Formas / Megas con
texto como `alola`; no se rediseñó ese filtro como un catálogo regional completo.

### 3.2 Detalle de Pokémon

El flujo actual de lectura es:

1. Artwork, acciones de variocolor/rugido/captura, nombre, ID y tipos.
2. Comentarios de RotomDex y descripción.
3. Estadísticas base y total BST.
4. Relaciones de tipo.
5. Línea evolutiva.
6. **Formas**, mediante tarjetas clicables.
7. Movimientos bajo demanda.
8. Localizaciones bajo demanda.

Se retiró el selector superior de chips como mecanismo principal de formas.
La cabecera tiene fondo oscuro y acento cian; descripción, estadísticas y relaciones
se agrupan en tarjetas. Las estadísticas muestran «PS» en lugar de «HP».
Las evoluciones indican su especie de origen para aclarar cadenas ramificadas.

Al tocar una tarjeta de forma:

- Se mantiene la ruta de detalle y cambia el Pokémon seleccionado.
- Se carga su ficha completa: nombre, ID, sprite, tipos y estadísticas.
- Se recalculan relaciones de tipo y se actualizan rugido y favoritos.
- Se marca la tarjeta seleccionada y se incluye la variedad predeterminada para volver.
- Descripción, evolución y calculadora de captura usan la **especie base**, no el
  ID alternativo como si fuera un ID de especie.
- Los comentarios de Rotom se recalculan si cambian los datos relevantes.

**Importante:** un cambio en la misma ruta no implica que todos los estados de
lectura/scroll se conserven durante la carga. Cualquier mejora de continuidad visual
debe tratarse como una tarea de UX concreta, no darse por implementada.

### 3.3 Movimientos

La ficha incorpora una consulta desplegable con métodos **Nivel, MT/MO, Huevo y
Tutor**. Cada tarjeta muestra nombre español si existe en API, potencia, precisión
y PP. Los valores no aplicables se muestran con «—».

- Se usa la lógica de movimientos existente del ViewModel, corregida para que un
  movimiento pueda aparecer en varios métodos de aprendizaje.
- Para cada método se toma el detalle de versión más reciente disponible por movimiento.
- Las consultas de detalle tienen concurrencia máxima de **4**.
- Límite actual: **60 por método y 30 para tutor**.
- Los errores permiten reintentar; no se cachea como éxito una carga fallida.

**No está implementado:** selector de juego/edición, lista completa sin límites,
nivel de aprendizaje visible, explicación del efecto o validación de legalidad
competitiva del moveset. No se debe interpretar la lista como legal para un juego concreto.

### 3.4 Localizaciones

La sección consulta los encuentros de la forma visualizada cuando se expande.
Muestra el área y los juegos/versiones que devuelve PokeAPI, con estados de carga,
error, reintento y ausencia de resultados.

Una respuesta vacía se explica como «sin encuentros registrados», **no como
«Pokémon imposible de obtener»**. Puede requerir evolución, evento, intercambio u
otro método no recogido por ese endpoint.

**No está implementado:** localización completa al español, horarios, niveles,
probabilidades, condiciones de encuentro, filtro por juego ni navegación a mapas
desde estas tarjetas. Esta sección no modificó el módulo de mapas.

## 4. Arquitectura y especificaciones

### Fuentes de datos

| Fuente | Responsabilidad | No debe contener/asumir |
| --- | --- | --- |
| Catálogo local | Slug, especie padre, nombre español, categoría y ayudas de búsqueda | Stats, tipos o IDs de formas como verdad local |
| Índice de PokeAPI | Nombres/URLs e IDs resueltos; descubrir variantes adicionales | Fichas completas de cada resultado |
| Ficha de PokeAPI | Tipos, stats, sprites, rugido y referencia a especie | Traducción perfecta de toda la UX |
| Endpoint de especie | Variedades, descripción, evolución y datos de captura | Que el ID de una forma sea un ID de especie |
| Room/memoria | Reutilización de datos previamente consultados | Cobertura offline completa de todos los endpoints |

### Flujos principales

```text
Selector:
  catálogo español + índice ligero de API
    → búsqueda/filtros locales al diálogo
    → elección de un slug o ID
    → ficha API / caché disponible
    → callback con datos reales al equipo o calculadora

Detalle:
  ID seleccionado → ficha Pokémon → tipos/stats/sprite/rugido
                              └──→ especie base → variedades/evolución/descripción
  tarjeta de forma → nuevo ID seleccionado en la misma ruta
```

### Endpoints utilizados

Base: `https://pokeapi.co/api/v2/`

| Endpoint | Uso |
| --- | --- |
| `pokemon-species?limit=10000` | Ampliación del listado nacional tras la carga inicial |
| `pokemon?limit=10000` | Índice ligero para resolver y descubrir formas |
| `pokemon/{id o nombre}` | Datos reales del Pokémon; también movimientos en el modelo extendido |
| `pokemon-species/{id}` | Descripción, referencia evolutiva y variedades |
| `type/{nombre}` | Filtro por tipo y relaciones de daño |
| `move/{nombre}` | Nombre y datos de cada movimiento |
| `pokemon/{id}/encounters` | Encuentros por forma |
| URL de cadena evolutiva devuelta por API | Etapas y condiciones evolutivas |

Los sprites/artworks y algunos rugidos usan recursos del proyecto PokeAPI en GitHub.
La Pokédex no necesita el backend de líderes para estas consultas.

### Estado, carga y caché

- Se cancelan cargas reemplazadas de detalle, evolución, relaciones, variedades y
  movimientos; las cancelaciones no se convierten en errores normales de red.
- Las variedades se cargan con hasta 4 consultas simultáneas y sin el antiguo
  recorte a 10 variantes. Si falta alguna, se informa y se permite reintentar.
- El índice del picker se conserva en memoria del ViewModel.
- Las fichas de formas y movimientos tienen cachés en memoria; los datos de formas
  consultadas también se guardan en Room para reutilización cuando sea posible.
- Los encuentros se conservan en el estado de la sección mientras permanece disponible;
  no tienen una caché persistente de Room implementada en esta actualización.
- No hay descarga offline de toda la Pokédex. Sin red, la cobertura depende de lo
  que esté previamente guardado; descripciones, encuentros e índices pueden faltar.

### Ejemplos de errores de datos corregidos

Blaziken Mega y Gardevoir Mega compartían el ID local `10051`. La API resuelve
Blaziken Mega como `10050` y Gardevoir Mega como `10051`. Aegislash Filo se resuelve
como `10026`, no el antiguo `10061`. Ya no es necesario copiar esos IDs al catálogo.
También se corrigieron los slugs de Tauros de Paldea (`…-breed`) y Darmanitan de
Galar (`darmanitan-galar-standard`).

## 5. Archivos cambiados y dónde intervenir

Rutas Kotlin relativas a `RotomPedia/app/src/main/java/com/duoc/rotompedia/`:

| Archivo | Cambio y responsabilidad |
| --- | --- |
| [`data/PokemonFormCatalog.kt`](../RotomPedia/app/src/main/java/com/duoc/rotompedia/data/PokemonFormCatalog.kt) | Nuevo catálogo de 68 etiquetas; nombres españoles y búsqueda normalizada |
| [`data/PokemonPickerIndex.kt`](../RotomPedia/app/src/main/java/com/duoc/rotompedia/data/PokemonPickerIndex.kt) | Nuevo ensamblado catálogo + índice API; identidad por slug y variantes adicionales |
| `data/PokemonFormsData.kt` | Eliminado y reemplazado funcionalmente por catálogo de UX + API; no se eliminaron sin reemplazo las formas existentes |
| `data/model/PokemonData.kt` | Referencia a especie y modelos de encuentros/versiones |
| `data/remote/PokeApiService.kt` | Consultas por nombre, índice de Pokémon y encuentros |
| [`ui/screens/PokemonDetailScreen.kt`](../RotomPedia/app/src/main/java/com/duoc/rotompedia/ui/screens/PokemonDetailScreen.kt) | Selección de forma, jerarquía visual, tarjetas, especie base y favoritos |
| [`ui/screens/PokemonReferenceSections.kt`](../RotomPedia/app/src/main/java/com/duoc/rotompedia/ui/screens/PokemonReferenceSections.kt) | Nuevas secciones desplegables de movimientos/localizaciones |
| [`ui/screens/TeamBuilderScreen.kt`](../RotomPedia/app/src/main/java/com/duoc/rotompedia/ui/screens/TeamBuilderScreen.kt) | Contiene el picker compartido; filtros aislados, claves y selección; corrección de llaves en la matriz |
| [`viewmodel/PokemonViewModel.kt`](../RotomPedia/app/src/main/java/com/duoc/rotompedia/viewmodel/PokemonViewModel.kt) | Carga/cancelación/caché; variedades, tipos y movimientos |

Otros archivos de la entrega:

- `RotomPedia/app/build.gradle.kts`: versión Android y Mockito 5.14.2 para JDK 21.
- `RotomPedia/app/src/test/java/com/duoc/rotompedia/PokemonFormCatalogTest.kt`:
  9 pruebas nuevas de búsqueda, catálogo, variantes e IDs resueltos.
- `RotomPedia/app/src/test/java/com/duoc/rotompedia/AuthViewModelTest.kt`:
  espera a finalizar corrutinas antes de comprobar el resultado; sin cambiar la lógica de login.
- `.github/workflows/release.yml`: pruebas y lint antes del APK; etiquetas con
  guion se publican como pre-release; notas de versión adjuntas.
- `README.md`, esta guía, `docs/v3.3-validacion.md` y `docs/v3.3-release-notes.md`:
  documentación y escenarios de revisión.

### Guía rápida: «quiero cambiar…»

| Necesidad | Empezar por |
| --- | --- |
| Nombre español, etiqueta o búsqueda de una forma | `PokemonFormCatalog.kt` |
| Cómo se descubren/unen resultados del selector | `PokemonPickerIndex.kt` |
| Filtros, tamaño o aspecto del selector compartido | `PokemonPickerDialog` en `TeamBuilderScreen.kt` |
| Orden, tarjetas o selección en la ficha | `PokemonDetailScreen.kt` |
| Datos y métodos de movimientos | `loadPokemonMoves` en `PokemonViewModel.kt` y su sección de UI |
| Información visible en encuentros | Modelos, `PokeApiService.kt` y `PokemonLocationsSection` |
| Persistencia offline | DAOs/entidades de caché + flujos de carga; no solo la UI |
| Versión instalada | `versionCode`/`versionName` en `app/build.gradle.kts` |
| APK y notas publicadas | Workflow, etiqueta Git y documento de notas |

**Regla para añadir una etiqueta curada:** comprobar el slug en API, añadir la
metadata con especie correcta, probar búsqueda y selección, y verificar la vuelta
a la variedad predeterminada. No reintroducir stats ni IDs copiados manualmente.

## 6. Qué se conservó y qué no se hizo

Se conservaron Team Builder, calculadoras, favoritos, mapas y backend existente.
En la matriz de equipo solo se corrigió el anidamiento de llaves que impedía
compilar: **no se cambiaron sus fórmulas de daño**. La actualización amplía el
listado de especies solicitado; no sustituye toda la arquitectura ni toda la UI.

No se implementaron nuevos mapas, reglas completas de simulación de combate,
un selector por juego, sincronización de equipos en nube ni una distribución de
producción con keystore privado. La inspiración en PokeInfo no equivale a una copia
completa de su diseño ni de su cobertura de datos.

## 7. Validación y cómo repetirla

Desde la raíz del repositorio, en PowerShell:

```powershell
cd RotomPedia
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

El APK local queda en `RotomPedia/app/build/outputs/apk/debug/app-debug.apk`, relativo
a la raíz del repositorio. CI usa JDK 17; la ejecución local validada usó JDK 21.

Las 12 pruebas son: 9 del catálogo, 2 de autenticación y 1 prueba de ejemplo.
**No son 12 pruebas de interfaz**: no demuestran por sí solas ausencia de crashes,
comportamiento con red lenta ni corrección visual. Además se contrastaron los 68
slugs y los Pokémon clave contra la API. El usuario reportó después la validación manual.

Ante cualquier cambio futuro, repetir los escenarios de
[`v3.3-validacion.md`](v3.3-validacion.md), especialmente picker, formas, equipo
guardado y calculadoras. Registrar dispositivo, Android y pasos exactos si aparece un fallo.

## 8. Limitaciones y deuda técnica conocidas

1. **Español incompleto:** las formas curadas tienen etiqueta española; variantes
   adicionales, áreas y juegos pueden mantener nombres de API.
2. **Movimientos de referencia:** límites por método y ausencia de filtro por juego;
   todavía no es una herramienta para certificar legalidad competitiva.
3. **Offline parcial:** no todos los datos de una ficha se guardan de manera persistente.
4. **Descubrimiento de variantes:** el índice identifica extras por IDs superiores
   a `10000`. Es una heurística del esquema actual de PokeAPI, no una propiedad
   universal del dominio; conviene revisarla si cambia la API.
5. **Separación de responsabilidades mejorable:** el picker sigue dentro de
   `TeamBuilderScreen.kt` y los encuentros se consultan desde la sección de UI.
   Extraerlos tendría sentido para pruebas/reutilización, no como refactor obligatorio.
6. **Cobertura automática limitada:** faltan pruebas de UI y de cambios rápidos de
   selección/cancelaciones; actualmente esos escenarios dependen de regresión manual.
7. **Distribución:** firma debug, versión RC y advertencias de herramientas/dependencias.
   El workflow conserva referencias a acciones con avisos de deprecación.
8. **Notas futuras:** el workflow apunta a `docs/v3.3-release-notes.md`; antes de
   otra entrega se debe actualizar ese archivo o parametrizarlo, para no publicar notas antiguas.

## 9. Cómo proseguir — propuesta, no trabajo ya implementado

### Prioridad 1: cerrar correctamente esta versión

- Registrar dispositivo/Android de la validación y cualquier observación visual.
- Decidir si se promueve a `3.3.0` estable o si hace falta otra RC con ajustes concretos.
- Si cambia el APK, incrementar `versionCode`, actualizar `versionName`, notas y etiqueta.
- Decidir firma persistente para las siguientes entregas; proteger keystore y
  contraseñas fuera del repositorio, con secretos de CI y copia de seguridad segura.
- Revisar una estrategia de respaldo antes de cambiar de firma: una firma nueva
  puede impedir actualizar una instalación anterior y desinstalar elimina datos locales.

**No basta con renombrar la release:** el APK publicado conserva la versión y firma
con las que fue compilado. Tampoco conviene mover/borrar la etiqueta de la RC existente.

### Prioridad 2: pulir la experiencia de Pokédex

- Completar nombres y categorías de formas relevantes en español.
- Mejorar continuidad de lectura al cambiar forma, estados de carga e imágenes de respaldo.
- Revisar accesibilidad, texto grande y pantallas estrechas con capturas reales.
- Añadir pruebas de UI para scroll del picker, búsquedas y cambio de forma.

Criterio de cierre: sin regresiones en exploración ni equipos, y con ejemplos de
antes/después aprobados por el usuario; no solo «compila».

### Prioridad 3: profundidad de datos

- Elegir juego/edición y filtrar movimientos y encuentros coherentemente.
- Mostrar nivel de aprendizaje, efectos y condiciones de encuentros cuando proceda.
- Sustituir límites fijos por paginación/carga progresiva si se necesita cobertura completa.
- Definir qué información debe funcionar offline y persistirla con una política clara.

Criterio de cierre: la pantalla informa de qué juego/fuente proviene cada dato y no
mezcla legalidad de distintas ediciones de forma implícita.

### Prioridad 4: ampliaciones, después de consolidar lo anterior

Evaluar mejoras competitivas del Team Builder y mapas interactivos con un alcance
propio. No mezclarlas con la corrección del picker ni con cambios de firma.

### Plantilla para pedir la siguiente mejora

```text
Objetivo:
Pantalla o función:
Qué pasa ahora:
Qué debería pasar:
Ejemplo de Pokémon/juego:
Captura o referencia visual:
Qué no debe cambiar:
Criterios para aprobarlo:
```

Trabajar con objetivos pequeños y verificables ayuda a no volver a encadenar
versiones que añaden funciones sin mejorar la experiencia general.
