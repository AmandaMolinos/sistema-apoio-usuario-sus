-- Mesmo esquema do SQLite, no dialeto do MySQL (InnoDB, utf8mb4).
-- "IF NOT EXISTS" porque o MySQL não desfaz CREATE TABLE: se uma execução cair no meio, a seguinte continua.
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(120) NOT NULL,
    email           VARCHAR(160) NOT NULL UNIQUE,
    senha           VARCHAR(100) NOT NULL,
    data_nascimento DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS perfil_saude (
    id_perfil           INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario          INT NOT NULL UNIQUE,
    peso                DECIMAL(5,2),
    altura              DECIMAL(3,2),
    tipo_sanguineo      VARCHAR(3),
    alergias            TEXT,
    observacoes_medicas TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vacina (
    id_vacina        INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario       INT NOT NULL,
    nome_vacina      VARCHAR(120) NOT NULL,
    data_aplicacao   DATE NOT NULL,
    lote             VARCHAR(60),
    local_aplicacao  VARCHAR(160),
    INDEX idx_vacina_usuario (id_usuario),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS medicamento (
    id_medicamento    INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario        INT NOT NULL,
    nome_medicamento  VARCHAR(120) NOT NULL,
    dosagem           VARCHAR(60),
    frequencia        VARCHAR(80),
    data_inicio       DATE,
    data_fim          DATE,
    INDEX idx_medicamento_usuario (id_usuario),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS unidade_saude (
    id_unidade        INT AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(160) NOT NULL,
    endereco          VARCHAR(255),
    municipio         VARCHAR(100),
    telefone          VARCHAR(30),
    tipo_atendimento  VARCHAR(100),
    latitude          DECIMAL(9,6),
    longitude         DECIMAL(9,6),
    fonte             VARCHAR(255),
    atualizado_em     DATE,
    INDEX idx_unidade_municipio (municipio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS campanha_vacinacao (
    id_campanha    INT AUTO_INCREMENT PRIMARY KEY,
    titulo         VARCHAR(160) NOT NULL,
    descricao      TEXT,
    data_inicio    DATE,
    data_fim       DATE,
    publico_alvo   VARCHAR(255),
    fonte          VARCHAR(255),
    atualizado_em  DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orientacao_saude (
    id_orientacao             INT AUTO_INCREMENT PRIMARY KEY,
    tema                      VARCHAR(160) NOT NULL,
    conteudo_preventivo       TEXT,
    procedimentos_recomendados TEXT,
    fonte                     VARCHAR(255),
    atualizado_em             DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analise_sintoma (
    id_analise           INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario           INT NOT NULL,
    id_orientacao        INT,
    sintomas_informados  TEXT NOT NULL,
    data_analise         DATETIME(6) NOT NULL,
    INDEX idx_analise_usuario (id_usuario),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_orientacao) REFERENCES orientacao_saude (id_orientacao) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
