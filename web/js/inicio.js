/* Página inicial: números gerais e prioridades do momento */

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
      ? ranking.map((o) => cartaoOcorrencia(o)).join('')
      : '<li class="vazio">Nenhuma ocorrência pendente. O bairro está em dia!</li>';
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
    document.getElementById('indicadores').innerHTML = '';
    document.getElementById('ranking').innerHTML = '';
  }
}

carregarInicio();
