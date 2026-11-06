/* =========================================================
   Bairro Ativo - funções usadas por todas as páginas
   ========================================================= */

const API = '/api';

const STATUS = {
  ABERTA: 'Aberta',
  EM_ANALISE: 'Em análise',
  EM_ANDAMENTO: 'Em andamento',
  RESOLVIDA: 'Resolvida',
  CANCELADA: 'Cancelada'
};

const PRIORIDADE = { ALTA: 'Prioridade alta', MEDIA: 'Prioridade média', BAIXA: 'Prioridade baixa' };

const ZONAS = { CENTRO: 'Centro', NORTE: 'Norte', SUL: 'Sul', LESTE: 'Leste', OESTE: 'Oeste' };

/* ---------- Sessão (quem está usando a plataforma) ----------
   Guardada no navegador depois de "Entrar". Cada requisição envia o perfil e o id
   nos cabeçalhos X-Perfil e X-Usuario; o backend confere as permissões. */

const CHAVE_SESSAO = 'bairroAtivo.sessao';

function obterSessao() {
  try {
    const texto = localStorage.getItem(CHAVE_SESSAO);
    return texto ? JSON.parse(texto) : null;
  } catch (e) {
    return null;
  }
}

function salvarSessao(usuario) {
  const sessao = { perfil: usuario.tipo, id: usuario.id, nome: usuario.nome, descricao: usuario.descricaoPerfil };
  try {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
  } catch (e) {
    /* navegador sem armazenamento: a sessão vale só nesta página */
  }
  window._sessaoMemoria = sessao;
  return sessao;
}

function encerrarSessao() {
  try {
    localStorage.removeItem(CHAVE_SESSAO);
  } catch (e) {
    /* ignora */
  }
  window._sessaoMemoria = null;
}

function sessaoAtual() {
  return obterSessao() || window._sessaoMemoria || null;
}

/** Usado pelas áreas restritas: sem o perfil certo, manda para a tela Entrar e volta depois. */
function exigirPerfil(perfil) {
  const sessao = sessaoAtual();
  if (!sessao || sessao.perfil !== perfil) {
    const volta = encodeURIComponent(location.pathname.split('/').pop() + location.search + location.hash);
    location.replace('entrar.html?perfil=' + perfil + '&volta=' + volta + (sessao ? '&trocar=1' : ''));
    return null;
  }
  return sessao;
}

/**
 * Chama a API e devolve o JSON. Em caso de erro lança Error com a mensagem
 * vinda do backend (ou uma mensagem amigável se o servidor não responder).
 */
async function api(caminho, { metodo = 'GET', corpo } = {}) {
  let resposta;
  try {
    const cabecalhos = corpo !== undefined ? { 'Content-Type': 'application/json' } : {};
    const sessao = sessaoAtual();
    if (sessao) {
      cabecalhos['X-Perfil'] = sessao.perfil;
      cabecalhos['X-Usuario'] = String(sessao.id);
    }
    resposta = await fetch(API + caminho, {
      method: metodo,
      headers: cabecalhos,
      body: corpo !== undefined ? JSON.stringify(corpo) : undefined
    });
  } catch (e) {
    throw new Error('Não foi possível conectar ao servidor. Verifique se a aplicação está rodando.');
  }
  if (resposta.status === 204) return null;
  let dados = null;
  try {
    dados = await resposta.json();
  } catch (e) {
    dados = null;
  }
  if (!resposta.ok) {
    if (resposta.status === 401 && sessaoAtual() && dados && /Sessão inválida|Entre na plataforma/.test(dados.mensagem)) {
      encerrarSessao();
    }
    throw new Error((dados && dados.mensagem) || 'Erro inesperado (' + resposta.status + ').');
  }
  return dados;
}

/* ---------- Mensagens na página ---------- */

function mostrarMensagem(elemento, texto, tipo = 'sucesso') {
  const alvo = typeof elemento === 'string' ? document.getElementById(elemento) : elemento;
  if (!alvo) return;
  alvo.className = 'mensagem mensagem--' + tipo;
  alvo.setAttribute('role', tipo === 'erro' ? 'alert' : 'status');
  alvo.textContent = texto;
  if (tipo === 'sucesso') {
    clearTimeout(alvo._timer);
    alvo._timer = setTimeout(() => { alvo.textContent = ''; }, 6000);
  }
}

function limparMensagem(elemento) {
  const alvo = typeof elemento === 'string' ? document.getElementById(elemento) : elemento;
  if (alvo) alvo.textContent = '';
}

/* ---------- Validação de formulários ---------- */

