CREATE TABLE usuario
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    nome          VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    senha_hash    VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL
        CHECK (role IN ('ROLE_CLIENTE', 'ROLE_ATENDENTE', 'ROLE_COZINHA', 'ROLE_GERENTE', 'ROLE_ADMIN')),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuario_email ON usuario (email);

CREATE TABLE unidade
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    nome               VARCHAR(100) NOT NULL,
    endereco           VARCHAR(255) NOT NULL,
    tipo_operacao      VARCHAR(30)  NOT NULL DEFAULT 'COZINHA_COMPLETA'
        CHECK (tipo_operacao IN ('COZINHA_COMPLETA', 'FORMATO_REDUZIDO')),
    ativa              BOOLEAN      NOT NULL DEFAULT TRUE,
    horario_abertura   TIME         NOT NULL,
    horario_fechamento TIME         NOT NULL
);

CREATE INDEX idx_unidade_ativa ON unidade (ativa) WHERE ativa = TRUE;


CREATE TABLE produto
(
    id         UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    nome       VARCHAR(100)   NOT NULL,
    descricao  TEXT,
    preco_base DECIMAL(10, 2) NOT NULL CHECK (preco_base >= 0),
    categoria  VARCHAR(80),
    sazonal    BOOLEAN        NOT NULL DEFAULT FALSE,
    ativo      BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_produto_ativo ON produto (ativo) WHERE ativo = TRUE;

CREATE TABLE consentimento_lgpd
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    usuario_id  UUID         NOT NULL,
    finalidade  VARCHAR(100) NOT NULL,
    aceitou     BOOLEAN      NOT NULL,
    data_aceite TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_origem   VARCHAR(45),
    CONSTRAINT fk_consentimento_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE INDEX idx_consentimento_usuario ON consentimento_lgpd (usuario_id);

CREATE TABLE estoque
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidade_id        UUID    NOT NULL,
    produto_id        UUID    NOT NULL,
    quantidade_atual  INTEGER NOT NULL DEFAULT 0
        CHECK (quantidade_atual >= 0),
    quantidade_minima INTEGER NOT NULL DEFAULT 0,
    atualizado_em     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_estoque_unidade
        FOREIGN KEY (unidade_id) REFERENCES unidade (id),
    CONSTRAINT fk_estoque_produto
        FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT uk_estoque_unidade_produto
        UNIQUE (unidade_id, produto_id)
);

CREATE INDEX idx_estoque_unidade ON estoque (unidade_id);


CREATE TABLE pedido
(
    id            UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    usuario_id    UUID           NOT NULL,
    unidade_id    UUID           NOT NULL,
    canal_pedido  VARCHAR(50)    NOT NULL
        CHECK (canal_pedido IN ('APP', 'TOTEM', 'BALCAO', 'PICKUP', 'WEB')),
    status        VARCHAR(50)    NOT NULL DEFAULT 'AGUARDANDO_PAGAMENTO'
        CHECK (status IN (
                          'AGUARDANDO_PAGAMENTO',
                          'EM_PREPARO',
                          'PRONTO',
                          'ENTREGUE',
                          'CANCELADO'
            )),
    valor_total   DECIMAL(10, 2) NOT NULL CHECK (valor_total >= 0),
    criado_em     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pedido_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_pedido_unidade
        FOREIGN KEY (unidade_id) REFERENCES unidade (id)
);

CREATE INDEX idx_pedido_usuario ON pedido (usuario_id);
CREATE INDEX idx_pedido_unidade ON pedido (unidade_id);
CREATE INDEX idx_pedido_status ON pedido (status);

CREATE INDEX idx_pedido_canal ON pedido (canal_pedido);

CREATE TABLE item_pedido
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id      UUID           NOT NULL,
    produto_id     UUID           NOT NULL,
    quantidade     INTEGER        NOT NULL CHECK (quantidade > 0),
    preco_unitario DECIMAL(10, 2) NOT NULL CHECK (preco_unitario >= 0),

    CONSTRAINT fk_item_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_item_produto
        FOREIGN KEY (produto_id) REFERENCES produto (id)
);

CREATE INDEX idx_item_pedido_pedido ON item_pedido (pedido_id);


CREATE TABLE pagamento
(
    id                UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    pedido_id         UUID        NOT NULL,
    forma_pagamento   VARCHAR(50) NOT NULL
        CHECK (forma_pagamento IN (
                                   'PIX', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'DINHEIRO', 'MOCK'
            )),
    status_pagamento  VARCHAR(50) NOT NULL DEFAULT 'PENDENTE'
        CHECK (status_pagamento IN (
                                    'PENDENTE', 'APROVADO', 'RECUSADO', 'TIMEOUT'
            )),
    gateway_pagamento VARCHAR(255),
    payload_retorno   TEXT,
    solicitado_em     TIMESTAMP,
    confirmado_em     TIMESTAMP,

    CONSTRAINT fk_pagamento_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT uk_pagamento_pedido UNIQUE (pedido_id)
);


CREATE TABLE log_status_pedido
(
    id                     UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    pedido_id              UUID        NOT NULL,
    status_anterior        VARCHAR(50) CHECK (status_anterior IN
                                              ('AGUARDANDO_PAGAMENTO',
                                               'EM_PREPARO',
                                               'PRONTO',
                                               'ENTREGUE',
                                               'CANCELADO')),
    status_novo            VARCHAR(50) NOT NULL CHECK (status_novo IN
                                                       ('AGUARDANDO_PAGAMENTO',
                                                        'EM_PREPARO',
                                                        'PRONTO',
                                                        'ENTREGUE',
                                                        'CANCELADO')),
    usuario_responsavel_id UUID,
    observacao             TEXT,
    atualizado_em          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_log_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_log_usuario
        FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario (id)
);

CREATE INDEX idx_log_pedido ON log_status_pedido (pedido_id);