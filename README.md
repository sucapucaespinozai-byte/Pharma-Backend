# PharmaBackend

API de farmacia con autenticación JWT, categorías, productos paginados, clientes,
ventas, reportes, caché Redis y observabilidad. Base recibida: Spring Boot 3.3.4,
Java 21 y Oracle. El frontend se publica en el repositorio Fronted.

## Despliegue en producción

### Arquitectura y puertos

```mermaid
flowchart LR
  L1[Navegador local 1] -->|HTTPS 443 recomendado| N[Nginx]
  L2[Navegador local 2] -->|HTTPS 443 recomendado| N
  L3[Navegador local 3] -->|HTTPS 443 recomendado| N
  N -->|HTTP privado 8080 /api/v1| B[PharmaBackend]
  B -->|JDBC privado 1521| O[Oracle FREEPDB1]
  B -->|TCP privado 6379| R[Redis compartido]
```

En laboratorio: navegador → localhost:8081 → contenedor Nginx:80; API en el
anfitrión:8080; Oracle publicado en localhost:1522 hacia el listener del contenedor
1521; Redis en 127.0.0.1:6379. El navegador nunca consulta Oracle ni Redis.
HTTPS 443 es una recomendación pendiente, no una función ya configurada.

### Variables de entorno

| Variable | Propósito |
|---|---|
| JAVA_HOME | Instalación de Java 21 para construir y ejecutar |
| SPRING_PROFILES_ACTIVE | Seleccionar prod; evita el perfil local en servidores |
| DB_URL | URL JDBC del Oracle de destino |
| DB_USERNAME | Usuario Oracle de la aplicación con permisos mínimos |
| DB_PASSWORD | Contraseña Oracle; solo entorno o gestor de secretos |
| JWT_SECRET | Clave de firma Base64 de al menos 256 bits; diferente por entorno |
| FRONTEND_ORIGINS | Lista exacta de orígenes autorizados, sin comodines |
| REDIS_HOST / REDIS_PORT | Dirección privada y puerto de Redis |
| REDIS_PASSWORD | Autenticación de Redis cuando se configure en producción |

No se publican valores reales. application-local.properties, .local/ y logs/ están
excluidos de Git; application-local.properties también se excluye del JAR. El
perfil local activa dev e importa ese archivo solo desde el sistema de archivos.
Prod no importa esa configuración de desarrollo. No copies esos archivos a un repositorio ni uses la clave de
desarrollo para producción. Las credenciales de las pruebas se leen de un archivo
local ignorado o del entorno, nunca de las capturas o informes.

### Construir y desplegar

1. Configura las variables anteriores en el proceso del servicio. Aprovisiona
   antes el esquema Oracle y sus copias de seguridad; prod usa ddl-auto=validate
   y no ejecuta migraciones automáticamente.
2. Con Java 21: `./mvnw.cmd clean package` en Windows o `./mvnw clean package`
   en Linux. Conserva el JAR, su hash y el commit en una carpeta de versiones.
3. Inicia una sola instancia: `java -jar target/tienda-0.0.1-SNAPSHOT.jar
   --spring.profiles.active=prod`. Comprueba que 8080 esté libre.
4. Desde el frontend, construye y levanta Nginx/Redis con el Compose de deploy.
   No levantes otra instancia Redis en el mismo puerto. La SPA se sirve por el
   proxy y usa /api/v1 relativo.
5. Verifica health público sin detalles, health de admin con db/redis UP, login,
   permisos, un listado y una operación de prueba. Revisa logs sin secretos.

El lanzador scripts/iniciar-backend.ps1 sirve para este laboratorio. Acepta
JAVA_HOME y variables externas; sus valores por defecto describen esta máquina.
En un servidor usa un gestor de procesos y la configuración de su propio entorno.

### Volver a una versión anterior

1. Conserva el JAR anterior y el commit correspondiente antes del despliegue.
2. Si falla health o una verificación funcional, detén solo la instancia nueva,
   ejecuta el JAR anterior con las mismas variables del entorno y verifica health.
3. Restaura también el index y assets de la versión frontend compatible. No borres
   de inmediato los assets con hash que todavía puedan usar pestañas abiertas.
4. Si una futura versión cambia el esquema, prepara antes una migración reversible
   o un plan de restauración de Oracle. No asumas que cambiar el JAR revierte datos.

### Estado de buenas prácticas

| Práctica | Estado | Acción pendiente o alcance |
|---|---|---|
| Rutas y reporte diferidos en SPA | Cumple | Ver README del frontend y mediciones A |
| Presupuestos y hash de bundles | Cumple | Seguir revisando cada build |
| Caché navegador y proxy Nginx | Cumple | Ver tabla B y prueba de despliegue |
| TTL e invalidación de catálogos/ventas | Cumple | Matriz C secuencial verificada; TTL final 10m |
| Caída de Redis y continuidad desde Oracle | Cumple | health informa DOWN; no equivale a alta disponibilidad |
| requestId en logs y errores; visible en SPA | Cumple | Incluye errores de seguridad y negocio |
| Logs sin credenciales, tokens ni DNI completo | Cumple | Auditoría D de esta ejecución; repetir continuamente |
| Actuator con permisos | Cumple | Health público limitado; métricas requieren administrador |
| Perfil prod y secretos externos | Cumple | Operador debe aprovisionar los secretos de cada entorno |
| HTTPS, aislamiento de red y credenciales Redis | Parcial | Configurar certificados, firewall, ACL y red privada |
| Backups Oracle y restauración ensayada | No cumple | Definir retención, destino y simulacro de recuperación |
| Alarmas y continuidad entre tres locales | Parcial | Configurar alertas, monitoreo externo y plan de caída WAN |
| Consistencia bajo concurrencia/múltiples nodos | Parcial | Probar ventas concurrentes y particiones de red |

### Tres recomendaciones priorizadas para los locales

1. **Seguridad del acceso:** HTTPS en Nginx, acceso entre locales por red privada
   o VPN, Oracle y Redis sin exposición pública, mínimos privilegios y rotación
   de secretos. La demostración HTTP local no sustituye estas medidas.
2. **Recuperación de datos:** backups cifrados de Oracle fuera del servidor,
   retención y restauración ensayada; un único Oracle y Redis compartidos para
   mantener catálogos coherentes. Redis es caché, no respaldo de las ventas.
3. **Operación y alertas:** monitoreo de health/latencias/errores desde fuera del
   servidor, alertas si Redis o Oracle caen, logs correlacionados y despliegues por
   versiones con rollback. Medir la conexión de cada local antes de activar precarga.

## Actividad autónoma S13

Informe: [docs/informe-s13.pdf](docs/informe-s13.pdf). Mediciones y capturas en
docs/evidencias-s13/. El informe distingue pruebas reales, simulación de red y
pendientes operativos. No se añade una presentación a las pantallas del negocio.
