# Defensa en Android Studio

La app 1.1 comparte un inventario escolar entre teléfonos conectados al equipo de la demostración. Cada servicio Spring Boot corre en un proceso distinto. El catálogo ofrece categorías y materiales guarda los cambios en una base H2 en disco. Las cuentas, reservas, bloqueos y adjuntos siguen en el teléfono.

## Preparar el equipo

Abre la raíz del repositorio en Android Studio. Instala Android SDK 37 y usa el wrapper Gradle incluido. El proyecto Android solicita JDK 25 para el daemon. Los servicios se pueden ejecutar con JDK 21. Conecta un emulador o un teléfono con depuración USB autorizada.

Desde la raíz, en dos terminales independientes de PowerShell, ejecuta un comando por terminal.

```powershell
.\gradlew.bat -p backend :catalogo:run
.\gradlew.bat -p backend :materiales:run
```

En una tercera terminal, usa el adb de Android SDK Platform-Tools. Si tienes varios dispositivos, añade `-s SERIAL` después de `adb` en cada comando.

```powershell
adb reverse tcp:8081 tcp:8081
adb reverse tcp:8082 tcp:8082
Invoke-RestMethod http://localhost:8081/categorias
Invoke-RestMethod http://localhost:8082/materiales
```

Los servicios escuchan solo en la interfaz local. ADB redirige las solicitudes del teléfono al PC. Repite la redirección después de desconectar el dispositivo. La configuración Android permite HTTP solo para localhost. Esta demostración usa materiales de ejemplo y carece de autenticación de servidor para despliegue público.

## Mostrar el flujo

1. Abre la app, inicia sesión y entra en **Inicio → Materiales compartidos**.
2. Selecciona la categoría **Lectura**, escribe **Atlas** y pulsa **Agregar**. La app hace un POST y vuelve a consultar el inventario.
3. Comprueba el registro con el GET de materiales en PowerShell. Pulsa **Editar**, cambia el nombre y elige **Guardar cambios**.
4. Detén y vuelve a iniciar el servicio de materiales desde el mismo directorio. Pulsa **Actualizar lista** para mostrar que se conservó. Pulsa **Eliminar** y vuelve a consultar el endpoint.
5. En la misma pantalla, muestra el próximo feriado de Chile. Retrofit consulta Nager.Date por HTTPS para el año en curso. Esta consulta informativa requiere internet. El calendario de reservas aplica la regla escolar de lunes a viernes.

| Servicio | Endpoint | Propósito |
| --- | --- | --- |
| Catálogo, 8081 | GET /categorias | Ofrecer categorías al formulario |
| Materiales, 8082 | GET /materiales | Leer el inventario compartido |
| Materiales, 8082 | POST /materiales | Crear con nombre y categoria |
| Materiales, 8082 | PUT /materiales/{id} | Actualizar ambos campos |
| Materiales, 8082 | DELETE /materiales/{id} | Quitar el registro |
| Nager.Date | GET https://date.nager.at/api/v3/PublicHolidays/{año}/CL | Consultar feriados chilenos |

Ejemplo de cuerpo JSON para POST o PUT: `{"nombre":"Atlas","categoria":"Lectura"}`. El servicio responde 400 ante campos vacíos o demasiado largos y 404 al actualizar o borrar un ID inexistente. Los cambios usan sentencias SQL parametrizadas. La carpeta `datos/` se crea en el directorio de ejecución del servicio (con Gradle, `backend/materiales/datos/`) y está excluida de Git.

## Código y pruebas

- `app/src/main/java/com/example/reservaescuela/data/RetrofitService.kt` define los tres clientes y sus contratos.
- `ui/MaterialesViewModel.kt` coordina consultas y cambios. `ui/screens/MaterialesScreen.kt` muestra los datos.
- `backend/catalogo/src/main` y `backend/materiales/src/main` contienen los endpoints, la configuración y el esquema SQL.
- `app/src/test` contiene las pruebas de fechas, reservas, usuarios, archivos e inventario. Las de cada servicio están en su directorio `src/test`.

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleRelease :app:bundleRelease
.\gradlew.bat -p backend test
```

Para un cambio en vivo, agrega una categoría en `Categorias.java`, modifica su prueba, ejecútala y reinicia catálogo. Pulsa **Actualizar lista** en el teléfono y crea un material con esa categoría.
