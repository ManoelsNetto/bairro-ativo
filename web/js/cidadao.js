/* Área do cidadão: registrar/editar ocorrências, meu cadastro, minhas ocorrências e as que apoio.
   O autor é sempre o cidadão conectado. */

const sessao = exigirPerfil('CIDADAO');
const form = document.getElementById('form-ocorrencia');
const formCadastro = document.getElementById('form-cadastro');
let categorias = [];
let eu = null;
let minhas = [];

const regras = {
  titulo: [Regras.obrigatorio('o título'), Regras.minimo(5), Regras.maximo(100)],
  descricao: [Regras.obrigatorio('a descrição do problema'), Regras.minimo(10), Regras.maximo(1000)],
  endereco: [Regras.obrigatorio('o endereço'), Regras.minimo(5), Regras.maximo(150)],
  referencia: [Regras.maximo(150)],
  idBairro: [Regras.selecionado('o bairro')],
  idCategoria: [Regras.selecionado('a categoria')]
};
const regrasCadastro = {
  nome: [Regras.obrigatorio('o nome'), Regras.minimo(3), Regras.maximo(100)],
  email: [Regras.obrigatorio('o e-mail'), Regras.email(), Regras.maximo(120)],
  telefone: [Regras.telefone()],
  idBairro: [Regras.selecionado('o bairro onde mora')]
};
validacaoAoDigitar(form, regras);
validacaoAoDigitar(formCadastro, regrasCadastro);

/* ---------- Registrar / editar ocorrência ---------- */

function mostrarOrgaoDaCategoria() {
  const c = categorias.find((x) => String(x.id) === form.elements.idCategoria.value);
  document.getElementById('dica-categoria').textContent = c
    ? 'Atendida por ' + c.orgao.nome + ' · prazo de ' + dias(c.prazoDias) : '';
}
form.elements.idCategoria.addEventListener('change', mostrarOrgaoDaCategoria);

function modoFormulario(ocorrencia) {
  limparErros(form);
  limparMensagem('mensagem-form');
  form.reset();
  const editando = Boolean(ocorrencia);
  document.getElementById('titulo-formulario').textContent = editando ? 'Editar ocorrência' : 'Registrar ocorrência';
  document.getElementById('botao-salvar').textContent = editando ? 'Salvar alterações' : 'Registrar ocorrência';
  document.getElementById('botao-cancelar').classList.toggle('oculto', !editando);
  form.elements.id.value = editando ? ocorrencia.id : '';
  if (editando) {
    form.elements.titulo.value = ocorrencia.titulo;
    form.elements.descricao.value = ocorrencia.descricao;
    form.elements.endereco.value = ocorrencia.endereco;
    form.elements.referencia.value = ocorrencia.referencia || '';
    form.elements.idBairro.value = ocorrencia.bairro.id;
    form.elements.idCategoria.value = ocorrencia.categoria.id;
  } else if (eu) {
    form.elements.idBairro.value = eu.bairro.id;
  }
  mostrarOrgaoDaCategoria();
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
    idCategoria: Number(form.elements.idCategoria.value)
  };
  try {
    await comBotaoOcupado(document.getElementById('botao-salvar'), 'Salvando…', () => (id
      ? api('/ocorrencias/' + id, { metodo: 'PUT', corpo })
      : api('/ocorrencias', { metodo: 'POST', corpo })));
    modoFormulario(null);
    if (location.search.includes('editar')) history.replaceState(null, '', 'cidadao.html');
    mostrarMensagem('mensagem', id ? 'Ocorrência atualizada.' : 'Ocorrência registrada! Agora os vizinhos já podem apoiá-la.');
    await carregarListas();
    document.getElementById('titulo-minhas').scrollIntoView({ behavior: 'smooth' });
  } catch (e) {
    mostrarMensagem('mensagem-form', e.message, 'erro');
  }
});

document.getElementById('botao-cancelar').addEventListener('click', () => {
  history.replaceState(null, '', 'cidadao.html');
  modoFormulario(null);
});

/* ---------- Minhas ocorrências / que apoio ---------- */

