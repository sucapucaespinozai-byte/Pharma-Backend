-- Datos de demostración existentes. No borra tablas, ventas ni la bitácora.
-- Ejecutar antes de cada ensayo, sin otros usuarios vendiendo el producto.
UPDATE productos SET stock = 200
WHERE nombre = '00 S14 Producto de prueba';
COMMIT;
SELECT id, nombre, precio, stock FROM productos WHERE nombre = '00 S14 Producto de prueba';
SELECT id, nombres, apellidos FROM cliente WHERE nombres = 'S14 Demo';
-- Esta preparación restablece SOLO el stock de la muestra. No simula una base vacía.
