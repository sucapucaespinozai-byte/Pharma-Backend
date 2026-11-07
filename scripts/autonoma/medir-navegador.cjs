const {chromium}=require('playwright');
const {fs,path,out,privateDir,credentials,save,login,makeApi,pages,must,docker}=require('./mediciones-comunes.cjs');
const assert=require('node:assert/strict');
const browserOptions={executablePath:'C:/Program Files/Google/Chrome/Application/chrome.exe',headless:true};
async function entrar(page,base){
 await page.goto(base+'/login');await page.locator('#username').fill(credentials.admin.username);
 await page.locator('#password').fill(credentials.admin.password);
 await page.getByRole('button',{name:'Ingresar',exact:true}).click();
 await page.waitForURL('**/categorias');await page.waitForLoadState('networkidle');
}
async function runComparacion(browser){
 const cases=[];
 for(const [label,port]of [['sin-precarga',8082],['con-precarga',8083]]){
  const runs=[];
  for(let iteration=0;iteration<3;iteration++){
   const ctx=await browser.newContext({viewport:{width:1365,height:860}}),p=await ctx.newPage();
   const cdp=await ctx.newCDPSession(p);await cdp.send('Network.enable');await cdp.send('Network.setCacheDisabled',{cacheDisabled:true});
   await cdp.send('Network.emulateNetworkConditions',{offline:false,latency:80,downloadThroughput:187500,uploadThroughput:93750,connectionType:'cellular3g'});
   let bytes=0,requests=[];cdp.on('Network.loadingFinished',e=>bytes+=e.encodedDataLength);
   p.on('request',r=>requests.push(new URL(r.url()).pathname));
   await entrar(p,'http://localhost:'+port);
   const bytesPantalla=bytes,firstRequests=[...new Set(requests.filter(x=>x.endsWith('.js')))];
   const t=performance.now();
   await Promise.all([p.waitForResponse(r=>new URL(r.url()).pathname.startsWith('/api/v1/ventas')&&r.status()===200),p.getByRole('link',{name:'Ventas',exact:true}).click()]);
   await p.getByRole('heading',{name:'Ventas',exact:true}).waitFor();
   const ventasMs=performance.now()-t;
   runs.push({iteracion:iteration+1,bytesPrimeraPantalla:bytesPantalla,ventasMs,chunksPrimeraPantalla:firstRequests});
   if(iteration===0)await p.screenshot({path:path.join(out,'A-'+label+'.png'),fullPage:true});
   await ctx.close();
  }
  cases.push({variante:label,condiciones:{latenciaMs:80,bajadaBytesSegundo:187500,cacheDeshabilitada:true,iteraciones:3},runs,mediaBytes:runs.reduce((a,b)=>a+b.bytesPrimeraPantalla,0)/3,mediaVentasMs:runs.reduce((a,b)=>a+b.ventasMs,0)/3});
  console.log(label+': '+cases.at(-1).mediaBytes.toFixed(0)+' bytes; Ventas '+cases.at(-1).mediaVentasMs.toFixed(1)+' ms');
 }
 save('A-precarga.json',cases);
}
async function runVenta(browser){
 const f=JSON.parse(fs.readFileSync(path.join(privateDir,'autonoma-fixtures.json'),'utf8'));
 const s=await login(),api=makeApi(s.token),ctx=await browser.newContext({viewport:{width:1365,height:900}}),p=await ctx.newPage();
 const errors=[];p.on('pageerror',e=>errors.push(e.message));
 try{
  await entrar(p,'http://localhost:8081');await p.goto('http://localhost:8081/ventas/nueva');
  await p.locator('#sale-client').selectOption(String(f.cliente.id));
  await p.locator('.sale-line__product select').selectOption(String(f.producto.id));
  await p.locator('.sale-line__quantity input').fill('1');
  await p.screenshot({path:path.join(out,'D-venta-formulario.png'),fullPage:true,mask:[p.locator('#sale-client')]});
  const r=await Promise.all([p.waitForResponse(r=>r.request().method()==='POST'&&new URL(r.url()).pathname==='/api/v1/ventas'),p.getByRole('button',{name:'Registrar venta',exact:true}).click()]).then(x=>x[0]);
  const venta=await r.json();assert.equal(r.status(),201);
  f.venta=venta;fs.writeFileSync(path.join(privateDir,'autonoma-fixtures.json'),JSON.stringify(f,null,2));
  const id=r.headers()['x-request-id'];await p.waitForURL('**/ventas/'+venta.id);await p.getByRole('heading',{name:'Información de la venta'}).waitFor();
  await p.screenshot({path:path.join(out,'D-venta-traza.png'),fullPage:true});
  const producto=(await pages(api,'productos')).find(x=>x.id===f.producto.id);
  const matrix=JSON.parse(fs.readFileSync(path.join(out,'C-matriz.json'),'utf8'));
  matrix.push({id:'C-04',operacion:'Venta registrada desde SPA',esperado:6,obtenido:producto?.stock,correcto:producto?.stock===6,requestId:id});save('C-matriz.json',matrix);
  await p.goto('http://localhost:8081/productos');await p.waitForLoadState('networkidle');await p.screenshot({path:path.join(out,'C-04-stock.png'),fullPage:true});
  const log=fs.readFileSync('PharmaBackend/logs/pharma-backend.log','utf8').split(/\r?\n/).filter(l=>l.includes(id));
  save('D-trazabilidad.json',{ventaId:venta.id,requestId:id,http:201,lineas:log,explicacion:['Inicio: número de líneas a registrar, sin DNI ni datos sensibles.','Fin: id interno de la venta preparada y duración del servicio.','Acceso: confirma respuesta HTTP 201 después de finalizar la transacción.'],erroresJavaScript:errors});
  const errorPromise=p.waitForResponse(r=>new URL(r.url()).pathname==='/api/v1/ventas/999999999');
  await p.goto('http://localhost:8081/ventas/999999999');const re=await errorPromise,body=await re.json();
  await p.getByRole('alert').filter({hasText:'Código de seguimiento:'}).waitFor();
  const text=await p.getByRole('alert').innerText();
  assert(body.requestId&&text.includes(body.requestId)&&body.requestId===re.headers()['x-request-id']);
  await p.screenshot({path:path.join(out,'D-error-requestId.png'),fullPage:true});
  save('D-error.json',{status:re.status(),requestId:body.requestId,cabecera:re.headers()['x-request-id'],mensajeSPA:text});
  const metric=await api('/actuator/metrics/http.server.requests?tag=uri:/api/v1/ventas');
  const postMetric=await api('/actuator/metrics/http.server.requests?tag=uri:/api/v1/ventas&tag=method:POST');
  const anon=await fetch('http://localhost:8080/actuator/health').then(r=>r.json());const admin=(await api('/actuator/health')).data;
  save('D-monitoreo-up.json',{anonimo:anon,administrador:admin,ventas:metric.data,ventasPost:postMetric.data});
  try{
   docker('stop','deploy-redis-1');await p.goto('http://localhost:8081/productos');await p.waitForLoadState('networkidle');
   await p.getByRole('heading',{name:'Productos',exact:true}).waitFor();assert(!await p.getByRole('alert').count());
   const h=await fetch('http://localhost:8080/actuator/health'),ha=await api('/actuator/health');const hp=await h.json();
   assert.equal(h.status,503);assert.equal(hp.status,'DOWN');
   await p.screenshot({path:path.join(out,'C-06-redis-caido.png'),fullPage:true});
   matrix.push({id:'C-06',operacion:'Redis detenido y productos en SPA',esperado:'Funciona; health DOWN',obtenido:'Lista visible; health '+h.status+' '+hp.status,correcto:true});save('C-matriz.json',matrix);
   save('D-monitoreo-down.json',{anonimo:hp,http:h.status,administrador:ha.data});
  }finally{docker('start','deploy-redis-1');}
 }finally{await ctx.close();}
 console.log('Venta real en SPA, stock, requestId y Redis caído verificados.');
}
(async()=>{const browser=await chromium.launch(browserOptions);try{
 if(process.argv.includes('--venta'))await runVenta(browser);else await runComparacion(browser);
}finally{await browser.close();}})().catch(e=>{console.error(e.message);process.exitCode=1});
