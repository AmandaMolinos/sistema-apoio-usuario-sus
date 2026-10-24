package br.com.apoiosus.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sessao")
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sessao")
    private Integer id;

    @Column(name = "id_usuario", nullable = false)
    private Integer usuarioId;

    // Só o hash do token é guardado; o token em si existe apenas com o cliente.
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "revogada_em")
    private LocalDateTime revogadaEm;

    protected Sessao() {
    }

    public Sessao(Integer usuarioId, String tokenHash, LocalDateTime criadoEm, LocalDateTime expiraEm) {
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
    }

    public boolean estaValida(LocalDateTime agora) {
        return revogadaEm == null && expiraEm.isAfter(agora);
    }

    public void revogar(LocalDateTime agora) {
        if (revogadaEm == null) {
            revogadaEm = agora;
        }
    }

    public Integer getId() {
        return id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }
}
