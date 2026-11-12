# PharmaBackend

API de farmacia con autenticaciÃ³n JWT, categorÃ­as, productos paginados, clientes,
ventas, reportes, cachÃ© Redis y observabilidad. Base recibida: Spring Boot 3.3.4,
Java 21 y Oracle. El frontend se publica en el repositorio Fronted.

## Despliegue en producciÃ³n

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

En laboratorio: navegador â†’ localhost:8081 â†’ contenedor Nginx:80; API en el
anfitriÃ³n:8080; Oracle publicado en localhost:1525 hacia el listener del contenedor
1521; Redis en 127.0.0.1:6379. El navegador nunca consulta Oracle ni Redis.
HTTPS 443 es una recomendaciÃ³n pendiente, no una funciÃ³n ya configurada.

### Variables de entorno

| Variable | PropÃ³sito |
|---|---|
| JAVA_HOME | InstalaciÃ³n de Java 21 para construir y ejecutar |
| SPRING_PROFILES_ACTIVE | Seleccionar prod; evita el perfil local en servidores |
| DB_URL | URL JDBC del Oracle de destino |
| DB_USERNAME | Usuario Oracle de la aplicaciÃ³n con permisos mÃ­nimos |
| DB_PASSWORD | ContraseÃ±a Oracle; solo entorno o gestor de secretos |
| JWT_SECRET | Clave de firma Base64 de al menos 256 bits; diferente por entorno |
| FRONTEND_ORIGINS | Lista exacta de orÃ­genes autorizados, sin comodines |
| REDIS_HOST / REDIS_PORT | DirecciÃ³n privada y puerto de Redis |
| REDIS_PASSWORD | AutenticaciÃ³n de Redis cuando se configure en producciÃ³n |

No se publican valores reales. application-local.properties, .local/ y logs/ estÃ¡n
excluidos de Git; application-local.properties tambiÃ©n se excluye del JAR. El
perfil local activa dev e importa ese archivo solo desde el sistema de archivos.
Prod no importa esa configuraciÃ³n de desarrollo. No copies esos archivos a un repositorio ni uses la clave de
desarrollo para producciÃ³n. Las credenciales de las pruebas se leen de un archivo
local ignorado o del entorno, nunca de las capturas o informes.

### Construir y desplegar

1. Configura las variables anteriores en el proceso del servicio. Aprovisiona
   antes el esquema Oracle y sus copias de seguridad; prod usa ddl-auto=validate
   y no ejecuta migraciones automÃ¡ticamente.
2. Con Java 21: `./mvnw.cmd clean package` en Windows o `./mvnw clean package`
   en Linux. Conserva el JAR, su hash y el commit en una carpeta de versiones.
3. Inicia una sola instancia: `java -jar target/tienda-0.0.1-SNAPSHOT.jar
   --spring.profiles.active=prod`. Comprueba que 8080 estÃ© libre.
4. Desde el frontend, construye y levanta Nginx/Redis con el Compose de deploy.
   No levantes otra instancia Redis en el mismo puerto. La SPA se sirve por el
   proxy y usa /api/v1 relativo.
5. Verifica health pÃºblico sin detalles, health de admin con db/redis UP, login,
   permisos, un listado y una operaciÃ³n de prueba. Revisa logs sin secretos.

El lanzador scripts/iniciar-backend.ps1 sirve para este laboratorio. Acepta
JAVA_HOME y variables externas; sus valores por defecto describen esta mÃ¡quina.
En un servidor usa un gestor de procesos y la configuraciÃ³n de su propio entorno.

### Volver a una versiÃ³n anterior

1. Conserva el JAR anterior y el commit correspondiente antes del despliegue.
2. Si falla health o una verificaciÃ³n funcional, detÃ©n solo la instancia nueva,
   ejecuta el JAR anterior con las mismas variables del entorno y verifica health.
3. Restaura tambiÃ©n el index y assets de la versiÃ³n frontend compatible. No borres
   de inmediato los assets con hash que todavÃ­a puedan usar pestaÃ±as abiertas.
4. Si una futura versiÃ³n cambia el esquema, prepara antes una migraciÃ³n reversible
   o un plan de restauraciÃ³n de Oracle. No asumas que cambiar el JAR revierte datos.

### Estado de buenas prÃ¡cticas

