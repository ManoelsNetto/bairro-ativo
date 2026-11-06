# Bairro Ativo: zeladoria colaborativa

> FIAP · Engenharia de Software · 2ESOA · Global Solution 2026/2 · **CitizenTech**

**Vídeo pitch:** _[COLE AQUI O LINK PÚBLICO DO VÍDEO]_

Bairro Ativo é uma plataforma em que **moradores registram problemas do espaço público** (buraco, poste apagado, entulho, calçada sem rampa, bueiro entupido), **vizinhos apoiam** as ocorrências que também os afetam e **servidores do órgão responsável atualizam o atendimento**, com histórico visível para todos. A fila de prioridade é calculada pela própria comunidade.

---

## 1. A proposta

| Critério | Resposta |
|---|---|
| **Quem é o cidadão beneficiado** | Moradores de bairros urbanos, principalmente da periferia, onde a zeladoria demora mais, e pessoas com mobilidade reduzida, que mais sofrem com calçadas quebradas e falta de iluminação. |
| **Qual problema é resolvido** | Os pedidos de zeladoria se perdem em canais lentos e sem retorno. O morador não sabe se alguém viu o problema, e a prefeitura não sabe o que é mais urgente para a comunidade. |
| **Como a solução funciona** | 1) O cidadão registra a ocorrência com endereço, bairro e categoria. 2) Os vizinhos apoiam ("isso também me afeta"). 3) O sistema calcula a prioridade. 4) O servidor do órgão responsável (ex.: Iluminação Pública para poste apagado) muda o status e deixa um comentário. 5) Todos acompanham o histórico e os relatórios por bairro e categoria. |
| **Tecnologias** | Java 21 (POO), JDBC, Oracle, API REST com Jersey (JAX-RS) sobre o servidor Grizzly e front em HTML5, CSS3 e JavaScript puro. |
| **Ecossistema** | Moradores e associações de bairro, subprefeituras e órgãos de zeladoria (iluminação, limpeza, drenagem, CET) e escolas e comércio local, que também são afetados. |
| **Como o impacto é medido** | Tempo médio de resolução, ocorrências fora do prazo por categoria, taxa de resolução, apoios por bairro e engajamento dos cidadãos, tudo na página **Relatórios**. |

### Dois perfis, duas visões

| | **Cidadão** | **Servidor público** |
|---|---|---|
| Quem é | Morador, cria a própria conta | Funcionário de um órgão (Zeladoria, Limpeza, Iluminação, Drenagem, Trânsito), cadastrado pela gestão |
| Tabela | `BA_CIDADAO` | `BA_SERVIDOR` (ligado a `BA_ORGAO`) |
| Página | **Área do cidadão** (`cidadao.html`): registrar ocorrência, minhas ocorrências, as que apoio e meu cadastro | **Área do servidor** (`servidor.html`): fila do meu órgão por prioridade e **Gestão de cadastros** (`gestao.html`) |
| Pode | Registrar, editar/excluir **as suas** (enquanto Abertas), apoiar as dos outros | Atualizar o status **das categorias do seu órgão** e manter bairros, órgãos, categorias e servidores |

Visitantes, sem entrar, consultam ocorrências e relatórios: a transparência é pública.

**Entrar (modo demonstração):** em `entrar.html` a pessoa escolhe o perfil e entra só com o e-mail, sem senha. Numa versão real, a identificação viria do gov.br. Contas de exemplo:

- Cidadã **Ana Souza**: `ana.souza@email.com`
- Servidora **Beatriz Nogueira** (Zeladoria Urbana): `beatriz.nogueira@prefeitura.sp.gov.br`
- Outras contas aparecem na própria tela Entrar, para testar o apoio entre vizinhos e o atendimento por órgãos diferentes.

Toda ação usa **o usuário conectado**: ninguém escolhe em nome de quem está agindo. O front envia o perfil e o id nos cabeçalhos `X-Perfil` e `X-Usuario`, e o backend confere as permissões em cada operação.

### Regra de prioridade

