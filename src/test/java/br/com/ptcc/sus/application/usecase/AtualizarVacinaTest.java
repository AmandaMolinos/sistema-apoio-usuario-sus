package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.application.port.VacinaPort;
import br.com.ptcc.sus.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtualizarVacinaTest {
    @Mock UsuarioPort usuarios;
    @Mock VacinaPort vacinas;
    @InjectMocks AtualizarVacina caso;

    @Test void naoPermiteAtualizarVacinaDeOutroUsuario() {
        when(usuarios.buscarPorEmail("ana@exemplo.com"))
                .thenReturn(Optional.of(new Usuario(4L, "Ana", "ana@exemplo.com", "hash", null)));
        when(vacinas.buscarPorIdEUsuarioId(9L, 4L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> caso.executar("ana@exemplo.com", 9L,
                "Influenza", LocalDate.of(2025, 4, 10), null, null));
        verify(vacinas, never()).salvar(any());
    }
}
