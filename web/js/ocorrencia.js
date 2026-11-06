/* Detalhe da ocorrência: o que aparece depende de quem está usando
   - visitante: só consulta
   - cidadão: apoia (ou retira o apoio); quem registrou pode editar/excluir enquanto Aberta
   - servidor do órgão responsável: atualiza o status */

const idOcorrencia = Number(parametroUrl('id'));
const formAtualizacao = document.getElementById('form-atualizacao');
const sessao = sessaoAtual();
let detalhe = null;

const regrasAtualizacao = {
  status: [Regras.selecionado('o novo status')],
  comentario: [Regras.obrigatorio('um comentário para o cidadão'), Regras.minimo(5), Regras.maximo(500)]
};
validacaoAoDigitar(formAtualizacao, regrasAtualizacao);

function areaApoio(o) {
  if (!o.pendente) {
    return '<p>Esta ocorrência foi encerrada e não recebe mais apoios.</p>';
  }
  if (!sessao) {
    return '<p>Este problema também afeta você?</p>'
      + '<a class="botao botao--destaque" href="entrar.html?perfil=CIDADAO&volta=' + encodeURIComponent('ocorrencia.html?id=' + o.id)
      + '">Entrar como cidadão para apoiar</a>';
  }
  if (sessao.perfil === 'SERVIDOR') {
    return '<p>Apoios são dados pelos cidadãos. Como servidor, você usa os apoios para priorizar o atendimento.</p>';
  }
  if (o.autor.id === sessao.id) {
    return '<p>Você registrou esta ocorrência. Compartilhe com os vizinhos para que eles apoiem.</p>';
  }
  if (detalhe.jaApoiou) {
    return '<p><strong>Você apoia esta ocorrência.</strong></p>'
      + '<button class="botao botao--secundario" type="button" id="botao-retirar">Retirar meu apoio</button>';
  }
  return '<p>Este problema também afeta você?</p>'
    + '<button class="botao botao--destaque" type="button" id="botao-apoiar">Apoiar esta ocorrência</button>';
}

