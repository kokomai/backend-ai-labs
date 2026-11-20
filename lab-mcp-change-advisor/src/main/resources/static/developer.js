import {analysisStates, formatTime, node, request, screenNames, showStatus, states} from './common.js';

const list = document.querySelector('#developer-tickets');
const detail = document.querySelector('#ticket-detail');
const analysis = document.querySelector('#ticket-analysis');
const action = document.querySelector('#ticket-action');
const message = document.querySelector('#developer-message');
const api = '/api/developer/tickets';
let selected = null;
let listRevision = 0;
let detailRevision = 0;
let lastList = '';
let lastDetail = '';
let lastAnalysis = '';
let lastAction = '';

async function refreshList() {
  const current = ++listRevision;
  const tickets = await request(api);
  if (current !== listRevision) return;
  const snapshot = JSON.stringify([tickets, selected]);
  if (snapshot === lastList) return;
  lastList = snapshot;
  if (!tickets.length) {
    list.replaceChildren(node('p', '접수된 문의가 없습니다.', 'hint'));
    return;
  }
  list.replaceChildren(...tickets.map((ticket) => {
    const button = node('button', null, 'ticket-button');
    button.setAttribute('aria-pressed', String(ticket.id === selected));
    button.append(
      node('strong', ticket.title),
      node('span', states[ticket.state] + ' · ' + analysisStates[ticket.analysisState], 'meta')
    );
    button.addEventListener('click', () => {
      if (selected === ticket.id) return;
      selected = ticket.id;
      if (ticket.id !== detail.dataset.ticket) {
        detail.replaceChildren(node('p', '문의를 불러오는 중…', 'hint'));
        detail.dataset.ticket = '';
        analysis.replaceChildren();
        action.replaceChildren();
        lastDetail = lastAnalysis = lastAction = '';
      }
      refresh();
    });
    return button;
  }));
}

async function refreshDetail() {
  if (!selected) return;
  const current = ++detailRevision;
  const ticket = await request(api + '/' + selected);
  if (current !== detailRevision || ticket.id !== selected) return;

  const detailSnapshot = JSON.stringify([ticket.id, ticket.state, ticket.assignee, ticket.reply]);
  if (detailSnapshot !== lastDetail) {
    lastDetail = detailSnapshot;
    detail.dataset.ticket = ticket.id;
    detail.replaceChildren(
      node('h2', ticket.title),
      node('span', states[ticket.state], 'badge'),
      node('p', screenNames[ticket.screenPath] + ' · ' + formatTime(ticket.createdAt), 'meta'),
      node('p', ticket.request, 'prose')
    );
    if (ticket.assignee) detail.append(node('p', '담당자 ' + ticket.assignee, 'meta'));
    if (ticket.reply) {
      const reply = node('div', null, 'section');
      reply.append(node('h3', '현업에 보낸 답변'), node('p', ticket.reply, 'prose'));
      detail.append(reply);
    }
  }

  const analysisSnapshot = JSON.stringify([ticket.id, ticket.analysisState, ticket.analysis]);
  if (analysisSnapshot !== lastAnalysis) {
    lastAnalysis = analysisSnapshot;
    renderAnalysis(ticket);
  }

  const actionSnapshot = JSON.stringify([ticket.id, ticket.state]);
  if (actionSnapshot !== lastAction) {
    lastAction = actionSnapshot;
    renderAction(ticket);
  }
}

function renderAnalysis(ticket) {
  const section = node('section', null, 'section');
  section.append(node('h3', '소스 분석'), node('span', analysisStates[ticket.analysisState], 'badge'));
  if (ticket.analysis) {
    const result = ticket.analysis;
    section.append(
      node('p', result.mode === 'LLM' ? 'LLM 제안 · MCP 소스 근거' : '규칙 기반 제안 · MCP 소스 근거', 'meta'),
      node('p', result.proposal, 'prose')
    );
    for (const evidence of result.evidence) {
      const snippet = node('details');
      const title = node('summary');
      title.append(
        node('strong', evidence.layer + ' '),
        node('code', evidence.path + ':' + evidence.startLine + '–' + evidence.endLine)
      );
      const lines = evidence.code.split('\n').map((line, index) =>
        String(evidence.startLine + index).padStart(3) + '  ' + line
      );
      snippet.append(title, node('pre', lines.join('\n')));
      section.append(snippet);
    }
    const retry = node('button', '다시 분석', 'secondary');
    retry.addEventListener('click', () => submit(ticket.id, 'analysis', {}, retry));
    section.append(retry);
  }
  analysis.replaceChildren(section);
}

function renderAction(ticket) {
  action.replaceChildren();
  if (ticket.state === 'RESOLVED') return;
  const claim = ticket.state === 'OPEN';
  const name = claim ? 'assignee' : 'reply';
  const field = node(claim ? 'input' : 'textarea');
  field.name = name;
  field.maxLength = claim ? 60 : 3000;
  field.required = true;
  if (!claim) field.rows = 4;
  const label = node('label', claim ? '담당자 이름' : '현업에 보낼 답변');
  label.append(field);
  const button = node('button', claim ? '문의 맡기' : '답변 등록', 'primary');
  const form = node('form', null, 'section');
  form.append(label, button);
  form.addEventListener('submit', (event) => {
    event.preventDefault();
    submit(ticket.id, claim ? 'claim' : 'reply', Object.fromEntries(new FormData(form)), button);
  });
  action.append(form);
}

async function submit(id, endpoint, body, button) {
  button.disabled = true;
  showStatus(message, '');
  try {
    await request(api + '/' + id + '/' + endpoint, body);
    await refresh();
  } catch (error) {
    showStatus(message, error.message, true);
  } finally {
    button.disabled = false;
  }
}

async function refresh() {
  try {
    await Promise.all([refreshList(), refreshDetail()]);
  } catch (error) {
    showStatus(message, error.message, true);
  }
}

refresh();
setInterval(refresh, 2500);
