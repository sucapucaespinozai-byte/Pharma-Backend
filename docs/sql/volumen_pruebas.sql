-- SOLO desarrollo: 5 000 productos y 20 000 ventas SIN detalle para medir listados.
-- Ejecutar una vez por esquema, DESPUES de V4. No es una migracion.
-- La carga se identifica por creado_por='carga-s14', no modifica filas anteriores.
-- Las ventas sin detalle no sirven para validar reportes.
insert into productos (nombre,precio,stock,estado,id_categoria,creado_por)
select 'Producto de prueba S14 '||lpad(level,5,'0'),round(dbms_random.value(1,200),2),
trunc(dbms_random.value(1,500)),1,(select min(id) from categorias),'carga-s14'
from dual connect by level<=5000;

insert into ventas(fecha,total,estado,cliente_id,creado_por)
select systimestamp-numtodsinterval(dbms_random.value(0,365),'DAY'),round(dbms_random.value(5,500),2),
'REGISTRADA',(select min(id) from cliente),'carga-s14'
from dual connect by level<=20000;
commit;
