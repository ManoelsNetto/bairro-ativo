/* Detalhe da ocorrência: apoios (N:N) e atualização de status pelo agente */

const idOcorrencia = Number(parametroUrl('id'));
const formApoio = document.getElementById('form-apoio');
const formAtualizacao = document.getElementById('form-atualizacao');
let cidadaos = [];
let detalhe = null;

const regrasApoio = { idCidadao: [Regras.selecionado('quem está apoiando')] };
const regrasAtualizacao = {
  idAgente: [Regras.selecionado('o agente responsável')],
  status: [Regras.selecionado('o novo status')],
  comentario: [Regras.obrigatorio('um comentário para o cidadão'), Regras.minimo(5), Regras.maximo(500)]
};
validacaoAoDigitar(formAtualizacao, regrasAtualizacao);

function renderizar() {
  const o = detalhe.ocorrencia;
  document.title = o.titulo + ' · Bairro Ativo';
  document.getElementById('titulo').textContent = o.titulo;
  document.getElementById('subtitulo').textContent = o.endereco + ' · ' + o.bairro.nome;
  document.getElementById('etiquetas').innerHTML = etiquetaStatus(o.status) + ' ' + etiquetaPrioridade(o);
  document.getElementById('descricao').textContent = o.descricao;

  const prazo = o.categoria.prazoDias;
  const info = [
    ['Categoria', esc(o.categoria.nome)],
    ['Prazo esperado', dias(prazo)],
    ['Endereço', esc(o.endereco)],
    ['Referência', esc(o.referencia || '—')],
    ['Bairro', esc(o.bairro.nome) + ' (zona ' + esc(ZONAS[o.bairro.zona]).toLowerCase() + ')'],
    ['Registrada por', esc(o.autor.nome)],
    ['Aberta em', formatarData(o.dataAbertura, true)],
    [o.dataResolucao ? 'Resolvida em' : 'Tempo em aberto',
      o.dataResolucao ? formatarData(o.dataResolucao, true) + ' (' + dias(o.diasEmAberto) + ')' : dias(o.diasEmAberto)]
  ];
  if (o.pendente) info.push(['Pontuação de prioridade', o.pontuacaoPrioridade + ' pontos']);
  document.getElementById('info').innerHTML = info.map(([t, d]) => '<dt>' + t + '</dt><dd>' + d + '</dd>').join('');

  const historico = detalhe.historico;
  document.getElementById('historico').innerHTML = '<li><time>' + formatarData(o.dataAbertura, true)
    + '</time><strong>Registrada</strong> por ' + esc(o.autor.nome) + '</li>'
    + historico.map((h) => '<li><time>' + formatarData(h.data, true) + '</time>'
      + etiquetaStatus(h.statusNovo) + ' <strong>' + esc(h.agente.nome) + '</strong> · ' + esc(h.agente.orgao)
      + '<p>' + esc(h.comentario) + '</p></li>').join('');

  document.getElementById('total-apoios').textContent = o.totalApoios;
  document.getElementById('apoiadores').innerHTML = detalhe.apoios.length
    ? detalhe.apoios.map((a) => '<li><span>' + esc(a.cidadao.nome) + '<small>' + esc(a.cidadao.bairro.nome)
      + ' · ' + formatarData(a.data) + '</small></span>'
      + (o.pendente ? '<button class="botao botao--perigo botao--pequeno" type="button" data-remover="'
        + a.cidadao.id + '" aria-label="Remover apoio de ' + esc(a.cidadao.nome) + '">Remover</button>' : '')
      + '</li>').join('')
    : '<li>Ninguém apoiou ainda. Seja o primeiro!</li>';

  // Só aparecem no seletor os cidadãos que ainda podem apoiar
  const jaApoiaram = new Set(detalhe.apoios.map((a) => a.cidadao.id));
  const disponiveis = cidadaos.filter((c) => c.id !== o.autor.id && !jaApoiaram.has(c.id));
  formApoio.elements.idCidadao.innerHTML = opcoes(disponiveis, (c) => c.id, (c) => c.nome + ' · ' + c.bairro.nome, 'Selecione…');
  formApoio.classList.toggle('oculto', !o.pendente);

  const secaoAtendimento = document.getElementById('secao-atendimento');
  secaoAtendimento.classList.toggle('oculto', detalhe.proximosStatus.length === 0);
  const ordem = Object.keys(STATUS);
  const proximos = [...detalhe.proximosStatus].sort((a, b) => ordem.indexOf(a) - ordem.indexOf(b));
  formAtualizacao.elements.status.innerHTML = opcoes(proximos, (s) => s, (s) => STATUS[s], 'Selecione…');
}

