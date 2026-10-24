package br.com.ptcc.sus.infrastructure.config;

import br.com.ptcc.sus.application.port.*;
import br.com.ptcc.sus.application.usecase.*;
import org.springframework.context.annotation.*;

@Configuration
public class UseCaseConfig {
    @Bean
    CadastroUsuario cadastroUsuario(UsuarioPort u, PerfilPort p, SenhaPort s) {
        return new CadastroUsuario(u, p, s);
    }

    @Bean
    LoginUsuario loginUsuario(UsuarioPort u, SenhaPort s, TokenPort t) {
        return new LoginUsuario(u, s, t);
    }

    @Bean
    ConsultarUsuario consultarUsuario(UsuarioPort u) {
        return new ConsultarUsuario(u);
    }

    @Bean
    ConsultarPerfil consultarPerfil(UsuarioPort u, PerfilPort p) {
        return new ConsultarPerfil(u, p);
    }

    @Bean
    AtualizarPerfil atualizarPerfil(UsuarioPort u, PerfilPort p) {
        return new AtualizarPerfil(u, p);
    }

    @Bean
    CadastrarVacina cadastrarVacina(UsuarioPort u, VacinaPort v) {
        return new CadastrarVacina(u, v);
    }

    @Bean
    ListarVacinas listarVacinas(UsuarioPort u, VacinaPort v) {
        return new ListarVacinas(u, v);
    }

    @Bean
    ConsultarVacina consultarVacina(UsuarioPort u, VacinaPort v) {
        return new ConsultarVacina(u, v);
    }

    @Bean
    AtualizarVacina atualizarVacina(UsuarioPort u, VacinaPort v) {
        return new AtualizarVacina(u, v);
    }

    @Bean
    ExcluirVacina excluirVacina(UsuarioPort u, VacinaPort v) {
        return new ExcluirVacina(u, v);
    }

    @Bean
    CadastrarMedicamento cadastrarMedicamento(UsuarioPort u, MedicamentoPort m) {
        return new CadastrarMedicamento(u, m);
    }

    @Bean
    ListarMedicamentos listarMedicamentos(UsuarioPort u, MedicamentoPort m) {
        return new ListarMedicamentos(u, m);
    }

    @Bean
    ConsultarMedicamento consultarMedicamento(UsuarioPort u, MedicamentoPort m) {
        return new ConsultarMedicamento(u, m);
    }

    @Bean
    AtualizarMedicamento atualizarMedicamento(UsuarioPort u, MedicamentoPort m) {
        return new AtualizarMedicamento(u, m);
    }

    @Bean
    ExcluirMedicamento excluirMedicamento(UsuarioPort u, MedicamentoPort m) {
        return new ExcluirMedicamento(u, m);
    }
}