# Reservas Escuela Escritora Marcela Paz

**Agenda compartida (1.2).** En la bienvenida o en Inicio, abre **Agenda compartida** para solicitar y aprobar reservas entre dispositivos por internet. [Configurar Render y Neon y probar ambos teléfonos](backend/reservas/README.md).

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

- Las cuentas, reservas, bloqueos, documentos e incidencias se guardan en **SharedPreferences**. El inventario de **Materiales compartidos** se guarda en el PC mediante Spring Boot. [Preparar los servicios y demostrar la persistencia](backend/README.md).
- Las contraseñas se almacenan como hash, pero no existe un servidor de autenticación.
- Los PDF, Word y fotos elegidos guardan la URI del archivo y necesitan mantener el permiso de lectura. Las fotos tomadas con la cámara se guardan en el almacenamiento interno de la aplicación.
- Antes de la entrega conviene comprobar la sincronización de Gradle, compilación y los flujos de registro, reservas, Inspectoría e incidencias en Android Studio.

## Entrega firmada

En `entrega/`, el APK sirve para instalar la aplicación en Android 8 o superior. El AAB es el paquete de publicación para Google Play. La rúbrica pide mostrar el APK firmado y explicar su generación (págs. 4 y 6).

Para repetir la entrega en Android Studio:

1. Ejecuta `.\gradlew.bat :app:testDebugUnitTest` y comprueba la variante `release`. El módulo `app` tiene habilitada la optimización con R8.
2. Abre **Build → Generate Signed App Bundle or APK**, selecciona **APK** y el módulo **app**.
3. Selecciona el almacén de firma privado y el alias `reserva-escuela`. Introduce la contraseña local, elige **release** y genera el APK.
4. Repite el asistente seleccionando **Android App Bundle** para generar el AAB con la misma llave.
5. Comprueba la firma del APK con `apksigner verify --verbose --print-certs ReservaEscuela-release.apk`. El comando muestra los esquemas verificados y la huella del certificado que puedes enseñar en la presentación. Para el AAB usa `jarsigner -verify -verbose -certs ReservaEscuela-release.aab`.

Para esta entrega se usaron las tareas Gradle `:app:assembleRelease` y `:app:bundleRelease`. Se comprobó la alineación del APK con `zipalign`, se firmó con `apksigner` y se firmó el AAB con `jarsigner`. La llave privada y su contraseña se conservan fuera del repositorio.
