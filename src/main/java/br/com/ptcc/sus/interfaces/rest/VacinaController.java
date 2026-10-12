package br.com.ptcc.sus.interfaces.rest;

import br.com.ptcc.sus.application.usecase.*;
import br.com.ptcc.sus.interfaces.rest.dto.Requests;
import br.com.ptcc.sus.interfaces.rest.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vacinas")
public class VacinaController {
    private final CadastrarVacina cadastrar;
    private final ListarVacinas listar;
    private final ConsultarVacina consultar;
    private final AtualizarVacina atualizar;
    private final ExcluirVacina excluir;

    public VacinaController(CadastrarVacina cadastrar, ListarVacinas listar, ConsultarVacina consultar,
                            AtualizarVacina atualizar, ExcluirVacina excluir) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.consultar = consultar;
        this.atualizar = atualizar;
        this.excluir = excluir;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.Vacina cadastrar(Authentication auth, @Valid @RequestBody Requests.Vacina request) {
        return Responses.Vacina.of(cadastrar.executar(auth.getName(), request.nome(), request.dataAplicacao(),
                request.lote(), request.localAplicacao()));
    }

    @GetMapping
    public List<Responses.Vacina> listar(Authentication auth) {
        return listar.executar(auth.getName()).stream().map(Responses.Vacina::of).toList();
    }

    @GetMapping("/{id}")
    public Responses.Vacina consultar(Authentication auth, @PathVariable Long id) {
        return Responses.Vacina.of(consultar.executar(auth.getName(), id));
    }

    @PutMapping("/{id}")
    public Responses.Vacina atualizar(Authentication auth, @PathVariable Long id,
                                      @Valid @RequestBody Requests.Vacina request) {
        return Responses.Vacina.of(atualizar.executar(auth.getName(), id, request.nome(),
                request.dataAplicacao(), request.lote(), request.localAplicacao()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(Authentication auth, @PathVariable Long id) {
        excluir.executar(auth.getName(), id);
    }
}
