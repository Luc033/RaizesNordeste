-- 1. Criação do Usuário ADMIN
-- Senha de acesso: 12345678
INSERT INTO usuario (id, nome, email, senha_hash, role, ativo)
VALUES ('224dae12-6cd3-4d94-9621-241c6e5e0e59',
        'Administrador',
        'admin@raizesdonordeste.com',
        '$2a$10$8dXJPpsS4s2g4uaMStR41.GqeHc4aYUPb6nad7npO.VAgVlYcEg3O',
        'ROLE_ADMIN',
        TRUE);

-- 2. Criação da Unidade FÍSICA
INSERT INTO unidade (id, nome, endereco, tipo_operacao, ativa, horario_abertura, horario_fechamento)
VALUES ('bf6d0317-2dd8-4d5e-9a79-171bb2330065',
        'Raízes do Nordeste - Centro',
        'Av. Central, 1000 - Curitiba/PR',
        'COZINHA_COMPLETA',
        TRUE,
        '11:00:00',
        '23:00:00');

-- 3. Criação dos PRODUTOS
-- Produto 1 (Para Teste de Sucesso)
INSERT INTO produto (id, nome, descricao, preco_base, categoria, sazonal, ativo)
VALUES ('86c16d3c-cdf4-447a-aa14-bf201bca12fd',
        'Falafel Artesanal (Porção)',
        'Deliciosos bolinhos de grão-de-bico com especiarias árabes.',
        25.50,
        'ENTRADAS',
        FALSE,
        TRUE);

-- Produto 2 (Para Teste de Falha por Estoque)
INSERT INTO produto (id, nome, descricao, preco_base, categoria, sazonal, ativo)
VALUES ('d6bc413c-efb8-4786-9247-94259ae38cc1',
        'Kibe Assado Especial',
        'Kibe tradicional assado no forno a lenha, recheado com coalhada.',
        45.00,
        'PRATOS PRINCIPAIS',
        FALSE,
        TRUE);

-- 4. Criação do ESTOQUE vinculando Produtos à Unidade
INSERT INTO estoque (unidade_id, produto_id, quantidade_atual, quantidade_minima)
VALUES
    -- Estoque suficiente
    ('bf6d0317-2dd8-4d5e-9a79-171bb2330065', '86c16d3c-cdf4-447a-aa14-bf201bca12fd', 100, 15),

    -- Estoque Quase Zerado
    ('bf6d0317-2dd8-4d5e-9a79-171bb2330065', 'd6bc413c-efb8-4786-9247-94259ae38cc1', 2, 10);