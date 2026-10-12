-- MODELO FÍSICO DO BANCO DE DADOS
-- Projeto: Sistema de Apoio ao Usuário do SUS

CREATE DATABASE IF NOT EXISTS apoio_usuario_sus
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE apoio_usuario_sus;

-- 1. Usuário
CREATE TABLE IF NOT EXISTS usuario (
                                       id_usuario BIGINT NOT NULL AUTO_INCREMENT,
                                       nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL COMMENT 'Armazenar somente hash seguro da senha',
    data_nascimento DATE,
    PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_email UNIQUE (email)
    ) ENGINE=InnoDB;

-- 2. Perfil de saúde: cada usuário possui um perfil.
CREATE TABLE IF NOT EXISTS perfil_saude (
                                            id_perfil BIGINT NOT NULL AUTO_INCREMENT,
                                            id_usuario BIGINT NOT NULL,
                                            peso DOUBLE,
                                            altura DOUBLE,
                                            tipo_sanguineo VARCHAR(5),
    alergias TEXT,
    observacoes_medicas TEXT,
    PRIMARY KEY (id_perfil),
    CONSTRAINT uq_perfil_usuario UNIQUE (id_usuario),
    CONSTRAINT fk_perfil_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- 3. Histórico de vacinas de um usuário.
CREATE TABLE IF NOT EXISTS vacina (
                                      id_vacina BIGINT NOT NULL AUTO_INCREMENT,
                                      id_usuario BIGINT NOT NULL,
                                      nome_vacina VARCHAR(150) NOT NULL,
    data_aplicacao DATE,
    lote VARCHAR(100),
    local_aplicacao VARCHAR(255),
    PRIMARY KEY (id_vacina),
    CONSTRAINT fk_vacina_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- 4. Medicamentos gerenciados pelo usuário.
CREATE TABLE IF NOT EXISTS medicamento (
                                           id_medicamento BIGINT NOT NULL AUTO_INCREMENT,
                                           id_usuario BIGINT NOT NULL,
                                           nome_medicamento VARCHAR(150) NOT NULL,
    dosagem VARCHAR(100),
    frequencia VARCHAR(150),
    data_inicio DATE,
    data_fim DATE,
    PRIMARY KEY (id_medicamento),
    CONSTRAINT fk_medicamento_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- 5. Unidades públicas de saúde consultadas no aplicativo.
CREATE TABLE IF NOT EXISTS unidade_saude (
                                             id_unidade BIGINT NOT NULL AUTO_INCREMENT,
                                             nome VARCHAR(180) NOT NULL,
    endereco VARCHAR(255),
    telefone VARCHAR(30),
    tipo_atendimento VARCHAR(100),
    latitude DOUBLE,
    longitude DOUBLE,
    PRIMARY KEY (id_unidade)
    ) ENGINE=InnoDB;

-- 6. Campanhas de vacinação consultadas no aplicativo.
CREATE TABLE IF NOT EXISTS campanha_vacinacao (
                                                  id_campanha BIGINT NOT NULL AUTO_INCREMENT,
                                                  titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    data_inicio DATE,
    data_fim DATE,
    publico_alvo VARCHAR(255),
    PRIMARY KEY (id_campanha)
    ) ENGINE=InnoDB;

-- 7. Orientações informativas consultadas na análise de sintomas.
CREATE TABLE IF NOT EXISTS orientacao_saude (
                                                id_orientacao BIGINT NOT NULL AUTO_INCREMENT,
                                                tema VARCHAR(180) NOT NULL,
    conteudo_preventivo TEXT,
    procedimentos_recomendados TEXT,
    PRIMARY KEY (id_orientacao)
    ) ENGINE=InnoDB;

-- 8. Análises informativas de sintomas realizadas pelo usuário.
-- A lista de sintomas da classe é armazenada como JSON no MySQL.
CREATE TABLE IF NOT EXISTS analise_sintoma (
                                               id_analise BIGINT NOT NULL AUTO_INCREMENT,
                                               id_usuario BIGINT NOT NULL,
                                               id_orientacao BIGINT,
                                               sintomas_informados JSON NOT NULL,
                                               data_analise DATE NOT NULL,
                                               PRIMARY KEY (id_analise),
    CONSTRAINT fk_analise_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_analise_orientacao FOREIGN KEY (id_orientacao)
    REFERENCES orientacao_saude (id_orientacao)
    ON UPDATE CASCADE ON DELETE SET NULL
    ) ENGINE=InnoDB;

-- Índices auxiliares para consultas frequentes.
CREATE INDEX idx_vacina_usuario ON vacina (id_usuario);
CREATE INDEX idx_medicamento_usuario ON medicamento (id_usuario);
CREATE INDEX idx_analise_usuario_data ON analise_sintoma (id_usuario, data_analise);
CREATE INDEX idx_campanha_vigencia ON campanha_vacinacao (data_inicio, data_fim);
CREATE INDEX idx_unidade_tipo ON unidade_saude (tipo_atendimento);

-- Fim do script.
