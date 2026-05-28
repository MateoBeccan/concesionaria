package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.EntregaChecklistItem;
import com.concesionaria.app.domain.EntregaUnidad;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoEntregaUnidad;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.EntregaChecklistItemRepository;
import com.concesionaria.app.repository.EntregaUnidadRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.VentaHistorialRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.dto.EntregaChecklistItemDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ClienteMapperImpl;
import com.concesionaria.app.service.mapper.InventarioMapperImpl;
import com.concesionaria.app.service.mapper.VehiculoMapperImpl;
import com.concesionaria.app.service.mapper.VentaMapperImpl;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EntregaUnidadServiceImplBusinessTest {

    @Mock
    private EntregaUnidadRepository entregaUnidadRepository;

    @Mock
    private EntregaChecklistItemRepository checklistItemRepository;

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private VentaHistorialRepository ventaHistorialRepository;

    private EntregaUnidadServiceImpl service;

    @BeforeEach
    void setUp() {
        EntregaUnidadValidator entregaUnidadValidator = new EntregaUnidadValidator(entregaUnidadRepository);
        EntregaChecklistManager entregaChecklistManager = new EntregaChecklistManager(checklistItemRepository);
        EntregaUnidadInventarioSync entregaUnidadInventarioSync = new EntregaUnidadInventarioSync(inventarioRepository);
        EntregaUnidadHistorialService entregaUnidadHistorialService = new EntregaUnidadHistorialService(ventaHistorialRepository);
        service =
            new EntregaUnidadServiceImpl(
                entregaUnidadRepository,
                ventaRepository,
                inventarioRepository,
                new ClienteMapperImpl(),
                new VehiculoMapperImpl(),
                new InventarioMapperImpl(),
                new VentaMapperImpl(),
                entregaUnidadValidator,
                entregaChecklistManager,
                entregaUnidadInventarioSync,
                entregaUnidadHistorialService
            );
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));
    }

    @Test
    void programarEntregaVentaValida() {
        Venta venta = ventaBase(EstadoVenta.PAGADA);
        Inventario inventario = inventarioBase();
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(entregaUnidadRepository.existsByVentaIdAndEstadoIn(1L, EnumSet.of(EstadoEntregaUnidad.PENDIENTE, EstadoEntregaUnidad.PROGRAMADA, EstadoEntregaUnidad.ENTREGADA)))
            .thenReturn(false);
        when(inventarioRepository.findByVehiculoId(10L)).thenReturn(Optional.of(inventario));
        when(entregaUnidadRepository.save(any(EntregaUnidad.class))).thenAnswer(inv -> {
            EntregaUnidad e = inv.getArgument(0);
            e.setId(5L);
            return e;
        });
        when(checklistItemRepository.findAllByEntregaUnidadIdOrderByIdAsc(5L)).thenReturn(List.of());

        var result = service.programarEntrega(1L, Instant.now().plusSeconds(3600), "ok");

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getEstado()).isEqualTo(EstadoEntregaUnidad.PROGRAMADA);
    }

    @Test
    void bloquearEntregaVentaCancelada() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaBase(EstadoVenta.CANCELADA)));
        assertThrows(BadRequestException.class, () -> service.programarEntrega(1L, Instant.now(), null));
    }

    @Test
    void noDuplicarEntregaPorVenta() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaBase(EstadoVenta.PAGADA)));
        when(entregaUnidadRepository.existsByVentaIdAndEstadoIn(1L, EnumSet.of(EstadoEntregaUnidad.PENDIENTE, EstadoEntregaUnidad.PROGRAMADA, EstadoEntregaUnidad.ENTREGADA)))
            .thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.programarEntrega(1L, Instant.now(), null));
    }

    @Test
    void bloquearConfirmacionChecklistIncompleto() {
        EntregaUnidad entrega = entregaBase(EstadoEntregaUnidad.PROGRAMADA);
        when(entregaUnidadRepository.findById(7L)).thenReturn(Optional.of(entrega));
        EntregaChecklistItem obligatorioPendiente = new EntregaChecklistItem();
        obligatorioPendiente.setId(1L);
        obligatorioPendiente.setObligatorio(true);
        obligatorioPendiente.setCompletado(false);
        when(checklistItemRepository.findAllByEntregaUnidadIdOrderByIdAsc(7L)).thenReturn(List.of(obligatorioPendiente));

        assertThrows(BadRequestException.class, () -> service.confirmarEntrega(7L, 15, "1/2", "x"));
    }

    @Test
    void confirmarEntregaChecklistCompleto() {
        EntregaUnidad entrega = entregaBase(EstadoEntregaUnidad.PROGRAMADA);
        when(entregaUnidadRepository.findById(7L)).thenReturn(Optional.of(entrega));

        EntregaChecklistItem obligatorioCompleto = new EntregaChecklistItem();
        obligatorioCompleto.setId(1L);
        obligatorioCompleto.setObligatorio(true);
        obligatorioCompleto.setCompletado(true);
        when(checklistItemRepository.findAllByEntregaUnidadIdOrderByIdAsc(7L)).thenReturn(List.of(obligatorioCompleto));
        when(inventarioRepository.findById(20L)).thenReturn(Optional.of(entrega.getInventario()));
        when(entregaUnidadRepository.save(any(EntregaUnidad.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.confirmarEntrega(7L, 42, "1/1", "ok");

        assertThat(result.getEstado()).isEqualTo(EstadoEntregaUnidad.ENTREGADA);
        verify(ventaHistorialRepository).save(any());
    }

    @Test
    void cancelarEntrega() {
        EntregaUnidad entrega = entregaBase(EstadoEntregaUnidad.PROGRAMADA);
        when(entregaUnidadRepository.findById(7L)).thenReturn(Optional.of(entrega));
        when(entregaUnidadRepository.save(any(EntregaUnidad.class))).thenAnswer(inv -> inv.getArgument(0));
        when(checklistItemRepository.findAllByEntregaUnidadIdOrderByIdAsc(7L)).thenReturn(List.of());

        var result = service.cancelarEntrega(7L, "reprogramar");

        assertThat(result.getEstado()).isEqualTo(EstadoEntregaUnidad.CANCELADA);
    }

    @Test
    void actualizaChecklist() {
        EntregaUnidad entrega = entregaBase(EstadoEntregaUnidad.PROGRAMADA);
        when(entregaUnidadRepository.findById(7L)).thenReturn(Optional.of(entrega));
        EntregaChecklistItem item = new EntregaChecklistItem();
        item.setId(1L);
        item.setCodigo("DOC");
        item.setDescripcion("Documentacion");
        item.setObligatorio(true);
        item.setCompletado(false);
        when(checklistItemRepository.findAllByEntregaUnidadIdOrderByIdAsc(7L)).thenReturn(List.of(item));
        when(checklistItemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(entregaUnidadRepository.save(any(EntregaUnidad.class))).thenAnswer(inv -> inv.getArgument(0));

        EntregaChecklistItemDTO dto = new EntregaChecklistItemDTO();
        dto.setId(1L);
        dto.setCompletado(true);
        dto.setObservaciones("ok");

        var result = service.actualizarChecklist(7L, List.of(dto));

        assertThat(result.getChecklist().get(0).getCompletado()).isTrue();
    }

    private Venta ventaBase(EstadoVenta estado) {
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setNombre("Ana");
        cliente.setApellido("Lopez");
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        User user = new User();
        user.setId(3L);
        user.setLogin("admin");
        Venta venta = new Venta();
        venta.setId(1L);
        venta.setEstado(estado);
        venta.setCliente(cliente);
        venta.setVehiculo(vehiculo);
        venta.setUser(user);
        return venta;
    }

    private Inventario inventarioBase() {
        Inventario i = new Inventario();
        i.setId(20L);
        i.setEstadoInventario(EstadoInventario.DISPONIBLE);
        return i;
    }

    private EntregaUnidad entregaBase(EstadoEntregaUnidad estado) {
        EntregaUnidad entrega = new EntregaUnidad();
        entrega.setId(7L);
        entrega.setEstado(estado);
        entrega.setVenta(ventaBase(EstadoVenta.PAGADA));
        entrega.setCliente(entrega.getVenta().getCliente());
        entrega.setVehiculo(entrega.getVenta().getVehiculo());
        entrega.setInventario(inventarioBase());
        entrega.setFechaProgramada(Instant.now());
        return entrega;
    }
}