| PrÃ¡ctica | Estado | AcciÃ³n pendiente o alcance |
|---|---|---|
| Rutas y reporte diferidos en SPA | Cumple | Ver README del frontend y mediciones A |
| Presupuestos y hash de bundles | Cumple | Seguir revisando cada build |
| CachÃ© navegador y proxy Nginx | Cumple | Ver tabla B y prueba de despliegue |
| TTL e invalidaciÃ³n de catÃ¡logos/ventas | Cumple | Matriz C secuencial verificada; TTL final 10m |
| CaÃ­da de Redis y continuidad desde Oracle | Cumple | health informa DOWN; no equivale a alta disponibilidad |
| requestId en logs y errores; visible en SPA | Cumple | Incluye errores de seguridad y negocio |
| Logs sin credenciales, tokens ni DNI completo | Cumple | AuditorÃ­a D de esta ejecuciÃ³n; repetir continuamente |
| Actuator con permisos | Cumple | Health pÃºblico limitado; mÃ©tricas requieren administrador |
| Perfil prod y secretos externos | Cumple | Operador debe aprovisionar los secretos de cada entorno |
| HTTPS, aislamiento de red y credenciales Redis | Parcial | Configurar certificados, firewall, ACL y red privada |
| Backups Oracle y restauraciÃ³n ensayada | No cumple | Definir retenciÃ³n, destino y simulacro de recuperaciÃ³n |
| Alarmas y continuidad entre tres locales | Parcial | Configurar alertas, monitoreo externo y plan de caÃ­da WAN |
| Consistencia bajo concurrencia/mÃºltiples nodos | Parcial | Probar ventas concurrentes y particiones de red |

### Tres recomendaciones priorizadas para los locales

1. **Seguridad del acceso:** HTTPS en Nginx, acceso entre locales por red privada
   o VPN, Oracle y Redis sin exposiciÃ³n pÃºblica, mÃ­nimos privilegios y rotaciÃ³n
   de secretos. La demostraciÃ³n HTTP local no sustituye estas medidas.
2. **RecuperaciÃ³n de datos:** backups cifrados de Oracle fuera del servidor,
   retenciÃ³n y restauraciÃ³n ensayada; un Ãºnico Oracle y Redis compartidos para
   mantener catÃ¡logos coherentes. Redis es cachÃ©, no respaldo de las ventas.
3. **OperaciÃ³n y alertas:** monitoreo de health/latencias/errores desde fuera del
   servidor, alertas si Redis o Oracle caen, logs correlacionados y despliegues por
   versiones con rollback. Medir la conexiÃ³n de cada local antes de activar precarga.

## Actividad autÃ³noma S13

El informe, las capturas para la presentaciÃ³n y las herramientas de mediciÃ³n
se entregan por separado. El proyecto conserva el backend, sus pruebas y su
lanzador; no contiene el informe PDF ni las capturas acadÃ©micas.

## Sesión 14: integración y estabilización

Se conserva este proyecto y su base Spring Boot 3.3.4 con Java 21. No se migra a Spring Boot 4 durante la estabilización.

- Oracle de esta práctica: contenedor `oracle-pharma`, puerto local 1525. El listener 1522 pertenece a otra instalación. El contenedor original `oracle-local` se conserva detenido.
- `dev` aplica Flyway V4 y valida el esquema. La base anterior se creó manualmente: se registra una línea base V3 sin volver a ejecutar ni fingir haber aplicado V1–V3. No modificar migraciones aplicadas. En `prod` las migraciones automáticas siguen desactivadas. El operador debe aplicar V4 antes.
- Productos y Ventas usan paginación SQL y orden estable por id para desempatar. Máximo 100 filas, lista blanca de orden y HTTP 409 para parámetros fuera del rango.
- Contrato existente conservado: `content`, `number`, `size`, `totalElements`, `totalPages`, `last`.
- Auditoría: columnas `creado_por` y `modificado_por`. `GET /api/v1/auditoria` exige `USUARIOS_ADMINISTRAR`. Eventos de negocio después del commit y login fallido en transacción independiente. El detalle no guarda contraseñas, tokens ni DNI.
- `docs/sql/volumen_pruebas.sql` es una carga manual exclusiva de desarrollo, fuera de las migraciones. Sus ventas sin detalle solo sirven para medir listados.
- Pruebas: `./mvnw.cmd test`. JAR: `./mvnw.cmd package`. El lanzador `INICIAR-BACKEND.cmd` usa Java 21 y dev.
