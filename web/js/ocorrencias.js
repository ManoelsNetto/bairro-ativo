/* Ocorrências: consulta pública com filtros */

const filtros = document.getElementById('form-filtros');

async function carregarLista() {
  const parametros = new URLSearchParams();
  ['q', 'status', 'bairro', 'categoria'].forEach((nome) => {
    const valor = filtros.elements[nome].value.trim();
    if (valor) parametros.set(nome, valor);
  });
  const contagem = document.getElementById('contagem');
  contagem.textContent = 'Carregando…';
  try {
    const ocorrencias = await api('/ocorrencias?' + parametros.toString());
    contagem.textContent = ocorrencias.length === 1 ? '1 ocorrência encontrada' : ocorrencias.length + ' ocorrências encontradas';
    document.getElementById('lista').innerHTML = ocorrencias.length
      ? ocorrencias.map((o) => cartaoOcorrencia(o)).join('')
      : '<li class="vazio">Nenhuma ocorrência com esses filtros.</li>';
  } catch (e) {
    contagem.textContent = '';
    mostrarMensagem('mensagem', e.message, 'erro');
  }
}

let esperaBusca;
filtros.addEventListener('input', () => {
  clearTimeout(esperaBusca);
  esperaBusca = setTimeout(carregarLista, 300);
});
filtros.addEventListener('submit', (ev) => { ev.preventDefault(); carregarLista(); });

async function iniciar() {
  const sessao = sessaoAtual();
  if (sessao && sessao.perfil === 'SERVIDOR') {
    document.getElementById('botao-nova').classList.add('oculto');
  }
  try {
    const [bairros, categorias] = await Promise.all([api('/bairros'), api('/categorias')]);
    filtros.elements.status.innerHTML = opcoes(Object.keys(STATUS), (s) => s, (s) => STATUS[s], 'Todos');
    filtros.elements.bairro.innerHTML = opcoes(bairros, (b) => b.id, (b) => b.nome, 'Todos');
    filtros.elements.categoria.innerHTML = opcoes(categorias, (c) => c.id, (c) => c.nome, 'Todas');
    const bairroUrl = parametroUrl('bairro');
    if (bairroUrl) filtros.elements.bairro.value = bairroUrl;
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
  await carregarLista();
}

iniciar();
