# Bairro Ativo

## Como executar

### Pré-requisitos

- **Java 21** ou superior (`java -version`)
- Acesso ao **Oracle** (o da FIAP: `oracle.fiap.com.br:1521:orcl`)
- Uma IDE Java com suporte a Maven (IntelliJ IDEA, Eclipse ou VS Code com o Extension Pack for Java)

### Passo a passo

1. **Crie as tabelas e a carga inicial no Oracle.** Abra `database/bairro_ativo.sql` no SQL Developer e execute como script (F5). O script pode ser rodado de novo: ele apaga e recria apenas as tabelas `BA_`.
2. **Configure o acesso ao banco.** Na raiz do projeto, copie o arquivo `.env.example` com o nome `.env` e preencha `DB_USER` (seu RM) e `DB_PASSWORD`.
3. **Abra o projeto na IDE.** Abra a pasta raiz (a que contém o `pom.xml`) como projeto Maven e aguarde o download das dependências.
4. **Rode a classe `Main`.** Ela fica em `src/main/java/br/com/fiap/bairroativo/Main.java`: abra o arquivo e execute o método `main` (Run).

O diretório de trabalho da execução precisa ser a raiz do projeto, que é o padrão das IDEs, porque é lá que a aplicação procura o `.env` e a pasta `web/`.

Acesse **http://localhost:8080** e clique em **Entrar** para escolher o perfil. A API fica em http://localhost:8080/api e o status da conexão com o banco em http://localhost:8080/api/status.

A entrada é em modo demonstração, só com o e-mail, sem senha. Contas de exemplo:

- Cidadã **Ana Souza**: `ana.souza@email.com`
- Servidora **Beatriz Nogueira** (Zeladoria Urbana): `beatriz.nogueira@prefeitura.sp.gov.br`
- Outras contas aparecem na própria tela Entrar.

> A porta pode ser alterada com a variável `PORT`. As variáveis do `.env` também podem vir do ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT`, `WEB_DIR`).
