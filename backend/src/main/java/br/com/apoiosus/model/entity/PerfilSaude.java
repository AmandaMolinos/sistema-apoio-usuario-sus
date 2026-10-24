package br.com.apoiosus.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "perfil_saude")
public class PerfilSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    private BigDecimal peso;

    private BigDecimal altura;

    @Column(name = "tipo_sanguineo")
    private String tipoSanguineo;

    private String alergias;

    @Column(name = "observacoes_medicas")
    private String observacoesMedicas;

    protected PerfilSaude() {
    }

    public PerfilSaude(Usuario usuario) {
        this.usuario = usuario;
    }

    public Integer getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public BigDecimal getAltura() {
        return altura;
    }

    public String getTipoSanguineo() {
        return tipoSanguineo;
    }

    public String getAlergias() {
        return alergias;
    }

    public String getObservacoesMedicas() {
        return observacoesMedicas;
    }
}

