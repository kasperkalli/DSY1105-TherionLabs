# TherionLabsApp - Captura de prospectos Duoc UC

Proyecto de la asignatura Desarrollo de Aplicaciones Móviles (DSY1105), Evaluación 2.
Aplicación Android para registrar datos personales y preferencias académicas de prospectos,
guardarlos en el dispositivo y consultarlos en un historial local.

## Integrantes

- Diego Barra
- Franco Tubi
- Maicol García
- Benjamín Osorio

## Funcionalidades implementadas

- Formulario de datos personales, establecimiento, modalidad y canal de contacto.
- Selección de región, sede, nivel de interés y hasta tres carreras.
- Validaciones centralizadas: RUT y dígito verificador, correo, celular, fecha de nacimiento,
  campos obligatorios y selección de carreras.
- Borrador compartido entre las pantallas mediante un ViewModel.
- Guardado local con Room, historial con detalles y eliminación con confirmación.
- Mensajes de error, estado de guardado y animaciones de navegación e interacción.
- Foto opcional tomada con la aplicación de cámara del dispositivo.
- Ubicación opcional de la captura con dirección aproximada, coordenadas, precisión y fecha.
- Consulta de la foto y ubicación guardadas desde los detalles del historial.

## Tecnologías y organización

Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel, corrutinas y Room.

```text
app/src/main/java/cl/duoc/therionlabs/
  data/local/        Entidad, DAO, base de datos y migraciones
  data/model/        Datos y errores del formulario
  data/repository/   Acceso a la persistencia
  data/viewmodel/    Estado, validación y operaciones de registro
  ui/components/    Componentes visuales y acceso a cámara/ubicación
  ui/screens/       Formulario, preferencias e historial
  ui/theme/         Tema visual
  util/             Reglas de validación
```

## Abrir y ejecutar en Android Studio

1. Extraer el ZIP en una carpeta nueva.
2. Elegir **Open** en Android Studio y seleccionar la carpeta que contiene
   `settings.gradle.kts` y `gradlew.bat`.
3. Usar una versión de Android Studio compatible con Android Gradle Plugin 9.3.3.
   Conservar el Gradle wrapper incluido (9.5.0) y un JDK compatible, como el JDK 21
   integrado en Android Studio.
4. Esperar la sincronización de Gradle. La primera ejecución puede necesitar internet
   para descargar dependencias y componentes del SDK.
5. Instalar la plataforma Android SDK indicada por el proyecto: API 36, revisión menor 1,
   y las herramientas que solicite Android Studio.
6. Seleccionar un teléfono o emulador con Android 8.0 (API 26) o superior.
7. Seleccionar la configuración **app** y pulsar **Run**.

Para compilar desde la terminal de Windows, con Java y el SDK configurados:

```powershell
.\gradlew.bat assembleDebug
```

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.
El ZIP no incluye `local.properties`: Android Studio debe configurar la ruta del SDK
para el equipo donde se abra el proyecto.

## Uso de los dos recursos nativos

En la segunda pantalla, antes de guardar, aparece **Foto y ubicación · opcionales**.

### Cámara

1. Pulsar **Tomar foto**.
2. Tomar y confirmar la foto en la aplicación de cámara.
3. Comprobar la vista previa al regresar al formulario.
4. Guardar el prospecto y abrir sus detalles en el historial.

Se usa el contrato `TakePicture` y una URI de `FileProvider`. La captura se delega a la
aplicación de cámara; esta app no accede directamente al hardware ni solicita un permiso
CAMERA propio. La imagen se conserva en el almacenamiento privado de la aplicación.
No se solicita permiso general de almacenamiento. Si no hay una aplicación de cámara o
se cancela la captura, se puede continuar sin foto.

### Ubicación

1. Activar la ubicación del dispositivo.
2. Pulsar **Obtener ubicación actual** y conceder el permiso cuando Android lo solicite.
3. Esperar las coordenadas y la dirección aproximada. Si no hay dirección, se conservan las coordenadas.
4. Guardar y comprobar los datos en los detalles del historial.

Se solicitan permisos de ubicación precisa y aproximada durante el uso. La ubicación
aproximada también se admite. Se usa `LocationManager` con proveedores disponibles;
la escucha se cancela al recibir una posición, abandonar la pantalla o tras 20 segundos.
Si se rechaza el permiso, la ubicación está desactivada o no hay señal, se informa al
usuario y se permite guardar sin ubicación. No hay acceso a ubicación en segundo plano.
En el emulador puede ser necesario enviar una posición desde **Extended controls > Location**.

Ambos adjuntos son opcionales y se pueden retirar del borrador. Durante su captura se
bloquea el botón de guardar para evitar guardar antes de recibir el resultado.

## Comprobación manual recomendada

- Intentar avanzar con campos vacíos y con RUT, correo, celular o fecha inválidos.
- Completar los datos y comprobar que se conservan al volver entre los dos pasos.
- Tomar una foto y también probar cancelar la cámara.
- Obtener ubicación y probar rechazar el permiso o desactivar la ubicación.
- Guardar con ambos recursos y también guardar sin adjuntos.
- Abrir los detalles, cerrar la app y volver a abrir: comprobar persistencia.
- Probar la actualización desde la base versión 2 y comprobar que siguen los registros.
- Probar la eliminación con su confirmación.

## Alcance y datos existentes

Los registros se guardan localmente; no hay sincronización con un servidor implementada.
Las fotos no se suben a internet. Los archivos de foto de borradores descartados o registros
eliminados no tienen limpieza automática en esta versión.

La base de datos utiliza la versión 4. Las migraciones 1 a 2, 2 a 3 y 3 a 4
añaden los campos necesarios sin borrar registros existentes.
La conservación al instalar una actualización exige el mismo identificador de aplicación
y la misma firma. No desinstalar la app si se necesitan conservar sus registros.
El borrador sin guardar no se recupera si Android termina el proceso.

## Colaboración

Repositorio: https://github.com/kasperkalli/DSY1105-TherionLabs

El equipo debe mantener los commits de participación y su planificación en Trello o una
herramienta equivalente, y adjuntar el enlace del tablero utilizado para la entrega.
Este README no acredita actividad colaborativa que no esté registrada en esas herramientas.


## Dirección aproximada

`util/DireccionUbicacion.kt` convierte coordenadas en una dirección usando Geocoder de
Android. La consulta no bloquea la interfaz y tiene una espera máxima de 8 segundos.
En Android 13 o superior se usa la API por callback; en versiones anteriores la consulta
se realiza en un hilo de trabajo. Si no hay servicio, no se encuentra una dirección,
se agota el tiempo o falla la consulta, el registro conserva las coordenadas.

La dirección puede contener calle, comuna o ciudad y no garantiza un número de calle.
Depende del servicio disponible y puede requerir conexión. Geocoder puede consultar al
proveedor de geocodificación del dispositivo con las coordenadas para resolverla; no hay
sincronización del prospecto con un servidor propio. La dirección se guarda junto a la
ubicación y se muestra primero tanto en el formulario como en el historial.
Los registros previos mantienen sus datos y no se geocodifican automáticamente.
