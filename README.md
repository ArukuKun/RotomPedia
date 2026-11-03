# 📱 RotomPedia

### Tu enciclopedia Pokémon en español, donde la aventura va más allá de la Pokédex.

**RotomPedia** es una aplicación móvil nativa para Android, desarrollada con **Kotlin, Jetpack Compose y arquitectura MVVM**, que busca ofrecer una experiencia completa para explorar el mundo Pokémon.

Inspirada en la aplicación **PokeInfo de Bruno Sousa**, RotomPedia nace con el propósito de reunir información Pokémon en español dentro de una interfaz intuitiva, moderna y funcional.

Más allá de consultar una Pokédex, el proyecto busca integrar herramientas para construir equipos, analizar estadísticas, consultar movimientos y habilidades, explorar regiones y descubrir nuevas formas de interactuar con la información.

> 🎓 **Origen académico:** RotomPedia comenzó como un proyecto para la Evaluación Final Transversal (EFT) de Desarrollo de Aplicaciones Móviles (DSY1105), durante 2025. Tras finalizar esa etapa con la versión 2.4, el proyecto continuó evolucionando de manera independiente como un desarrollo personal.

## 🚀 Estado actual del proyecto

**Última versión publicada:** `v3.4.0-rc1`  
**Estado:** Release Candidate / En desarrollo  
**Próxima actualización:** `v3.5 — Enciclopedia enriquecida`

