# Agenda compartida en dos celulares

En ambos celulares instala la misma versión 1.2 y entra en **Agenda compartida** desde la bienvenida o Inicio. Esta agenda tiene datos propios e incluye las solicitudes docentes y las reservas confirmadas por Inspectoría. El formulario anterior **Reservar** sigue usando datos locales. Los bloqueos locales y las reservas antiguas del teléfono no se cargan a la nube.

## 1. Crear la base en Neon

En [Neon](https://console.neon.tech) crea un proyecto de demostración. En **Connect**, elige tu base y el rol propietario del proyecto. Anota el host, el nombre de la base, el rol y su contraseña en un lugar privado. La API crea las tablas en el primer arranque. [Guía de la clase](https://github.com/Umbingelelo/tienda-neon)

## 2. Publicar la API en Render

En [Render](https://dashboard.render.com), entra a **New → Blueprint**, conecta el repositorio **Thomys229/AplicacionCasoSemestras** y selecciona la rama que contiene `render.yaml`. Revisa el plan que muestra el panel antes de crear el servicio. La configuración ya indica el Dockerfile y el endpoint de salud.

| Variable privada en Render | Valor |
| --- | --- |
| DB_URL | `jdbc:postgresql://HOST/BASE?sslmode=require` sustituyendo HOST y BASE por los de Neon |
| DB_USERNAME | Rol de PostgreSQL indicado por Neon |
| DB_PASSWORD | Contraseña de ese rol |
| DOCENTE_PASSWORD | Contraseña exclusiva que eliges para el acceso docente de la demo |
| INSPECTORIA_PASSWORD | Otra contraseña exclusiva para el acceso de Inspectoría |

Mantén las cinco variables en el panel de Render. Para la defensa usa dos cuentas de demostración y actividades ficticias. Cuando Render muestre **Live**, abre la URL pública del servicio seguida de `/api/health`. Debe devolver `{"estado":"ok"}`. Guarda la URL base HTTPS para ambos teléfonos. [Guía de la clase](https://github.com/Umbingelelo/tienda-render)

## 3. Ensayar la presentación

1. En el teléfono de tu compañero abre **Agenda compartida**, pega la URL base de Render, elige **docente** e introduce la contraseña docente. En tu teléfono entra como **inspectoria** con la otra contraseña.
2. Solicita el laboratorio para el siguiente día hábil, 09:00–10:00. En Inspectoría aparece **Pendiente** en la siguiente consulta automática. Pulsa **Aceptar**. El primer teléfono mostrará **Aceptada**. También puedes rechazar solicitudes.
3. Crea desde Inspectoría otra reserva para un horario libre. Se confirma directamente y aparece en el teléfono docente. La agenda consulta cada cinco segundos mientras está visible. La primera consulta puede tardar si el servicio estaba suspendido.
4. Para mostrar persistencia, cierra y vuelve a abrir la agenda o consulta en el editor SQL de Neon: `SELECT espacio, fecha, inicio, fin, estado FROM reserva_compartida ORDER BY fecha, inicio;`.
5. Si ya estaba instalada una versión firmada en modo debug, usa otro dispositivo de prueba o vuelve a compilar con la misma firma para conservar los datos locales.

El servidor comprueba capacidad, fechas y jornada académica en horario de Chile. Al aprobar, bloquea la fila del espacio dentro de una transacción para impedir dos aprobaciones que se crucen. En `AgendaTest.java` hay pruebas HTTP de dos clientes, autorización y aprobaciones concurrentes. Ejecuta `.\gradlew.bat -p backend :reservas:test` desde la raíz. La prueba local usa H2 en memoria y CI ejecuta las mismas pruebas en PostgreSQL.
