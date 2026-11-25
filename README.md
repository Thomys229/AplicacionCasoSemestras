# Reservas Escuela Escritora Marcela Paz

Aplicación Android desarrollada con **Kotlin**, **Jetpack Compose** y **Material 3** para gestionar reservas de espacios de una escuela, documentos e incidencias.

## Ejecutar

1. Abrir el repositorio en **Android Studio**.
2. Esperar la sincronización de Gradle.
3. Ejecutar en un emulador o teléfono con Android 8.0 (API 26) o superior.
4. Para probar las funciones, registrar una cuenta de demostración como **Inspectoría** y otra como **Docente**.

> Esta aplicación es un prototipo académico: los usuarios pueden seleccionar su perfil al registrarse y no existe autenticación de roles mediante un servidor. No usar este sistema para datos reales o acceso administrativo de producción.

## Funcionalidades de la versión 2

- **Perfiles:** Docente, UTP, Dirección, PIE e Inspectoría. Registro, inicio de sesión y edición de perfil.
- **Solicitudes de reservas:** formulario con grupo, asistentes, recursos y talleres. Validación de capacidad, horarios, días hábiles, anticipación y conflictos.
- **Gestión de Inspectoría:** aceptación y rechazo justificado de solicitudes; bloqueo de espacios.
- **Avisos:** notificaciones locales para solicitudes y decisiones de Inspectoría.
- **Incidencias:** registro de problemas, gravedad, adjuntos PDF/Word, fotos de galería y fotos tomadas con la cámara.
- **Documentos:** agregar PDF, Word, fotos de la galería y fotos tomadas con la cámara. Permite abrir y eliminar archivos.
- **Navegación:** menú lateral, barra inferior y resumen de reservas.

## Archivos principales

| Componente | Ubicación |
|---|---|
| Modelos y validaciones | `app/src/main/java/com/example/reservaescuela/model/Models.kt` |
| Almacenamiento local | `app/src/main/java/com/example/reservaescuela/data/LocalStore.kt` |
| Estado y lógica | `app/src/main/java/com/example/reservaescuela/ui/AppViewModel.kt` |
| Autenticación | `app/src/main/java/com/example/reservaescuela/ui/screens/AuthScreens.kt` |
| Reservas y bloqueos | `app/src/main/java/com/example/reservaescuela/ui/screens/ReservasScreens.kt` |
| Incidencias | `app/src/main/java/com/example/reservaescuela/ui/screens/IncidenciasScreen.kt` |
| Avisos y perfil | `app/src/main/java/com/example/reservaescuela/ui/screens/CuentaScreens.kt` |
| Navegación | `app/src/main/java/com/example/reservaescuela/ui/navigation/AppNavigation.kt` |
| Campos y archivos | `app/src/main/java/com/example/reservaescuela/ui/components/Components.kt` |

## Historial de desarrollo

La rama `main` conserva la primera versión. La rama `mejoras-v2` agrega funciones mediante commits separados:

1. Roles de usuarios.
2. Solicitudes de reservas y validaciones.
3. Aprobaciones, bloqueos y avisos.
4. Incidencias con adjuntos.
5. Documentación y correcciones.

## Limitaciones del prototipo

- Los datos permanecen en **SharedPreferences** del dispositivo. No se sincronizan entre teléfonos ni entre usuarios instalados en dispositivos distintos.
- Las contraseñas se almacenan como hash, pero no existe un servidor de autenticación.
- Los PDF, Word y fotos elegidos guardan la URI del archivo y necesitan mantener el permiso de lectura. Las fotos tomadas con la cámara se guardan en el almacenamiento interno de la aplicación.
- Antes de la entrega conviene comprobar la sincronización de Gradle, compilación y los flujos de registro, reservas, Inspectoría e incidencias en Android Studio.
