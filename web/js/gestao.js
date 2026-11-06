/* Gestão de cadastros (só servidores): bairros, órgãos, categorias, servidores e lista de cidadãos.
   Os quatro primeiros usam o mesmo CRUD genérico. */

const sessao = exigirPerfil('SERVIDOR');

function crudSimples({ form, lista, mensagem, caminho, nome, regras, montarCorpo, preencher, linha, aoMudar }) {
  const botaoSalvar = form.querySelector('[type=submit]');
  const botaoCancelar = form.querySelector('[data-cancelar]');
  const textoSalvar = botaoSalvar.textContent;
  let itens = [];
  validacaoAoDigitar(form, regras);

  function editar(item) {
    limparErros(form);
    limparMensagem(mensagem);
    form.reset();
    form.elements.id.value = item ? item.id : '';
    if (item) preencher(item);
    botaoSalvar.textContent = item ? 'Salvar alterações' : textoSalvar;
    botaoCancelar.classList.toggle('oculto', !item);
    if (item) form.querySelector('input:not([type=hidden])').focus();
  }

  async function carregar() {
    try {
      itens = await api(caminho);
      lista.innerHTML = itens.length ? itens.map(linha).join('') : '<tr><td colspan="4">Nada cadastrado ainda.</td></tr>';
      if (aoMudar) aoMudar(itens);
    } catch (e) {
      mostrarMensagem(mensagem, e.message, 'erro');
    }
  }

  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    limparMensagem(mensagem);
    if (!validarFormulario(form, regras)) return;
    const id = form.elements.id.value;
    try {
      await comBotaoOcupado(botaoSalvar, 'Salvando…', () => (id
        ? api(caminho + '/' + id, { metodo: 'PUT', corpo: montarCorpo() })
        : api(caminho, { metodo: 'POST', corpo: montarCorpo() })));
      editar(null);
      mostrarMensagem(mensagem, id ? nome + ' atualizado(a).' : nome + ' cadastrado(a).');
      await carregar();
    } catch (e) {
      mostrarMensagem(mensagem, e.message, 'erro');
    }
  });

  botaoCancelar.addEventListener('click', () => editar(null));

  lista.addEventListener('click', async (ev) => {
    const botaoEditar = ev.target.closest('[data-editar]');
    const botaoExcluir = ev.target.closest('[data-excluir]');
    if (botaoEditar) editar(itens.find((i) => i.id === Number(botaoEditar.dataset.editar)));
    if (botaoExcluir) {
      const item = itens.find((i) => i.id === Number(botaoExcluir.dataset.excluir));
      if (!confirm('Excluir "' + item.nome + '"?')) return;
      try {
        await api(caminho + '/' + item.id, { metodo: 'DELETE' });
        mostrarMensagem(mensagem, nome + ' excluído(a).');
        if (form.elements.id.value === String(item.id)) editar(null);
        await carregar();
      } catch (e) {
        mostrarMensagem(mensagem, e.message, 'erro');
      }
    }
  });

  carregar();
  return { carregar };
}

function botoesAcao(id) {
  return '<td data-rotulo="Ações"><div class="acoes">'
    + '<button class="botao botao--secundario botao--pequeno" type="button" data-editar="' + id + '">Editar</button>'
    + '<button class="botao botao--perigo botao--pequeno" type="button" data-excluir="' + id + '">Excluir</button>'
    + '</div></td>';
}

/* Os selects de órgão (em categorias e servidores) acompanham o cadastro de órgãos */
function atualizarSelectsDeOrgao(orgaos) {
  ['categoria-orgao', 'servidor-orgao'].forEach((id) => {
    const select = document.getElementById(id);
    const atual = select.value;
    select.innerHTML = opcoes(orgaos, (g) => g.id, (g) => g.nome + ' (' + g.sigla + ')', 'Selecione…');
    select.value = atual;
  });
}

