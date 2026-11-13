# Resultados de pruebas de la actividad autónoma S14

Fecha local de la suite: 2026-11-13T17:02:39.510000-05:00 (America/Lima).
Versión del frontend probada: `fa5f1ca935223489c73a805451ba0ab9d8a4610e`. Backend: `edf1843f91c5911b3916183389d2bd67910c5ca5`. Estos commits corresponden al mismo contenido de código que se ejecutó; los commits posteriores solo incorporan documentación y evidencias.

## Suite end-to-end

Comando en PowerShell desde PharmaSoft:

```powershell
$env:E2E_URL='http://localhost:8081'
$env:E2E_BROWSER='msedge'
npx playwright test --repeat-each=3
```

14 pruebas distintas, tres repeticiones cada una: **42 aprobadas, 0 fallidas, 0 intermitentes y 0 omitidas**. Duración total: 111.34 segundos. Playwright 1.65.0, Microsoft Edge, un worker, retries=0, sin waitForTimeout. Se ejecuta contra Oracle local 2, Redis y Nginx de la práctica, no contra servicios simulados.

[Informe HTML real](evidencias/s14-estabilizacion/playwright/index.html) y [resultados con cada repetición](evidencias/s14-estabilizacion/resultados-e2e.json). Descargar el repositorio y abrir el HTML localmente; GitHub muestra el archivo como código. También se puede abrir `npx playwright show-report` en el frontend después de ejecutar.

Pruebas originales: ruta protegida, credenciales incorrectas, retorno tras login, venta y stock, paginación con F5, restricción de almacén y edición oculta al vendedor. Nuevas: E2E-08 stock obsoleto y 409 sin venta; E2E-09 venta vacía bloqueada; E2E-10 cliente y venta inmediata; E2E-11 filtro desde página 3; E2E-12 venta y requestId en auditoría; E2E-13 auditoría denegada al vendedor; E2E-14 token alterado.

E2E-08 modifica la cantidad de la solicitud saliente para representar stock obsoleto. El 409 y las consultas de comprobación proceden del backend real, no de una respuesta simulada.

## Fallo encontrado y corregido

La primera ejecución se detuvo al fallar E2E-12 buscando el selector Acción. Se inspeccionó trace.zip y el snapshot: el nombre de la etiqueta implícita incluía el contenido del select. Se añadió aria-label explícito. INC-09 documenta la corrección en frontend `ee8dc27`. La traza original se conserva localmente en tmp/autonoma14-inc09, sin publicarla porque puede contener datos de autenticación. La corrida final posterior a la corrección pasó las tres repeticiones completas. No se habilitaron reintentos.

## Backend y API

Compilación Java 21 y empaquetado correctos. **41 pruebas Java, 0 fallos, 0 errores**. Incluyen commit/rollback, permisos, caché y eventos nuevos sin datos personales.

La [colección Postman](postman/PharmaBackend.postman_collection.json) tiene 16 casos. Se ejecutaron las mismas solicitudes con `scripts/verificar-api-demo.py`, que comprueba estados y dependencias con HTTP real. No se afirma una ejecución desde la interfaz de Postman ni Newman. Resultado: 16/16 correctos, duración 1.67 segundos. [Detalle de respuestas](evidencias/s14-estabilizacion/resultados-api.json).

Incluye productos y auditoría 200, venta y categoría 201, productos/ventas 400, auditoría sin sesión 401, auditoría vendedor 403, venta/producto inexistentes 404 y cantidad/size excesivos 409. Configurar adminPassword y vendedorPassword localmente en Postman, ejecutar las solicitudes en orden. El runner usa E2E_ADMIN_PASSWORD y E2E_VENDEDOR_PASSWORD por entorno. Ningún token de respuesta se guarda en Git.

## Trazabilidad real

Venta **20027**, creadoPor **vendedor**, requestId `edc87e21-7c38-4601-8869-6a84f56a7218`. El evento coincide con la respuesta y con el log. [JSON](evidencias/s14-estabilizacion/trazabilidad.json), [log](evidencias/s14-estabilizacion/trazabilidad-log.txt) y [consulta Oracle](evidencias/s14-estabilizacion/trazabilidad-oracle.txt).

## Alcance

Las ventas consumen stock real del producto ficticio de demostración. No se ha probado concurrencia de varios locales ni fallos WAN. Las corridas E2E son automáticas y no se presentan como los tres recorridos manuales ni los ensayos personales que pide la guía. Esos registros y el video siguen pendientes del estudiante.
