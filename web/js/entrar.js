/* Entrar: escolha do perfil (cidadão ou servidor) com contas de exemplo */

const destinoVolta = parametroUrl('volta');
const perfilPedido = parametroUrl('perfil');

function irDepoisDeEntrar(usuario) {
  salvarSessao(usuario);
  const padrao = usuario.tipo === 'CIDADAO' ? 'cidadao.html' : 'servidor.html';
  const volta = destinoVolta && /^[a-z]+\.html/.test(destinoVolta) ? destinoVolta : padrao;
  // só volta para a página pedida se ela for do perfil que acabou de entrar
  const paginaDoOutroPerfil = (usuario.tipo === 'CIDADAO' && /^(servidor|gestao)\.html/.test(volta))
    || (usuario.tipo === 'SERVIDOR' && /^cidadao\.html/.test(volta));
  location.href = paginaDoOutroPerfil ? padrao : volta;
}

async function entrar(perfil, email, mensagem) {
  limparMensagem(mensagem);
  try {
    const usuario = await api('/sessao', { metodo: 'POST', corpo: { perfil, email } });
    irDepoisDeEntrar(usuario);
  } catch (e) {
    mostrarMensagem(mensagem, e.message, 'erro');
  }
}

function configurarFormulario(idForm, perfil, mensagem) {
  const form = document.getElementById(idForm);
  const regras = { email: [Regras.obrigatorio('o e-mail'), Regras.email()] };
  validacaoAoDigitar(form, regras);
  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    if (!validarFormulario(form, regras)) return;
    await comBotaoOcupado(form.querySelector('[type=submit]'), 'Entrando…',
      () => entrar(perfil, form.elements.email.value.trim(), mensagem));
  });
}

configurarFormulario('form-cidadao', 'CIDADAO', 'mensagem-cidadao');
configurarFormulario('form-servidor', 'SERVIDOR', 'mensagem-servidor');

document.getElementById('exemplo-cidadao').addEventListener('click', (ev) =>
  comBotaoOcupado(ev.currentTarget, 'Entrando…', () => entrar('CIDADAO', 'ana.souza@email.com', 'mensagem-cidadao')));
document.getElementById('exemplo-servidor').addEventListener('click', (ev) =>
  comBotaoOcupado(ev.currentTarget, 'Entrando…',
    () => entrar('SERVIDOR', 'beatriz.nogueira@prefeitura.sp.gov.br', 'mensagem-servidor')));

/* ---------- Criar conta de cidadão ---------- */

const formCadastro = document.getElementById('form-cadastro');
const botaoCriar = document.getElementById('botao-criar-conta');
const regrasCadastro = {
  nome: [Regras.obrigatorio('o nome'), Regras.minimo(3), Regras.maximo(100)],
  email: [Regras.obrigatorio('o e-mail'), Regras.email(), Regras.maximo(120)],
  telefone: [Regras.telefone()],
  idBairro: [Regras.selecionado('o bairro onde mora')]
};
validacaoAoDigitar(formCadastro, regrasCadastro);

botaoCriar.addEventListener('click', () => {
  const aberto = formCadastro.classList.toggle('oculto') === false;
  botaoCriar.setAttribute('aria-expanded', String(aberto));
  if (aberto) formCadastro.elements.nome.focus();
});

formCadastro.addEventListener('submit', async (ev) => {
  ev.preventDefault();
  limparMensagem('mensagem-cadastro');
  if (!validarFormulario(formCadastro, regrasCadastro)) return;
  try {
    const cidadao = await comBotaoOcupado(formCadastro.querySelector('[type=submit]'), 'Criando conta…', () =>
      api('/cidadaos', {
        metodo: 'POST',
        corpo: {
          nome: formCadastro.elements.nome.value.trim(),
          email: formCadastro.elements.email.value.trim(),
          telefone: formCadastro.elements.telefone.value.trim(),
          idBairro: Number(formCadastro.elements.idBairro.value)
        }
      }));
    irDepoisDeEntrar(cidadao);
  } catch (e) {
    mostrarMensagem('mensagem-cadastro', e.message, 'erro');
  }
});

/* ---------- Contas de exemplo ---------- */

function botaoConta(conta) {
  return '<li><button type="button" data-perfil="' + conta.perfil + '" data-email="' + esc(conta.email) + '">'
    + '<span>' + esc(conta.nome) + '<small>' + esc(conta.descricao) + '</small></span>'
    + '<span aria-hidden="true">Entrar →</span></button></li>';
}

async function iniciar() {
  const sessao = sessaoAtual();
  if (parametroUrl('trocar') && sessao) {
    mostrarMensagem('mensagem', 'Esta área é de outro perfil. Você está como ' + sessao.nome
      + '. Entre com uma conta de ' + (perfilPedido === 'SERVIDOR' ? 'servidor público' : 'cidadão') + '.', 'info');
  } else if (perfilPedido && !sessao) {
    mostrarMensagem('mensagem', 'Entre como ' + (perfilPedido === 'SERVIDOR' ? 'servidor público' : 'cidadão')
      + ' para continuar.', 'info');
  }
  const alvo = document.getElementById(perfilPedido === 'SERVIDOR' ? 'email-servidor' : 'email-cidadao');
  if (perfilPedido) alvo.focus();

  try {
    const [bairros, contas] = await Promise.all([api('/bairros'), api('/sessao/exemplos')]);
    formCadastro.elements.idBairro.innerHTML = opcoes(bairros, (b) => b.id, (b) => b.nome, 'Selecione…');
    document.getElementById('exemplos-cidadao').innerHTML = contas.filter((c) => c.perfil === 'CIDADAO').map(botaoConta).join('');
    document.getElementById('exemplos-servidor').innerHTML = contas.filter((c) => c.perfil === 'SERVIDOR').map(botaoConta).join('');
  } catch (e) {
    mostrarMensagem('mensagem', e.message, 'erro');
  }
}

document.querySelector('main').addEventListener('click', (ev) => {
  const botao = ev.target.closest('.contas-exemplo button');
  if (!botao) return;
  const mensagem = botao.dataset.perfil === 'CIDADAO' ? 'mensagem-cidadao' : 'mensagem-servidor';
  botao.disabled = true;
  entrar(botao.dataset.perfil, botao.dataset.email, mensagem).finally(() => { botao.disabled = false; });
});

iniciar();
