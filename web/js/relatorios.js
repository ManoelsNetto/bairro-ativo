/* Relatórios: consultas SQL com JOIN e agregação expostas pela API */

function celula(rotulo, valor, numero) {
  return '<td data-rotulo="' + rotulo + '"' + (numero ? ' class="numero"' : '') + '>' + valor + '</td>';
}

function porcentagem(parte, total) {
  return total ? Math.round((parte / total) * 100) + '%' : '—';
}

async function carregarRelatorios() {
  try {
    const [resumo, bairros, categorias, ranking, engajamento, orgaos] = await Promise.all([
      api('/relatorios/resumo'), api('/relatorios/bairros'), api('/relatorios/categorias'),
      api('/ocorrencias/ranking?limite=10'), api('/relatorios/engajamento?limite=8'), api('/relatorios/orgaos')]);

    document.getElementById('indicadores').innerHTML = [
      ['Ocorrências', resumo.total, ''],
      ['Pendentes', resumo.pendentes, ''],
      ['Fora do prazo', resumo.atrasadas, 'indicador--alerta'],
      ['Taxa de resolução', porcentagem(resumo.resolvidas, resumo.total), ''],
      ['Tempo médio de solução', resumo.diasMediosResolucao != null ? resumo.diasMediosResolucao.toLocaleString('pt-BR') + ' dias' : '—', ''],
      ['Cidadãos participando', resumo.cidadaos, '']
    ].map(([rotulo, valor, classe]) => '<div class="indicador ' + classe + '"><div class="indicador__valor">'
      + valor + '</div><div class="indicador__rotulo">' + rotulo + '</div></div>').join('');

    document.getElementById('tabela-bairros').innerHTML = bairros.map((b) => {
      const pPend = b.total ? (b.pendentes / b.total) * 100 : 0;
      const pRes = b.total ? (b.resolvidas / b.total) * 100 : 0;
      return '<tr>'
        + celula('Bairro', '<a href="ocorrencias.html?bairro=' + b.idBairro + '">' + esc(b.bairro) + '</a>')
        + celula('Zona', esc(ZONAS[b.zona]))
        + celula('Total', b.total, true) + celula('Pendentes', b.pendentes, true)
        + celula('Resolvidas', b.resolvidas, true) + celula('Apoios', b.apoios, true)
        + celula('Tempo médio', b.diasMediosResolucao != null ? b.diasMediosResolucao.toLocaleString('pt-BR') + ' dias' : '—', true)
        + celula('Distribuição', '<div class="barra" role="img" aria-label="' + b.pendentes + ' pendentes e '
          + b.resolvidas + ' resolvidas"><span class="b-pendente" style="width:' + pPend + '%"></span>'
          + '<span class="b-resolvida" style="width:' + pRes + '%"></span></div>')
        + '</tr>';
    }).join('');

    document.getElementById('tabela-categorias').innerHTML = categorias.map((c) => '<tr>'
      + celula('Categoria', esc(c.categoria)) + celula('Órgão', esc(c.orgao)) + celula('Prazo', dias(c.prazoDias), true)
      + celula('Total', c.total, true) + celula('Pendentes', c.pendentes, true)
      + celula('Fora do prazo', c.atrasadas ? '<span class="etiqueta etiqueta--atraso">' + c.atrasadas + '</span>' : '0', true)
      + celula('Resolvidas no prazo', c.resolvidas ? c.resolvidasNoPrazo + ' de ' + c.resolvidas : '—', true)
      + '</tr>').join('');

    document.getElementById('tabela-orgaos').innerHTML = orgaos.map((g) => '<tr>'
      + celula('Órgão', '<strong>' + esc(g.orgao) + '</strong> (' + esc(g.sigla) + ')')
      + celula('Pendentes', g.pendentes, true)
      + celula('Fora do prazo', g.atrasadas ? '<span class="etiqueta etiqueta--atraso">' + g.atrasadas + '</span>' : '0', true)
      + celula('Resolvidas', g.resolvidas, true)
      + celula('Tempo médio', g.diasMediosResolucao != null ? g.diasMediosResolucao.toLocaleString('pt-BR') + ' dias' : '—', true)
      + '</tr>').join('');

    document.getElementById('tabela-ranking').innerHTML = ranking.length ? ranking.map((o) => '<tr>'
      + celula('Ocorrência', '<a href="ocorrencia.html?id=' + o.id + '">' + esc(o.titulo) + '</a>')
      + celula('Bairro', esc(o.bairro.nome)) + celula('Apoios', o.totalApoios, true)
      + celula('Dias', o.diasEmAberto, true)
      + celula('Pontos', '<span class="etiqueta prioridade-' + o.nivelPrioridade + '">' + o.pontuacaoPrioridade + '</span>', true)
      + '</tr>').join('') : '<tr><td colspan="5">Nenhuma ocorrência pendente.</td></tr>';

    document.getElementById('tabela-engajamento').innerHTML = engajamento.map((e) => '<tr>'
      + celula('Cidadão', esc(e.nome)) + celula('Bairro', esc(e.bairro))
      + celula('Registros', e.ocorrenciasRegistradas, true) + celula('Apoios', e.apoiosDados, true)
      + '</tr>').join('');
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
    document.getElementById('indicadores').innerHTML = '';
  }
}

carregarRelatorios();
