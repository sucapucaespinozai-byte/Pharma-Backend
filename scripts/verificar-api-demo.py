"""Ejecuta las solicitudes de la colección Postman con HTTP real (Python estándar).
Configurar E2E_ADMIN_PASSWORD y E2E_VENDEDOR_PASSWORD por entorno.
No ejecuta JavaScript Postman: comprueba los mismos estados y dependencias explícitas.
Solo datos ficticios: la venta 201 consume una unidad del producto de demostración.
"""
from pathlib import Path
import os, json, re, time, datetime, urllib.request, urllib.error
root=Path(__file__).resolve().parents[1]
collection=json.loads((root/'docs/postman/PharmaBackend.postman_collection.json').read_text(encoding='utf-8'))
variables={v['key']:str(v['value']) for v in collection['variable']}
variables.update(adminPassword=os.environ['E2E_ADMIN_PASSWORD'],vendedorPassword=os.environ['E2E_VENDEDOR_PASSWORD'])
variables['baseUrl']=os.environ.get('API_URL',variables['baseUrl'])
results=[];trace={};started=time.perf_counter()
def expand(s):
 s=s.replace('{{$timestamp}}',str(time.time_ns()))
 return re.sub(r'\{\{(\w+)\}\}',lambda m:variables[m[1]],s)
for item in collection['item']:
 req=item['request'];data=expand(req['body']['raw']).encode() if 'body' in req else None
 headers={h['key']:expand(h['value']) for h in req['header']}
 request=urllib.request.Request(expand(req['url']),data=data,method=req['method'],headers=headers)
 t=time.perf_counter()
 try:r=urllib.request.urlopen(request,timeout=30)
 except urllib.error.HTTPError as e:r=e
 with r:
  status=r.code;payload=r.read();body=json.loads(payload) if payload else None;rid=r.headers.get('X-Request-Id')
 expected=int(re.search(r'HTTP (\d+)',item['event'][0]['script']['exec'][0])[1])
 ok=status==expected
 results.append({'caso':item['name'],'esperado':expected,'obtenido':status,'correcto':ok,'ms':round((time.perf_counter()-t)*1000,2),'requestId':rid})
 print(item['name'],status,'OK' if ok else 'FALLO')
 if not ok:break
 if item['name']=='Login admin':variables['adminToken']=body['token']
 if item['name']=='Login vendedor':variables['vendorToken']=body['token']
 if item['name']=='Productos paginados 200':variables['productoId']=str(body['content'][0]['id'])
 if item['name']=='Cliente demo 200':variables['clienteId']=str(body['content'][0]['id'])
 if item['name']=='Venta real demo 201':trace={'venta':body,'requestId':rid}
 if item['name']=='Auditoria ventas 200':
  trace['evento']=next((e for e in body['content'] if e['entidadId']==trace['venta']['id']),None)
  assert trace['evento'] and trace['evento']['requestId']==trace['requestId']
 if item['name']=='Auditoria categorias 200':assert body['totalElements']>0
out=root/'docs/evidencias/s14-estabilizacion';out.mkdir(parents=True,exist_ok=True)
(out/'resultados-api.json').write_text(json.dumps({'fecha':datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=-5))).isoformat(),'ejecutor':'Runner HTTP de la coleccion, no interfaz de Postman','duracionSegundos':round(time.perf_counter()-started,2),'casos':results},ensure_ascii=False,indent=2),encoding='utf-8')
if trace:
 (out/'trazabilidad.json').write_text(json.dumps(trace,ensure_ascii=False,indent=2),encoding='utf-8')
 log=(root/'logs/consola.log').read_text(encoding='utf-8',errors='replace')
 (out/'trazabilidad-log.txt').write_text('\n'.join(l for l in log.splitlines() if trace['requestId'] in l)+'\n',encoding='utf-8')
assert len(results)==len(collection['item']) and all(r['correcto'] for r in results),'Hay casos fallidos. Revisar resultados-api.json.'
