package com.example.tienda.service.impl;

import com.example.tienda.dto.DetalleVentaResponseDTO;
import com.example.tienda.dto.VentaRequestDTO;
import com.example.tienda.dto.VentaResponseDTO;
import com.example.tienda.entity.Cliente;
import com.example.tienda.entity.DetalleVenta;
import com.example.tienda.entity.Producto;
import com.example.tienda.entity.Venta;
import com.example.tienda.enums.EstadoVenta;
import com.example.tienda.exception.RecursosNoEncontradoException;
import com.example.tienda.exception.ReglaNegocioException;
import com.example.tienda.repository.ClienteRepository;
import com.example.tienda.repository.ProductoRepository;
import com.example.tienda.repository.VentaRepository;
import com.example.tienda.service.service.VentaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class VentaServicelmpl implements VentaService {
    private static final Logger log = LoggerFactory.getLogger(VentaServicelmpl.class);

    private static final String ORDEN_POR_DEFECTO = "fecha";
    private static final List<String> CAMPOS_ORDENABLES = List.of("id", "fecha", "total", "estado");

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final org.springframework.context.ApplicationEventPublisher eventos;

    public VentaServicelmpl(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository, org.springframework.context.ApplicationEventPublisher eventos) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.eventos=eventos;
    }

    private VentaResponseDTO convertirResponse(Venta venta) {
        List<DetalleVentaResponseDTO> detalles = venta.getDetalles()
                .stream()
                .map(detalle ->
                        new DetalleVentaResponseDTO(
                                detalle.getProducto().getId(),
                                detalle.getProducto().getNombre(),
                                detalle.getCantidad(),
                                detalle.getPrecio(),
                                detalle.getSubtotal()
                        )
                ).toList();

        String clienteNombre = venta.getCliente().getNombres() + " " + venta.getCliente().getApellidos();

        return new VentaResponseDTO(
                venta.getId(),
                venta.getFecha(),
                venta.getCliente().getId(),
                clienteNombre,
                venta.getEstado().name(),
                venta.getTotal(),
                detalles, venta.getCreadoPor()
        );
    }

    private Sort construirSort(String ordenarPor, String direccion) {
        String campo = (ordenarPor == null || ordenarPor.isBlank())
                ? ORDEN_POR_DEFECTO
                : ordenarPor.trim();

        if (!CAMPOS_ORDENABLES.contains(campo)) {
            throw new ReglaNegocioException(
                    "El campo de ordenamiento '"
                            + campo
                            + "' no está permitido. Campos válidos: "
                            + CAMPOS_ORDENABLES);
        }

        String sentido = (direccion == null || direccion.isBlank())
                ? "desc"
                : direccion.trim();

        if (!sentido.equalsIgnoreCase("asc")
                && !sentido.equalsIgnoreCase("desc")) {
            throw new ReglaNegocioException(
                    "La dirección de ordenamiento '"
                            + sentido
                            + "' no está permitida. Valores válidos: asc, desc");
        }

        return sentido.equalsIgnoreCase("asc")
                ? Sort.by(campo).ascending()
                : Sort.by(campo).descending();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listar() {
        return ventaRepository.findAll().stream().map(this::convertirResponse).toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public VentaResponseDTO registrar(VentaRequestDTO request) {
        return crear(request);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscar(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Venta no encontrada con id: " + id));
        return convertirResponse(venta);
    }

    @Override
    @Transactional(readOnly=true)
    public List<VentaResponseDTO> buscar(Long clienteId,EstadoVenta estado,LocalDate desde,LocalDate hasta,String ordenarPor,String direccion) {
        return buscar(clienteId,estado,desde,hasta,ordenarPor,direccion,0,100).content();
    }
    @Override
    @Transactional(readOnly=true)
    public com.example.tienda.dto.PaginaResponseDTO<VentaResponseDTO> buscar(Long clienteId,EstadoVenta estado,LocalDate desde,LocalDate hasta,String ordenarPor,String direccion,int pagina,int tamanio) {
        if(desde!=null&&hasta!=null&&desde.isAfter(hasta))throw new ReglaNegocioException("El rango de fechas es inválido");
        var pageable=com.example.tienda.util.PaginacionUtil.construir(pagina,tamanio,ordenarPor,direccion,CAMPOS_ORDENABLES);
        var resultado=ventaRepository.findAll((r,q,cb)->{
            var p=new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if(clienteId!=null)p.add(cb.equal(r.get("cliente").get("id"),clienteId));
            if(estado!=null)p.add(cb.equal(r.get("estado"),estado));
            if(desde!=null)p.add(cb.greaterThanOrEqualTo(r.get("fecha"),desde.atStartOfDay()));
            if(hasta!=null)p.add(cb.lessThan(r.get("fecha"),hasta.plusDays(1).atStartOfDay()));
            return cb.and(p.toArray(jakarta.persistence.criteria.Predicate[]::new));
        },pageable);
        // El listado no carga todas las líneas. GET /ventas/{id} entrega el detalle.
        return com.example.tienda.dto.PaginaResponseDTO.de(resultado.map(v -> new VentaResponseDTO(
                v.getId(), v.getFecha(), v.getCliente().getId(),
                v.getCliente().getNombres()+" "+v.getCliente().getApellidos(),
                v.getEstado().name(), v.getTotal(), List.of(), v.getCreadoPor())));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public VentaResponseDTO crear(VentaRequestDTO request) {
        long inicioVenta = System.nanoTime();
        log.info("Inicio registrar venta | lineas={}", request.getDetalles() == null ? 0 : request.getDetalles().size());
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RecursosNoEncontradoException("Cliente no encontrado con id: " + request.getClienteId()));

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new ReglaNegocioException("No se puede registrar una venta para un cliente inactivo");
        }

        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La lista de detalles no puede estar vacía.");
        }

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.REGISTRADA);

        BigDecimal total = BigDecimal.ZERO;

        for (var item : request.getDetalles()) {
            if (item.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad del producto debe ser mayor a cero.");
            }

            Producto producto = productoRepository.findById(item.getProductoId()).orElseThrow(() ->
                    new RecursosNoEncontradoException("Producto no encontrado con id: " + item.getProductoId()));

            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new ReglaNegocioException("El producto " + producto.getNombre() + " se encuentra inactivo");
            }

            if (producto.getStock() < item.getCantidad()) {
                throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock()
                        + ", solicitado: " + item.getCantidad());
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalle.setSubtotal(subtotal);

            venta.agregarDetalle(detalle);
            total = total.add(subtotal);

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        venta.setTotal(total);

        Venta guardada = ventaRepository.save(venta);
        eventos.publishEvent(com.example.tienda.auditoria.EventoAuditoria.de("VENTA_REGISTRADA","Venta",guardada.getId(),"total="+guardada.getTotal()+" | items="+guardada.getDetalles().size()));

        VentaResponseDTO respuesta = convertirResponse(guardada);
        log.info("Fin registrar venta | ventaId={} | lineas={} | duracionMs={}", guardada.getId(),
                request.getDetalles().size(), java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioVenta));
        return respuesta;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public VentaResponseDTO actualizar(Long id, VentaRequestDTO request) {
        Venta existente = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Venta no encontrada con ID: " + id));

        Venta actualizada = ventaRepository.save(existente);
        return convertirResponse(actualizada);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public void eliminar(Long id) {
        Venta existente = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Venta no encontrada con ID: " + id));
        ventaRepository.delete(existente);
    }
}