async function carregar() {
  try {
    detalhe = await api('/ocorrencias/' + idOcorrencia);
    renderizar();
  } catch (e) {
    document.getElementById('titulo').textContent = 'Ocorrência indisponível';
    document.getElementById('conteudo-detalhe').classList.add('oculto');
    mostrarMensagem('mensagem', e.message, 'erro');
  }
}

formApoio.addEventListener('submit', async (ev) => {
  ev.preventDefault();
  if (!validarFormulario(formApoio, regrasApoio)) return;
  try {
    await comBotaoOcupado(formApoio.querySelector('[type=submit]'), 'Apoiando…', async () => {
      await api('/ocorrencias/' + idOcorrencia + '/apoios', {
        metodo: 'POST', corpo: { idCidadao: Number(formApoio.elements.idCidadao.value) }
      });
      await carregar();
    });
    mostrarMensagem('mensagem-apoio', 'Apoio registrado. Obrigado por participar!');
  } catch (e) {
    mostrarMensagem('mensagem-apoio', e.message, 'erro');
  }
});

document.getElementById('apoiadores').addEventListener('click', async (ev) => {
  const botao = ev.target.closest('[data-remover]');
  if (!botao || !confirm('Remover este apoio?')) return;
  try {
    await comBotaoOcupado(botao, 'Removendo…', async () => {
      await api('/ocorrencias/' + idOcorrencia + '/apoios/' + botao.dataset.remover, { metodo: 'DELETE' });
      await carregar();
    });
    mostrarMensagem('mensagem-apoio', 'Apoio removido.');
  } catch (e) {
    mostrarMensagem('mensagem-apoio', e.message, 'erro');
  }
});

formAtualizacao.addEventListener('submit', async (ev) => {
  ev.preventDefault();
  limparMensagem('mensagem-atualizacao');
  if (!validarFormulario(formAtualizacao, regrasAtualizacao)) return;
  try {
    await comBotaoOcupado(formAtualizacao.querySelector('[type=submit]'), 'Salvando…', () =>
      api('/ocorrencias/' + idOcorrencia + '/atualizacoes', {
        metodo: 'POST',
        corpo: {
          idAgente: Number(formAtualizacao.elements.idAgente.value),
          status: formAtualizacao.elements.status.value,
          comentario: formAtualizacao.elements.comentario.value.trim()
        }
      }));
    formAtualizacao.reset();
    await carregar();
    mostrarMensagem('mensagem', 'Status atualizado. O cidadão já pode ver o andamento.');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (e) {
    mostrarMensagem('mensagem-atualizacao', e.message, 'erro');
  }
});

async function iniciar() {
  if (!idOcorrencia) {
    document.getElementById('titulo').textContent = 'Ocorrência não informada';
    document.getElementById('conteudo-detalhe').classList.add('oculto');
    return;
  }
  try {
    const [listaCidadaos, agentes] = await Promise.all([api('/usuarios?tipo=CIDADAO'), api('/usuarios?tipo=AGENTE')]);
    cidadaos = listaCidadaos;
    formAtualizacao.elements.idAgente.innerHTML = opcoes(agentes, (a) => a.id, (a) => a.nome + ' · ' + a.orgao, 'Selecione…');
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
  await carregar();
}

iniciar();
