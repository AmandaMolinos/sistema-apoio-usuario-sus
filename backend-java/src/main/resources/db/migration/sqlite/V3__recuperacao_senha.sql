CREATE TABLE recuperacao_senha (
    id_recuperacao  INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario      INTEGER NOT NULL,
    token_hash      VARCHAR(64) NOT NULL UNIQUE,
    criado_em       TIMESTAMP NOT NULL,
    expira_em       TIMESTAMP NOT NULL,
    usado_em        TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE
);

CREATE INDEX idx_recuperacao_usuario ON recuperacao_senha (id_usuario);
