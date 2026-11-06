# Roteiro do vídeo pitch (até 3:00)

Regra do PDF: mostrar o site funcionando, nada de vídeo de slides ou leitura de tela. Testar o link numa aba anônima.

| Tempo | Bloco | O que mostrar / falar |
|---|---|---|
| 0:00–0:30 | **O problema** | Uma pessoa real (ou foto) diante de um buraco ou poste apagado. Dado real: use um número oficial de pedidos de zeladoria da sua cidade (ex.: 156 / SP156), pesquisado e com fonte citada. "Quem pede não sabe se alguém viu; quem atende não sabe o que é mais urgente." |
| 0:30–0:50 | **A solução** | Página inicial do Bairro Ativo: registrar, apoiar, acompanhar. A comunidade monta a fila de prioridade. |
| 0:50–2:10 | **Demonstração** | **Como cidadã (Ana Souza, botão de exemplo em Entrar):** 1) Na Área do cidadão, registrar "Poste apagado" na categoria Iluminação (mostrar a validação em JS com um campo vazio e o aviso "Atendida por Iluminação Pública"). **Como vizinho (Carlos Lima):** 2) Abrir a ocorrência e clicar em Apoiar: o contador e a prioridade sobem. **Como servidora (Sandra Oliveira, Iluminação Pública):** 3) A ocorrência aparece na fila do órgão; mudar para "Em andamento" com comentário, e o histórico aparece. 4) Rápido: mostrar que a Beatriz (Zeladoria) não consegue atender iluminação, o que deixa clara a separação por órgão. 5) Relatórios: por bairro, por órgão e fora do prazo. 6) Rápido: SQL Developer mostrando o registro novo em BA_OCORRENCIA e BA_APOIO (prova de que grava no banco). |
| 2:10–2:40 | **Impacto** | Moradores (transparência), prefeitura (fila priorizada e dados por bairro), pessoas com deficiência (categoria de acessibilidade). ODS 10 e 11. Se tiverem feito a validação com pessoas reais, citar o que mudou por causa dela. |
| 2:40–3:00 | **Fechamento** | "Um bairro melhor começa quando todo mundo enxerga o problema. Bairro Ativo." |

Dica: antes de gravar, rode `database/bairro_ativo.sql` de novo para começar com os dados de demonstração limpos.
