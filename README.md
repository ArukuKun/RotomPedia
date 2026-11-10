# 📱 RotomPedia

**RotomPedia** es una Pokédex y enciclopedia Pokémon en español para Android, desarrollada en Kotlin y Jetpack Compose con arquitectura MVVM. Conecta exploración nacional, variedades, equipos, herramientas y datos verificables, con identidad visual propia.

El proyecto nació en la asignatura "Desarrollo de Aplicaciones Móviles" (DSY1105) y continúa evolucionando por versiones. Conserva gestión local de usuarios, biometría y un microservicio de líderes de gimnasio; no todos los módulos requieren ese backend.

## Estado actual y versiones

**v3.6.0-rc1 compilada y firmada (§5.8):** candidata `30601`/`3.6.0-rc1` con el piloto de **Hoenn / Esmeralda** conectado al explorador de mapas: catálogo de encuentros por edición con integridad comprobada, búsqueda por zona/Pokémon/número nacional, filtros de método y ficha de zona con niveles/condiciones. Los dos registros vinculados a Colosseum permanecen excluidos del cliente. El mapa antiguo solo es referencia visual: **no existen pines geográficos verificados**. Evidencia local (10 nov 2026): **202 Kotlin + 43 Python**, lint 0 errores, `assembleRelease` correcto, certificado `5831c434…3f1aa` idéntico a RC14 (actualización compatible), SHA-256 `84c0a44da7d8c3cf0f8518a37ded837d7e7e6a105a750ba3b8d2675c43e1c34a`. Primera RC en **variante release** (RC1–RC14 eran debug). **Publicada como pre-release** el 10 nov 2026: https://github.com/ArukuKun/RotomPedia/releases/tag/v3.6.0-rc1 (APK + `.sha256`, digest remoto verificado byte a byte); CI *Release APK* del tag en verde (revalida certificado/checksum/`versionName` antes de publicar) y *Verify* de la rama en verde (`BUILD SUCCESSFUL in 8m 54s`) tras ampliar el límite de tests de 5 a 15 min. La revisión visual en teléfono sigue pendiente. Véase §5.8 del único Plan Maestro.
**Selector regional y memoria (§5.7):** al entrar a Mapas por primera vez aparece el selector con **Kanto primero**; se recuerda la última región abierta y Atrás vuelve al selector para cambiar. Ficha de ubicación con pestañas Pokémon/Objetos-NPC agrupadas por método; sin pines ni objetos inventados. Cartografía ilustrada original, otras regiones y objetos/NPC con fuente siguen pendientes; no es la reconstrucción artística final.

**Continuidad local 5.5:** búsqueda de Pokémon por nombre/número ahora limita los registros mostrados en la ficha de la zona (p. ej. Ralts), mientras que buscar por ruta mantiene el catálogo completo; mapa y lista de subáreas respetan los filtros, con opción de limpiarlos. Se comprobó la lógica Kotlin aisladamente y pasaron las 43 pruebas Python. Sin APK verificada ni nueva pre-release. Ver §5.5 del mismo Plan Maestro.