```
pontuação = 3 × apoios + dias em aberto + 10 (se passou do prazo da categoria)
Prioridade alta ≥ 35 · média ≥ 15 · baixa < 15   (só para ocorrências pendentes)
```

### Ciclo de vida da ocorrência

```
ABERTA → EM_ANALISE → EM_ANDAMENTO → RESOLVIDA
   └──────────┴────────────┴──────→ CANCELADA
```

Também é possível pular etapas para a frente (ABERTA → RESOLVIDA, por exemplo). Nunca é possível voltar, e RESOLVIDA e CANCELADA são finais.

### Conexão com os ODS da ONU

- **ODS 11: Cidades e comunidades sustentáveis.** Meta 11.2 (transporte e calçadas seguros e acessíveis), meta 11.7 (espaços públicos seguros e inclusivos) e meta 11.3 (planejamento participativo).
- **ODS 10: Redução das desigualdades.** Os relatórios por bairro mostram onde o atendimento demora mais, e as categorias de acessibilidade dão voz a pessoas com deficiência e idosos.

### Inclusiva e segura

- Interface responsiva e acessível: HTML semântico, navegação por teclado, link "pular para o conteúdo", contraste AA e mensagens de erro ligadas aos campos (`aria-describedby`).
- Leve: sem frameworks no front e com fonte do sistema, então carrega bem em celulares simples e com internet fraca.
- Proteção de dados: as listagens públicas mostram só o nome de quem registrou, apoiou ou atendeu, nunca e-mail ou telefone. Os contatos dos cidadãos só aparecem para servidores, na Gestão, e cada cidadão só vê e altera o próprio cadastro. As credenciais do banco ficam no `.env`, fora do git.

---

## 2. Como executar

### Pré-requisitos

- **Java 21** ou superior (`java -version`)
- Acesso ao **Oracle** (o da FIAP: `oracle.fiap.com.br:1521:orcl`)
- Não é preciso instalar o Maven: o projeto traz o Maven Wrapper (`mvnw`)

### Passo a passo

```bash
# 1. Crie as tabelas e a carga inicial no Oracle
#    Abra database/bairro_ativo.sql no SQL Developer e execute como script (F5).
#    O script pode ser rodado de novo: ele apaga e recria apenas as tabelas BA_.

# 2. Configure o acesso ao banco
cp .env.example .env
#    edite o .env com o seu DB_USER (rm) e DB_PASSWORD

# 3. Compile e gere o jar (na raiz do projeto)
./mvnw package            # Windows: mvnw.cmd package

# 4. Suba a aplicação (na raiz do projeto, para ele achar a pasta web/)
java -jar target/bairro-ativo.jar
```

Acesse **http://localhost:8080** e clique em **Entrar** para escolher o perfil. A API fica em http://localhost:8080/api e o status da conexão com o banco em http://localhost:8080/api/status.

> A porta pode ser alterada com a variável `PORT`. As variáveis do `.env` também podem vir do ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT`, `WEB_DIR`).

---

## 3. Estrutura do projeto

```
bairro-ativo/
├── database/bairro_ativo.sql        # DDL (PK, FK, NOT NULL, UNIQUE, CHECK) + carga + consultas de relatório
├── docs/diagramas/
│   ├── diagrama-er.png|pdf|svg      # Diagrama entidade-relacionamento
│   ├── diagrama-classes.png|pdf|svg # Diagrama de classes UML completo (todas as classes)
│   ├── diagrama-classes-dominio.*   # Recorte do pacote model, para leitura rápida
│   └── GeradorUml.java              # Gera o UML a partir do código compilado (sempre bate com o código)
├── src/main/java/br/com/fiap/bairroativo/
│   ├── Main.java                    # Sobe o Grizzly: API em /api e páginas em /
│   ├── model/      # Entidades e regras de domínio (herança, interface, enums)
│   ├── dao/        # Acesso a dados via JDBC (PreparedStatement), um DAO por tabela
│   ├── service/    # Regras de negócio e validações
│   ├── resource/   # Interface: endpoints REST (Jersey / JAX-RS)
│   ├── dto/        # Objetos de entrada/saída da API e dos relatórios
│   ├── exception/  # Exceções da aplicação e tradução dos erros do Oracle
│   └── config/     # Conexão, .env, JSON e tratamento global de erros
└── web/                             # Front-end: HTML, CSS e JS puro
    ├── index.html        Início (pública)
    ├── entrar.html       Escolha do perfil, contas de exemplo e criação de conta do cidadão
    ├── ocorrencias.html  Consulta pública com filtros
    ├── ocorrencia.html   Detalhe: apoio (cidadão), editar/excluir (autor), atendimento (servidor do órgão)
    ├── cidadao.html      Área do cidadão
    ├── servidor.html     Área do servidor: fila de atendimento do órgão
    ├── gestao.html       Gestão de cadastros (servidor)
    ├── relatorios.html   Relatórios (pública)
    ├── css/estilo.css
    └── js/comum.js (+ um script por página)
