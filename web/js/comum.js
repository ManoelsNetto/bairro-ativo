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

/**
 * Chama a API e devolve o JSON. Em caso de erro lança Error com a mensagem
 * vinda do backend (ou uma mensagem amigável se o servidor não responder).
 */
async function api(caminho, { metodo = 'GET', corpo } = {}) {
  let resposta;
  try {
    resposta = await fetch(API + caminho, {
      method: metodo,
      headers: corpo !== undefined ? { 'Content-Type': 'application/json' } : {},
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

/* ---------- Menu (igual em todas as páginas) ---------- */

document.addEventListener('DOMContentLoaded', () => {
  const pagina = window.location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.menu a').forEach((link) => {
    const destino = link.getAttribute('href');
    if (destino === pagina || (pagina === 'ocorrencia.html' && destino === 'ocorrencias.html')) {
      link.setAttribute('aria-current', 'page');
    }
  });

  const botao = document.querySelector('.menu-botao');
  const menu = document.getElementById('menu-principal');
  if (botao && menu) {
    botao.addEventListener('click', () => {
      const aberto = menu.classList.toggle('aberto');
      botao.setAttribute('aria-expanded', String(aberto));
    });
  }
});
