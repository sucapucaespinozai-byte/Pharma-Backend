# Auditoría de PharmaSoft

La bitácora permite identificar quién hizo una operación y seguir una venta desde la respuesta HTTP hasta Oracle. Solo administradores con `USUARIOS_ADMINISTRAR` consultan `/api/v1/auditoria` y la pantalla `/auditoria`. La SPA usa permisoGuard y oculta el enlace a los demás usuarios. El backend aplica la misma autorización.

La pantalla pagina en el servidor con el componente compartido Paginador. Usuario, acción, desde, hasta, página y tamaño se guardan en los parámetros de la URL. Al filtrar vuelve a página 0. La tabla muestra fecha, usuario, acción, entidad y su ID, detalle y requestId.

| Acción | Momento | Detalle guardado |
|---|---|---|
| LOGIN_EXITOSO | Autenticación correcta | Sin contraseña ni token |
| LOGIN_FALLIDO | Autenticación rechazada | Sin contraseña ni token |
| VENTA_REGISTRADA | Después del commit de la venta | Total y número de líneas |
| PRECIO_MODIFICADO | Después del commit del cambio | Precio anterior y nuevo |
| PRODUCTO_ELIMINADO | Después del commit de la baja lógica | Nombre del producto |
| CLIENTE_REGISTRADO | Después del commit del alta | Texto fijo, ID en entidadId, sin datos personales |
| CATEGORIA_REGISTRADA | Después del commit del alta | Texto fijo, ID en entidadId |

## Tres registros con funciones distintas

Las columnas `creado_por` y `modificado_por` indican el responsable actual del registro mediante AuditorAware y los listeners de JPA. No son el historial completo. La bitácora `auditoria_eventos` conserva acciones con fecha, usuario, entidad y requestId. Los logs explican solicitudes, tiempos y fallos operativos. `RequestIdFilter` genera el UUID y lo devuelve en `X-Request-Id`, y MDC lo añade al log.

Los servicios publican EventoAuditoria dentro de la transacción. `AuditoriaListener` escucha AFTER_COMMIT y `AuditoriaServiceImpl.registrar` abre REQUIRES_NEW. Si la transacción original se revierte, no queda un evento de éxito. Si la escritura de la bitácora falla después del commit, se registra el error en el log: la venta ya confirmada no puede revertirse en ese momento. No se afirma entrega garantizada frente a caída de Oracle; para eso haría falta un outbox.

Un login fallido no tiene un commit de negocio que esperar. AuthServiceImpl registra el intento independientemente para que un rechazo no borre la evidencia.

Nunca se guardan contraseñas, JWT, cabeceras Authorization, claves de conexión ni cuerpos completos de solicitudes. Los nuevos eventos no incluyen DNI, correo, teléfono ni dirección. No se deben volcar sessionStorage ni las credenciales locales en capturas o Git.

## Caso trazado

La evidencia nueva se registra en [trazabilidad.json](evidencias/s14-estabilizacion/trazabilidad.json) y [trazabilidad-log.txt](evidencias/s14-estabilizacion/trazabilidad-log.txt). Incluye venta, creadoPor, evento y mismo requestId del log. La consulta directa en Oracle usa `docs/sql/trazabilidad.sql`. Las capturas de la práctica se conservan en evidencias/s14-estabilizacion/capturas-practica. El test E2E-12 comprueba el caso en la SPA y conserva los filtros al recargar.

Código: `src/main/java/com/example/tienda/auditoria/`, `service/impl/AuditoriaServiceImpl.java`, `config/AuditoriaConfig.java`, `config/RequestIdFilter.java` y `../PharmaSoft/src/app/features/auditoria/`.
