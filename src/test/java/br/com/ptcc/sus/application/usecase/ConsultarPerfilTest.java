package br.com.ptcc.sus.application.usecase;
import br.com.ptcc.sus.application.port.*;import br.com.ptcc.sus.domain.model.*;import org.junit.jupiter.api.*;import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.*;import org.mockito.junit.jupiter.MockitoExtension;import java.util.Optional;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class ConsultarPerfilTest {
 @Mock UsuarioPort usuarios;@Mock PerfilPort perfis;@InjectMocks ConsultarPerfil caso;
 @Test void consultaPerfilDoUsuario(){when(usuarios.buscarPorEmail("ana@exemplo.com")).thenReturn(Optional.of(new Usuario(3L,"Ana","ana@exemplo.com","hash",null)));var p=new PerfilSaude(9L,3L,60.0,1.7,"O+",null,null);when(perfis.buscarPorUsuarioId(3L)).thenReturn(Optional.of(p));assertEquals(60.0,caso.executar("ana@exemplo.com").peso());}
 @Test void falhaQuandoUsuarioNaoExiste(){when(usuarios.buscarPorEmail("x@x.com")).thenReturn(Optional.empty());assertThrows(IllegalArgumentException.class,()->caso.executar("x@x.com"));}
}