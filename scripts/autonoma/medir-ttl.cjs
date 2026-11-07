const {save,login,makeApi,redis}=require('./mediciones-comunes.cjs');
(async()=>{
 const s=await login(),api=makeApi(s.token);redis('DEL','categorias::todas');
 const first=await api('categorias');if(first.status!==200)throw Error('No se pudo cargar catálogo');
 const initial=Number(redis('TTL','categorias::todas')),inicio=new Date().toISOString();
 console.log('TTL inicial: '+initial+' s. Esperando expiración real de la clave.');
 await new Promise(r=>setTimeout(r,Math.max(initial+2,62)*1000));
 const expired=Number(redis('TTL','categorias::todas'));const after=await api('categorias');const regenerated=Number(redis('TTL','categorias::todas'));
 save('C-ttl-1min.json',{inicio,fin:new Date().toISOString(),ttlInicial:initial,ttlExpirado:expired,ttlRegenerado:regenerated,httpTrasExpirar:after.status,correcto:initial>0&&initial<=60&&expired===-2&&regenerated>0&&regenerated<=60});
 console.log('TTL expirado '+expired+'; regenerado '+regenerated+' s.');
})().catch(e=>{console.error(e.message);process.exitCode=1});
