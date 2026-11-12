# Incidencias S14

Registradas al detectar cada defecto, antes de corregirlo. Validación final con
oracle-local2: 38 pruebas Java correctas, 21/21 ejecuciones E2E sin reintentos y
8/8 casos de la matriz de integración. No quedan incidencias altas abiertas
en el alcance probado de esta práctica.

| ID | Módulo y reproducción | Severidad | Causa | Corrección y commit | Estado |
|---|---|---|---|---|---|
| INC-01 | Productos descarga todo el catálogo al abrir | Alta | Concatenación de páginas y filtro local | Página y búsqueda del servidor, URL y cancelación de solicitudes. Frontend 3ea85e4; backend 55ea01d | Cerrada |
| INC-02 | Ventas sin paginación; filtros no conservan URL y estados no válidos | Alta | Respuesta de lista completa y estado solo en memoria | Page en servidor, filtros/orden/tamaño en URL, página 0 al cambiar filtros. Frontend 3ea85e4; backend 55ea01d y 662e90e | Cerrada |
| INC-03 | Cambiar precio o vender no permite identificar responsable | Alta | Sin columnas de auditoría ni eventos | V4, AuditorAware, eventos AFTER_COMMIT, login independiente y consulta protegida. Backend 55ea01d; pantalla 3ea85e4 | Cerrada |
| INC-04 | Nueva venta carga todo y ofrece productos no disponibles | Alta | Selectores sin búsqueda ni filtro de disponibilidad | Buscadores de servidor, estado activo y stock positivo. Frontend 3ea85e4; backend 55ea01d | Cerrada |
| INC-05 | No se puede registrar un cliente desde la SPA | Media | API existente sin pantalla correspondiente | Registro integrado y búsqueda inmediata en Nueva venta. Frontend 3ea85e4 | Cerrada |
| INC-06 | Playwright: Registrar venta recarga sin enviar POST | Alta | ngSubmit sin formGroup en el formulario adaptado | Listener submit con preventDefault y guardado único. Frontend f1ecffe; prueba real de venta y stock | Cerrada |
| INC-07 | Selector muestra 10 con 20 filas iniciales | Baja | Opciones renderizadas después del value del select | Selected por opción y comprobación del valor inicial en E2E. Frontend f1ecffe | Cerrada |
| INC-08 | oracle-local2 no recibe conexiones en 1524 | Alta | Docker publica 1524:1523 pero listener escuchaba en 1521 | Listener existente en 1523, servicios registrados, JDBC en 1524. Mismo contenedor, sin recrearlo. Backend 662e90e | Cerrada |

## Matriz de integración comprobada

| ID | Resultado observado | Estado |
|---|---|---|
| I-01 | Cliente registrado desde la SPA y seleccionado en una nueva venta | Correcto |
| I-02 | Búsqueda paginada ofrece productos activos con stock al precio vigente | Correcto |
| I-03 | Venta de 2 unidades y stock actualizado al navegar por el menú sin F5 | Correcto |
| I-04 | Reporte incrementado en 2 unidades y visible en Inicio como admin | Correcto |
| I-05 | Evento VENTA_REGISTRADA con vendedor y requestId coincidente con la respuesta | Correcto |
| I-06 | Precio editado desde SPA con evento anterior/nuevo y modificadoPor=admin | Correcto |
| I-07 | Expiración con reloj de navegador controlado, login y borrador recuperado | Correcto |
| I-08 | Desde página 5, cambiar fecha vuelve a página 1 con estado en URL | Correcto |

También se verificó HTTP 403 al consultar auditoría como vendedor, y que una
venta rechazada con HTTP 409 no aumente el número de eventos de venta.
Las pruebas Java verifican además que el listener no registre eventos tras rollback.

Los datos de volumen son ficticios y solo de desarrollo. Las ventas de carga
carecen de detalle y no validan reportes. Las pruebas de venta usan el producto
00 S14 Producto de prueba y leen el stock actual antes de actuar.
No se han evaluado concurrencia entre locales, fallos de WAN ni carga de producción.
