const fs=require('node:fs');
const path=require('node:path');
const {execFileSync}=require('node:child_process');
const root=process.env.PHARMA_WORKSPACE||path.resolve(__dirname,'../../..');
const out=path.join(root,'autonoma-s13/evidencias');
const privateDir=path.join(root,'PharmaBackend/.local');
const credentials=JSON.parse(fs.readFileSync(path.join(privateDir,'autonoma-credenciales.json'),'utf8').replace(/^\uFEFF/,''));
fs.mkdirSync(out,{recursive:true});
function save(name,data){fs.writeFileSync(path.join(out,name),JSON.stringify(data,null,2));}
async function login(role='admin'){
 const r=await fetch('http://localhost:8080/api/v1/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(credentials[role])});
 if(!r.ok)throw Error(`Login de pruebas: HTTP ${r.status}`);
 return r.json();
}
function makeApi(token){return async function api(url,method='GET',body){
 const start=performance.now();
 const r=await fetch(url.startsWith('http')?url:'http://localhost:8080'+(url.startsWith('/actuator')?url:'/api/v1/'+url),{method,headers:{Authorization:`Bearer ${token}`,...(body?{'Content-Type':'application/json'}:{})},...(body?{body:JSON.stringify(body)}:{})});
 const text=await r.text();let data;try{data=JSON.parse(text)}catch{data=text}
 return {status:r.status,durationMs:performance.now()-start,requestId:r.headers.get('x-request-id'),cacheControl:r.headers.get('cache-control'),data};
};}
function redis(...args){return execFileSync('docker',['exec','deploy-redis-1','redis-cli','--raw',...args],{encoding:'utf8'}).trim();}
function docker(...args){return execFileSync('docker',args,{encoding:'utf8'}).trim();}
async function pages(api,recurso){let rows=[];for(let p=0;p<100;p++){const r=await api(`${recurso}?pagina=${p}&tamanio=100&ordenarPor=id&direccion=asc`);if(r.status!==200)throw Error('No se pudo listar '+recurso);if(Array.isArray(r.data))return r.data;rows.push(...r.data.content);if(r.data.last)return rows;}throw Error('Límite de paginación alcanzado');}
function must(r,expected){if(r.status!==expected)throw Error(`Se esperaba HTTP ${expected}; obtenido ${r.status}: ${r.data?.message||''}`);return r.data;}
module.exports={fs,path,root,out,privateDir,credentials,save,login,makeApi,redis,docker,pages,must};
