# 📱 RotomPedia

**RotomPedia** es una Pokédex y enciclopedia Pokémon en español para Android, desarrollada en Kotlin y Jetpack Compose con arquitectura MVVM. Conecta exploración nacional, variedades, equipos, herramientas y datos verificables, con identidad visual propia.

El proyecto nació en la asignatura "Desarrollo de Aplicaciones Móviles" (DSY1105) y continúa evolucionando por versiones. Conserva gestión local de usuarios, biometría y un microservicio de líderes de gimnasio; no todos los módulos requieren ese backend.

## Estado actual y versiones

**Prueba pública actual:** [pre-release `v3.5.0-rc1`](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.5.0-rc1), con v3.5-A/B/C integradas. [Descargar APK para Android](https://github.com/ArukuKun/RotomPedia/releases/download/v3.5.0-rc1/RotomPedia-v3.5.0-rc1.apk). No es la versión base estable `3.5.0`; v3.4 queda como referencia histórica.

**Código publicado:** rama `feature/v3.5-bc-encyclopedia`, tag `v3.5.0-rc1` en `a857f7e`; verificación local y CI correctas, validación del usuario en Android pendiente.

**Política de publicación autorizada:** subir las entregas de prueba a GitHub como **pre-release** (hitos A/B/C, RC y otras pruebas); publicar las versiones base `X.Y.0` como **release normal**, solo cuando se decida cerrar esa versión. No convertir una prueba en estable automáticamente. Cada entrega incluirá APK con la firma de actualización comprobada, versión Android creciente y SHA-256. `v3.5.0-rc1` ya está disponible para probar desde el teléfono.

**Pulido post-RC1 local:** Pokédex nacional sin desplegables, proporciones originales restauradas, grupos huevo, Gigamax y vista Lista/Cuadrícula de tres columnas en `feature/v3.5-pokedex-polish`. **No está incluido en el APK público anterior.** Esta intervención se limita a Pokédex y a actualizar el único Plan Maestro; no autoriza nuevos tags/releases ni el resto del backlog. Evidencias y pendientes en [§4.1 del Plan Maestro](docs/ROTOMPEDIA_PLAN_MAESTRO.md#41-estabilización-posterior-a-v350-rc1--pokédex-alcance-inmediato-autorizado).

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
| v3.5 post-RC1 — Pokédex | Pulido local verificado; validación en teléfono pendiente | 1025 especies sin desplegables; sprites 72 dp, grupos huevo, formas por filtros y Lista/Cuadrícula. Sin nueva versión publicada. |
| v3.6 — Mapas interactivos | Planificada; sin iniciar | Piloto por región/juego, datos y activos verificados; navegación inspirada en PokeInfo. |
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
* **Exploración:** Orden nacional y búsqueda local por nombre, número y alias; filtros combinables. En el pulido local post-RC1, Pokédex muestra solo especies por defecto, sin desplegables; variedades por categoría/origen, mientras el picker conserva grupos. Grupos huevo desde el mismo índice fijado de PokeAPI, Gigamax diferenciada y Lista/Cuadrícula de tres columnas sin límite de nueve.

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
| Pulido Pokédex post-RC1 local | 77 pruebas Kotlin y 21 Python correctas; lint sin errores, APK de aplicación/instrumentación compilados. Sin validación de dispositivo ni CI nuevo | Room v10 sin cambios ni migraciones nuevas; identidades previas del snapshot intactas |

- Datos personales e IDs técnicos se conservan; no se usa borrado destructivo como recuperación de una migración fallida.
- Los APK publicados de esta etapa son de validación con firma de depuración, no firma privada de producción. Consultar las notas de la release antes de actualizar; no desinstalar para forzar una actualización sin respaldo.
- No instalar una versión antigua sobre una base ya migrada a un esquema más reciente. El código local puede adelantarse al APK público; el nombre de una versión Android no demuestra por sí solo qué hitos están publicados.
- La pre-release publicada usa `3.5.0-rc1` / `30501`, con el mismo certificado comprobado que el APK público de v3.4.0-rc1. Incluye SHA-256; no es la versión base estable `3.5.0`. No desinstales para forzar una actualización si Android muestra un conflicto de firma.
- El pulido local no cambia firma ni numeración. Una posible numeración `0.x` es solo una propuesta pendiente; no renombra releases ni reduce el `versionCode`.
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