function renderizar() {
  const o = detalhe.ocorrencia;
  document.title = o.titulo + ' · Bairro Ativo';
  document.getElementById('titulo').textContent = o.titulo;
  document.getElementById('subtitulo').textContent = o.endereco + ' · ' + o.bairro.nome;
  document.getElementById('etiquetas').innerHTML = etiquetaStatus(o.status) + ' ' + etiquetaPrioridade(o);
  document.getElementById('descricao').textContent = o.descricao;

  const info = [
    ['Categoria', esc(o.categoria.nome)],
    ['Órgão responsável', esc(o.categoria.orgao.nome)],
    ['Prazo esperado', dias(o.categoria.prazoDias)],
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

  document.getElementById('historico').innerHTML = '<li><time>' + formatarData(o.dataAbertura, true)
    + '</time><strong>Registrada</strong> por ' + esc(o.autor.nome) + '</li>'
    + detalhe.historico.map((h) => '<li><time>' + formatarData(h.data, true) + '</time>'
      + etiquetaStatus(h.statusNovo) + ' <strong>' + esc(h.servidor.nome) + '</strong> · '
      + esc(h.servidor.cargo) + ', ' + esc(h.servidor.orgao.nome)
      + '<p>' + esc(h.comentario) + '</p></li>').join('');

  document.getElementById('total-apoios').textContent = o.totalApoios;
  document.getElementById('area-apoio').innerHTML = areaApoio(o);
  document.getElementById('apoiadores').innerHTML = detalhe.apoios.length
    ? detalhe.apoios.map((a) => '<li><span>' + esc(a.cidadao.nome) + '<small>' + esc(a.cidadao.bairro.nome)
      + ' · ' + formatarData(a.data) + '</small></span></li>').join('')
    : '<li>Ninguém apoiou ainda.</li>';

  // Autor: editar/excluir
  const areaAutor = document.getElementById('area-autor');
  const ehAutor = sessao && sessao.perfil === 'CIDADAO' && o.autor.id === sessao.id;
  const podeExcluir = ehAutor && (o.status === 'ABERTA' || o.status === 'CANCELADA');
  areaAutor.classList.toggle('oculto', !ehAutor);
  document.getElementById('link-editar').classList.toggle('oculto', !detalhe.podeEditar);
  document.getElementById('link-editar').href = 'cidadao.html?editar=' + o.id + '#nova';
  document.getElementById('botao-excluir').classList.toggle('oculto', !podeExcluir);
  if (ehAutor && !detalhe.podeEditar && !podeExcluir) {
    areaAutor.querySelector('.dica').textContent = 'Ela já está em atendimento, então não pode mais ser alterada. Acompanhe pelo histórico.';
  }

  // Servidor: atendimento
  const secaoAtendimento = document.getElementById('secao-atendimento');
  const ehServidor = sessao && sessao.perfil === 'SERVIDOR';
  secaoAtendimento.classList.toggle('oculto', !ehServidor || !o.pendente);
  formAtualizacao.classList.toggle('oculto', !detalhe.podeAtender);
  const ordem = Object.keys(STATUS);
  const proximos = [...detalhe.proximosStatus].sort((a, b) => ordem.indexOf(a) - ordem.indexOf(b));
  formAtualizacao.elements.status.innerHTML = opcoes(proximos, (s) => s, (s) => STATUS[s], 'Selecione…');
  document.getElementById('atendendo-como').textContent = detalhe.podeAtender
    ? 'Você está atendendo como ' + sessao.nome + ' (' + sessao.descricao + '). O comentário fica visível para todos.'
    : 'Esta ocorrência é atendida por ' + o.categoria.orgao.nome + '. Só servidores desse órgão podem atualizar o status.';
  if (ehServidor) document.getElementById('link-voltar').setAttribute('href', 'servidor.html');
  if (ehServidor) document.getElementById('link-voltar').textContent = '← Voltar para a fila de atendimento';
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

document.getElementById('area-apoio').addEventListener('click', async (ev) => {
  const apoiar = ev.target.closest('#botao-apoiar');
  const retirar = ev.target.closest('#botao-retirar');
  if (!apoiar && !retirar) return;
  limparMensagem('mensagem-apoio');
  try {
    await comBotaoOcupado(apoiar || retirar, apoiar ? 'Apoiando…' : 'Retirando…', async () => {
      await api('/ocorrencias/' + idOcorrencia + '/apoio', { metodo: apoiar ? 'POST' : 'DELETE' });
      await carregar();
    });
    mostrarMensagem('mensagem-apoio', apoiar ? 'Apoio registrado. Obrigado por participar!' : 'Seu apoio foi retirado.');
  } catch (e) {
    mostrarMensagem('mensagem-apoio', e.message, 'erro');
  }
});

document.getElementById('botao-excluir').addEventListener('click', async (ev) => {
  if (!confirm('Excluir esta ocorrência? Os apoios e o histórico também serão apagados.')) return;
  try {
    await comBotaoOcupado(ev.currentTarget, 'Excluindo…', () => api('/ocorrencias/' + idOcorrencia, { metodo: 'DELETE' }));
    location.href = 'cidadao.html?excluida=1';
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
});

formAtualizacao.addEventListener('submit', async (ev) => {
  ev.preventDefault();
  limparMensagem('mensagem-atualizacao');
  if (!validarFormulario(formAtualizacao, regrasAtualizacao)) return;
  try {
    await comBotaoOcupado(formAtualizacao.querySelector('[type=submit]'), 'Salvando…', async () => {
      await api('/ocorrencias/' + idOcorrencia + '/atualizacoes', {
        metodo: 'POST',
        corpo: {
          status: formAtualizacao.elements.status.value,
          comentario: formAtualizacao.elements.comentario.value.trim()
        }
      });
      formAtualizacao.reset();
      await carregar();
    });
    mostrarMensagem('mensagem', 'Status atualizado. O cidadão já pode ver o andamento.');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (e) {
    mostrarMensagem('mensagem-atualizacao', e.message, 'erro');
  }
});

if (!idOcorrencia) {
  document.getElementById('titulo').textContent = 'Ocorrência não informada';
  document.getElementById('conteudo-detalhe').classList.add('oculto');
} else {
  carregar();
}
