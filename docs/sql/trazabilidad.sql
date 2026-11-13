-- Conectarse con TIENDA al FREEPDB1 existente: localhost:1524, oracle-local2.
-- Sustituir :venta_id por la venta que aparece en trazabilidad.json.
SELECT id, estado, total, creado_por FROM ventas WHERE id = :venta_id;
SELECT fecha, usuario, accion, entidad_id, detalle, request_id
FROM auditoria_eventos WHERE accion = 'VENTA_REGISTRADA' AND entidad_id = :venta_id;
