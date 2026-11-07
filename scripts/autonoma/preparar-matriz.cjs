const {fs,path,privateDir,save,login,makeApi,pages,must}=require('./mediciones-comunes.cjs');
(async()=>{
 const s=await login(),api=makeApi(s.token),suffix=String(Date.now()).slice(-9);
 const fixtures={nombre:'AUTONOMA S13 '+suffix};const tests=[];
 function persist(){fs.writeFileSync(path.join(privateDir,'autonoma-fixtures.json'),JSON.stringify(fixtures,null,2));save('C-matriz.json',tests);}
 try{
  fixtures.categoria=must(await api('categorias','POST',{nombre:fixtures.nombre,descripcion:'Registro temporal para matriz autónoma',activo:true}),201);persist();
  const list=must(await api('categorias'),200);
  tests.push({id:'C-01',operacion:'Crear categoría',esperado:'Aparece inmediatamente',correcto:list.some(x=>x.id===fixtures.categoria.id)});persist();
  fixtures.producto=must(await api('productos','POST',{nombre:fixtures.nombre,precio:2.5,stock:7,categoriaId:fixtures.categoria.id}),201);persist();
  await api(`productos/${fixtures.producto.id}`);await pages(api,'productos');
  const nuevo=fixtures.nombre+' EDITADA';must(await api('categorias/'+fixtures.categoria.id,'PUT',{nombre:nuevo,descripcion:'Renombrada para prueba',activo:true}),200);
  const renamed=(await pages(api,'productos')).find(p=>p.id===fixtures.producto.id);
  tests.push({id:'C-02',operacion:'Renombrar categoría',esperado:nuevo,obtenido:renamed?.categoria?.nombre,correcto:renamed?.categoria?.nombre===nuevo});persist();
  must(await api('productos/'+fixtures.producto.id,'PUT',{nombre:fixtures.nombre,precio:3.75,stock:7,categoriaId:fixtures.categoria.id}),200);
  const price=must(await api('productos/'+fixtures.producto.id),200),priceList=(await pages(api,'productos')).find(x=>x.id===fixtures.producto.id);
  tests.push({id:'C-03',operacion:'Editar precio',esperado:3.75,obtenidoId:price.precio,obtenidoLista:priceList?.precio,correcto:Number(price.precio)===3.75&&Number(priceList?.precio)===3.75});persist();
  const empty=must(await api('categorias','POST',{nombre:fixtures.nombre+' VACIA',descripcion:'Temporal',activo:true}),201);await api('categorias');must(await api('categorias/'+empty.id,'DELETE'),204);
  const afterDelete=must(await api('categorias'),200);
  tests.push({id:'C-05',operacion:'Eliminar categoría sin productos',esperado:'No aparece',correcto:!afterDelete.some(x=>x.id===empty.id)});persist();
  fixtures.cliente=must(await api('clientes','POST',{dni:String(80000000+Number(suffix)%19000000),nombres:'Prueba autónoma',apellidos:'Cliente temporal',email:`autonoma-${suffix}@example.invalid`,telefono:'900000000',direccion:'Datos ficticios para laboratorio',estado:true}),201);persist();
  console.log('Matriz C-01, C-02, C-03, C-05 ejecutada; registros temporales preparados para venta desde SPA.');
 }catch(e){persist();throw e}
})().catch(e=>{console.error(e.message);process.exitCode=1});
