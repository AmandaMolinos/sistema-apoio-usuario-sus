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
@RequestMapping("/api/medicamentos")
public class MedicamentoController {
    private final CadastrarMedicamento cadastrar;
    private final ListarMedicamentos listar;
    private final ConsultarMedicamento consultar;
    private final AtualizarMedicamento atualizar;
    private final ExcluirMedicamento excluir;

    public MedicamentoController(CadastrarMedicamento cadastrar, ListarMedicamentos listar,
                                 ConsultarMedicamento consultar, AtualizarMedicamento atualizar,
                                 ExcluirMedicamento excluir) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.consultar = consultar;
        this.atualizar = atualizar;
        this.excluir = excluir;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.Medicamento cadastrar(Authentication auth, @Valid @RequestBody Requests.Medicamento request) {
        return Responses.Medicamento.of(cadastrar.executar(auth.getName(), request.nome(), request.dosagem(),
                request.frequencia(), request.dataInicio(), request.dataFim()));
    }

    @GetMapping
    public List<Responses.Medicamento> listar(Authentication auth) {
        return listar.executar(auth.getName()).stream().map(Responses.Medicamento::of).toList();
    }

    @GetMapping("/{id}")
    public Responses.Medicamento consultar(Authentication auth, @PathVariable Long id) {
        return Responses.Medicamento.of(consultar.executar(auth.getName(), id));
    }

    @PutMapping("/{id}")
    public Responses.Medicamento atualizar(Authentication auth, @PathVariable Long id,
                                           @Valid @RequestBody Requests.Medicamento request) {
        return Responses.Medicamento.of(atualizar.executar(auth.getName(), id, request.nome(), request.dosagem(),
                request.frequencia(), request.dataInicio(), request.dataFim()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(Authentication auth, @PathVariable Long id) {
        excluir.executar(auth.getName(), id);
    }
}
