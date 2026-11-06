/* Ocorrências: listagem com filtros + formulário de cadastro e edição */

const form = document.getElementById('form-ocorrencia');
const secaoForm = document.getElementById('secao-formulario');
const botaoNova = document.getElementById('botao-nova');
const filtros = document.getElementById('form-filtros');
let ocorrencias = [];

const regras = {
  titulo: [Regras.obrigatorio('o título'), Regras.minimo(5), Regras.maximo(100)],
  descricao: [Regras.obrigatorio('a descrição do problema'), Regras.minimo(10), Regras.maximo(1000)],
  endereco: [Regras.obrigatorio('o endereço'), Regras.minimo(5), Regras.maximo(150)],
  referencia: [Regras.maximo(150)],
  idBairro: [Regras.selecionado('o bairro')],
  idCategoria: [Regras.selecionado('a categoria')],
  idAutor: [Regras.selecionado('quem está registrando')]
};
validacaoAoDigitar(form, regras);

function abrirFormulario(ocorrencia) {
  limparErros(form);
  limparMensagem('mensagem-form');
  form.reset();
  const editando = Boolean(ocorrencia);
  document.getElementById('titulo-formulario').textContent = editando ? 'Editar ocorrência' : 'Registrar ocorrência';
  document.getElementById('botao-salvar').textContent = editando ? 'Salvar alterações' : 'Registrar ocorrência';
  form.elements.idAutor.disabled = editando;
  if (editando) {
    form.elements.id.value = ocorrencia.id;
    form.elements.titulo.value = ocorrencia.titulo;
    form.elements.descricao.value = ocorrencia.descricao;
    form.elements.endereco.value = ocorrencia.endereco;
    form.elements.referencia.value = ocorrencia.referencia || '';
    form.elements.idBairro.value = ocorrencia.bairro.id;
    form.elements.idCategoria.value = ocorrencia.categoria.id;
    form.elements.idAutor.value = ocorrencia.autor.id;
  } else {
    form.elements.id.value = '';
  }
  secaoForm.classList.remove('oculto');
  botaoNova.setAttribute('aria-expanded', 'true');
  secaoForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
  form.elements.titulo.focus({ preventScroll: true });
}

function fecharFormulario() {
  secaoForm.classList.add('oculto');
  botaoNova.setAttribute('aria-expanded', 'false');
  if (location.hash === '#nova') history.replaceState(null, '', location.pathname);
}

form.addEventListener('submit', async (ev) => {
  ev.preventDefault();
  limparMensagem('mensagem-form');
  if (!validarFormulario(form, regras)) {
    mostrarMensagem('mensagem-form', 'Confira os campos destacados.', 'erro');
    return;
  }
  const id = form.elements.id.value;
  const corpo = {
    titulo: form.elements.titulo.value.trim(),
    descricao: form.elements.descricao.value.trim(),
    endereco: form.elements.endereco.value.trim(),
    referencia: form.elements.referencia.value.trim(),
    idBairro: Number(form.elements.idBairro.value),
    idCategoria: Number(form.elements.idCategoria.value),
    idAutor: Number(form.elements.idAutor.value)
  };
  try {
    await comBotaoOcupado(document.getElementById('botao-salvar'), 'Salvando…', () => (id
      ? api('/ocorrencias/' + id, { metodo: 'PUT', corpo })
      : api('/ocorrencias', { metodo: 'POST', corpo })));
    fecharFormulario();
    mostrarMensagem('mensagem', id ? 'Ocorrência atualizada.' : 'Ocorrência registrada! Agora os vizinhos já podem apoiá-la.');
    await carregarLista();
  } catch (e) {
    mostrarMensagem('mensagem-form', e.message, 'erro');
  }
});

