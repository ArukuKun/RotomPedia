# 📱 RotomPedia (Evaluación DSY1105)

> **Referencia histórica del módulo Android.** El estado de versiones 3.x, futuras 4.x y posteriores se mantiene en el [README principal](../README.md), y el alcance/evidencias en el [Plan Maestro](../docs/ROTOMPEDIA_PLAN_MAESTRO.md). Las características académicas descritas a continuación no sustituyen esos documentos ni certifican el estado actual de cada versión.

RotomPedia es una aplicación nativa de Android, desarrollada en Kotlin, que sirve como una Pokédex moderna y funcional. Este proyecto fue creado como parte de la Evaluación Parcial 2 de la asignatura "Desarrollo de Aplicaciones Móviles" (DSY1105).

La aplicación permite a los usuarios crear una cuenta, iniciar sesión y explorar la Pokédex Nacional completa, accediendo a detalles exhaustivos de cada Pokémon, incluyendo sus estadísticas, descripciones, evoluciones y relaciones de tipo.

## ✨ Características Principales

El proyecto está dividido en dos módulos funcionales principales:

### 1. Sistema de Autenticación y Usuarios
* **Registro de Nuevos Usuarios:** Pantalla para la creación de nuevas cuentas (IE 2.2.2).
* **Login de Usuarios:** Formulario de inicio de sesión para usuarios registrados.
* **Gestión de Sesión:** Mantiene al usuario autenticado dentro de la app.

### 2. Pokédex (Consumo de API y Gestión de Estado)
* **Pokédex Nacional Completa:** Carga y muestra la lista de los 1025 Pokémon utilizando la [PokeAPI](https://pokeapi.co/) (IL 2.5).
* **Navegación Funcional:** Flujo de navegación claro desde la lista principal a la pantalla de detalles, pasando el ID del Pokémon (IE 2.2.1).
* **Gestión de Estado Asíncrono:** La aplicación maneja de forma robusta los estados de **Cargando**, **Éxito** y **Error** en todas las llamadas a la API, usando `ViewModel` y `StateFlow` (IE 2.3.1).
* **Diseño Adaptativo:** La interfaz de usuario soporta completamente el **Modo Claro** y el **Modo Oscuro** del sistema (IE 2.1.1).

### 3. Pantalla de Detalles Avanzada
Al seleccionar un Pokémon, el usuario accede a una pantalla de detalles con la siguiente información:
* **Descripción del Pokédex:** Obtenida de la API y filtrada para mostrar la versión **en español**.
* **Estadísticas Base:** Muestra los stats (HP, Ataque, Defensa, etc.) con barras de progreso dinámicas que cambian de color (rojo a verde) según el valor.
* **Relaciones de Tipo:** Muestra las **debilidades** y **fortalezas** del Pokémon contra otros tipos.
* **Cadena Evolutiva:** Carga y muestra la línea de evolución completa del Pokémon, con sus respectivas imágenes.

## 🛠️ Stack Tecnológico
* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose (para toda la interfaz)
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Navegación:** Navigation Compose
* **Asincronía:** Kotlin Coroutines y StateFlow
* **Networking:** Retrofit (para consumir la API)
* **Parsing JSON:** Moshi
* **Carga de Imágenes:** Coil
* **Colaboración:** Git y GitHub (Flujo de Fork y Pull Request) (IE 2.5.1)

## 👨‍💻 Colaboradores

* **[ZorVaK749](https://github.com/ZorVaK749)**: Desarrollo del módulo de Pokédex (Consumo de API, UI/UX, navegación y pantallas de detalles).
* **[ArukuSR](https://github.com/ArukuSR)**: Desarrollo del módulo de Autenticación (Login y Registro).
