package br.com.ptcc.sus.application.usecase;
import br.com.ptcc.sus.application.port.*;import br.com.ptcc.sus.domain.model.*;import org.junit.jupiter.api.*;import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.*;import org.mockito.junit.jupiter.MockitoExtension;import java.time.LocalDate;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class CadastroUsuarioTest {
 @Mock UsuarioPort usuarios;@Mock PerfilPort perfis;@Mock SenhaPort senhas;@InjectMocks CadastroUsuario caso;
 @Test void cadastraUsuarioEPerfil(){when(usuarios.emailExiste("ana@exemplo.com")).thenReturn(false);when(senhas.hash("senhaSegura123")).thenReturn("hash");when(usuarios.salvar(any())).thenReturn(new Usuario(7L,"Ana","ana@exemplo.com","hash",LocalDate.of(1990,1,1)));
 var u=caso.executar("Ana","ana@exemplo.com","senhaSegura123",LocalDate.of(1990,1,1));assertEquals(7L,u.id());verify(perfis).salvar(argThat(p->p.usuarioId().equals(7L)));verify(senhas).hash("senhaSegura123");}
 @Test void rejeitaEmailDuplicado(){when(usuarios.emailExiste("ana@exemplo.com")).thenReturn(true);assertThrows(IllegalArgumentException.class,()->caso.executar("Ana","ana@exemplo.com","senhaSegura123",null));verify(usuarios,never()).salvar(any());}
}