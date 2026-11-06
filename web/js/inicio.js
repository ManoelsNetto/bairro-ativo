/* Página inicial: números gerais e fila de prioridade */

function cartaoOcorrencia(o) {
  return '<li class="ocorrencia">'
    + '<div class="ocorrencia__topo">' + etiquetaStatus(o.status) + ' ' + etiquetaPrioridade(o) + '</div>'
    + '<h3><a href="ocorrencia.html?id=' + o.id + '">' + esc(o.titulo) + '</a></h3>'
    + '<p class="ocorrencia__meta">' + esc(o.bairro.nome) + ' · ' + esc(o.categoria.nome) + '</p>'
    + '<p class="ocorrencia__numeros"><span><strong>' + o.totalApoios + '</strong> apoios</span>'
    + '<span><strong>' + dias(o.diasEmAberto) + '</strong> em aberto</span></p>'
    + '<div class="ocorrencia__acoes"><a class="botao botao--secundario botao--pequeno" href="ocorrencia.html?id='
    + o.id + '">Ver e apoiar</a></div>'
    + '</li>';
}

async function carregarInicio() {
  try {
    const [resumo, ranking] = await Promise.all([api('/relatorios/resumo'), api('/ocorrencias/ranking?limite=6')]);
    document.getElementById('indicadores').innerHTML = [
      ['Ocorrências registradas', resumo.total, ''],
      ['Aguardando solução', resumo.pendentes, ''],
      ['Fora do prazo', resumo.atrasadas, 'indicador--alerta'],
      ['Resolvidas', resumo.resolvidas, ''],
      ['Apoios da vizinhança', resumo.apoios, '']
    ].map(([rotulo, valor, classe]) => '<div class="indicador ' + classe + '"><div class="indicador__valor">'
      + valor + '</div><div class="indicador__rotulo">' + rotulo + '</div></div>').join('');

    document.getElementById('ranking').innerHTML = ranking.length
      ? ranking.map(cartaoOcorrencia).join('')
      : '<li class="vazio">Nenhuma ocorrência pendente. O bairro está em dia!</li>';
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
    document.getElementById('indicadores').innerHTML = '';
    document.getElementById('ranking').innerHTML = '';
  }
}

carregarInicio();