```

### Como os requisitos foram atendidos

| Requisito | Onde está |
|---|---|
| **Diagrama de classes (UML)** | `docs/diagramas/diagrama-classes.*`, gerado por reflexão sobre as classes compiladas, com todos os atributos, métodos e relacionamentos |
| **POO** | `Usuario` (abstrata) → `Cidadao` e `ServidorPublico`, com herança e polimorfismo: `podeEditar(Ocorrencia)` e `podeAtender(Ocorrencia)` têm regras diferentes em cada perfil e são elas que decidem as permissões. `Ocorrencia implements Priorizavel`. Todos os DAOs de entidade implementam `CrudDAO<T>`. Atributos privados com getters e setters. |
| **CRUD completo via JDBC** | Inserir, listar, buscar, atualizar e excluir para Bairro, Órgão, Categoria, Cidadão, Servidor e Ocorrência, mais inserir e excluir Apoio (N:N) e inserir Atualização com transação (`commit`/`rollback`) |
| **Camadas separadas** | Pacotes `model`, `dao`, `service` e `resource` (interface) |
| **Tratamento de erros** | `ValidacaoException` (400), `NaoAutenticadoException` (401), `SemPermissaoException` (403), `NaoEncontradoException` (404), `RegraNegocioException` (409) e `DadosException`, que traduz códigos ORA (00001, 02291, 02292, 02290, 01400, 12899, falhas de conexão) em mensagens claras. O `ErroHandler` garante resposta JSON e o servidor nunca cai. |
| **Diagrama ER** | `docs/diagramas/diagrama-er.*`: 8 tabelas, PK, FK e cardinalidades, com **N:N em BA_APOIO** (cidadão × ocorrência) |
| **Script .sql** | `database/bairro_ativo.sql`: CREATE TABLE com PK, FK, NOT NULL, 7 UNIQUE e 6 CHECK, mais INSERTs de carga. Testado no Oracle da FIAP e roda sem erro, inclusive várias vezes seguidas. |
| **Navegação** | 8 páginas com o mesmo menu (`<header>`, `<nav>`, `<main>`, `<footer>`, `<section>`, `<aside>`) |
| **Telas do CRUD** | Formulários e listagens para ocorrências e cadastro do cidadão (Área do cidadão) e para bairros, órgãos, categorias e servidores (Gestão), com validação em JS (`js/comum.js` → `validarFormulario`) e mensagens na página |
| **Responsivo** | Layout fluido com grid e breakpoints: no celular o menu vira botão e as tabelas viram cartões |
| **Diferencial: web ligada ao backend** | Todas as telas leem e gravam no Oracle pela API, sem dados fixos no JS |
| **Diferencial: relatórios com SQL** | `RelatorioDAO`: JOIN, GROUP BY, COUNT, SUM, AVG e CASE, exibidos em `relatorios.html` |

---

## 4. API REST

`[C]` = exige cidadão conectado · `[S]` = exige servidor conectado · sem marca = público.

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/status` | Saúde da API e do banco |
| POST | `/api/sessao` | Entrar `{ "perfil": "CIDADAO", "email": "ana.souza@email.com" }` |
| GET | `/api/sessao` · `/api/sessao/exemplos` | Quem está conectado · contas de demonstração |
| POST | `/api/cidadaos` | Criar conta de cidadão |
| GET | `/api/cidadaos` `[S]` | Lista de cidadãos (com contato) |
| GET/PUT/DELETE | `/api/cidadaos/{id}` | Meu cadastro `[C]`, só o próprio (o servidor também pode consultar) |
| GET/POST | `/api/servidores` `[S]` | Listar / cadastrar servidor |
| GET/PUT/DELETE | `/api/servidores/{id}` `[S]` | Buscar / atualizar / excluir servidor |
| GET · POST/PUT/DELETE | `/api/bairros` · `/api/orgaos` · `/api/categorias` (+ `/{id}`) | Consulta pública · alterações `[S]` |
| GET | `/api/ocorrencias?status=&bairro=&categoria=&q=` | Consulta pública com filtros |
| GET | `/api/ocorrencias/{id}` | Detalhe com apoios, histórico e o que o usuário atual pode fazer |
| GET | `/api/ocorrencias/ranking?limite=5` | Prioridades do momento |
| POST | `/api/ocorrencias` `[C]` | Registrar (o autor é o cidadão conectado) |
| PUT/DELETE | `/api/ocorrencias/{id}` `[C]` | Editar / excluir (só o autor) |
| GET | `/api/ocorrencias/minhas` · `/apoiadas` `[C]` | Minhas ocorrências · as que apoio |
| POST/DELETE | `/api/ocorrencias/{id}/apoio` `[C]` | Apoiar / retirar meu apoio |
| GET | `/api/ocorrencias/fila` `[S]` | Fila do órgão do servidor, por prioridade |
| POST | `/api/ocorrencias/{id}/atualizacoes` `[S]` | Mudar status `{ "status": "EM_ANDAMENTO", "comentario": "..." }` |
| GET | `/api/relatorios/resumo` · `/bairros` · `/categorias` · `/orgaos` · `/engajamento` | Relatórios SQL |