[📦 Ver versiones y descargar APK](https://github.com/ArukuKun/RotomPedia/releases)

La versión 3.4 representa una evolución importante de la arquitectura y de la organización de datos de RotomPedia.

Entre sus mejoras destacan:

- Índice nacional compartido entre la Pokédex y las herramientas.
- **1.025 especies y 1.351 variedades** incluidas en el índice de la release.
- Orden nacional y asociación correcta de variantes.
- Clasificación de megaevoluciones, formas regionales y otras variedades.
- Búsquedas parciales en español y filtros combinables.
- Sistema compartido de resolución de sprites e imágenes.
- Mejoras de persistencia y conservación de datos mediante Room.
- Integración con los sistemas existentes de equipos, favoritos y calculadoras.

La versión `3.4.0-rc1` continúa siendo una versión de pruebas, no una publicación estable definitiva.

---

## ✨ Características principales

### 📕 1. Pokédex Nacional

El núcleo de RotomPedia es una Pokédex organizada según la numeración nacional.

**Funcionalidades:**

- Consulta de especies y variedades mediante PokeAPI.
- Búsqueda por nombre, número y términos relacionados.
- Nombres y tipos presentados en español.
- Filtros por generación, tipo y categorías de variantes.
- Sprites, artworks y versiones variocolor cuando están disponibles.
- Estadísticas base y suma total de estadísticas (BST).
- Relaciones de tipos, debilidades y resistencias.
- Líneas evolutivas.
- Formas alternativas, regionales, megaevoluciones y transformaciones.
- Consulta de movimientos y localizaciones.
- Sistema de favoritos con persistencia local.

Las variantes conservan visualmente el número de su especie nacional, mientras los identificadores internos permiten consultar sus datos específicos.

### ⚔️ 2. Team Builder y herramientas competitivas

RotomPedia incluye herramientas orientadas a la creación y análisis de equipos Pokémon.

- Crear y guardar equipos personalizados.
- Seleccionar Pokémon y sus variedades.
- Consultar estadísticas individuales.
- Configurar movimientos y parámetros de los miembros.
- Trabajar con IVs, EVs, naturalezas y niveles.
- Consultar relaciones de tipos y cobertura defensiva.
- Analizar enfrentamientos mediante herramientas complementarias.
- Reutilizar los datos de la Pokédex nacional.

El objetivo es facilitar la planificación de equipos sin depender de catálogos de Pokémon separados.

### 🧮 3. Calculadoras Pokémon

La aplicación incorpora diferentes herramientas de consulta y cálculo relacionadas con las mecánicas de Pokémon, incluyendo:

- Calculadora de estadísticas.
- Calculadora de captura.
- Herramienta de enfrentamiento entre Pokémon.
- Consulta de tipos y efectividad.

Estas funcionalidades seguirán evolucionando para ofrecer resultados cada vez más claros y contextualizados.

### 🎒 4. Objetos, movimientos y habilidades

RotomPedia dispone de información sobre movimientos y objetos utilizados en Pokémon.

La versión 3.5 busca ampliar estos módulos para convertirlos en una enciclopedia más completa, incorporando progresivamente:

- Sprites reales de objetos cuando estén disponibles.
- Nombres y descripciones en español.
- Información de movimientos, potencia, precisión y PP.
- Métodos de aprendizaje.
- Habilidades Pokémon.
- Relaciones entre Pokémon, objetos, movimientos y evoluciones.
- Información contextualizada por juego o edición cuando los datos lo permitan.

**Estado:** ampliación en desarrollo para v3.5.

### 🗺️ 5. Mapas y regiones

La aplicación dispone de un módulo de mapas regionales que sirve como base para futuras mejoras.

Se proyecta ampliar esta funcionalidad hacia mapas interactivos inspirados en PokeInfo, incluyendo navegación por rutas y ciudades, consultas de encuentros, objetos y datos asociados a diferentes versiones de los juegos.

**Estado:** visor existente, con ampliación interactiva planificada.

### 🔐 6. Usuarios, autenticación y favoritos

- Registro e inicio de sesión local.
- Persistencia mediante Room Database.
- Autenticación biométrica compatible con dispositivos Android.
- Gestión de sesión mediante StateFlow.
- Favoritos y equipos vinculados al usuario.
- Conservación de información entre actualizaciones compatibles.

### 🏅 7. Líderes de Gimnasio

RotomPedia conserva un módulo desarrollado durante su etapa académica que utiliza un microservicio REST propio.

Este módulo permite consultar líderes de gimnasio de distintas regiones, mostrando información, imágenes, especialidades y medallas.

El backend está desarrollado de manera independiente con Spring Boot y Spring Data JPA.

> La disponibilidad de esta sección depende de la conexión y configuración del microservicio.

---

## 🛠️ Stack tecnológico

### 📱 Aplicación Android

| Tecnología | Utilización |
|---|---|
| Kotlin | Lenguaje principal |
| Jetpack Compose | Interfaz declarativa |
| Material 3 | Componentes visuales |
| MVVM | Organización de la lógica de presentación |
| StateFlow y Coroutines | Gestión de estados y tareas asíncronas |
| Room / SQLite | Persistencia de datos |
| Retrofit + Moshi | Consumo de servicios REST |
| Coil | Carga de sprites e imágenes |
| Android Biometric | Autenticación biométrica |
| JUnit, Mockito y Robolectric | Pruebas automatizadas |
| Gradle | Compilación y dependencias |

### ☁️ Backend

El repositorio incluye un módulo de microservicio independiente.

- Spring Boot.
- Kotlin y JVM.
- Spring Data JPA.
- H2 Database.
- Controladores REST.

### 🌐 Servicios y fuentes de datos

- [PokeAPI](https://pokeapi.co/) — Información estructurada de Pokémon, movimientos, objetos, especies y otros recursos.
- [PokeAPI Sprites](https://github.com/PokeAPI/sprites) — Recursos gráficos utilizados por la aplicación.
- Microservicio propio — Información de líderes de gimnasio.

---

## 🧱 Arquitectura del proyecto

RotomPedia utiliza una arquitectura Android basada en MVVM, complementada con repositorios compartidos y persistencia local.

El objetivo de esta organización es que las distintas herramientas consulten una fuente común de información, manteniendo sus estados de interfaz independientes.

**Componentes principales:**

- **UI:** pantallas y componentes Jetpack Compose.
- **ViewModels:** lógica de presentación y estados.
- **Repositories:** acceso y coordinación de datos.
- **Room:** índice nacional, cachés y datos del usuario.
- **PokeAPI:** obtención y actualización de información Pokémon.
- **Backend independiente:** funcionalidades complementarias.

La separación entre especies, variedades y formas permite conservar la identidad nacional de cada Pokémon sin confundirla con los identificadores técnicos de la API.

---

## 📜 Historia y evolución del proyecto

RotomPedia comenzó como una entrega académica, pero posteriormente se convirtió en un proyecto de desarrollo continuo.

| Versión | Etapa | Descripción |
|---|---|---|
| v1.0 – v2.4 | 🎓 Académica | Desarrollo para la EFT de DSY1105 durante 2025 |
| v3.0 – v3.2 | 🚀 Expansión personal | Incorporación y evolución de funcionalidades más allá de la evaluación |
| v3.3 | 📕 Pokédex-first | Mejoras de formas, exploración, movimientos y localizaciones |
| v3.4 | 🧱 Arquitectura y organización | Índice nacional, repositorios compartidos, filtros y recursos gráficos |
| v3.5 | 🔨 En desarrollo | Enciclopedia enriquecida de objetos, movimientos y habilidades |
| v3.6 | 📋 Planificada | Mapas interactivos inspirados en PokeInfo |
| v3.7 | 📋 Planificada | Evolución de la interfaz y Rotom contextual |

Las versiones y funcionalidades futuras pueden ajustarse durante el desarrollo.

### 🎓 Etapa académica — EFT 2025

La base original de RotomPedia fue desarrollada como parte de la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**.

Durante esta etapa se implementaron funcionalidades como autenticación local, biometría, favoritos, consumo de APIs, mapas y un backend de líderes de gimnasio.

La versión **2.4 marcó el cierre del desarrollo destinado a la evaluación académica**.

### 🌟 Etapa de desarrollo personal — Desde v3.0

Tras finalizar la evaluación, decidimos continuar evolucionando RotomPedia como un proyecto independiente.

El nuevo objetivo dejó de estar limitado a los requisitos de una asignatura, orientándose hacia una herramienta Pokémon más completa, práctica y agradable de utilizar.

Esta nueva etapa busca combinar la experiencia de consulta que ofrecía PokeInfo con funcionalidades propias, una interfaz en español y una arquitectura que permita seguir incorporando contenido.

---

## 🗓️ Hoja de ruta

### ✅ Completado

- [x] Proyecto académico EFT hasta v2.4.
- [x] Expansión funcional desde v3.0.
- [x] Mejoras de la Pokédex y formas en v3.3.
- [x] v3.4-A: persistencia y arquitectura compartida.
- [x] v3.4-B: índice nacional, identidades y filtros.
- [x] v3.4-C: resolución de imágenes y recursos gráficos.
- [x] Publicación de v3.4.0-rc1 para validación.

### 🔨 En desarrollo — v3.5

- [ ] Ampliación de la enciclopedia de objetos.
- [ ] Información detallada de movimientos.
- [ ] Integración de habilidades Pokémon.
- [ ] Mejoras de traducción y presentación.
- [ ] Relaciones entre las diferentes herramientas.

### 🔮 Futuras versiones

- [ ] Mapas interactivos por región y edición.
- [ ] Consulta de Pokémon y objetos por localización.
- [ ] Mejoras generales de UI/UX.
- [ ] Rotom como acompañante contextual.
- [ ] Comentarios y consejos según la pantalla y las acciones del usuario.
- [ ] Ampliación de funciones disponibles sin conexión.

---

## 📥 Instalación y ejecución

### Descargar la aplicación

Puedes consultar las versiones publicadas desde:

**[GitHub Releases — RotomPedia](https://github.com/ArukuKun/RotomPedia/releases)**

La última versión pública de referencia es `v3.4.0-rc1`, distribuida para pruebas.

**Requisitos de esa versión:**
- Android 7.1 o superior.
- Conexión a Internet para consultas que no estén disponibles en caché.

**Advertencia:** la APK publicada es una compilación de desarrollo. Antes de actualizar una instalación que contiene equipos o favoritos importantes, conviene respaldar los datos. Desinstalar la aplicación puede eliminar su información local.

### Compilar desde el código fuente

1. Clonar el repositorio:

```bash
git clone https://github.com/ArukuKun/RotomPedia.git
```

2. Abrir la carpeta `RotomPedia/RotomPedia` del repositorio en Android Studio.

3. Sincronizar las dependencias de Gradle.

4. Seleccionar un emulador o dispositivo Android compatible.

5. Compilar y ejecutar la aplicación.

Para generar una APK de depuración desde el proyecto Android:

**Windows:**
```powershell
.\gradlew.bat assembleDebug
```

**Linux/macOS:**
```bash
./gradlew assembleDebug
```

### Backend de líderes de gimnasio

El backend se encuentra en la carpeta `rotompediabackend/`.

Para utilizar el módulo de líderes, es necesario iniciar el microservicio y configurar la aplicación Android para acceder a su dirección correspondiente.

Las funciones basadas directamente en PokeAPI no requieren este backend.

---

## 👨‍💻 Desarrolladores y colaboradores

RotomPedia fue creado originalmente como un proyecto colaborativo.

### [ZorVaK749](https://github.com/ZorVaK749)

**Desarrollo Android, backend y evolución del proyecto**

Entre sus contribuciones se encuentran:

- Desarrollo del microservicio original.
- Implementación de la Pokédex y favoritos.
- Integración de APIs y persistencia.
- Desarrollo de lógica de aplicación y ViewModels.
- Pruebas automatizadas.
- Desarrollo y evolución de funcionalidades posteriores a la etapa académica.

### [ArukuSR](https://github.com/ArukuSR)

**Autenticación, mapas y gestión de versiones**

Entre sus contribuciones se encuentran:

- Implementación inicial de autenticación y registro.
- Desarrollo del módulo original de mapas regionales.
- Gestión del repositorio y combinación de ramas.
- Preparación de compilaciones y firma durante la etapa académica.

El proyecto conserva el reconocimiento de sus contribuciones originales y continúa evolucionando más allá de la evaluación universitaria.

---

## 💜 Inspiración y agradecimientos

RotomPedia está inspirada en **PokeInfo, de Bruno Sousa**, una aplicación cuya forma de presentar y organizar la información Pokémon fue una importante referencia para este proyecto.

El propósito no es reproducir literalmente aquella aplicación, sino desarrollar una alternativa propia en español, con nuevas herramientas y una experiencia de uso adaptada a nuestra visión.

Agradecimientos a:

- [PokeAPI](https://pokeapi.co/) y sus colaboradores.
- Los proyectos comunitarios que mantienen información y recursos Pokémon.
- Las personas que prueban y ayudan a mejorar RotomPedia.

### Aviso

RotomPedia es un proyecto independiente y no oficial, desarrollado con fines educativos, de aprendizaje y uso personal.

Pokémon y sus elementos asociados pertenecen a sus respectivos titulares. Este proyecto no está afiliado, patrocinado ni respaldado por Nintendo, Game Freak o The Pokémon Company.

---

**RotomPedia — De un proyecto universitario a una enciclopedia Pokémon en constante evolución.** ⚡
