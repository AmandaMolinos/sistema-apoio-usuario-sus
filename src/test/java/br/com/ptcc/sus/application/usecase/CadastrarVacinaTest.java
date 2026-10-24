package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.application.port.VacinaPort;
import br.com.ptcc.sus.domain.model.Usuario;
import br.com.ptcc.sus.domain.model.Vacina;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastrarVacinaTest {
    @Mock UsuarioPort usuarios;
    @Mock VacinaPort vacinas;
    @InjectMocks CadastrarVacina caso;

    @Test void cadastraVacinaAssociadaAoUsuarioAutenticado() {
        when(usuarios.buscarPorEmail("ana@exemplo.com"))
                .thenReturn(Optional.of(new Usuario(4L, "Ana", "ana@exemplo.com", "hash", null)));
        when(vacinas.salvar(any(Vacina.class))).thenAnswer(inv -> inv.getArgument(0));
        Vacina resultado = caso.executar("ana@exemplo.com", "Influenza",
                LocalDate.of(2025, 4, 10), "LOTE123", "UBS Central");
        assertEquals(4L, resultado.usuarioId());
        assertEquals("Influenza", resultado.nome());
    }

    @Test void rejeitaUsuarioInexistente() {
        when(usuarios.buscarPorEmail("x@exemplo.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                caso.executar("x@exemplo.com", "Influenza", LocalDate.of(2025, 4, 10), null, null));
        verifyNoInteractions(vacinas);
    }
}