Os erros sempre voltam como `{ "status": 403, "mensagem": "Esta ocorrência é atendida por Iluminação Pública. Você é de Zeladoria Urbana.", "caminho": "/ocorrencias/2/atualizacoes" }`.

### Regras de negócio

- Só **cidadãos** registram e apoiam. Só o **servidor do órgão responsável** pela categoria muda o status.
- Quem registrou não pode apoiar a própria ocorrência, e cada cidadão apoia uma vez só (PK composta).
- Ocorrências encerradas não recebem apoio nem mudam de status.
- O cidadão só edita a ocorrência enquanto ela está **Aberta**. Só é possível excluir ocorrências **Abertas** ou **Canceladas**; as que estão em atendimento ou resolvidas ficam no histórico.
- A mudança de status e o registro no histórico acontecem na **mesma transação**. O `UPDATE` confere o status anterior para evitar que duas atualizações simultâneas se sobrescrevam.
- Não é possível excluir bairro, órgão, categoria, cidadão ou servidor que tenha registros vinculados (mensagem específica para cada caso).
- O servidor conectado não pode excluir o próprio cadastro.

---

## 5. Validação com pessoas reais

_[Preencher pelo grupo: com quem conversaram (moradores, síndico, associação de bairro, agente da subprefeitura), o que ouviram e o que mudou no projeto por causa disso. Exemplo: "Moradores do Capão Redondo disseram que o maior problema é não saber se alguém viu o pedido → criamos o histórico público de atendimento."]_

---

## 6. Uso de IA generativa

| Ferramenta | Para quê |
|---|---|
| Claude (Anthropic) via Claude Code | Apoio na estruturação inicial do projeto, sugestão de modelagem, revisão de código, explicação de erros do Oracle e do Jersey e revisão de textos |

Todo o código foi revisado, executado e testado pelo grupo, que entende o funcionamento de cada camada. _[Ajustar esta seção para refletir exatamente como o grupo usou IA.]_

---

## 7. Integrantes

Veja `integrantes.txt`.
