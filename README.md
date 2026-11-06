# Bairro Ativo: zeladoria colaborativa

> FIAP · Engenharia de Software · 2ESOA · Global Solution 2026/2 · **CitizenTech**

**Vídeo pitch:** _[COLE AQUI O LINK PÚBLICO DO VÍDEO]_

Bairro Ativo é uma plataforma em que **moradores registram problemas do espaço público** (buraco, poste apagado, entulho, calçada sem rampa, bueiro entupido), **vizinhos apoiam** as ocorrências que também os afetam e **agentes da prefeitura atualizam o atendimento**, com histórico visível para todos. A fila de prioridade é calculada pela própria comunidade.

---

## 1. A proposta

| Critério | Resposta |
|---|---|
| **Quem é o cidadão beneficiado** | Moradores de bairros urbanos, principalmente da periferia, onde a zeladoria demora mais, e pessoas com mobilidade reduzida, que mais sofrem com calçadas quebradas e falta de iluminação. |
| **Qual problema é resolvido** | Os pedidos de zeladoria se perdem em canais lentos e sem retorno. O morador não sabe se alguém viu o problema, e a prefeitura não sabe o que é mais urgente para a comunidade. |
| **Como a solução funciona** | 1) O cidadão registra a ocorrência com endereço, bairro e categoria. 2) Os vizinhos apoiam ("isso também me afeta"). 3) O sistema calcula a prioridade. 4) O agente público muda o status e deixa um comentário. 5) Todos acompanham o histórico e os relatórios por bairro e categoria. |
| **Tecnologias** | Java 21 (POO), JDBC, Oracle, API REST com Jersey (JAX-RS) sobre o servidor Grizzly e front em HTML5, CSS3 e JavaScript puro. |
| **Ecossistema** | Moradores e associações de bairro, subprefeituras e órgãos de zeladoria (iluminação, limpeza, drenagem, CET) e escolas e comércio local, que também são afetados. |
| **Como o impacto é medido** | Tempo médio de resolução, ocorrências fora do prazo por categoria, taxa de resolução, apoios por bairro e engajamento dos cidadãos, tudo na página **Relatórios**. |

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
- Proteção de dados: as listagens públicas de ocorrência e apoio mostram só o nome de quem registrou ou apoiou, nunca o e-mail ou o telefone. As credenciais do banco ficam no `.env`, fora do git.

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

Acesse **http://localhost:8080**. A API fica em http://localhost:8080/api e o status da conexão com o banco em http://localhost:8080/api/status.

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
    ├── index.html  ocorrencias.html  ocorrencia.html  usuarios.html  cadastros.html  relatorios.html
    ├── css/estilo.css
    └── js/comum.js (+ um script por página)
