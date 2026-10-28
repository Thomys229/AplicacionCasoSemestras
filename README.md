# Reservas Escuela Escritora Marcela Paz

App Android hecha con Kotlin, Jetpack Compose y Material 3 para reservar espacios de la escuela y subir documentos.

## Cómo ejecutarla

1. Abrir esta carpeta en Android Studio (la que contiene `gradlew` y `settings.gradle.kts`) y esperar el Gradle Sync.
2. Ejecutar en un emulador o teléfono con Android 8.0 (API 26) o superior.
3. Registrar una cuenta y probar las pantallas.

## Pantallas

| Pantalla | Archivo |
|---|---|
| Pantalla de bienvenida | `ui/screens/AuthScreens.kt` (WelcomeScreen) |
| Registro de usuarios | `ui/screens/AuthScreens.kt` (RegisterScreen) |
| Iniciar sesión | `ui/screens/AuthScreens.kt` (LoginScreen) |
| Vista de bienvenida (inicio) | `ui/screens/HomeScreen.kt` |
| Reservar y mis reservas | `ui/screens/ReservasScreens.kt` |
| Editar datos de usuario | `ui/screens/CuentaScreens.kt` (PerfilScreen) |
| Cargar, ver y eliminar documentos | `ui/screens/DocumentosScreen.kt` |
| Cerrar sesión | `ui/screens/CuentaScreens.kt` (LogoutScreen) |

## Requisitos del encargo

| Requisito | Dónde está |
|---|---|
| Material 3 | Todas las pantallas, tema en `ui/theme/Theme.kt` |
| Formularios validados | Login, registro, perfil y reservas |
| Navegación | `ui/navigation/AppNavigation.kt` (Navigation Compose) |
| Menú | Menú lateral (icono de las tres rayas) |
| Barra superior e inferior | `MainNav` en `AppNavigation.kt` |
| Gestión de estado | `ui/AppViewModel.kt` |
| Almacenamiento local | `data/LocalStore.kt` (SharedPreferences) |
| Recursos nativos (PDF y Word) | Selector de archivos en `ui/components/Components.kt` |
| Animaciones | Transiciones entre pantallas, bienvenida y contadores del inicio |

## Estructura

```
model/        datos (usuario, reserva, documento, espacios)
data/         guardado local
utils/        hash de contraseñas
ui/           AppViewModel (estado)
ui/navigation rutas, menú y barras
ui/screens    una pantalla por función
ui/components campos de formulario y selector de archivos
```

## Entrega

**GitHub:** crear un repositorio, subir el proyecto (`git init`, `git add .`, `git commit`, `git push`) y agregar al profesor en Settings → Collaborators.

**Trello:** crear un tablero con listas (Pendiente, En progreso, Terminado), una tarjeta por pantalla, y compartirlo con el profesor por correo.

## Alcance

Los datos son de demostración y quedan en el teléfono (no hay servidor). Las contraseñas se guardan como hash. De los archivos se guarda la referencia que entrega Android, no una copia.
