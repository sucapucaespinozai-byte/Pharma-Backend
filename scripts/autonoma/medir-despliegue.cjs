const {chromium}=require('playwright');
const {fs,path,out,privateDir,credentials,save,login,makeApi}=require('./mediciones-comunes.cjs');
const {execFileSync}=require('node:child_process');
const assert=require('node:assert/strict');
const ready=path.join(privateDir,'B-ready.flag'),deployed=path.join(privateDir,'B-deployed.flag');
for(const f of [ready,deployed])if(fs.existsSync(f))fs.unlinkSync(f);
(async()=>{
 const browser=await chromium.launch({executablePath:'C:/Program Files/Google/Chrome/Application/chrome.exe',headless:true});
 try{
  const ctx=await browser.newContext({viewport:{width:1365,height:900}}),p=await ctx.newPage();
  const cdp=await ctx.newCDPSession(p);await cdp.send('Network.enable');await cdp.send('Network.setCacheDisabled',{cacheDisabled:false});
  let rows=[],extra=new Map(),cache=new Set();
  cdp.on('Network.requestServedFromCache',x=>cache.add(x.requestId));
  cdp.on('Network.responseReceivedExtraInfo',x=>extra.set(x.requestId,x.statusCode));
  cdp.on('Network.responseReceived',({requestId,response:r})=>rows.push({id:requestId,path:new URL(r.url).pathname,status:r.status,cacheControl:r.headers['Cache-Control']||r.headers['cache-control']||'',disco:!!r.fromDiskCache}));
  async function enter(){await p.goto('http://localhost:8081/login');await p.locator('#username').fill(credentials.admin.username);await p.locator('#password').fill(credentials.admin.password);await p.getByRole('button',{name:'Ingresar',exact:true}).click();await p.waitForURL('**/categorias');await p.waitForLoadState('networkidle');}
  await enter();await cdp.send('Network.clearBrowserCache');
  await p.goto('http://localhost:8081/ventas');await p.waitForLoadState('networkidle');
  rows=[];extra=new Map();cache=new Set();await p.reload();await p.waitForLoadState('networkidle');
  await p.evaluate(async()=>{await fetch('/index.html').then(r=>r.text());const s=JSON.parse(sessionStorage.getItem('pharmasoft.sesion'));await fetch('/api/v1/categorias',{headers:{Authorization:`Bearer ${s.token}`}}).then(r=>r.text());});
  const normalized=rows.map(r=>({...r,httpReal:extra.get(r.id)||r.status,origen:r.disco?'disco':cache.has(r.id)?'memoria':extra.get(r.id)===304?'304':'red'}));
  const build=fs.readFileSync('autonoma-s13/evidencias/A-build-sin-precarga.log','utf8');
  const ventaChunk=build.match(/^(\S+\.js)\s+\|\s+venta-list/m)?.[1];assert(ventaChunk);
  const pick=pattern=>[...normalized].reverse().find(r=>pattern.test(r.path));
  const table=[pick(/^\/index\.html$/),pick(/^\/main-.*\.js$/),pick(new RegExp('/'+ventaChunk.replaceAll('.','\\.')+'$')),pick(/^\/styles-.*\.css$/),pick(/^\/api\/v1\/categorias$/)];assert(table.every(Boolean));
  const s=await login();let curl=[];
  for(const resource of table){
   const args=['-sS','-I'];let input;
   if(resource.path.startsWith('/api/')){args.push('--config','-');input=`header = "Authorization: Bearer ${s.token}"\n`;}
   args.push('http://localhost:8081'+resource.path);
   curl.push({recurso:resource.path,cabeceras:execFileSync('curl.exe',args,{input,encoding:'utf8'})});
  }
  save('B-cabeceras.json',{procedimiento:'Chrome: caché habilitada, recarga de /ventas, petición index.html y categorías. curl -I sin token en argumentos ni archivos.',tabla:table,curl});
  await p.goto('http://localhost:8081/productos');await p.waitForLoadState('networkidle');
  const beforeText=await p.locator('.page-heading p').innerText();
  await p.screenshot({path:path.join(out,'B-despliegue-antes.png'),fullPage:true});
  rows=[];await p.reload();await p.waitForLoadState('networkidle');const beforeAssets=rows.filter(r=>/\.(js|css)$/.test(r.path)).map(r=>r.path);
  fs.writeFileSync(ready,'ready');console.log('B listo: navegador conserva caché; esperando despliegue nuevo.');
  const start=Date.now();while(!fs.existsSync(deployed)){if(Date.now()-start>180000)throw Error('Se agotó la espera del despliegue');await new Promise(r=>setTimeout(r,300));}
  rows=[];extra=new Map();cache=new Set();await p.reload();await p.waitForLoadState('networkidle');
  const afterText=await p.locator('.page-heading p').innerText();assert.notEqual(afterText,beforeText);
  const afterAssets=rows.filter(r=>/\.(js|css)$/.test(r.path)).map(r=>({...r,httpReal:extra.get(r.id)||r.status,origen:r.disco?'disco':cache.has(r.id)?'memoria':'red'}));
  await p.screenshot({path:path.join(out,'B-despliegue-despues.png'),fullPage:true});
  save('B-despliegue.json',{textoAntes:beforeText,textoDespues:afterText,assetsAntes:beforeAssets,assetsDespues:afterAssets,nuevos:afterAssets.filter(r=>!beforeAssets.includes(r.path)).map(r=>r.path),metodo:'Sin detener Nginx y sin borrar caché: copiar assets con hash; reemplazar index.html al final; conservar assets previos.'});
  console.log('Despliegue y políticas de caché verificados.');
 }finally{await browser.close();}
})().catch(e=>{console.error(e.message);process.exitCode=1});
