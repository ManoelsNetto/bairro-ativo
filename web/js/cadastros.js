/* Bairros e categorias: dois CRUDs simples com a mesma lógica */

function crudSimples({ form, lista, mensagem, caminho, nome, regras, montarCorpo, preencher, linha }) {
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
}

function botoesAcao(id) {
  return '<td data-rotulo="Ações"><div class="acoes">'
    + '<button class="botao botao--secundario botao--pequeno" type="button" data-editar="' + id + '">Editar</button>'
    + '<button class="botao botao--perigo botao--pequeno" type="button" data-excluir="' + id + '">Excluir</button>'
    + '</div></td>';
}

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
    descricao: [Regras.maximo(200)]
  },
  montarCorpo: () => ({
    nome: formCategoria.elements.nome.value.trim(),
    prazoDias: Number(formCategoria.elements.prazoDias.value),
    descricao: formCategoria.elements.descricao.value.trim()
  }),
  preencher: (c) => {
    formCategoria.elements.nome.value = c.nome;
    formCategoria.elements.prazoDias.value = c.prazoDias;
    formCategoria.elements.descricao.value = c.descricao || '';
  },
  linha: (c) => '<tr><td data-rotulo="Categoria"><strong>' + esc(c.nome) + '</strong>'
    + (c.descricao ? '<br><small>' + esc(c.descricao) + '</small>' : '') + '</td>'
    + '<td data-rotulo="Prazo" class="numero">' + dias(c.prazoDias) + '</td>' + botoesAcao(c.id) + '</tr>'
});
