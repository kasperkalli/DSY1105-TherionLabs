# Verificación

## Comprobaciones ejecutadas

- Compilación del APK de revisión: correcta (`assembleDebug`).
- Pruebas unitarias: 8 ejecutadas, sin fallos (`testDebugUnitTest`).
- Análisis de Android: 0 errores y 24 advertencias (`lintDebug`). Se mantienen las versiones de herramientas y dependencias de la entrega.
- Compilación de las pruebas instrumentadas: correcta (`compileDebugAndroidTestKotlin`).
- Firma del APK: verificada con `apksigner`.
- Comparación de las migraciones con el esquema generado por Room: las cuatro tablas, sus columnas, valores predeterminados, índices y claves foráneas coinciden desde las versiones 1, 2, 3 y 4 hasta la 5.

Comprobaciones adicionales con SQLite:

- Migración 1 → 5: esquema correcto y ficha anterior conservada.
- Migración 2 → 5: esquema correcto y ficha anterior conservada.
- Migración 3 → 5: esquema correcto y ficha anterior conservada.
- Migración 4 → 5: esquema correcto y ficha anterior conservada.
- Eliminación: se rechaza evento con prospectos y se permite evento vacío.
- Duplicados: RUT normalizado y correo se comprueban dentro del evento.
- Seguimiento: la base impide borrar una ficha con gestiones.

## Comprobación en dispositivo

No se ejecutaron el recorrido visual ni las pruebas instrumentadas en un teléfono o emulador. Cámara, permisos, ubicación, geocodificación y apertura de aplicaciones externas deben comprobarse en el dispositivo del equipo.

1. Ingresar como administrativo, crear un evento y comprobar que aparece con cero prospectos.
2. Ingresar como captador, abrir el evento y guardar una ficha.
3. Revisar que se abre el listado del mismo evento y que la ficha aparece allí.
4. Ingresar como captador2 y comprobar que no puede ver la ficha del primer captador.
5. Ingresar como administrativo, consultar el evento y registrar dos gestiones; ambas deben conservarse.
6. Probar la búsqueda y el filtro por estado.
7. Cambiar entre Valparaíso y Viña del Mar; las carreras deben corresponder a la sede seleccionada.
8. Intentar repetir un RUT o correo en el mismo evento y comprobar el rechazo. Repetir en otro evento debe permitirse.
9. Probar fotografía y ubicación, incluyendo cancelación y permisos rechazados.
10. Actualizar una instalación de la entrega con la misma firma y consultar las fichas en Registros anteriores sin evento.
