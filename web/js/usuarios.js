/* Usuários: CRUD de cidadãos e agentes públicos */

const form = document.getElementById('form-usuario');
let usuarios = [];
let filtroTipo = '';

function tipoSelecionado() {
  return form.querySelector('input[name=tipo]:checked').value;
}

function regras() {
  const base = {
    nome: [Regras.obrigatorio('o nome'), Regras.minimo(3), Regras.maximo(100)],
    email: [Regras.obrigatorio('o e-mail'), Regras.email(), Regras.maximo(120)],
    telefone: [Regras.telefone()]
  };
  if (tipoSelecionado() === 'CIDADAO') {
    base.idBairro = [Regras.selecionado('o bairro onde mora')];
  } else {
    base.orgao = [Regras.obrigatorio('o órgão'), Regras.minimo(3), Regras.maximo(80)];
    base.matricula = [Regras.obrigatorio('a matrícula'), Regras.minimo(3), Regras.maximo(20)];
  }
  return base;
}

form.addEventListener('input', (ev) => {
  const lista = regras()[ev.target.name];
  if (lista && ev.target.getAttribute('aria-invalid') === 'true') {
    marcarErro(ev.target, lista.map((r) => r(ev.target.value)).find(Boolean) || null);
  }
});

function alternarCampos() {
  const agente = tipoSelecionado() === 'AGENTE';
  document.getElementById('campos-agente').classList.toggle('oculto', !agente);
  document.getElementById('campo-bairro').classList.toggle('oculto', agente);
  limparErros(form);
}
form.querySelectorAll('input[name=tipo]').forEach((r) => r.addEventListener('change', alternarCampos));

function modoEdicao(usuario) {
  limparErros(form);
  limparMensagem('mensagem-form');
  form.reset();
  const editando = Boolean(usuario);
  document.getElementById('titulo-formulario').textContent = editando ? 'Editar usuário' : 'Novo usuário';
  document.getElementById('botao-salvar').textContent = editando ? 'Salvar alterações' : 'Cadastrar';
  document.getElementById('botao-cancelar').classList.toggle('oculto', !editando);
  form.querySelectorAll('input[name=tipo]').forEach((r) => { r.disabled = editando; });
  form.elements.id.value = editando ? usuario.id : '';
  if (editando) {
    form.querySelector('input[name=tipo][value=' + usuario.tipo + ']').checked = true;
    form.elements.nome.value = usuario.nome;
    form.elements.email.value = usuario.email;
    form.elements.telefone.value = usuario.telefone || '';
    if (usuario.tipo === 'CIDADAO') form.elements.idBairro.value = usuario.bairro.id;
    else {
      form.elements.orgao.value = usuario.orgao;
      form.elements.matricula.value = usuario.matricula;
    }
  }
  alternarCampos();
  if (editando) {
    form.scrollIntoView({ behavior: 'smooth', block: 'start' });
    form.elements.nome.focus({ preventScroll: true });
  }
}

form.addEventListener('submit', async (ev) => {
  ev.preventDefault();
  limparMensagem('mensagem-form');
  if (!validarFormulario(form, regras())) {
    mostrarMensagem('mensagem-form', 'Confira os campos destacados.', 'erro');
    return;
  }
  const id = form.elements.id.value;
  const tipo = tipoSelecionado();
  const corpo = {
    tipo,
    nome: form.elements.nome.value.trim(),
    email: form.elements.email.value.trim(),
    telefone: form.elements.telefone.value.trim(),
    idBairro: tipo === 'CIDADAO' ? Number(form.elements.idBairro.value) : null,
    orgao: tipo === 'AGENTE' ? form.elements.orgao.value.trim() : null,
    matricula: tipo === 'AGENTE' ? form.elements.matricula.value.trim() : null
  };
  try {
    await comBotaoOcupado(document.getElementById('botao-salvar'), 'Salvando…', () => (id
      ? api('/usuarios/' + id, { metodo: 'PUT', corpo })
      : api('/usuarios', { metodo: 'POST', corpo })));
    mostrarMensagem('mensagem', id ? 'Cadastro atualizado.' : 'Usuário cadastrado com sucesso.');
    modoEdicao(null);
    await carregar();
  } catch (e) {
    mostrarMensagem('mensagem-form', e.message, 'erro');
  }
});

document.getElementById('botao-cancelar').addEventListener('click', () => modoEdicao(null));

async function excluir(id) {
  const u = usuarios.find((x) => x.id === id);
  if (!confirm('Excluir o cadastro de ' + u.nome + '?')) return;
  try {
    await api('/usuarios/' + id, { metodo: 'DELETE' });
    mostrarMensagem('mensagem', 'Cadastro excluído.');
    if (form.elements.id.value === String(id)) modoEdicao(null);
    await carregar();
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
}

function linha(u) {
  const cidadao = u.tipo === 'CIDADAO';
  return '<tr>'
    + '<td data-rotulo="Nome"><strong>' + esc(u.nome) + '</strong></td>'
    + '<td data-rotulo="Perfil">' + (cidadao ? 'Cidadão' : 'Agente público') + '</td>'
    + '<td data-rotulo="Contato">' + esc(u.email) + (u.telefone ? '<br><small>' + esc(u.telefone) + '</small>' : '') + '</td>'
    + '<td data-rotulo="' + (cidadao ? 'Bairro' : 'Órgão') + '">'
    + (cidadao ? esc(u.bairro.nome) : esc(u.orgao) + '<br><small>Matrícula ' + esc(u.matricula) + '</small>') + '</td>'
    + '<td data-rotulo="Ações"><div class="acoes">'
    + '<button class="botao botao--secundario botao--pequeno" type="button" data-editar="' + u.id + '">Editar</button>'
    + '<button class="botao botao--perigo botao--pequeno" type="button" data-excluir="' + u.id + '">Excluir</button>'
    + '</div></td></tr>';
}

async function carregar() {
  try {
    usuarios = await api('/usuarios' + (filtroTipo ? '?tipo=' + filtroTipo : ''));
    document.getElementById('contagem').textContent = usuarios.length + (usuarios.length === 1 ? ' usuário' : ' usuários');
    document.getElementById('lista').innerHTML = usuarios.length
      ? usuarios.map(linha).join('')
      : '<tr><td colspan="5">Nenhum usuário cadastrado.</td></tr>';
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
}

document.getElementById('lista').addEventListener('click', (ev) => {
  const editar = ev.target.closest('[data-editar]');
  const remover = ev.target.closest('[data-excluir]');
  if (editar) modoEdicao(usuarios.find((u) => u.id === Number(editar.dataset.editar)));
  if (remover) excluir(Number(remover.dataset.excluir));
});

document.querySelectorAll('.abas button').forEach((botao) => botao.addEventListener('click', () => {
  document.querySelectorAll('.abas button').forEach((b) => b.setAttribute('aria-pressed', String(b === botao)));
  filtroTipo = botao.dataset.tipo;
  carregar();
}));

async function iniciar() {
  try {
    const bairros = await api('/bairros');
    form.elements.idBairro.innerHTML = opcoes(bairros, (b) => b.id, (b) => b.nome, 'Selecione…');
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
  alternarCampos();
  await carregar();
}

iniciar();
