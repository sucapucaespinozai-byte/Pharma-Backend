# Repetir las mediciones del laboratorio

Estos scripts usan Node con fetch y Chrome. No son dependencias de la aplicación.
Instala aquí las dependencias con npm install y ejecuta los scripts desde la carpeta
padre que contiene PharmaBackend y PharmaSoft. PHARMA_WORKSPACE puede indicar otra
raíz. Los builds y resultados se generan bajo autonoma-s13/.

Crea PharmaBackend/.local/autonoma-credenciales.json (ignorado) con los campos admin
 y vendedor, cada uno con username/password de tus usuarios de prueba. No publiques
ese archivo. No contienen credenciales reales los scripts de medición.

Orden utilizado:
1. Arrancar Oracle, Redis, Nginx y el backend prod recién reiniciado, TTL 10m.
2. node PharmaBackend/scripts/autonoma/medir-cache.cjs: 20 GET por catálogo.
3. node PharmaBackend/scripts/autonoma/preparar-matriz.cjs: crea registros temporales.
4. node PharmaBackend/scripts/autonoma/medir-navegador.cjs --venta: venta desde SPA,
   trazabilidad, error con requestId y caída de Redis. Restaura Redis en finally.
5. Para A, construir variantes sin/con precarga y servirlas con el mismo nginx.conf
   en 8082 y 8083; habilitar esos orígenes solo durante el experimento. Ejecutar
   medir-navegador.cjs sin --venta. Usa red simulada de 80 ms y 1,5 Mbit/s, 3 repeticiones.
6. medir-despliegue.cjs conserva el navegador con caché, crea una marca B-ready.flag
   en .local y espera B-deployed.flag. Construir la versión con texto cambiado, copiar
   assets e index al final y crear esa segunda marca; la recarga compara las versiones.
7. Reiniciar con scripts/iniciar-backend.ps1 -Perfil prod -Reiniciar -CacheTtl 1m;
   ejecutar medir-ttl.cjs. Espera la expiración real, no manipula el reloj.
8. Restaurar con -CacheTtl 10m; ejecutar finalizar-api.cjs para comprobar 600 s,
   retirar únicamente los registros temporales y auditar el log sin exportar secretos.

Antes de repetir revisa los puertos y el esquema de pruebas. La matriz registra una
venta ficticia y detiene Redis brevemente. Ejecutar en laboratorio, no en una botica
que esté atendiendo. Las capturas ocultan el selector con el DNI del cliente temporal.

El analizador utilizado fue una copia local de los recursos públicos de
https://esbuild.github.io/analyze/, ejecutada sin conexión; el metafile no salió del equipo.
