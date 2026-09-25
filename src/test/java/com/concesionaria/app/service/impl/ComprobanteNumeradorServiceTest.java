package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.ComprobanteCorrelativo;
import com.concesionaria.app.repository.ComprobanteCorrelativoRepository;
import com.concesionaria.app.service.exception.BadRequestException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComprobanteNumeradorServiceTest {

    @Mock
    private ComprobanteCorrelativoRepository comprobanteCorrelativoRepository;

    @Test
    void correlativoEnCeroDevuelveUno() {
        ComprobanteNumeradorService service = new ComprobanteNumeradorService(comprobanteCorrelativoRepository);
        ComprobanteCorrelativo correlativo = correlativo(4L, 0L);
        when(comprobanteCorrelativoRepository.findByTipoComprobanteIdForUpdate(4L)).thenReturn(Optional.of(correlativo));

        Long siguiente = service.siguienteNumero(4L);

        assertThat(siguiente).isEqualTo(1L);
        assertThat(correlativo.getUltimoNumero()).isEqualTo(1L);
    }

    @Test
    void correlativoEnCuarentaYUnoDevuelveCuarentaYDos() {
        ComprobanteNumeradorService service = new ComprobanteNumeradorService(comprobanteCorrelativoRepository);
        ComprobanteCorrelativo correlativo = correlativo(7L, 41L);
        when(comprobanteCorrelativoRepository.findByTipoComprobanteIdForUpdate(7L)).thenReturn(Optional.of(correlativo));

        Long siguiente = service.siguienteNumero(7L);

        assertThat(siguiente).isEqualTo(42L);
        assertThat(correlativo.getUltimoNumero()).isEqualTo(42L);
    }

    @Test
    void aseguraExistenciaAntesDeBloquear() {
        ComprobanteNumeradorService service = new ComprobanteNumeradorService(comprobanteCorrelativoRepository);
        ComprobanteCorrelativo correlativo = correlativo(9L, 0L);
        when(comprobanteCorrelativoRepository.findByTipoComprobanteIdForUpdate(9L)).thenReturn(Optional.of(correlativo));

        service.siguienteNumero(9L);

        InOrder inOrder = inOrder(comprobanteCorrelativoRepository);
        inOrder.verify(comprobanteCorrelativoRepository).asegurarFila(9L);
        inOrder.verify(comprobanteCorrelativoRepository).findByTipoComprobanteIdForUpdate(9L);
    }

    @Test
    void actualizaExactamenteElCorrelativoEsperado() {
        ComprobanteNumeradorService service = new ComprobanteNumeradorService(comprobanteCorrelativoRepository);
        ComprobanteCorrelativo correlativo = correlativo(12L, 5L);
        when(comprobanteCorrelativoRepository.findByTipoComprobanteIdForUpdate(12L)).thenReturn(Optional.of(correlativo));

        service.siguienteNumero(12L);

        ArgumentCaptor<ComprobanteCorrelativo> captor = ArgumentCaptor.forClass(ComprobanteCorrelativo.class);
        verify(comprobanteCorrelativoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipoComprobanteId()).isEqualTo(12L);
        assertThat(captor.getValue().getUltimoNumero()).isEqualTo(6L);
    }

    @Test
    void tipoIdNullFallaSinInicializar() {
        ComprobanteNumeradorService service = new ComprobanteNumeradorService(comprobanteCorrelativoRepository);

        assertThrows(BadRequestException.class, () -> service.siguienteNumero(null));

        verify(comprobanteCorrelativoRepository, never()).asegurarFila(null);
    }

    private ComprobanteCorrelativo correlativo(Long tipoComprobanteId, Long ultimoNumero) {
        ComprobanteCorrelativo correlativo = new ComprobanteCorrelativo();
        correlativo.setTipoComprobanteId(tipoComprobanteId);
        correlativo.setUltimoNumero(ultimoNumero);
        return correlativo;
    }
}