async function excluir(id) {
  const o = ocorrencias.find((x) => x.id === id);
  if (!confirm('Excluir a ocorrência "' + o.titulo + '"? Os apoios e o histórico também serão apagados.')) return;
  try {
    await api('/ocorrencias/' + id, { metodo: 'DELETE' });
    mostrarMensagem('mensagem', 'Ocorrência excluída.');
    await carregarLista();
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
}

function cartao(o) {
  const podeEditar = o.status === 'ABERTA';
  const podeExcluir = o.status === 'ABERTA' || o.status === 'CANCELADA';
  return '<li class="ocorrencia">'
    + '<div class="ocorrencia__topo">' + etiquetaStatus(o.status) + ' ' + etiquetaPrioridade(o) + '</div>'
    + '<h3><a href="ocorrencia.html?id=' + o.id + '">' + esc(o.titulo) + '</a></h3>'
    + '<p class="ocorrencia__meta">' + esc(o.endereco) + ' · ' + esc(o.bairro.nome) + '<br>'
    + esc(o.categoria.nome) + ' · aberta em ' + formatarData(o.dataAbertura) + '</p>'
    + '<p class="ocorrencia__numeros"><span><strong>' + o.totalApoios + '</strong> apoios</span>'
    + (o.pendente ? '<span><strong>' + dias(o.diasEmAberto) + '</strong> em aberto</span>'
      : o.dataResolucao ? '<span>resolvida em <strong>' + dias(o.diasEmAberto) + '</strong></span>' : '')
    + '</p>'
    + '<div class="ocorrencia__acoes">'
    + '<a class="botao botao--secundario botao--pequeno" href="ocorrencia.html?id=' + o.id + '">Detalhes</a>'
    + (podeEditar ? '<button class="botao botao--secundario botao--pequeno" type="button" data-editar="' + o.id
      + '">Editar</button>' : '')
    + (podeExcluir ? '<button class="botao botao--perigo botao--pequeno" type="button" data-excluir="' + o.id
      + '">Excluir</button>' : '')
    + '</div></li>';
}

async function carregarLista() {
  const parametros = new URLSearchParams();
  ['q', 'status', 'bairro', 'categoria'].forEach((nome) => {
    const valor = filtros.elements[nome].value.trim();
    if (valor) parametros.set(nome, valor);
  });
  const contagem = document.getElementById('contagem');
  contagem.textContent = 'Carregando…';
  try {
    ocorrencias = await api('/ocorrencias?' + parametros.toString());
    contagem.textContent = ocorrencias.length === 1 ? '1 ocorrência encontrada' : ocorrencias.length + ' ocorrências encontradas';
    document.getElementById('lista').innerHTML = ocorrencias.length
      ? ocorrencias.map(cartao).join('')
      : '<li class="vazio">Nenhuma ocorrência com esses filtros.</li>';
  } catch (e) {
    contagem.textContent = '';
    mostrarMensagem('mensagem', e.message, 'erro');
  }
}

document.getElementById('lista').addEventListener('click', (ev) => {
  const editar = ev.target.closest('[data-editar]');
  const remover = ev.target.closest('[data-excluir]');
  if (editar) abrirFormulario(ocorrencias.find((o) => o.id === Number(editar.dataset.editar)));
  if (remover) excluir(Number(remover.dataset.excluir));
});

botaoNova.addEventListener('click', () => abrirFormulario(null));
document.getElementById('botao-cancelar').addEventListener('click', fecharFormulario);

let esperaBusca;
filtros.addEventListener('input', () => {
  clearTimeout(esperaBusca);
  esperaBusca = setTimeout(carregarLista, 300);
});
filtros.addEventListener('submit', (ev) => { ev.preventDefault(); carregarLista(); });

async function iniciar() {
  try {
    const [bairros, categorias, cidadaos] = await Promise.all([
      api('/bairros'), api('/categorias'), api('/usuarios?tipo=CIDADAO')]);
    form.elements.idBairro.innerHTML = opcoes(bairros, (b) => b.id, (b) => b.nome, 'Selecione…');
    form.elements.idCategoria.innerHTML = opcoes(categorias, (c) => c.id, (c) => c.nome + ' (prazo ' + c.prazoDias + ' dias)', 'Selecione…');
    form.elements.idAutor.innerHTML = opcoes(cidadaos, (u) => u.id, (u) => u.nome + ' · ' + u.bairro.nome, 'Selecione…');
    filtros.elements.status.innerHTML = opcoes(Object.keys(STATUS), (s) => s, (s) => STATUS[s], 'Todos');
    filtros.elements.bairro.innerHTML = opcoes(bairros, (b) => b.id, (b) => b.nome, 'Todos');
    filtros.elements.categoria.innerHTML = opcoes(categorias, (c) => c.id, (c) => c.nome, 'Todas');
    const bairroUrl = parametroUrl('bairro');
    if (bairroUrl) filtros.elements.bairro.value = bairroUrl;
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
  await carregarLista();
  if (location.hash === '#nova') abrirFormulario(null);
}

iniciar();
