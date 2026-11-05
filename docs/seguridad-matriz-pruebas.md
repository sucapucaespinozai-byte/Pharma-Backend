# Matriz de pruebas de seguridad

La matriz corresponde al Reto 01 de la guía LP II, Sesión 10. Las cuentas de prueba se crean
con `V2__seguridad.sql`; no deben usarse fuera de desarrollo. Inicia Oracle y la API, configura
`JWT_SECRET` con una clave Base64 de al menos 32 bytes y ejecuta estas pruebas con Swagger o
Postman. Para generar una clave en PowerShell:

```powershell
$bytes = [byte[]]::new(48)
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:DB_USERNAME = '<usuario de Oracle>'
$env:DB_PASSWORD = '<contraseña de Oracle>'
```

Si Oracle no está en `localhost:1522/FREEPDB1`, define también `DB_URL` con la URL de
conexión del esquema. Estos valores se establecen localmente y no se guardan en el repositorio.

| # | Usuario / condición | Petición o acción | Resultado esperado |
|---:|---|---|---|
| 1 | Sin token | `GET /api/v1/health` | `200`; el health check es público. |
| 2 | Sin token | `GET /api/v1/productos` | `401`; debe iniciar sesión. |
| 3 | Credenciales incorrectas | Login como `admin` con contraseña errónea | `401`; mensaje genérico `Usuario o contraseña incorrectos`. |
| 4 | Cuenta deshabilitada | Login `inactivo` | `401` con el mismo mensaje genérico de credenciales incorrectas. |
| 5 | Username vacío | `POST /api/v1/auth/login` | `400`; `validationErrors.username` presente. |
| 6 | Vendedor | Login con la cuenta de prueba | `200`; token, rol `VENDEDOR` y permisos del rol. |
| 7 | Vendedor | Decodificar el payload del JWT | Contiene `sub`, `authorities`, `iat` y `exp`; `exp - iat` coincide con la expiración configurada. |
| 8 | Vendedor | `GET /api/v1/auth/me` | `200`; devuelve la cuenta sin contraseña ni hash. |
| 9 | Vendedor | `GET /api/v1/productos` | `200`. |
| 10 | Vendedor | `DELETE /api/v1/productos/1` | `403`; no puede escribir en el catálogo. |
| 11 | Vendedor | Registrar una venta válida | `201`. |
| 12 | Vendedor | `GET /api/v1/reportes/ventas-por-categoria` | `403`; no tiene `REPORTES_VER`. |
| 13 | Almacenero | Registrar una venta | `403`; no tiene `VENTAS_REGISTRAR`. |
| 14 | Almacenero | `PUT /api/v1/productos/{id}` con datos válidos | `200`. |
| 15 | Administrador | `GET /api/v1/reportes/ventas-por-categoria` | `200`. |
| 16 | Supervisora | Login con la cuenta de prueba | `200`; incluye `VENDEDOR` y `ALMACENERO`, con la unión de sus permisos. |
| 17 | Supervisora | Registrar venta y actualizar producto | `201` y `200`; los permisos de ambos roles se combinan. |
| 18 | Supervisora | `GET /api/v1/reportes/ventas-por-categoria` | `403`; ninguno de sus roles permite consultar reportes. |
| 19 | Administrador | `GET /api/v1/empleados` y `GET /api/v1/roles` | `200`; María aparece con `tieneUsuario: false`. |
| 20 | Administrador | Crear el usuario de María con `empleadoId` y `rolIds` existentes | `201`; incluye el rol, no la contraseña; el hash se guarda con BCrypt. |
| 21 | Administrador | Repetir el alta para el mismo empleado con otro username | `409`; el empleado ya tiene usuario. |
| 22 | Administrador | Crear usuario con un `rolIds` inexistente | `404`; indica los IDs de roles faltantes. |
| 23 | Vendedor | Alterar un carácter del token y repetir una petición protegida | `401`; token no válido. |
| 24 | Vendedor | Usar un token vencido | `401`; token expirado y se debe iniciar sesión de nuevo. |
| 25 | Usuario autenticado | `PATCH /api/v1/auth/password` con contraseña actual y una nueva válida | `204`; la base conserva un hash BCrypt nuevo. |
| 26 | Usuario autenticado | Cambiar la contraseña con clave actual incorrecta o reutilizar la misma | `401` o `409`, respectivamente; no se modifica el hash. |

Para comprobar únicamente las pruebas automatizadas sin iniciar Oracle:

```powershell
.\mvnw.cmd '-Dtest=JwtServiceTest,PermisosPorRolTest,AuthServiceImplTest,SecurityAuthorizationTest' test
```

Las capturas requeridas por la guía deben tomarse al ejecutar las pruebas manuales contra la
base Oracle y guardarse como evidencia de la entrega. No se incluyen tokens ni secretos en este
repositorio.
