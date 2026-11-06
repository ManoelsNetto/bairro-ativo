/* Área do servidor: indicadores e fila de atendimento do órgão do servidor conectado */

const sessao = exigirPerfil('SERVIDOR');

async function iniciar() {
  if (!sessao) return;
  try {
    const [eu, fila, orgaos, categorias] = await Promise.all([
      api('/servidores/' + sessao.id), api('/ocorrencias/fila'), api('/relatorios/orgaos'), api('/categorias')]);
    const orgao = eu.orgao;
    document.getElementById('saudacao').textContent = 'Olá, ' + eu.nome.split(' ')[0] + '! ' + eu.cargo + ' · '
      + orgao.nome + '. Aqui está a fila de atendimento do seu órgão.';

    const meu = orgaos.find((g) => g.idOrgao === orgao.id) || { pendentes: 0, atrasadas: 0, resolvidas: 0 };
    const altas = fila.filter((o) => o.nivelPrioridade === 'ALTA').length;
    document.getElementById('indicadores').innerHTML = [
      ['Pendentes no ' + orgao.sigla, meu.pendentes, ''],
      ['Prioridade alta', altas, 'indicador--alerta'],
      ['Fora do prazo', meu.atrasadas, 'indicador--alerta'],
      ['Resolvidas', meu.resolvidas, ''],
      ['Tempo médio de solução', meu.diasMediosResolucao != null ? meu.diasMediosResolucao.toLocaleString('pt-BR') + ' dias' : '—', '']
    ].map(([rotulo, valor, classe]) => '<div class="indicador ' + classe + '"><div class="indicador__valor">'
      + valor + '</div><div class="indicador__rotulo">' + rotulo + '</div></div>').join('');

    const minhasCategorias = categorias.filter((c) => c.orgao.id === orgao.id);
    document.getElementById('categorias-orgao').innerHTML = minhasCategorias.length
      ? minhasCategorias.map((c) => '<span>' + esc(c.nome) + ' · ' + dias(c.prazoDias) + '</span>').join('')
      : '<p>Nenhuma categoria ligada ao seu órgão. Ajuste em Gestão de cadastros.</p>';

    document.getElementById('fila').innerHTML = fila.length ? fila.map((o) => '<tr>'
      + '<td data-rotulo="Prioridade"><span class="etiqueta prioridade-' + o.nivelPrioridade + '">'
      + o.pontuacaoPrioridade + ' pts</span>' + (o.atrasada ? ' <span class="etiqueta etiqueta--atraso">Fora do prazo</span>' : '') + '</td>'
      + '<td data-rotulo="Ocorrência"><a href="ocorrencia.html?id=' + o.id + '">' + esc(o.titulo) + '</a><br><small>'
      + esc(o.categoria.nome) + '</small></td>'
      + '<td data-rotulo="Bairro">' + esc(o.bairro.nome) + '</td>'
      + '<td data-rotulo="Status">' + etiquetaStatus(o.status) + '</td>'
      + '<td data-rotulo="Apoios" class="numero">' + o.totalApoios + '</td>'
      + '<td data-rotulo="Dias" class="numero">' + o.diasEmAberto + '</td>'
      + '<td data-rotulo="Ações"><div class="acoes"><a class="botao botao--primario botao--pequeno" href="ocorrencia.html?id='
      + o.id + '">Atender</a></div></td></tr>').join('')
      : '<tr><td colspan="7">Nenhuma pendência para o seu órgão. Tudo em dia!</td></tr>';
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
    if (!sessaoAtual()) location.replace('entrar.html?perfil=SERVIDOR');
  }
}

iniciar();
