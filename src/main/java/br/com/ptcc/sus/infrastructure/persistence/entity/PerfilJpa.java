package br.com.ptcc.sus.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "perfil_saude", uniqueConstraints = @UniqueConstraint(name = "uq_perfil_usuario", columnNames = "id_usuario"))
public class PerfilJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    public Long id;
    @Column(name = "id_usuario", nullable = false, unique = true)
    public Long usuarioId;
    public Double peso;
    public Double altura;
    @Column(name = "tipo_sanguineo", length = 5)
    public String tipoSanguineo;
    @Column(columnDefinition = "TEXT")
    public String alergias;
    @Column(name = "observacoes_medicas", columnDefinition = "TEXT")
    public String observacoesMedicas;

    protected PerfilJpa() {
    }

    public PerfilJpa(Long uid, Double p, Double a, String t, String al, String o) {
        usuarioId = uid;
        peso = p;
        altura = a;
        tipoSanguineo = t;
        alergias = al;
        observacoesMedicas = o;
    }
}