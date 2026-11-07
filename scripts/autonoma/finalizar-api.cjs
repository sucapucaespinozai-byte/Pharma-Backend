const {fs,path,privateDir,save,login,makeApi,redis,must}=require('./mediciones-comunes.cjs');
(async()=>{
 const s=await login(),api=makeApi(s.token);
 redis('DEL','categorias::todas');must(await api('categorias'),200);
 const ttl=Number(redis('TTL','categorias::todas'));if(ttl<590||ttl>600)throw Error('No se restauró el TTL de diez minutos');
 save('C-ttl-restaurado.json',{ttlSegundos:ttl,configuracion:'10m',correcto:true});
 const f=JSON.parse(fs.readFileSync(path.join(privateDir,'autonoma-fixtures.json'),'utf8'));
 for(const [key,recurso]of [['venta','ventas'],['producto','productos'],['categoria','categorias'],['cliente','clientes']]){
  if(f[key]){const r=await api(recurso+'/'+f[key].id,'DELETE');if(![204,404].includes(r.status))throw Error('No se pudo limpiar '+recurso);}
 }
 save('C-limpieza.json',{correcto:true,descripcion:'Solo se eliminaron los cuatro registros temporales creados por los scripts de la actividad; se conservaron los datos previos y DEMO.'});
 const log=fs.readFileSync('PharmaBackend/logs/pharma-backend.log','utf8');
 const cleanIds=log.replace(/\b[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\b/gi,'<request-id>');
 const local=fs.readFileSync('PharmaBackend/src/main/resources/application-local.properties','utf8');
 const known=local.split(/\r?\n/).filter(x=>/^(spring\.datasource\.password|app\.jwt\.secret)=/.test(x)).map(x=>x.slice(x.indexOf('=')+1));
 known.push(fs.readFileSync(path.join(privateDir,'jwt-prod.txt'),'utf8').trim());
 const tests={palabrasPasswordBearerSecret:(log.match(/password|Bearer |secret/gi)||[]).length,patronJwtCompleto:(log.match(/eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/g)||[]).length,dniOchoDigitosFueraDeUuid:(cleanIds.match(/(?<!\d)\d{8}(?!\d)/g)||[]).length,valoresPrivadosConocidos:known.filter(x=>x&&log.includes(x)).length};
 save('D-auditoria.json',{archivo:'logs/pharma-backend.log',pruebas:tests,correcto:Object.values(tests).every(x=>x===0),nota:'Se excluyen los UUID al buscar patrones de DNI; no se copian valores privados al informe.'});
 console.log('TTL final '+ttl+' s; datos temporales limpiados; auditoría: '+JSON.stringify(tests));
})().catch(e=>{console.error(e.message);process.exitCode=1});
