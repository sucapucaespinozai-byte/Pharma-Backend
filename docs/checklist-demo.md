# Lista previa a la demostración

- [ ] Docker Desktop activo. Usar el MISMO oracle-local2, puerto 1524 hacia listener 1523, servicio FREEPDB1 y esquema TIENDA. No crear ni recrear contenedores.
- [ ] Redis existente deploy-redis-1 en 6379 y Nginx existente deploy-web-1 en 8081 activos.
- [ ] Compilar backend con Java 21 (`mvnw.cmd package`) y arrancar `scripts/iniciar-backend.ps1 -Perfil dev`. Puerto 8080. Las claves permanecen en application-local.properties excluido de Git.
- [ ] Compilar SPA (`npm run build`) para el volumen de Nginx existente, o `npm start` en 4200. Abrir http://localhost:8081.
- [ ] Ejecutar SQL de preparación únicamente sobre el producto ficticio. Confirmar cliente S14 Demo, producto 00 S14 Producto de prueba y stock 200.
- [ ] Comprobar acceso de admin, vendedor y almacen con las credenciales locales de demostración. No capturar contraseñas ni JWT.
- [ ] Cerrar sesiones anteriores. Tener abiertas pestañas de SPA, Actuator, Postman, Oracle, log e informe Playwright.
- [ ] Recorrer los seis bloques del guion dos veces y registrar los tiempos reales.
- [ ] Completar tres recorridos manuales desde los datos preparados.
- [ ] Grabar y verificar video de máximo 10 minutos. Añadir enlace al guion y al informe de entrega.
- [ ] Plan B: capturas en docs/evidencias/s14-estabilizacion/capturas-practica y reporte HTML guardado. Explicar el problema si se usa el respaldo.
- [ ] Antes de etiquetar v1.0-demo confirmar commit, suite estable, cambios subidos y revisión del estudiante.

Las casillas son una verificación previa al día de exposición, no una afirmación de tareas realizadas.
