CREATE TABLE IF NOT EXISTS recuperacao_senha (
    id_recuperacao  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario      INT NOT NULL,
    token_hash      VARCHAR(64) NOT NULL UNIQUE,
    criado_em       DATETIME(6) NOT NULL,
    expira_em       DATETIME(6) NOT NULL,
    usado_em        DATETIME(6),
    INDEX idx_recuperacao_usuario (id_usuario),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
