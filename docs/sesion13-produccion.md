# Sesión 13 - PharmaBackend

Backend de farmacia con el contrato real de PharmaSoft: login JWT, catálogos,
clientes paginados, ventas y reportes. Conserva la base Spring Boot 3.3.4 / Java
21 del proyecto recibido; la guía menciona Boot 4, por lo que esa diferencia de
versión debe constar en la sustentación. No se declara como migración a Boot 4.

## Optimización y observabilidad

- Redis 7: categorías y productos, TTL 10 minutos, estadísticas y DTO serializables
  (incluye la categoría anidada del producto y el envoltorio de página).
- Productos: GET /api/v1/productos?pagina=0&tamanio=10&ordenarPor=nombre&direccion=asc.
  Clave distinta por página, tamaño, orden y dirección. Máximo 100 por página.
- Crear/actualizar/eliminar invalidan el catálogo correspondiente. Modificar o
  eliminar una categoría también invalida productos. Registrar/crear/actualizar/
  eliminar ventas invalida productos. La anotación está en métodos públicos que
  se invocan mediante el proxy de Spring, incluido el crear que usa el controlador.
- Caché aplicada alrededor de la transacción y RedisCacheManager transactionAware:
  las escrituras se invalidan tras confirmar la operación en Oracle.
- Fallo de Redis: advertencia sin secretos y lectura desde Oracle, timeout 500 ms.
- RequestIdFilter: UUID generado en el servidor, respuesta X-Request-Id, MDC y log
  de acceso con método, URI, estado y duración; limpieza del MDC en finally.
- Actuator: health público sin detalles; admin con ROLE_ADMIN ve componentes.
  /actuator/** exige USUARIOS_ADMINISTRAR mediante el JWT existente. Vendedor: 403.
- prod: secretos por entorno, validate, SQL y Swagger desactivados, archivo de log
  con rotación de 10 MB, historial de 14 archivos y límite total de 200 MB.

## Ejecución

Usar C:/PHARMAPROYECT/INICIAR-PRACTICA.cmd. Inicia Java 21 y Nginx/Redis de Docker.
Abre http://localhost:8081/login. El API escucha en 8080; Oracle FREEPDB1 / TIENDA
en 1522. Si la API ya está encendida no inicia otra copia. Reinicio deliberado:

```powershell
& C:\PHARMAPROYECT\INICIAR-PRACTICA.ps1 -Perfil prod -Reiniciar
```

El lanzador lee las credenciales de desarrollo locales existentes y las pasa al
proceso mediante DB_URL, DB_USERNAME y DB_PASSWORD. Genera otra clave JWT para
prod en .local/jwt-prod.txt. Ese directorio y application-local.properties están
excluidos de Git. Para un despliegue externo configura tus propios secretos y
orígenes, y no compartas esos archivos.

Para desarrollo y SQL visible: mismo lanzador con -Perfil dev -Reiniciar.
En IntelliJ hay una configuración compartida PharmaBackend Java 21. No uses Run
si el lanzador ya tiene ocupado el puerto 8080; es una segunda instancia.

## Verificación

30 pruebas Java correctas, incluida carga de contexto y pruebas de caché/filtro.
Pruebas reales contra Oracle y Redis en C:/PHARMAPROYECT/evidencias-s13:
invalidation tras escritura, páginas distintas, venta con stock de 5 a 4,
caída de Redis con API 200 y health 503, recuperación a UP, JWT de vendedor 403.
Los registros creados para las pruebas se eliminaron al terminar.

Se tomó una comparación controlada con spring.cache.type=none y redis sobre el
mismo código y datos. No se presenta como una captura histórica anterior a las
sesiones previas. Las consultas de usuarios necesarias para validar JWT no se
confunden con las consultas SQL al catálogo que se está midiendo.
