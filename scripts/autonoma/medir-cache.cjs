const {save,login,makeApi,redis}=require('./mediciones-comunes.cjs');
(async()=>{
 const s=await login();const api=makeApi(s.token);
 redis('DEL','categorias::todas','productos::0-10-nombre-asc');
 const results={procedimiento:'Backend recién iniciado, claves concretas eliminadas, 20 GET secuenciales por catálogo, cliente Node fetch, mismo Oracle y proceso.',caches:[]};
 for(const [name,url]of [['categorias','categorias'],['productos','productos?pagina=0']]){
  const rows=[];
  for(let n=1;n<=20;n++){const r=await api(url);if(r.status!==200)throw Error(`${name}: HTTP ${r.status}`);rows.push({n,status:r.status,durationMs:r.durationMs,requestId:r.requestId});}
  const hit=(await api(`/actuator/metrics/cache.gets?tag=name:${name}&tag=result:hit`)).data;
  const miss=(await api(`/actuator/metrics/cache.gets?tag=name:${name}&tag=result:miss`)).data;
  const hits=hit.measurements[0].value,misses=miss.measurements[0].value;
  results.caches.push({nombre:name,peticiones:rows,hits,misses,tasaAciertos:hits/(hits+misses),primerFalloMs:rows[0].durationMs,mediaAciertosMs:rows.slice(1).reduce((a,b)=>a+b.durationMs,0)/19,ttl: Number(redis('TTL',name==='categorias'?'categorias::todas':'productos::0-10-nombre-asc'))});
  console.log(name+': '+hits+' aciertos, '+misses+' fallos');
 }
 save('C-cache-20.json',results);
})().catch(e=>{console.error(e.message);process.exitCode=1});
