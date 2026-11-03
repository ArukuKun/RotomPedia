# 📱 RotomPedia (Evaluación Final Transversal - EFT)

**RotomPedia** es una aplicación móvil nativa desarrollada en Android (Kotlin) con arquitectura MVVM. Este proyecto representa la culminación de la asignatura "Desarrollo de Aplicaciones Móviles" (DSY1105), integrando consumo de APIs externas, persistencia local, biometría y conexión con un microservicio propio.

El proyecto simula una Pokédex moderna que permite gestionar usuarios, guardar favoritos, visualizar mapas y consultar información en tiempo real de líderes de gimnasio a través de un backend dedicado.

## ✨ Características Principales

### Continuidad: v3.4 terminada; siguiente etapa v3.5

Los hitos v3.4-A/B/C están completados e integrados en la
[prerelease `v3.4.0-rc1`](https://github.com/ArukuKun/RotomPedia/releases/tag/v3.4.0-rc1).
Incluye índice nacional compartido, Pokédex/picker agrupados, filtros locales e
imágenes de la variedad correcta. No se ha promovido a versión estable.

**Documento principal: [Plan Maestro de RotomPedia](docs/ROTOMPEDIA_PLAN_MAESTRO.md).**
Registra evidencias, bugs residuales de captura/textos y el alcance propuesto de
v3.5 (objetos, movimientos y habilidades), pendiente de aprobación. Después siguen
v3.6 (mapas por edición) y v3.7 (Rotom contextual e interfaz).

La [guía v3.3](docs/README-v3.3.md) y su
[validación](docs/v3.3-validacion.md) se conservan como documentación histórica,
no como el plan vigente.

El proyecto ha evolucionado para incluir funcionalidades avanzadas divididas en módulos:

### 1. 🔐 Seguridad y Autenticación
* **Persistencia de Usuarios:** Registro y Login gestionados localmente con **Room Database**.
* **Biometría:** Inicio de sesión mediante **Huella Digital/Biometría** para usuarios recurrentes (IE 2.2.2).
* **Gestión de Sesión:** Manejo de estados de sesión activa/inactiva con `StateFlow`.

### 2. 📕 Pokédex Avanzada (Consumo API Externa)
* **Pokédex Nacional:** Listado de Pokémon consumiendo la [PokeAPI](https://pokeapi.co/).
* **Favoritos (Room):** Sistema para marcar/desmarcar Pokémon favoritos, guardándolos en base de datos local y permitiendo filtrar la lista para ver solo los guardados.
* **Detalles Completos:** Estadísticas, Tipos (con traducción al español), Sprites y Cadena Evolutiva.
* **Buscador Inteligente:** Filtrado por nombre o número en tiempo real.

### 3. ☁️ Microservicio Propio (Backend Spring Boot)
* **Integración Backend:** La app consume un servicio REST propio desarrollado en **Spring Boot (Java)**.
* **Líderes de Gimnasio:** Visualización de líderes por región (Kanto, Johto, Hoenn) con datos e imágenes servidas desde el microservicio (IE 3.1.1).
* **Visualización Dinámica:** Tarjetas con foto, especialidad (con color dinámico) y medalla.

### 4. 🗺️ Extras y Calidad de Software
* **Visor de Mapas:** Pantalla interactiva para visualizar mapas de las regiones del juego.
* **Pruebas Unitarias:** Implementación de Tests Unitarios con **JUnit** y **Mockito** cubriendo la lógica de negocio (ViewModel) (IE 3.2.1).
* **UI Adaptativa:** Interfaz moderna construida 100% con **Jetpack Compose**.

## 🛠️ Stack Tecnológico

### 📱 Frontend (Android)
* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose (Material3)
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Base de Datos Local:** Room Database (SQLite)
* **Networking:** Retrofit + Moshi Converter
* **Imágenes:** Coil
* **Testing:** JUnit 4 + Mockito
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
3.  **Frontend:** Abrir la carpeta `app` en Android Studio y ejecutar en un emulador/dispositivo.
    * *Nota:* Asegurarse de que el backend esté corriendo para ver la sección de "Líderes".

---
*Evaluación Final Transversal - DSY1105 - 2025*
