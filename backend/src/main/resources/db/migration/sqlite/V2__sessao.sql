CREATE TABLE sessao (
    id_sessao    INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario   INTEGER NOT NULL,
    token_hash   VARCHAR(64) NOT NULL UNIQUE,
    criado_em    TIMESTAMP NOT NULL,
    expira_em    TIMESTAMP NOT NULL,
    revogada_em  TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE
);

CREATE INDEX idx_sessao_usuario ON sessao (id_usuario);
