package com.example.tienda.security;

import com.example.tienda.config.CorsConfig;
import com.example.tienda.config.SecurityConfig;
import com.example.tienda.controller.AuthController;
import com.example.tienda.controller.HealthController;
import com.example.tienda.controller.ProductoController;
import com.example.tienda.controller.ReporteController;
import com.example.tienda.controller.UsuarioController;
import com.example.tienda.controller.VentaController;
import com.example.tienda.dto.auth.EmpleadoResponseDTO;
import com.example.tienda.dto.auth.LoginResponseDTO;
import com.example.tienda.service.service.AuthService;
import com.example.tienda.service.service.ProductoService;
import com.example.tienda.service.service.ReporteService;
import com.example.tienda.service.service.UsuarioService;
import com.example.tienda.service.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        HealthController.class,
        ProductoController.class,
        ReporteController.class,
        UsuarioController.class,
        VentaController.class
})
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        SecurityErrorWriter.class
})
class SecurityAuthorizationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private AuthService authService;
    @MockBean
    private ProductoService productoService;
    @MockBean
    private VentaService ventaService;
    @MockBean
    private ReporteService reporteService;
    @MockBean
    private UsuarioService usuarioService;

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedCatalogRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Debe iniciar sesión para acceder a este recurso"));
    }

    @Test
    void vendorCanReadProducts() throws Exception {
        when(productoService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/productos")
                        .with(user("vendedor").authorities(() -> "CATALOGO_LEER")))
                .andExpect(status().isOk());
    }

    @Test
    void vendorCannotDeleteProducts() throws Exception {
        mockMvc.perform(delete("/api/v1/productos/1")
                        .with(user("vendedor").authorities(() -> "CATALOGO_LEER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("No tiene permisos para realizar esta operación"));
    }

    @Test
    void warehouseCanEditProducts() throws Exception {
        mockMvc.perform(put("/api/v1/productos/1")
                        .with(user("almacen").authorities(() -> "CATALOGO_ESCRIBIR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Producto","precio":5.50,"stock":10,"categoriaId":1}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void vendorCanRegisterSales() throws Exception {
        mockMvc.perform(post("/api/v1/ventas")
                        .with(user("vendedor").authorities(() -> "VENTAS_REGISTRAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"detalles":[{"productoId":1,"cantidad":1}]}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void warehouseCannotRegisterSales() throws Exception {
        mockMvc.perform(post("/api/v1/ventas")
                        .with(user("almacen").authorities(() -> "CATALOGO_ESCRIBIR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"detalles":[{"productoId":1,"cantidad":1}]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportsRequireTheReportPermission() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/ventas-por-categoria")
                        .with(user("vendedor").authorities(() -> "VENTAS_LEER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadReports() throws Exception {
        when(reporteService.ventasPorCategoria(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reportes/ventas-por-categoria")
                        .with(user("admin").authorities(() -> "REPORTES_VER")))
                .andExpect(status().isOk());
    }

    @Test
    void loginIsPublicAndValidatesItsBody() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponseDTO(
                "signed-token",
                "Bearer",
                3600,
                "admin",
                "Admin User",
                List.of("ADMIN"),
                List.of("REPORTES_VER")
        ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"Admin2026*"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" ","password":"Admin2026*"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.username").exists());
    }

    @Test
    void onlyAdminsCanListEmployees() throws Exception {
        when(usuarioService.listarEmpleados()).thenReturn(List.of(
                new EmpleadoResponseDTO(6L, "45667788", "Maria", "Huaman Torres",
                        "Cajera", "mhuaman@pharmasoft.pe", null, true, false)
        ));

        mockMvc.perform(get("/api/v1/empleados")
                        .with(user("admin").authorities(() -> "USUARIOS_ADMINISTRAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tieneUsuario").value(false));

        mockMvc.perform(get("/api/v1/empleados")
                        .with(user("vendedor").authorities(() -> "CLIENTES_LEER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanChangeTheirPassword() throws Exception {
        mockMvc.perform(put("/api/v1/auth/password")
                        .with(user("vendedor").authorities(() -> "VENTAS_LEER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"passwordActual":"Vendedor2026*","passwordNueva":"Nueva2026*"}
                                """))
                .andExpect(status().isNoContent());
    }
}
