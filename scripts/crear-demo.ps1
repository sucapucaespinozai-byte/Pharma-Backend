$ErrorActionPreference='Stop'
$baseDemo='http://localhost:8080/api/v1'
$sesionDemo=Invoke-RestMethod "$baseDemo/auth/login" -Method Post -ContentType application/json -Body (@{username='admin';password='Admin2026*'}|ConvertTo-Json)
$cabecerasDemo=@{Authorization="Bearer $($sesionDemo.token)"}
function PostDemo($ruta,$cuerpo) { Invoke-RestMethod "$baseDemo/$ruta" -Method Post -Headers $cabecerasDemo -ContentType application/json -Body ($cuerpo|ConvertTo-Json -Depth 5) }
$categoriasDemo=Invoke-RestMethod "$baseDemo/categorias" -Headers $cabecerasDemo
$categoriaDemo=$categoriasDemo|Where-Object nombre -eq 'DEMO S13'|Select-Object -First 1
if (!$categoriaDemo) { $categoriaDemo=PostDemo 'categorias' @{nombre='DEMO S13';descripcion='Datos ficticios para practicar; no usar para ventas reales';activo=$true} }
$productosDemo=Invoke-RestMethod "$baseDemo/productos?pagina=0&tamanio=100&ordenarPor=id&direccion=asc" -Headers $cabecerasDemo
$productoDemo=$productosDemo.content|Where-Object nombre -eq 'DEMO S13 - Producto de practica'|Select-Object -First 1
if (!$productoDemo) { $productoDemo=PostDemo 'productos' @{nombre='DEMO S13 - Producto de practica';precio=2.5;stock=10;categoriaId=$categoriaDemo.id} }
$clientesDemo=Invoke-RestMethod "$baseDemo/clientes?pagina=0&tamanio=100" -Headers $cabecerasDemo
$clienteDemo=$clientesDemo.content|Where-Object email -eq 'demo.s13@example.invalid'|Select-Object -First 1
if (!$clienteDemo) { $clienteDemo=PostDemo 'clientes' @{dni='99001326';nombres='DEMO S13';apellidos='Cliente ficticio';email='demo.s13@example.invalid';telefono='900000000';direccion='Registro ficticio de laboratorio';estado=$true} }
$datosDemo=@{categoriaId=$categoriaDemo.id;productoId=$productoDemo.id;clienteId=$clienteDemo.id;descripcion='Datos ficticios de demostracion; creados para probar Nueva venta.'}
$datosDemo|ConvertTo-Json|Set-Content (Join-Path $PSScriptRoot '../.local/datos-demo.json')
Write-Host 'Datos DEMO S13 listos. Entra a Ventas > Nueva venta y selecciona el cliente y producto DEMO.'