**RC14 publicada (§4.16):** `30514`, `feature/v3.5-main-games-hisui`: Shadow/Colosseum/XD fuera del contenido seleccionable, sin borrar identidades o datos guardados; Hisui incluye las siete especies de Leyendas Arceus, con etiquetas de especie/forma. Generación VIII, catálogo nacional y distinción de Luna Carmesí intactos. **179 Kotlin/31 Python**, tres índices, lint/build y firma compatibles correctos. [Release](https://github.com/ArukuKun/RotomPedia/actions/runs/42161749576) y [Verify](https://github.com/ArukuKun/RotomPedia/actions/runs/42161746379) correctos al **intento 1**, mismo SHA `88c94e60208782720a7223477ad4debcfb9c167c`. Revisión visual en teléfono pendiente, no estable.

**Devolución sobre RC12:** el usuario aprueba en términos generales el resultado visual. Para la **versión final** quedan registradas en §4.13 del [Plan Maestro](docs/ROTOMPEDIA_PLAN_MAESTRO.md) estas ideas: funcionamiento sin internet como prioridad principal (hoy los índices/cachés solo ofrecen cobertura parcial), fila y columna de tipos fijas al desplazar la tabla, y conservar la posición al alternar Lista/Cuadrícula en Pokédex. No se implementan ni se publica otra versión por esta devolución; aceptación general no equivale a validación exhaustiva.

**Ajuste §4.14 incluido en RC13:** habilidades con descripción española disponible al abrir (267 de 374; no todas traducidas), edición explícita e inglés extenso opcional; tipo y categoría de movimientos con etiquetas de color más legibles. No cambia filtros ni resuelve las traducciones pendientes.

**Entrega previa RC13 (§4.15):** encabezados fijos de tabla de tipos y posición compartida Lista/Cuadrícula en Pokédex, junto con §4.14; se conservan en RC14. Evidencia histórica de 172 Kotlin/31 Python, publicación y verificación posterior en el Plan Maestro. Offline queda para el final, con clasificación de funciones que necesitan conexión. v3.6 inicia auditoría de fuentes/cartografía (§5), sin modificar los mapas en este parche ni presentar sus ubicaciones antiguas como verificadas.

**CI histórico de RC13:** tag `966dee0cbde42582c195cd7084ea3b8461e155df`. [Release APK](https://github.com/ArukuKun/RotomPedia/actions/runs/42156593562) correcto al intento 2 y publicación automática verificada (`draft=false`, `prerelease=true`), sin omitir controles. [Verify del tag](https://github.com/ArukuKun/RotomPedia/actions/runs/42156589312) falló en ambos intentos por AppNotIdleException/timeout en cuatro casos existentes. **La comprobación independiente posterior ya pasó:** [Verify 42158459741](https://github.com/ArukuKun/RotomPedia/actions/runs/42158459741), intento 1, SHA `11cdc0b0bda27aa9a412d31d97ace449586fdf77`; solo difiere documentación respecto al tag, sin cambios en app/tools ni APK. Se conserva la evidencia; RC14 pasó ambos CI sin reintentos.

**Entrega integrada publicada: `3.5.0-rc12` / `30512`.** Inicio real, acceso/registro y conjunto de pantallas/ventanas con marco/colores compartidos y Claro/Oscuro/Sistema; P3 añade movimientos por variedad/edición y cuatro pestañas deslizables. **162 Kotlin/31 Python**, tres índices, lint/build y firma compatible correctos; ambos workflows pasaron al primer intento. Revisión visual en teléfono pendiente. Alcance/evidencias en **§4.12 del único Plan Maestro**. Mapas interactivos se reconstruirán después con ubicaciones verificables; sus datos/implementación no se amplían aquí.

**Prioridad actual: revisar la interfaz Rotom integrada en teléfono.** RC11 fue el piloto histórico, instalado y considerado insuficiente por el usuario; RC12 lo sustituye por la entrega conjunta solicitada, sin micro-RC por módulo. Apariencia persistente independiente de las seis formas; solo el visor cartográfico diferido conserva la frontera legacy. [Maqueta](docs/prototypes/rotom-identity.html) y auditoría en §4.10; historia de V1 en §4.11 y entrega integrada en **§4.12 del único [Plan Maestro](docs/ROTOMPEDIA_PLAN_MAESTRO.md)**. Verificación técnica no equivale a aceptación visual ni autoriza cierre estable.

**Prueba pública actual:** [pre-release `v3.5.0-rc14`](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc14). [Descargar APK para Android](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc14/RotomPedia-v3.5.0-rc14.apk). Instalar sobre RC13 **sin desinstalar ni borrar datos**. No es estable; RC1–RC13 se conservan intactas. HTTP 200, 24189633 bytes; SHA-256 `d601eb1b2982b72cdecbd3960c2a510b4cf78473fa320a492ab9732cf96f2328`, coincidente con el digest público y el checksum adjunto.

**Entrega integrada previa RC12 (histórica):** rama `feature/v3.5-complete-rotom-interface`, tag `v3.5.0-rc12` en `a3109167acf06d1c2699c49944599670e113ac49`. [Release](https://github.com/ArukuKun/RotomPedia/actions/runs/42148685271) y [Verify](https://github.com/ArukuKun/RotomPedia/actions/runs/42148680638) correctos, intento 1. APK y checksum originales intactos; evidencia en §4.12.

**Política permanente de publicación:** implementar una fase autorizada incluye permiso para commits, push, nuevo tag, GitHub Actions y **pre-release con APK descargable**, sin pedir autorización adicional. Exige verificaciones correctas, firma compatible, `versionCode` creciente, SHA-256, notas y problemas conocidos. No sobrescribir releases anteriores. **Versiones estables, cambios de firma y acciones destructivas requieren aprobación independiente**; esta política no autoriza nuevas fases. Ver §8.1 del único Plan Maestro.

**Pulido post-RC1:** Pokédex nacional sin desplegables, proporciones originales restauradas, grupos huevo, Gigamax y Lista/Cuadrícula de tres columnas. Publicado como `v3.5.0-rc2` / `30502`, con la misma firma compatible que RC1; **no incluye el resto del backlog**. Evidencias y pendientes en [§4.1 del Plan Maestro](docs/ROTOMPEDIA_PLAN_MAESTRO.md#41-estabilización-posterior-a-v350-rc1--pokédex-alcance-inmediato-autorizado).

**Observaciones de RC2:** publicadas sus correcciones en `v3.5.0-rc3` / `30503`: botón de filtros con borde e icono, filtros/vista/scroll restaurados al volver de la ficha, orden regional verificado (Hisui con sus iniciales al comienzo) y retirada del filtro general «Variedades estables» y duplicados Tótem de Pokédex, sin borrar sus identidades. 86 pruebas Kotlin/23 Python correctas; validación de teléfono pendiente. Detalles en §4.2 del único Plan Maestro. Las ausencias de numeración en Pokédex base de Galar se muestran al final y se informan, sin mezclar DLC.

**Correcciones post-RC3 publicadas:** `v3.5.0-rc4` / `30504`: filtro en barra superior, panel sin párrafos ni categorías Cosméticos/Combate, primigenias en Megaevoluciones y Pikachu con gorra fuera de Regionales. Conserva orden regional y memoria de navegación de RC3. Nombres corregidos también en ficha; sin cambios de datos personales ni implementación P3. Evidencia y validación pendiente en §4.3 del mismo Plan Maestro.

**BIO-001 publicada:** `v3.5.0-rc5` / `30505`: bloque compacto de especie/categoría, altura/peso de la variedad y habilidades visibles con acceso a sus fichas existentes. Sin cambios de Room ni datos personales; medidas/categoría pueden faltar al reabrir offline. 100 pruebas Kotlin/23 Python, lint/build, snapshots y ambos workflows correctos; validación visual pendiente. Ver §4.4 del mismo Plan Maestro; P3 sigue para después.

**Parche visual publicado:** `v3.5.0-rc6` / `30506`: mini sprites de la variedad exacta junto a los portadores de habilidades y sección «Formas» oculta cuando no hay alternativas. Mantiene lista completa, navegación y datos guardados; 105 Kotlin/23 Python y ambos workflows correctos, validación visual pendiente. Alcance y evidencia en §4.5 del Plan Maestro; evolución de Overqwil y P3 siguen pendientes.

**EVO-001 publicada:** `v3.5.0-rc7` / `30507`: 22 relaciones de Hisui verificadas para Arceus y Escarlata/Púrpura, con selección de edición y variedades exactas. La referencia general de especie se conserva por separado; otras ediciones no se extrapolan. Sin cambios de datos personales ni P3. 117 Kotlin/23 Python y ambos workflows correctos; validación visual pendiente. Evidencias y matriz en §4.6 del mismo Plan Maestro.

**ABIL-001 publicada:** `v3.5.0-rc8` / `30508`: total y contadores normales/ocultas por variedad distinta, sin duplicar slots ni perder roles, sprites o navegación. Revisión de los 311 nombres españoles: tres nombres anteriores se muestran actualizados (Pereza, Viscosecreción, Esclusa de Aire), con alias históricos y procedencia; no se inventan los 63 nombres ausentes. Sin cambios de snapshot, Room ni datos personales. 125 Kotlin/23 Python y ambos workflows correctos; validación visual pendiente. Ver §4.7 del Plan Maestro; P3 diferida.

**ITEM-002 publicada:** `v3.5.0-rc9` / `30509`: objetos por 8 bolsillos de referencia desde la misma fuente fijada, con búsqueda/filtros combinables y fichas/sprites existentes. Los 2223 objetos anteriores permanecen idénticos; el asset solo añade relaciones de bolsillos, sin cambios de Room ni datos personales. No certifica organización o disponibilidad por edición ni crea inventario personal. 134 Kotlin/26 Python y ambos workflows correctos; validación visual pendiente. Ver §4.8 del Plan Maestro; P3 diferida.

**Corrección de interfaz publicada:** `v3.5.0-rc10` / `30510`, rama `feature/v3.5-clean-client-ui`: retirados avisos de proveedor/caché/procedencia técnica de layouts de cliente, bloques opcionales vacíos ocultos y errores breves con reintento. En objetos, botón superior «Filtros» con icono/contador y selectores con borde/flecha. Datos y trazabilidad se conservan en modelos/docs/tests; sin cambios de sprites ni datos personales. 140 Kotlin/28 Python, tres índices, lint/build y firma compatible correctos. Ver §4.9 del mismo Plan Maestro; validación visual pendiente. P3 y sprites faltantes siguen para después.

**Documento principal: [Plan Maestro de RotomPedia](docs/ROTOMPEDIA_PLAN_MAESTRO.md).**
Contiene alcance, autorización, evidencias, incidencias residuales y criterios de salida. Este README es la entrada resumida y se mantiene para **todas las versiones: 3.x, 4.x y posteriores**, no un documento congelado en v3.3/v3.4.

### Serie 3.x

| Versión / hito | Estado real | Contenido y referencia |
|---|---|---|
| v3.3.0-rc1 | Prerelease histórica | Refactor inicial de formas; [guía](docs/README-v3.3.md) y [validación](docs/v3.3-validacion.md). |
| v3.4-A/B/C integrada | Terminada y publicada como prerelease | Índice y repositorios compartidos, Pokédex/picker nacional, filtros e imágenes por variedad. [Release RC1](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.4.0-rc1). |
| v3.5-A — Objetos | En pre-release integrada; validación manual pendiente | 2223 objetos, búsqueda local, fichas con procedencia, sprites y caché. |
| v3.5-B — Movimientos | En pre-release integrada; validación manual pendiente | 937 movimientos, repositorio común, textos españoles con procedencia y aprendizaje por edición. |
| v3.5-C — Habilidades | En pre-release integrada; validación manual pendiente | 374 habilidades, relaciones normales/ocultas por variedad/slot y enlaces verificados. |
| v3.5 post-RC1 — Pokédex | Publicada como pre-release v3.5.0-rc2; validación en teléfono pendiente | 1025 especies sin desplegables; sprites 72 dp, grupos huevo, formas por filtros y Lista/Cuadrícula. |
| v3.5 post-RC2 — Comodidad Pokédex | Publicada como pre-release v3.5.0-rc3; nueva validación pendiente | Botón reconocible, memoria al volver, orden regional y formas sin duplicados Tótem. |
| v3.5 post-RC3 — Barra y filtros | Publicada como pre-release v3.5.0-rc4; validación visual pendiente | Filtro en barra, panel limpio, primigenias y gorras corregidas; conserva navegación y datos. |
| BIO-001 — Ficha biológica | Publicada como pre-release v3.5.0-rc5; validación visual pendiente | Especie/categoría, medidas de variedad y habilidades normales/oculta con sus fichas existentes. |
| v3.6 — Mapas interactivos | Piloto local; sin APK publicada | Piloto por región/juego, datos y activos verificados; navegación inspirada en PokeInfo. |
| v3.7 — Rotom contextual e interfaz | Planificada; sin iniciar | Contexto local, personalidad, control de frecuencia y accesibilidad; IA opcional. |

### Serie 4.x y posteriores

Sin alcance ni calendario aprobados. Se incorporarán nuevas filas a este mismo README cuando se definan; no se atribuye a una futura v4.x una función todavía sin versión asignada.

### Proyecto futuro: Rotom Intelligence Pipeline

**Pendiente de investigación y prototipo; no iniciado.** Investigación propuesta después de v3.5 e integración progresiva con v3.7, **sin bloquear v3.6**. Combina conocimiento verificable, base local, contexto y personalidad, con IA generativa opcional solo para reformular hechos validados. Rotom no será un chatbot.

Las etapas RI-1 a RI-5, controles de privacidad, funcionamiento offline y criterios de aceptación están en el [apartado 16 del Plan Maestro](docs/ROTOMPEDIA_PLAN_MAESTRO.md#16-proyecto-futuro--rotom-intelligence-pipeline). Su registro no autoriza implementación, servicios externos ni gastos.

### Regla de mantenimiento de este README

Al cerrar **cada hito** y preparar **cada versión**, incluidos futuros cambios de serie mayor:

1. Actualizar estado público y desarrollo local por separado: planificado, en desarrollo, implementado, validado o publicado no son sinónimos.
2. Registrar en la tabla el alcance real y, cuando exista, enlazar el tag/release exacto. No inventar un tag para una versión local ni marcarla estable por pasar tests.
3. Sincronizar características, limitaciones, pruebas y compatibilidad/migraciones con la evidencia del Plan Maestro. Distinguir pruebas automáticas de validación en móvil.
4. Para publicar, verificar versión Android, APK, firma y checksum; mantener autorización explícita para push/tag/release. Esta regla documental **no publica ni actualiza automáticamente** archivos o releases.
5. Mantener un único README principal y un único Plan Maestro. Los documentos históricos se conservan como evidencia, no como copias paralelas del estado vigente.

## ✨ Características implementadas

La [guía v3.3](docs/README-v3.3.md) y su
[validación](docs/v3.3-validacion.md) se conservan como documentación histórica,
no como el plan vigente.

El proyecto ha evolucionado para incluir funcionalidades avanzadas divididas en módulos:

### 1. 🔐 Seguridad y Autenticación
* **Persistencia de Usuarios:** Registro y Login gestionados localmente con **Room Database**.
* **Biometría:** Inicio de sesión mediante **Huella Digital/Biometría** para usuarios recurrentes (IE 2.2.2).
* **Gestión de Sesión:** Manejo de estados de sesión activa/inactiva con `StateFlow`.

### 2. 📕 Pokédex Avanzada (Consumo API Externa)
* **Pokédex Nacional:** Índice local de 1025 especies y 1351 variedades a partir de relaciones de [PokeAPI](https://pokeapi.co/), compartido con el selector de equipos y herramientas.
* **Favoritos (Room):** Sistema para marcar/desmarcar Pokémon favoritos, guardándolos en base de datos local y permitiendo filtrar la lista para ver solo los guardados.
* **Detalle y variedades:** Estadísticas, tipos en español, evoluciones, imágenes de la variedad seleccionada y placeholders cuando no existen recursos válidos. No se presenta un texto de especie como descripción específica de otra variedad.
* **Exploración:** Orden nacional y búsqueda local por nombre, número y alias; filtros combinables. Desde RC2, Pokédex muestra solo especies por defecto, sin desplegables; variedades por categoría/origen, mientras el picker conserva grupos. Grupos huevo desde el mismo índice fijado de PokeAPI, Gigamax diferenciada y Lista/Cuadrícula de tres columnas sin límite de nueve.

### 3. 🎒 Enciclopedia de objetos — v3.5-A incluida en RC1
* **Catálogo:** 2223 objetos, 2218 con nombre español; categorías y atributos con búsqueda local.
* **Fichas:** Sprites, descripciones y efectos con procedencia/idioma; precio por edición solo cuando hay datos. Caché persistente, error y reintento.
* **Conexiones:** Evolución → objeto y objeto → Pokémon portadores; sin añadir equipamiento persistido ni inferir legalidad de uso.
* **Cobertura:** Los efectos sin traducción española se identifican; no se promete traducción integral ni detalle completo offline antes de descargarlo.

### Movimientos y habilidades — v3.5-B/C incluidas en RC1
* **Referencia compartida:** Snapshot local con hash y revisión fijada: 937 movimientos, 374 habilidades (311 nombres españoles), 2943 relaciones de habilidad por variedad y 2372 relaciones MT/MO–movimiento–objeto. Sin una petición por ficha o pulsación ni sprites inventados.
* **Aprendizaje:** Completo bajo demanda por variedad, edición, método y nivel; caché persistente para reapertura offline. Páginas de 40 sin topes ocultos; catálogo general separado del aprendizaje.
* **Editor:** Alias españoles/ingleses/slugs sin reescribir textos guardados; nombres desconocidos siguen visibles, no se muestran como vacíos.
* **Límites:** Edición filtra aprendizaje, textos y MT/MO. Las estadísticas de movimientos y relaciones de habilidades son referencias actuales, no reconstrucciones históricas. Efectos sin español identificados como inglés; sin certificación de legalidad.

### 4. ☁️ Microservicio Propio (Backend Spring Boot)
* **Integración Backend:** La app consume un servicio REST propio desarrollado en **Spring Boot (Java)**.
* **Líderes de Gimnasio:** Visualización de líderes por región (Kanto, Johto, Hoenn) con datos e imágenes servidas desde el microservicio (IE 3.1.1).
* **Visualización Dinámica:** Tarjetas con foto, especialidad (con color dinámico) y medalla.

### 5. 🗺️ Herramientas y extras existentes
* **Visor de Mapas:** Pantalla interactiva para visualizar mapas de las regiones del juego.
* **Equipos y herramientas:** Editor de equipos, estadísticas, movimientos y calculadora de captura. v3.5-B incorpora texto español por edición y fallback explícito; el cálculo de captura sigue diferido.
* **UI Adaptativa:** Interfaz moderna construida 100% con **Jetpack Compose**.

## Calidad, compatibilidad y límites

| Base | Evidencia disponible | Esquema de datos |
|---|---|---|
| v3.4.0-rc1 publicada | 39 pruebas Kotlin, 12 Python, compilación/lint correctos y CI satisfactorio; reporte manual del usuario con incidencias concretas | Room v8 |
| v3.5-A local | 52 pruebas Kotlin, 15 Python, lint sin errores (con advertencias), APK de aplicación y pruebas ensamblados; validación en Android real pendiente | Room v9: tres tablas nuevas de objetos; tablas existentes sin cambios |
| v3.5-A/B/C local (4 de noviembre de 2026) | 66 pruebas Kotlin, 19 Python, lint sin errores (110 advertencias y 30 sugerencias), APK de aplicación e instrumentación ensamblados; no ejecutados en dispositivo | Room v10: añade solo caché de aprendizaje respecto a v9; conserva cuentas, equipos, favoritos, EV/IV y movimientos |
| Pulido Pokédex RC2 | 77 pruebas Kotlin y 21 Python correctas; lint sin errores, APK de aplicación/instrumentación compilados y CI correcto. Firma/checksum/versión verificados por Actions antes de publicar; sin validación de dispositivo | Room v10 sin cambios ni migraciones nuevas; identidades previas del snapshot intactas |
| Comodidad Pokédex RC3 | 86 pruebas Kotlin y 23 Python correctas; lint sin errores, compilación y ambos workflows correctos; navegación/estado probados en Robolectric, validación de teléfono pendiente | Room v10 sin cambios; añade solo metadatos regionales al snapshot existente, sin modificar identidades previas |

- Datos personales e IDs técnicos se conservan; no se usa borrado destructivo como recuperación de una migración fallida.
- Los APK publicados de esta etapa son de validación con firma de depuración, no firma privada de producción. Consultar las notas de la release antes de actualizar; no desinstalar para forzar una actualización sin respaldo.
- No instalar una versión antigua sobre una base ya migrada a un esquema más reciente. El código local puede adelantarse al APK público; el nombre de una versión Android no demuestra por sí solo qué hitos están publicados.
- La pre-release actual usa `3.5.0-rc9` / `30509` (RC8: `30508`), con el mismo certificado comprobado que las RC anteriores. Incluye SHA-256; no es la versión base estable `3.5.0`. Instalar como actualización; no desinstales ni borres datos para forzarla si Android informa conflicto de firma.
- SHA-256 de RC9: `b4686824d1445ed80438d52f1e402e9ea37ea243e97460438fba21a2b4489c12`. [Verificación y publicación Actions correctas](https://github.com/ArukuKun/RotomPedia/actions/runs/42000420294); evidencias históricas conservadas en §4.1–§4.7 del plan.
- Una posible numeración `0.x` es solo una propuesta pendiente; no renombra releases ni reduce el `versionCode`. Estables y cambios de firma siguen requiriendo aprobación.
- Pendientes concretos: captura (`CAP-001`–`CAP-003`), validación manual del español de movimientos (`MOV-001`, implementado localmente), textos en pantallas estrechas (`UI-001`) y recursos gráficos aún ausentes. Véase el Plan Maestro, sin reabrir v3.4 completa.

## 🛠️ Stack Tecnológico

### 📱 Frontend (Android)
* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose (Material3)
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Base de Datos Local:** Room Database (SQLite)
* **Networking:** Retrofit + Moshi Converter
* **Imágenes:** Coil
* **Testing:** JUnit 4, Mockito, Robolectric, pruebas de Compose/Room y validadores Python
* **Seguridad:** BiometricManager

### 💻 Backend (Microservicio)
* **Framework:** Spring Boot 3.4.0 (Java 17)
* **Base de Datos:** H2 Database (En memoria)
* **Persistencia:** Spring Data JPA
* **API:** REST Controller

## 👨‍💻 Colaboradores y Roles

Este proyecto fue desarrollado colaborativamente utilizando GitFlow:

* **[ZorVaK749](https://github.com/ZorVaK749)** (Lead Developer & Backend):
    * Desarrollo completo del Microservicio en Spring Boot.
    * Implementación de la lógica de Pokédex y Favoritos (Room).
    * Integración de Pruebas Unitarias (Mockito).
    * Diseño de UI/UX avanzado y lógica de ViewModels.

* **[ArukuSR](https://github.com/ArukuSR)** (Auth & Release Manager):
    * Desarrollo inicial del módulo de Autenticación (Login/Registro).
    * Gestión del Repositorio y fusión de ramas.
    * **Generación y Firma del APK (Release):** Encargado de la creación del Keystore (`.jks`) y la compilación final del `app-release.apk` (IE 3.3.1).
    * Implementacion de apartado de mapas regionales en la aplicacion.

## 🚀 Instalación y Ejecución

Para probar el proyecto completo:

1.  Clonar el repositorio.
2.  **Backend:** Abrir la carpeta del backend y ejecutar el servidor Spring Boot (Puerto 8080 u 8081).
3.  **Frontend:** Abrir `RotomPedia/` en Android Studio y ejecutar en un emulador/dispositivo (mínimo Android 7.1/API 25).
    * El backend es necesario para la sección de "Líderes"; los índices incluidos de Pokémon/objetos no dependen de él. Las fichas no guardadas requieren acceso a PokeAPI.

Comprobación local en Windows, desde la raíz del repositorio:

```powershell
cd RotomPedia
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

En Linux/macOS usar `./gradlew` desde el mismo directorio. Las pruebas Python se ejecutan desde la raíz con `python -m unittest discover -s tools/tests -v`. Generar snapshots nuevos es mantenimiento separado, no un requisito de cada compilación ni autorización para publicarlos.

---
*Evaluación Final Transversal - DSY1105 - 2025*