if (sessao) {
  const formBairro = document.getElementById('form-bairro');
  formBairro.elements.zona.innerHTML = opcoes(Object.keys(ZONAS), (z) => z, (z) => ZONAS[z], 'Selecione…');
  crudSimples({
    form: formBairro,
    lista: document.getElementById('lista-bairros'),
    mensagem: 'mensagem-bairro',
    caminho: '/bairros',
    nome: 'Bairro',
    regras: {
      nome: [Regras.obrigatorio('o nome do bairro'), Regras.minimo(2), Regras.maximo(80)],
      zona: [Regras.selecionado('a zona')],
      cidade: [Regras.obrigatorio('a cidade'), Regras.minimo(2), Regras.maximo(60)]
    },
    montarCorpo: () => ({
      nome: formBairro.elements.nome.value.trim(),
      zona: formBairro.elements.zona.value,
      cidade: formBairro.elements.cidade.value.trim()
    }),
    preencher: (b) => {
      formBairro.elements.nome.value = b.nome;
      formBairro.elements.zona.value = b.zona;
      formBairro.elements.cidade.value = b.cidade;
    },
    linha: (b) => '<tr><td data-rotulo="Bairro"><a href="ocorrencias.html?bairro=' + b.id + '">' + esc(b.nome) + '</a></td>'
      + '<td data-rotulo="Zona">' + esc(ZONAS[b.zona]) + '</td><td data-rotulo="Cidade">' + esc(b.cidade) + '</td>'
      + botoesAcao(b.id) + '</tr>'
  });

  const formOrgao = document.getElementById('form-orgao');
  crudSimples({
    form: formOrgao,
    lista: document.getElementById('lista-orgaos'),
    mensagem: 'mensagem-orgao',
    caminho: '/orgaos',
    nome: 'Órgão',
    regras: {
      nome: [Regras.obrigatorio('o nome do órgão'), Regras.minimo(3), Regras.maximo(80)],
      sigla: [Regras.obrigatorio('a sigla'), Regras.minimo(2), Regras.maximo(10)],
      telefone: [Regras.telefone()]
    },
    montarCorpo: () => ({
      nome: formOrgao.elements.nome.value.trim(),
      sigla: formOrgao.elements.sigla.value.trim(),
      telefone: formOrgao.elements.telefone.value.trim()
    }),
    preencher: (g) => {
      formOrgao.elements.nome.value = g.nome;
      formOrgao.elements.sigla.value = g.sigla;
      formOrgao.elements.telefone.value = g.telefone || '';
    },
    linha: (g) => '<tr><td data-rotulo="Órgão"><strong>' + esc(g.nome) + '</strong></td>'
      + '<td data-rotulo="Sigla">' + esc(g.sigla) + '</td><td data-rotulo="Telefone">' + esc(g.telefone || '—') + '</td>'
      + botoesAcao(g.id) + '</tr>',
    aoMudar: atualizarSelectsDeOrgao
  });

  const formCategoria = document.getElementById('form-categoria');
  crudSimples({
    form: formCategoria,
    lista: document.getElementById('lista-categorias'),
    mensagem: 'mensagem-categoria',
    caminho: '/categorias',
    nome: 'Categoria',
    regras: {
      nome: [Regras.obrigatorio('o nome da categoria'), Regras.minimo(3), Regras.maximo(60)],
      prazoDias: [Regras.inteiroEntre(1, 365)],
      idOrgao: [Regras.selecionado('o órgão responsável')],
      descricao: [Regras.maximo(200)]
    },
    montarCorpo: () => ({
      nome: formCategoria.elements.nome.value.trim(),
      prazoDias: Number(formCategoria.elements.prazoDias.value),
      idOrgao: Number(formCategoria.elements.idOrgao.value),
      descricao: formCategoria.elements.descricao.value.trim()
    }),
    preencher: (c) => {
      formCategoria.elements.nome.value = c.nome;
      formCategoria.elements.prazoDias.value = c.prazoDias;
      formCategoria.elements.idOrgao.value = c.orgao.id;
      formCategoria.elements.descricao.value = c.descricao || '';
    },
    linha: (c) => '<tr><td data-rotulo="Categoria"><strong>' + esc(c.nome) + '</strong>'
      + (c.descricao ? '<br><small>' + esc(c.descricao) + '</small>' : '') + '</td>'
      + '<td data-rotulo="Órgão">' + esc(c.orgao.sigla) + '</td>'
      + '<td data-rotulo="Prazo" class="numero">' + dias(c.prazoDias) + '</td>' + botoesAcao(c.id) + '</tr>'
  });

  const formServidor = document.getElementById('form-servidor');
  crudSimples({
    form: formServidor,
    lista: document.getElementById('lista-servidores'),
    mensagem: 'mensagem-servidor',
    caminho: '/servidores',
    nome: 'Servidor',
    regras: {
      nome: [Regras.obrigatorio('o nome'), Regras.minimo(3), Regras.maximo(100)],
      email: [Regras.obrigatorio('o e-mail'), Regras.email(), Regras.maximo(120)],
      telefone: [Regras.telefone()],
      matricula: [Regras.obrigatorio('a matrícula'), Regras.minimo(3), Regras.maximo(20)],
      cargo: [Regras.obrigatorio('o cargo'), Regras.minimo(3), Regras.maximo(60)],
      idOrgao: [Regras.selecionado('o órgão')]
    },
    montarCorpo: () => ({
      nome: formServidor.elements.nome.value.trim(),
      email: formServidor.elements.email.value.trim(),
      telefone: formServidor.elements.telefone.value.trim(),
      matricula: formServidor.elements.matricula.value.trim(),
      cargo: formServidor.elements.cargo.value.trim(),
      idOrgao: Number(formServidor.elements.idOrgao.value)
    }),
    preencher: (s) => {
      formServidor.elements.nome.value = s.nome;
      formServidor.elements.email.value = s.email;
      formServidor.elements.telefone.value = s.telefone || '';
      formServidor.elements.matricula.value = s.matricula;
      formServidor.elements.cargo.value = s.cargo;
      formServidor.elements.idOrgao.value = s.orgao.id;
    },
    linha: (s) => '<tr><td data-rotulo="Servidor"><strong>' + esc(s.nome) + '</strong>'
      + (s.id === sessao.id ? ' <span class="etiqueta status-RESOLVIDA">você</span>' : '')
      + '<br><small>' + esc(s.cargo) + ' · ' + esc(s.matricula) + '</small></td>'
      + '<td data-rotulo="Órgão">' + esc(s.orgao.sigla) + '</td>'
      + '<td data-rotulo="Contato">' + esc(s.email) + (s.telefone ? '<br><small>' + esc(s.telefone) + '</small>' : '') + '</td>'
      + botoesAcao(s.id) + '</tr>'
  });

  api('/cidadaos').then((cidadaos) => {
    document.getElementById('lista-cidadaos').innerHTML = cidadaos.map((c) => '<tr>'
      + '<td data-rotulo="Nome"><strong>' + esc(c.nome) + '</strong></td>'
      + '<td data-rotulo="Bairro">' + esc(c.bairro.nome) + '</td>'
      + '<td data-rotulo="Contato">' + esc(c.email) + (c.telefone ? '<br><small>' + esc(c.telefone) + '</small>' : '') + '</td>'
      + '<td data-rotulo="Cadastro">' + formatarData(c.dataCadastro) + '</td></tr>').join('');
  }).catch((e) => {
    document.getElementById('lista-cidadaos').innerHTML = '<tr><td colspan="4">' + esc(e.message) + '</td></tr>';
  });
}