async function excluirOcorrencia(id) {
  const o = minhas.find((x) => x.id === id);
  if (!confirm('Excluir a ocorrência "' + o.titulo + '"?')) return;
  try {
    await api('/ocorrencias/' + id, { metodo: 'DELETE' });
    mostrarMensagem('mensagem', 'Ocorrência excluída.');
    if (form.elements.id.value === String(id)) modoFormulario(null);
    await carregarListas();
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
}

function acoesMinha(o) {
  return (o.status === 'ABERTA' ? '<button class="botao botao--secundario botao--pequeno" type="button" data-editar="'
    + o.id + '">Editar</button>' : '')
    + (o.status === 'ABERTA' || o.status === 'CANCELADA'
      ? '<button class="botao botao--perigo botao--pequeno" type="button" data-excluir="' + o.id + '">Excluir</button>' : '');
}

async function carregarListas() {
  try {
    const [lista, apoiadas] = await Promise.all([api('/ocorrencias/minhas'), api('/ocorrencias/apoiadas')]);
    minhas = lista;
    document.getElementById('contagem-minhas').textContent = minhas.length === 1 ? '1 registrada' : minhas.length + ' registradas';
    document.getElementById('lista-minhas').innerHTML = minhas.length
      ? minhas.map((o) => cartaoOcorrencia(o, acoesMinha(o))).join('')
      : '<li class="vazio">Você ainda não registrou nenhuma ocorrência. Use o formulário acima.</li>';
    document.getElementById('lista-apoiadas').innerHTML = apoiadas.length
      ? apoiadas.map((o) => cartaoOcorrencia(o)).join('')
      : '<li class="vazio">Você ainda não apoiou nenhuma ocorrência.</li>';
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
}

document.getElementById('lista-minhas').addEventListener('click', (ev) => {
  const editar = ev.target.closest('[data-editar]');
  const excluir = ev.target.closest('[data-excluir]');
  if (editar) {
    modoFormulario(minhas.find((o) => o.id === Number(editar.dataset.editar)));
    document.getElementById('nova').scrollIntoView({ behavior: 'smooth' });
    form.elements.titulo.focus({ preventScroll: true });
  }
  if (excluir) excluirOcorrencia(Number(excluir.dataset.excluir));
});

/* ---------- Meu cadastro ---------- */

function preencherCadastro() {
  formCadastro.elements.nome.value = eu.nome;
  formCadastro.elements.email.value = eu.email;
  formCadastro.elements.telefone.value = eu.telefone || '';
  formCadastro.elements.idBairro.value = eu.bairro.id;
  document.getElementById('saudacao').textContent = 'Olá, ' + eu.nome.split(' ')[0]
    + '! Você mora em ' + eu.bairro.nome + '. Registre problemas do seu bairro e acompanhe o que você apoia.';
}

formCadastro.addEventListener('submit', async (ev) => {
  ev.preventDefault();
  limparMensagem('mensagem-cadastro');
  if (!validarFormulario(formCadastro, regrasCadastro)) return;
  try {
    eu = await comBotaoOcupado(formCadastro.querySelector('[type=submit]'), 'Salvando…', () =>
      api('/cidadaos/' + sessao.id, {
        metodo: 'PUT',
        corpo: {
          nome: formCadastro.elements.nome.value.trim(),
          email: formCadastro.elements.email.value.trim(),
          telefone: formCadastro.elements.telefone.value.trim(),
          idBairro: Number(formCadastro.elements.idBairro.value)
        }
      }));
    salvarSessao(eu);
    desenharPerfil();
    preencherCadastro();
    mostrarMensagem('mensagem-cadastro', 'Cadastro atualizado.');
  } catch (e) {
    mostrarMensagem('mensagem-cadastro', e.message, 'erro');
  }
});

document.getElementById('botao-excluir-conta').addEventListener('click', async () => {
  if (!confirm('Excluir sua conta? Seus apoios serão removidos. Essa ação não pode ser desfeita.')) return;
  try {
    await api('/cidadaos/' + sessao.id, { metodo: 'DELETE' });
    encerrarSessao();
    location.href = 'index.html';
  } catch (e) {
    mostrarMensagem('mensagem-cadastro', e.message, 'erro');
  }
});

/* ---------- Início ---------- */

async function iniciar() {
  if (!sessao) return;
  try {
    const [bairros, listaCategorias, cidadao] = await Promise.all([
      api('/bairros'), api('/categorias'), api('/cidadaos/' + sessao.id)]);
    categorias = listaCategorias;
    eu = cidadao;
    form.elements.idBairro.innerHTML = opcoes(bairros, (b) => b.id, (b) => b.nome, 'Selecione…');
    formCadastro.elements.idBairro.innerHTML = opcoes(bairros, (b) => b.id, (b) => b.nome, 'Selecione…');
    form.elements.idCategoria.innerHTML = opcoes(categorias, (c) => c.id, (c) => c.nome, 'Selecione…');
    preencherCadastro();
    modoFormulario(null);
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
    if (!sessaoAtual()) location.replace('entrar.html?perfil=CIDADAO');
    return;
  }
  await carregarListas();
  const editar = Number(parametroUrl('editar'));
  if (editar) {
    const o = minhas.find((x) => x.id === editar);
    if (o && o.status === 'ABERTA') modoFormulario(o);
  }
  if (parametroUrl('excluida')) mostrarMensagem('mensagem', 'Ocorrência excluída.');
  if (location.hash === '#nova') {
    document.getElementById('nova').scrollIntoView();
    form.elements.titulo.focus({ preventScroll: true });
  }
}

iniciar();
