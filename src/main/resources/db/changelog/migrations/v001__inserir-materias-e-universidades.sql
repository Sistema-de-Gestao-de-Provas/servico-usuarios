--liquibase formatted sql

--changeset Diego Casemiro:v001-inserir-materias

INSERT INTO ${schema}.materia (nome, criado_em) VALUES
('Língua Portuguesa', CURRENT_TIMESTAMP),
('Literatura', CURRENT_TIMESTAMP),
('Redação', CURRENT_TIMESTAMP),
('Língua Inglesa', CURRENT_TIMESTAMP),
('Língua Espanhola', CURRENT_TIMESTAMP),
('Artes', CURRENT_TIMESTAMP),
('Educação Física', CURRENT_TIMESTAMP),
('Matemática', CURRENT_TIMESTAMP),
('Física', CURRENT_TIMESTAMP),
('Química', CURRENT_TIMESTAMP),
('Biologia', CURRENT_TIMESTAMP),
('História', CURRENT_TIMESTAMP),
('Geografia', CURRENT_TIMESTAMP),
('Filosofia', CURRENT_TIMESTAMP),
('Sociologia', CURRENT_TIMESTAMP);

--rollback DELETE FROM ${schema}.materia WHERE nome IN ('Língua Portuguesa', 'Literatura', 'Redação', 'Língua Inglesa', 'Língua Espanhola', 'Artes', 'Educação Física', 'Matemática', 'Física', 'Química', 'Biologia', 'História', 'Geografia', 'Filosofia', 'Sociologia');

--changeset Diego Casemiro:v001-inserir-universidades

INSERT INTO ${schema}.universidade (nome, cidade, estado, criado_em) VALUES
-- RJ
('Universidade Federal do Rio de Janeiro', 'Rio de Janeiro', 'RJ', CURRENT_TIMESTAMP),
('Universidade Federal Fluminense', 'Niterói', 'RJ', CURRENT_TIMESTAMP),
('Universidade Federal Rural do Rio de Janeiro', 'Seropédica', 'RJ', CURRENT_TIMESTAMP),
('Universidade Federal do Estado do Rio de Janeiro', 'Rio de Janeiro', 'RJ', CURRENT_TIMESTAMP),
('Centro Federal de Educação Tecnológica Celso Suckow da Fonseca', 'Rio de Janeiro', 'RJ', CURRENT_TIMESTAMP),
('Instituto Federal do Rio de Janeiro', 'Rio de Janeiro', 'RJ', CURRENT_TIMESTAMP),
('Instituto Federal Fluminense', 'Campos dos Goytacazes', 'RJ', CURRENT_TIMESTAMP),
('Escola Nacional de Ciências Estatísticas', 'Rio de Janeiro', 'RJ', CURRENT_TIMESTAMP),
('Universidade do Estado do Rio de Janeiro', 'Rio de Janeiro', 'RJ', CURRENT_TIMESTAMP),
('Universidade Estadual do Norte Fluminense Darcy Ribeiro', 'Campos dos Goytacazes', 'RJ', CURRENT_TIMESTAMP),
('Faculdade de Educação Tecnológica do Estado do Rio de Janeiro - Três Rios', 'Três Rios', 'RJ', CURRENT_TIMESTAMP),
-- MG
('Universidade Federal de Minas Gerais', 'Belo Horizonte', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal de Juiz de Fora', 'Juiz de Fora', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal de Viçosa', 'Viçosa', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal de Uberlândia', 'Uberlândia', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal de Lavras', 'Lavras', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal de Ouro Preto', 'Ouro Preto', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal de São João del-Rei', 'São João del-Rei', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal de Itajubá', 'Itajubá', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal do Triângulo Mineiro', 'Uberaba', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal dos Vales do Jequitinhonha e Mucuri', 'Diamantina', 'MG', CURRENT_TIMESTAMP),
('Universidade Federal de Alfenas', 'Alfenas', 'MG', CURRENT_TIMESTAMP),
('Centro Federal de Educação Tecnológica de Minas Gerais', 'Belo Horizonte', 'MG', CURRENT_TIMESTAMP),
('Instituto Federal de Minas Gerais', 'Belo Horizonte', 'MG', CURRENT_TIMESTAMP),
('Instituto Federal do Norte de Minas Gerais', 'Montes Claros', 'MG', CURRENT_TIMESTAMP),
('Instituto Federal do Sul de Minas Gerais', 'Pouso Alegre', 'MG', CURRENT_TIMESTAMP),
('Instituto Federal do Sudeste de Minas Gerais', 'Juiz de Fora', 'MG', CURRENT_TIMESTAMP),
('Instituto Federal do Triângulo Mineiro', 'Uberaba', 'MG', CURRENT_TIMESTAMP),
('Universidade do Estado de Minas Gerais', 'Belo Horizonte', 'MG', CURRENT_TIMESTAMP),
-- SP
('Universidade de São Paulo', 'São Paulo', 'SP', CURRENT_TIMESTAMP),
('Universidade Federal de São Paulo', 'São Paulo', 'SP', CURRENT_TIMESTAMP),
('Universidade Federal de São Carlos', 'São Carlos', 'SP', CURRENT_TIMESTAMP),
('Universidade Federal do ABC', 'Santo André', 'SP', CURRENT_TIMESTAMP),
('Instituto Federal de São Paulo', 'São Paulo', 'SP', CURRENT_TIMESTAMP);

--rollback DELETE FROM ${schema}.universidade WHERE estado IN ('RJ', 'MG', 'SP');
