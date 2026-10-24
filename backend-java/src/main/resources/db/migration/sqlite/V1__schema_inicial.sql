CREATE TABLE usuario (
    id_usuario      INTEGER PRIMARY KEY AUTOINCREMENT,
    nome            VARCHAR(120) NOT NULL,
    email           VARCHAR(160) NOT NULL UNIQUE,
    senha           VARCHAR(100) NOT NULL,
    data_nascimento DATE
);

CREATE TABLE perfil_saude (
    id_perfil           INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario          INTEGER NOT NULL UNIQUE,
    peso                DECIMAL(5,2),
    altura              DECIMAL(3,2),
    tipo_sanguineo      VARCHAR(3),
    alergias            TEXT,
    observacoes_medicas TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE
);

CREATE TABLE vacina (
    id_vacina        INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario       INTEGER NOT NULL,
    nome_vacina      VARCHAR(120) NOT NULL,
    data_aplicacao   DATE NOT NULL,
    lote             VARCHAR(60),
    local_aplicacao  VARCHAR(160),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE
);

CREATE TABLE medicamento (
    id_medicamento    INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario        INTEGER NOT NULL,
    nome_medicamento  VARCHAR(120) NOT NULL,
    dosagem           VARCHAR(60),
    frequencia        VARCHAR(80),
    data_inicio       DATE,
    data_fim          DATE,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE
);

CREATE TABLE unidade_saude (
    id_unidade        INTEGER PRIMARY KEY AUTOINCREMENT,
    nome              VARCHAR(160) NOT NULL,
    endereco          VARCHAR(255),
    municipio         VARCHAR(100),
    telefone          VARCHAR(30),
    tipo_atendimento  VARCHAR(100),
    latitude          DECIMAL(9,6),
    longitude         DECIMAL(9,6),
    fonte             VARCHAR(255),
    atualizado_em     DATE
);

CREATE TABLE campanha_vacinacao (
    id_campanha    INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo         VARCHAR(160) NOT NULL,
    descricao      TEXT,
    data_inicio    DATE,
    data_fim       DATE,
    publico_alvo   VARCHAR(255),
    fonte          VARCHAR(255),
    atualizado_em  DATE
);

CREATE TABLE orientacao_saude (
    id_orientacao             INTEGER PRIMARY KEY AUTOINCREMENT,
    tema                      VARCHAR(160) NOT NULL,
    conteudo_preventivo       TEXT,
    procedimentos_recomendados TEXT,
    fonte                     VARCHAR(255),
    atualizado_em             DATE
);

CREATE TABLE analise_sintoma (
    id_analise           INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario           INTEGER NOT NULL,
    id_orientacao        INTEGER,
    sintomas_informados  TEXT NOT NULL,
    data_analise         TIMESTAMP NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_orientacao) REFERENCES orientacao_saude (id_orientacao) ON DELETE SET NULL
);

CREATE INDEX idx_vacina_usuario ON vacina (id_usuario);
CREATE INDEX idx_medicamento_usuario ON medicamento (id_usuario);
CREATE INDEX idx_analise_usuario ON analise_sintoma (id_usuario);
CREATE INDEX idx_unidade_municipio ON unidade_saude (municipio);
