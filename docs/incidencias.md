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

## Fichas ampliadas

Fecha de hallazgo: 12/11/2026 para INC-01 a INC-08. Fecha de documentación ampliada: 13/11/2026.

### INC-01

Módulo: Productos. Fecha: 12/11/2026. Estado: cerrada.

Pasos: Abrir Productos con 5000 registros.

Esperado: Solo solicitar la página visible. Obtenido antes de corregir: Descargaba páginas completas.

Causa raíz: Concatenación y búsqueda local. Corrección: ver fila INC-01 de la tabla anterior. Commit: 3ea85e4 / 55ea01d. Regresión: E2E: pagina del servidor y URL se conservan con F5.

### INC-02

Módulo: Ventas. Fecha: 12/11/2026. Estado: cerrada.

Pasos: Ir a página 3 y cambiar fecha.

Esperado: Volver a página 1 y preservar filtros en URL. Obtenido antes de corregir: Se mantenía una página fuera del filtro.

Causa raíz: Estado local y respuesta sin Page. Corrección: ver fila INC-02 de la tabla anterior. Commit: 3ea85e4 / 55ea01d / 662e90e. Regresión: E2E-11.

### INC-03

Módulo: Auditoría. Fecha: 12/11/2026. Estado: cerrada.

Pasos: Registrar venta o cambiar precio y consultar responsable.

Esperado: Columnas de responsable y evento confirmado. Obtenido antes de corregir: No había trazabilidad.

Causa raíz: Faltaban columnas y eventos. Corrección: ver fila INC-03 de la tabla anterior. Commit: 55ea01d / 3ea85e4. Regresión: E2E-12 y AuditoriaTransaccionTest.

### INC-04

Módulo: Nueva venta. Fecha: 12/11/2026. Estado: cerrada.

Pasos: Abrir buscador y consultar disponibilidad.

Esperado: Solo productos activos con stock, paginados. Obtenido antes de corregir: Cargaba catálogo completo e incluía no disponibles.

Causa raíz: Selector sin filtrado del servidor. Corrección: ver fila INC-04 de la tabla anterior. Commit: 3ea85e4 / 55ea01d. Regresión: Prueba original venta desde SPA descuenta stock sin F5.

### INC-05

Módulo: Clientes. Fecha: 12/11/2026. Estado: cerrada.

Pasos: Intentar registrar cliente y venderle.

Esperado: Registro desde SPA y selección inmediata. Obtenido antes de corregir: No existía pantalla de registro.

Causa raíz: Faltaba integración de API en SPA. Corrección: ver fila INC-05 de la tabla anterior. Commit: 3ea85e4. Regresión: E2E-10.

### INC-06

Módulo: Pruebas / Ventas. Fecha: 12/11/2026. Estado: cerrada.

Pasos: Completar formulario y pulsar Registrar venta.

Esperado: Un POST y comprobante, sin recarga nativa. Obtenido antes de corregir: Se recargaba sin POST.

Causa raíz: ngSubmit no activo en formulario adaptado. Corrección: ver fila INC-06 de la tabla anterior. Commit: f1ecffe. Regresión: Prueba original venta desde SPA descuenta stock sin F5.

### INC-07

Módulo: Paginador. Fecha: 12/11/2026. Estado: cerrada.

Pasos: Abrir Productos con tamaño inicial 20.

Esperado: Selector 20 y 20 filas. Obtenido antes de corregir: Selector mostraba 10.

Causa raíz: Opciones renderizadas después del value. Corrección: ver fila INC-07 de la tabla anterior. Commit: f1ecffe. Regresión: Prueba original pagina del servidor y URL se conservan con F5.

### INC-08

Módulo: Infraestructura. Fecha: 12/11/2026. Estado: cerrada.

Pasos: Conectar JDBC a localhost:1524.

Esperado: Conexión al Oracle local 2 existente. Obtenido antes de corregir: Listener no accesible.

Causa raíz: Mapeo 1524:1523 con listener interno 1521. Corrección: ver fila INC-08 de la tabla anterior. Commit: 662e90e. Regresión: TiendaApplicationTests y ejecución contra Oracle local2.

## Resumen de incidencias

Por severidad: 6 altas, 1 media, 1 baja, 0 críticas. Por módulo principal: Productos 1, Ventas 2, Auditoría 1, Nueva venta 1, Clientes 1, Paginador 1, Infraestructura 1. Abiertas en el alcance probado: 0. Concurrencia de varios locales y fallos de red no están evaluados y siguen como límites, sin afirmar que estén resueltos.

## INC-09: nombre accesible del filtro de auditoría

Fecha: 13/11/2026. Módulo: Auditoría / pruebas E2E. Severidad: media. Estado: cerrada.

Pasos: crear una venta como admin, abrir Auditoría e intentar seleccionar la acción por su nombre exacto. Esperado: localizar el filtro Acción y seleccionar VENTA_REGISTRADA. Obtenido: timeout en selectOption.

Causa raíz: etiqueta implícita alrededor del select con las opciones dentro del nombre consultado por Playwright. La traza y el snapshot permitieron localizarlo. Corrección: aria-label explícito en el select. Commit frontend ee8dc27. Regresión: E2E-12 aprobada tres veces sin reintentos.

Resumen actualizado: 9 incidencias cerradas, 0 abiertas. Severidad: 6 altas, 2 medias, 1 baja, 0 críticas. Módulos principales: Productos 1, Ventas 2, Auditoría 2, Nueva venta 1, Clientes 1, Paginador 1, Infraestructura 1. No se oculta el fallo inicial ni se lo cuenta como prueba aprobada.
