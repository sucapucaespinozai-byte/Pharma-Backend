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

    public VentaServicelmpl(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
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
                detalles
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
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> buscar(Long clienteId, EstadoVenta estado, LocalDate desde, LocalDate hasta, String ordenarPor, String direccion) {
        long inicio = System.currentTimeMillis();

        log.info("Inicio buscar ventas | clienteId={} | estado={} | "
                        + "desde={} | hasta={} | ordenarPor={} | direccion={}",
                clienteId, estado, desde, hasta, ordenarPor, direccion);

        if (desde != null
                && hasta != null
                && desde.isAfter(hasta)) {

            throw new ReglaNegocioException(
                    "El rango de fechas es inválido: 'desde' ("
                            + desde
                            + ") es posterior a 'hasta' ("
                            + hasta + ")");
        }

        Sort sort = construirSort(ordenarPor, direccion);

        LocalDateTime desdeHora = (desde == null)
                ? null
                : desde.atStartOfDay();

        LocalDateTime hastaHora = (hasta == null)
                ? null
                : hasta.atTime(LocalTime.MAX);

        List<VentaResponseDTO> resultado =
                ventaRepository
                        .buscar(clienteId, estado, desdeHora, hastaHora, sort)
                        .stream()
                        .map(this::convertirResponse)
                        .toList();

        log.info("Fin buscar ventas | clienteId={} | estado={} | "
                        + "desde={} | hasta={} | orden={} {} | "
                        + "filas={} | duracionMs={}",
                clienteId, estado, desde, hasta, ordenarPor, direccion,
                resultado.size(),
                System.currentTimeMillis() - inicio);

        return resultado;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public VentaResponseDTO crear(VentaRequestDTO request) {
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

        return convertirResponse(guardada);
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