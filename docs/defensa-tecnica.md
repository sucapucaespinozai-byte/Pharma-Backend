# Preparación de la defensa técnica

Respuestas basadas en este código para estudiar y explicar con palabras propias. El estudiante debe revisarlas y practicar sin apoyo.

1. **¿Por qué paginar en el servidor?** Descargar 5000 productos para mostrar 20 desperdicia red y memoria. PaginacionUtil construye PageRequest con límites de tamaño y campos de orden permitidos. Spring Data aplica offset/fetch al select de la página en Oracle y obtiene el total con una consulta count cuando hace falta. En una última página parcial puede inferir el total y omitir el count. Ver `util/PaginacionUtil.java`, `service/impl/ProductoServicelmpl.java` y `repository/ProductoRepository.java`.

2. **¿Cómo evito una venta a medias?** VentaServicelmpl.crear tiene @Transactional: cabecera, detalles y descuento de stock se confirman juntos o se revierten. El evento se publica dentro de esa transacción y AuditoriaListener espera AFTER_COMMIT. El test AuditoriaTransaccionTest prueba commit y rollback. Esto no prueba ausencia de carreras entre ventas concurrentes de varios locales.

3. **¿Qué pasa si vence el token durante una venta?** AuthService programa expiración y aviso. VentaForm conserva el borrador por usuario en sessionStorage. Al vencer, o al recibir 401 en errorInterceptor, se elimina la sesión y se abre login con returnUrl y expirada=1. Tras autenticarse se vuelve a la ruta y se recupera el borrador. Un token alterado es rechazado por el backend. Ver frontend `core/auth/auth.service.ts`, `core/interceptors/error.interceptor.ts` y `features/ventas/venta-form.ts`.

4. **¿Por qué invalidar caché al vender?** Una venta reduce stock. Reutilizar la página cacheada mostraría unidades anteriores. @CacheEvict limpia productos tras la operación y el interceptor de caché se ordena por fuera del interceptor de transacciones: invalida después de que el método transaccional retorna correctamente. Ver `service/impl/VentaServicelmpl.java` y `config/CacheConfig.java`.

5. **¿Log, columna y bitácora son lo mismo?** El log permite diagnosticar una solicitud. creado_por y modificado_por señalan responsables de la fila. La bitácora conserva acciones con fecha y requestId. Ver `config/RequestIdFilter.java`, `config/AuditoriaConfig.java`, `entity/AuditoriaEvento.java` y docs/auditoria.md.

6. **¿Cuándo considero estable una prueba?** Debe pasar tres repeticiones, sin retries y sin waitForTimeout. Espero URL, elemento visible o respuesta concreta. Si falla, conservo trace.zip, registro causa y arreglo antes de volver a ejecutar. INC-06 documenta el submit incorrecto detectado en la práctica. Ver frontend `playwright.config.ts` y `e2e/`.

7. **¿Ocultar el menú es seguridad suficiente?** No. SiPermiso y permisoGuard mejoran la navegación, pero SecurityConfig autoriza cada petición. Un vendedor recibe 403 al consultar auditoría aunque escriba directamente la URL. Ver `config/SecurityConfig.java`, frontend `app.routes.ts` y `e2e/autonoma.spec.ts`.

8. **¿Cómo sigo una venta?** Comparo el ID de venta, creadoPor, el evento VENTA_REGISTRADA y X-Request-Id. Busco el mismo UUID entre corchetes en el log. RequestIdFilter genera el identificador y EventoAuditoria lo toma del MDC antes del commit. Ver docs/evidencias/s14-estabilizacion/trazabilidad.json.

9. **¿Qué pasa si Redis falla?** CacheConfig registra el error y permite continuar contra Oracle. Evita depender del caché para guardar ventas. CacheBehaviorTest comprueba lectura y escritura con errores de caché. Redis acelera consultas, Oracle mantiene los datos.

10. **¿Por qué dos eventos nuevos y sin datos personales?** CLIENTE_REGISTRADO y CATEGORIA_REGISTRADA permiten seguir altas que se usan en ventas y catálogo. Guardan ID, actor y texto fijo. No necesitan correo, DNI ni contraseñas. Ver `service/impl/ClienteServicelmpl.java`, `CategoriaServicelmpl.java` y `EventosRegistroTest.java`.
