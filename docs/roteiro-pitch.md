# Roteiro do vídeo pitch (até 3:00)

Regra do PDF: mostrar o site funcionando, nada de vídeo de slides ou leitura de tela. Testar o link numa aba anônima.

| Tempo | Bloco | O que mostrar / falar |
|---|---|---|
| 0:00–0:30 | **O problema** | Uma pessoa real (ou foto) diante de um buraco ou poste apagado. Dado real: use um número oficial de pedidos de zeladoria da sua cidade (ex.: 156 / SP156), pesquisado e com fonte citada. "Quem pede não sabe se alguém viu; quem atende não sabe o que é mais urgente." |
| 0:30–0:50 | **A solução** | Página inicial do Bairro Ativo: registrar, apoiar, acompanhar. A comunidade monta a fila de prioridade. |
| 0:50–2:10 | **Demonstração** | 1) Registrar ocorrência (mostrar a validação em JS com um campo vazio e depois o salvamento). 2) Abrir o detalhe e apoiar com outro cidadão: o contador e a prioridade sobem. 3) Como agente, mudar para "Em andamento" com comentário: o histórico aparece. 4) Tentar apoiar a própria ocorrência: mensagem de erro clara. 5) Relatórios: ocorrências por bairro, fora do prazo e tempo médio. 6) Rápido: SQL Developer mostrando o registro novo nas tabelas BA_OCORRENCIA e BA_APOIO (prova de que grava no banco). |
| 2:10–2:40 | **Impacto** | Moradores (transparência), prefeitura (fila priorizada e dados por bairro), pessoas com deficiência (categoria de acessibilidade). ODS 10 e 11. Se tiverem feito a validação com pessoas reais, citar o que mudou por causa dela. |
| 2:40–3:00 | **Fechamento** | "Um bairro melhor começa quando todo mundo enxerga o problema. Bairro Ativo." |

Dica: antes de gravar, rode `database/bairro_ativo.sql` de novo para começar com os dados de demonstração limpos.