const Regras = {
  obrigatorio: (rotulo) => (v) => (v.trim() === '' ? 'Informe ' + rotulo + '.' : null),
  selecionado: (rotulo) => (v) => (v === '' ? 'Selecione ' + rotulo + '.' : null),
  minimo: (n) => (v) => (v.trim() !== '' && v.trim().length < n ? 'Use pelo menos ' + n + ' caracteres.' : null),
  maximo: (n) => (v) => (v.trim().length > n ? 'Use no máximo ' + n + ' caracteres.' : null),
  email: () => (v) => (v.trim() !== '' && !/^[\w.+-]+@[\w-]+(\.[\w-]+)+$/.test(v.trim())
    ? 'Informe um e-mail válido, por exemplo nome@email.com.' : null),
  telefone: () => (v) => {
    if (v.trim() === '') return null;
    const digitos = v.replace(/\D/g, '').length;
    return /^[0-9()+\-\s]+$/.test(v) && digitos >= 10 && digitos <= 13
      ? null : 'Use DDD + número, por exemplo (11) 91234-5678.';
  },
  inteiroEntre: (min, max) => (v) => {
    const n = Number(v);
    return v.trim() === '' || !Number.isInteger(n) || n < min || n > max
      ? 'Informe um número inteiro entre ' + min + ' e ' + max + '.' : null;
  }
};

/**
 * Valida os campos do formulário conforme as regras ({ nomeDoCampo: [regra, ...] }).
 * Mostra a mensagem abaixo de cada campo e foca o primeiro inválido. Retorna true se tudo ok.
 */
function validarFormulario(form, regras) {
  let primeiroInvalido = null;
  Object.entries(regras).forEach(([nome, lista]) => {
    const campo = form.elements[nome];
    if (!campo) return;
    const valor = campo.value ?? '';
    let erro = null;
    for (const regra of lista) {
      erro = regra(valor);
      if (erro) break;
    }
    marcarErro(campo, erro);
    if (erro && !primeiroInvalido) primeiroInvalido = campo;
  });
  if (primeiroInvalido) primeiroInvalido.focus();
  return !primeiroInvalido;
}

function marcarErro(campo, erro) {
  const id = campo.id || campo.name;
  let saida = document.getElementById(id + '-erro');
  if (!saida && campo.parentElement) {
    saida = document.createElement('span');
    saida.id = id + '-erro';
    saida.className = 'erro-campo';
    campo.parentElement.appendChild(saida);
  }
  if (erro) {
    campo.setAttribute('aria-invalid', 'true');
    campo.setAttribute('aria-describedby', saida.id);
  } else {
    campo.removeAttribute('aria-invalid');
  }
  if (saida) saida.textContent = erro || '';
}

function limparErros(form) {
  form.querySelectorAll('[aria-invalid]').forEach((c) => c.removeAttribute('aria-invalid'));
  form.querySelectorAll('.erro-campo').forEach((e) => { e.textContent = ''; });
}

/** Limpa o erro do campo assim que o usuário corrige. */
function validacaoAoDigitar(form, regras) {
  form.addEventListener('input', (ev) => {
    const lista = regras[ev.target.name];
    if (!lista || ev.target.getAttribute('aria-invalid') !== 'true') return;
    const erro = lista.map((r) => r(ev.target.value)).find(Boolean);
    marcarErro(ev.target, erro || null);
  });
}

/* ---------- Formatação ---------- */

