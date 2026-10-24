package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.MedicamentoPort;
import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.domain.model.Medicamento;
import br.com.ptcc.sus.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarMedicamentoTest {
    @Mock UsuarioPort usuarios;
    @Mock MedicamentoPort medicamentos;
    @InjectMocks CadastrarMedicamento caso;

    @Test void cadastraMedicamentoAssociadoAoUsuarioAutenticado() {
        when(usuarios.buscarPorEmail("ana@exemplo.com"))
                .thenReturn(Optional.of(new Usuario(4L, "Ana", "ana@exemplo.com", "hash", null)));
        when(medicamentos.salvar(any(Medicamento.class))).thenAnswer(inv -> inv.getArgument(0));
        Medicamento resultado = caso.executar("ana@exemplo.com", "Medicamento A", "500 mg",
                "a cada 8 horas", LocalDate.of(2025, 4, 10), null);
        assertEquals(4L, resultado.usuarioId());
        assertEquals("Medicamento A", resultado.nome());
        assertNull(resultado.dataFim());
    }

    @Test void rejeitaPeriodoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Medicamento(null, 4L, "Medicamento A",
                "500 mg", "1x ao dia", LocalDate.of(2025, 4, 10), LocalDate.of(2025, 4, 9)));
    }
}
