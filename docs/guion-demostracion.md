# Guion de demostración de 10 minutos

| Tiempo | Bloque | Acciones y explicación |
|---|---|---|
| 0:00–1:00 | Contexto | La botica necesita controlar stock y responsables. Presentar administrador, vendedor y almacén. Mostrar arquitectura.png: Edge, Angular, Nginx, API, Oracle local 2 y Redis. |
| 1:00–4:30 | Flujo | Entrar como vendedor. Registrar cliente ficticio o elegir S14 Demo. Buscar 00 S14 Producto, anotar stock, vender 2 unidades, abrir comprobante y volver a Productos. Comprobar stock anterior menos 2 sin F5. |
| 4:30–6:00 | Seguridad | Mostrar menú limitado del vendedor, URL /auditoria denegada, 401 con token inválido y 403 en la colección de API. Explicar conservación del borrador al vencer la sesión. |
| 6:00–7:30 | Volumen | Mostrar más de 5000 productos, cambiar página, recargar y conservar URL. Explicar Redis y abrir Actuator health con admin. No inventar cifras de rendimiento nuevas. |
| 7:30–8:30 | Auditoría | Entrar como admin, filtrar VENTA_REGISTRADA, localizar venta y requestId. Buscar ese UUID en el log y comparar creado_por en Oracle. Mostrar los dos eventos nuevos. |
| 8:30–10:00 | Calidad | Ejecutar suite E2E, abrir informe HTML y registro de incidencias. Señalar pruebas sin reintentos y resultados de las tres repeticiones. Si la ejecución excede el bloque, mostrar la corrida guardada e indicar que sigue ejecutándose. |

## Ensayos personales requeridos

El estudiante debe practicar dos veces con cronómetro. No se han inventado ensayos personales ni duraciones.

| Ensayo | Fecha | Duración real | Desvío y mejora |
|---|---|---|---|
| 1 | Pendiente | Pendiente | Completar después de ensayar |
| 2 | Pendiente | Pendiente | Completar después de ensayar |

## Tres verificaciones manuales

Antes de cada recorrido ejecutar docs/sql/preparar-demo.sql sobre los datos de muestra. El script no borra la base ni su historial. Las pruebas automáticas no sustituyen estas tres verificaciones manuales.

| Corrida | Fecha | Duración | Venta y stock | Desvíos |
|---|---|---|---|---|
| 1 | Pendiente | Pendiente | Pendiente | Pendiente |
| 2 | Pendiente | Pendiente | Pendiente | Pendiente |
| 3 | Pendiente | Pendiente | Pendiente | Pendiente |

## Video

Pendiente de grabar por el estudiante: demostración completa de máximo 10 minutos. Añadir aquí el enlace accesible antes de entregar. Las capturas reales permiten preparar el respaldo, pero no se presentan como un video ya grabado.
