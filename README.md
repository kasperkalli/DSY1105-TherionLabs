# TherionLabs · Valparaíso

Aplicación Android para organizar eventos de captación, registrar prospectos y gestionar su seguimiento en la Región de Valparaíso.

Mantiene el formulario, el historial desplegable, los recursos nativos y el diseño de la entrega de DSY1105. Utiliza azul marino y amarillo, con identidad propia de TherionLabs y sin logotipos de Duoc UC.

## Perfiles

| Perfil | Funciones |
|---|---|
| Administrativo | Crear eventos, consultar todos sus prospectos, registrar gestiones y eliminar eventos vacíos. Puede eliminar prospectos que todavía no tengan gestiones. |
| Captador | Seleccionar un evento existente, registrar prospectos y consultar sus propias capturas dentro de cada evento. |

Las restricciones se aplican en la navegación y en las operaciones de guardado. El rol se comprueba contra la cuenta almacenada antes de crear o eliminar eventos, guardar prospectos y registrar gestiones.

## Cuentas de prueba

| Usuario | Contraseña | Perfil |
|---|---|---|
| administrativo | Admin2026! | Administrativo |
| captador | Captador2026! | Captador |
| captador2 | Captador2026! | Captador |

Las cuentas se inicializan al ingresar por primera vez. Las contraseñas se verifican mediante PBKDF2 con una sal distinta por cuenta. La autenticación es local y la sesión termina al cerrar el proceso de la aplicación.

## Recorrido

1. Ingresar como administrativo y crear un evento con nombre, institución, fecha, comuna y descripción.
2. Cerrar sesión e ingresar como captador en el mismo dispositivo.
3. Seleccionar el evento y pulsar **Registrar prospecto**.
4. Completar datos personales, autorización de contacto y preferencias académicas. La fotografía y la ubicación son opcionales.
5. Elegir Valparaíso o Viña del Mar. Las carreras disponibles cambian según la sede.
6. Guardar. La aplicación abre los prospectos del evento correspondiente.
7. En **Registros**, abrir un evento para consultar sus fichas. El administrativo ve todos los prospectos; el captador ve únicamente los registrados con su cuenta.
8. Como administrativo, desplegar una ficha y abrir **Contacto y seguimiento**. Registrar resultado, canal y observación. El responsable y la fecha se guardan automáticamente.

El historial permite buscar por nombre, RUT, correo y captador. Administración también puede filtrar por estado de seguimiento. Abrir el marcador o el correo no realiza una llamada ni envía un mensaje automáticamente. Sin autorización de contacto solo se habilitan gestiones internas con canal **Otro**.

## Alcance regional

La región de interés es fija: **Región de Valparaíso**. Solo se ofrecen las sedes **Valparaíso** y **Viña del Mar**. El catálogo está separado por sede y escuela; al cambiar de sede se conservan únicamente las carreras que también estén disponibles en la nueva selección.

Se usa un catálogo local de preferencias, consultado en las páginas de ambas sedes. No representa una reserva de vacante ni confirma jornadas o modalidades. Su procedencia se encuentra en [CATALOGO.md](CATALOGO.md).

## Persistencia y actualización

Room utiliza la base `therionlabs_database`, versión 5. La migración desde la versión 4 del proyecto entregado agrega usuarios, eventos, responsables y gestiones sin borrar prospectos, fotografías ni ubicaciones. También se mantienen las migraciones de las versiones 1, 2 y 3.

Las fichas anteriores no tienen evento ni captador asignado. Administración puede consultarlas en **Registros anteriores sin evento**. No se adjudican automáticamente a una cuenta o a una actividad nueva.

El mismo RUT o correo no se puede registrar dos veces dentro del mismo evento. Se normalizan los formatos y la comprobación se realiza en la misma transacción que la inserción. Sí se permite registrar a esa persona en otro evento. Un evento con prospectos no se puede eliminar; una ficha con gestiones tampoco puede borrarse.

La actualización de una instalación existente requiere el mismo identificador de aplicación y la misma firma. El APK de revisión tiene una firma de desarrollo; para actualizar la instalación del equipo se debe compilar con su firma habitual. Este proyecto migra la base del ZIP entregado, no la del otro proyecto que también utilizaba el número de versión 4 con un esquema diferente.

## Abrir en Android Studio

1. Extraer el ZIP y abrir la carpeta que contiene `settings.gradle.kts`.
2. Usar JDK 21 y el Gradle wrapper incluido.
3. Instalar Android SDK API 36, revisión menor 1, y las herramientas que solicite Android Studio.
4. Ejecutar `app` en un teléfono o emulador Android 8.0 / API 26 o superior.

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

Para ejecutar las pruebas de Room y repositorios en un teléfono o emulador conectado:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Organización

- `data/local`: entidades, consultas y migraciones de Room.
- `data/model`: datos y errores del formulario.
- `data/repository`: operaciones de persistencia y comprobación de permisos.
- `data/viewmodel`: acceso, eventos, formularios y estado de las pantallas.
- `domain`: perfiles, eventos, seguimiento, contraseñas y catálogo regional.
- `ui/components`: controles del formulario y recursos nativos.
- `ui/screens`: acceso, eventos, formulario, preferencias, registros y seguimiento.
- `util`: validación de datos y resolución de direcciones.

## Límites actuales

Los perfiles comparten información dentro de una misma instalación. No hay servidor, sincronización entre teléfonos ni recuperación de contraseñas. Las fotografías permanecen en el almacenamiento privado y todavía no tienen limpieza automática al retirar un adjunto o eliminar una ficha. La dirección aproximada depende del servicio de geocodificación del dispositivo; si falla, se conservan las coordenadas.

El borrador de captura se conserva durante los cambios de configuración y puede restaurarse al volver a ingresar con la misma cuenta si Android guardó el estado. Cerrar sesión o confirmar su descarte lo elimina.

## Equipo

Diego Barra, Franco Tubi, Maicol García y Benjamín Osorio.