function esc(texto) {
  return String(texto ?? '').replace(/[&<>"']/g, (c) => (
    { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function formatarData(iso, comHora = false) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleDateString('pt-BR') + (comHora
    ? ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '');
}

function dias(n) {
  return n === 1 ? '1 dia' : n + ' dias';
}

function etiquetaStatus(status) {
  return '<span class="etiqueta status-' + status + '">' + esc(STATUS[status] || status) + '</span>';
}

function etiquetaPrioridade(o) {
  if (!o.pendente) return '';
  return '<span class="etiqueta prioridade-' + o.nivelPrioridade + '" title="Pontuação ' + o.pontuacaoPrioridade
    + '">' + PRIORIDADE[o.nivelPrioridade] + '</span>'
    + (o.atrasada ? ' <span class="etiqueta etiqueta--atraso">Fora do prazo</span>' : '');
}

function opcoes(lista, valor, rotulo, vazio) {
  return (vazio !== undefined ? '<option value="">' + esc(vazio) + '</option>' : '')
    + lista.map((item) => '<option value="' + esc(valor(item)) + '">' + esc(rotulo(item)) + '</option>').join('');
}

function parametroUrl(nome) {
  return new URLSearchParams(window.location.search).get(nome);
}

/** Desabilita o botão de envio enquanto a requisição está em andamento. */
async function comBotaoOcupado(botao, textoOcupado, acao) {
  const original = botao.textContent;
  botao.disabled = true;
  botao.textContent = textoOcupado;
  try {
    return await acao();
  } finally {
    botao.disabled = false;
    botao.textContent = original;
  }
}

/* ---------- Topo: menu (igual em todas as páginas) e perfil conectado ---------- */

function iniciais(nome) {
  const partes = String(nome || '?').trim().split(/\s+/);
  return (partes[0][0] + (partes.length > 1 ? partes[partes.length - 1][0] : '')).toUpperCase();
}

function desenharPerfil() {
  const area = document.getElementById('perfil-topo');
  const faixa = document.getElementById('faixa-perfil');
  const sessao = sessaoAtual();
  if (!area) return;
  if (!sessao) {
    area.innerHTML = '<a class="botao botao--destaque botao--pequeno" href="entrar.html">Entrar</a>';
    if (faixa) faixa.classList.add('oculto');
    return;
  }
  const cidadao = sessao.perfil === 'CIDADAO';
  const destino = cidadao ? 'cidadao.html' : 'servidor.html';
  area.innerHTML = '<a class="perfil-chip' + (cidadao ? '' : ' perfil-chip--servidor') + '" href="' + destino + '">'
    + '<span class="perfil-chip__avatar" aria-hidden="true">' + esc(iniciais(sessao.nome)) + '</span>'
    + '<span class="perfil-chip__texto">' + esc(sessao.nome) + '<small>' + esc(sessao.descricao || '') + '</small></span></a>'
    + '<button class="botao-sair" type="button" id="botao-sair">Sair</button>';
  document.getElementById('botao-sair').addEventListener('click', () => {
    encerrarSessao();
    location.href = 'index.html';
  });
  if (faixa) {
    faixa.className = 'faixa-perfil faixa-perfil--' + (cidadao ? 'cidadao' : 'servidor');
    faixa.innerHTML = cidadao
      ? 'Você está usando como <strong>cidadão</strong>. <a href="cidadao.html">Ir para a Área do cidadão</a>'
      : 'Você está usando como <strong>servidor público</strong>' + (sessao.descricao ? ' · ' + esc(sessao.descricao.replace(/^Servidor público · /, '')) : '')
        + '. <a href="servidor.html">Ir para a fila de atendimento</a>';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const pagina = window.location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.menu a').forEach((link) => {
    const destino = link.getAttribute('href');
    const mesmaArea = (pagina === 'ocorrencia.html' && destino === 'ocorrencias.html')
      || (pagina === 'gestao.html' && destino === 'servidor.html');
    if (destino === pagina || mesmaArea) {
      link.setAttribute('aria-current', 'page');
    }
  });
  desenharPerfil();

  const botao = document.querySelector('.menu-botao');
  const menu = document.getElementById('menu-principal');
  if (botao && menu) {
    botao.addEventListener('click', () => {
      const aberto = menu.classList.toggle('aberto');
      botao.setAttribute('aria-expanded', String(aberto));
    });
  }
});

/* ---------- Cartão de ocorrência (listas) ---------- */

function cartaoOcorrencia(o, acoesExtras = '') {
  return '<li class="ocorrencia">'
    + '<div class="ocorrencia__topo">' + etiquetaStatus(o.status) + ' ' + etiquetaPrioridade(o) + '</div>'
    + '<h3><a href="ocorrencia.html?id=' + o.id + '">' + esc(o.titulo) + '</a></h3>'
    + '<p class="ocorrencia__meta">' + esc(o.endereco) + ' · ' + esc(o.bairro.nome) + '<br>'
    + esc(o.categoria.nome) + ' · aberta em ' + formatarData(o.dataAbertura) + '</p>'
    + '<p class="ocorrencia__numeros"><span><strong>' + o.totalApoios + '</strong> apoios</span>'
    + (o.pendente ? '<span><strong>' + dias(o.diasEmAberto) + '</strong> em aberto</span>'
      : o.dataResolucao ? '<span>resolvida em <strong>' + dias(o.diasEmAberto) + '</strong></span>' : '')
    + '</p>'
    + '<div class="ocorrencia__acoes"><a class="botao botao--secundario botao--pequeno" href="ocorrencia.html?id='
    + o.id + '">Ver detalhes</a>' + acoesExtras + '</div></li>';
}