```

### Como os requisitos foram atendidos

| Requisito | Onde está |
|---|---|
| **Diagrama de classes (UML)** | `docs/diagramas/diagrama-classes.*`, gerado por reflexão sobre as classes compiladas, com todos os atributos, métodos e relacionamentos |
| **POO** | `Usuario` (abstrata) → `Cidadao` e `AgentePublico` (herança e polimorfismo em `podeRegistrarOcorrencias()` e `podeAtenderOcorrencias()`). `Ocorrencia implements Priorizavel`. Todos os DAOs de entidade implementam `CrudDAO<T>`. Atributos privados com getters e setters. |
| **CRUD completo via JDBC** | Inserir, listar, buscar, atualizar e excluir para Bairro, Categoria, Usuário e Ocorrência, mais inserir e excluir Apoio (N:N) e inserir Atualização com transação (`commit`/`rollback`) |
| **Camadas separadas** | Pacotes `model`, `dao`, `service` e `resource` (interface) |
| **Tratamento de erros** | `ValidacaoException` (400), `NaoEncontradoException` (404), `RegraNegocioException` (409) e `DadosException`, que traduz códigos ORA (00001, 02291, 02292, 02290, 01400, 12899, falhas de conexão) em mensagens claras. O `ErroHandler` garante resposta JSON e o servidor nunca cai. |
| **Diagrama ER** | `docs/diagramas/diagrama-er.*`: 6 tabelas, PK, FK e cardinalidades, com **N:N em BA_APOIO** (cidadão × ocorrência) |
| **Script .sql** | `database/bairro_ativo.sql`: CREATE TABLE com PK, FK, NOT NULL, 4 UNIQUE e 8 CHECK, mais INSERTs de carga. Testado no Oracle da FIAP e roda sem erro, inclusive várias vezes seguidas. |
| **Navegação** | 6 páginas com o mesmo menu (`<header>`, `<nav>`, `<main>`, `<footer>`, `<section>`, `<aside>`) |
| **Telas do CRUD** | Formulários e listagens para ocorrências, usuários, bairros e categorias, com validação em JS (`js/comum.js` → `validarFormulario`) e mensagens na página |
| **Responsivo** | Layout fluido com grid e breakpoints: no celular o menu vira botão e as tabelas viram cartões |
| **Diferencial: web ligada ao backend** | Todas as telas leem e gravam no Oracle pela API, sem dados fixos no JS |
| **Diferencial: relatórios com SQL** | `RelatorioDAO`: JOIN, GROUP BY, COUNT, SUM, AVG e CASE, exibidos em `relatorios.html` |

---

## 4. API REST

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/status` | Saúde da API e do banco |
| GET/POST | `/api/bairros` | Listar / cadastrar bairro |
| GET/PUT/DELETE | `/api/bairros/{id}` | Buscar / atualizar / excluir |
| GET/POST | `/api/categorias` | Listar / cadastrar categoria |
| GET/PUT/DELETE | `/api/categorias/{id}` | Buscar / atualizar / excluir |
| GET/POST | `/api/usuarios?tipo=CIDADAO\|AGENTE` | Listar (filtro opcional) / cadastrar |
| GET/PUT/DELETE | `/api/usuarios/{id}` | Buscar / atualizar / excluir |
| GET/POST | `/api/ocorrencias?status=&bairro=&categoria=&q=` | Listar com filtros / registrar |
| GET/PUT/DELETE | `/api/ocorrencias/{id}` | Detalhe (com apoios e histórico) / editar / excluir |
| GET | `/api/ocorrencias/ranking?limite=5` | Fila de prioridade |
| POST | `/api/ocorrencias/{id}/apoios` | Apoiar `{ "idCidadao": 1 }` |
| DELETE | `/api/ocorrencias/{id}/apoios/{idCidadao}` | Remover apoio |
| POST | `/api/ocorrencias/{id}/atualizacoes` | Mudar status `{ "idAgente": 9, "status": "EM_ANDAMENTO", "comentario": "..." }` |
| GET | `/api/relatorios/resumo` · `/bairros` · `/categorias` · `/engajamento` | Relatórios SQL |

Os erros sempre voltam como `{ "status": 409, "mensagem": "Quem registrou a ocorrência não pode apoiá-la.", "caminho": "/ocorrencias/3/apoios" }`.

### Regras de negócio

- Só **cidadãos** registram e apoiam, e só **agentes públicos** mudam o status.
- Quem registrou não pode apoiar a própria ocorrência, e cada cidadão apoia uma vez só (PK composta).
- Ocorrências encerradas não recebem apoio nem mudam de status.
- O cidadão só edita a ocorrência enquanto ela está **Aberta**. Só é possível excluir ocorrências **Abertas** ou **Canceladas**; as que estão em atendimento ou resolvidas ficam no histórico.
- A mudança de status e o registro no histórico acontecem na **mesma transação**. O `UPDATE` confere o status anterior para evitar que duas atualizações simultâneas se sobrescrevam.
- Não é possível excluir bairro, categoria ou usuário que tenha registros vinculados (mensagem específica para cada caso).

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
