import {formatTime, node, request, screenNames, showStatus, states} from './common.js';

const form = document.querySelector('#create-ticket');
const message = document.querySelector('#form-message');
const list = document.querySelector('#business-tickets');
const screen = document.querySelector('#screen');
const sampleLink = document.querySelector('#sample-link');
const sampleHome = sampleLink.href;
const storageKey = 'change-advisor-tickets';
const suppliedId = new URLSearchParams(location.search).get('ticket');
const uuid = /^[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}$/i;
const stored = JSON.parse(localStorage.getItem(storageKey) || '[]');
const ids = new Set([suppliedId, ...stored].filter((id) => uuid.test(id || '')));
let lastRender = '';
let revision = 0;

screen.addEventListener('change', updateSampleLink);

function updateSampleLink() {
  sampleLink.href = new URL(screen.value || '/', sampleHome).href;
}

async function refresh() {
  const current = ++revision;
  const results = await Promise.allSettled(
    [...ids].map((id) => request('/api/business/tickets/' + id))
  );
  if (current !== revision) return;
  const tickets = results.filter((result) => result.status === 'fulfilled').map((result) => result.value);
  if (tickets.some((ticket) => ticket.id === suppliedId)) {
    localStorage.setItem(storageKey, JSON.stringify([...new Set([suppliedId, ...stored])]));
  }
  const failed = tickets.length !== results.length;
  const snapshot = JSON.stringify([tickets, failed]);
  if (snapshot === lastRender) return;
  lastRender = snapshot;
  const cards = tickets.map(renderTicket);
  if (!cards.length && !failed) cards.push(node('p', '아직 접수된 문의가 없습니다.', 'hint'));
  if (failed) cards.push(node('p', '일부 문의를 조회할 수 없습니다.', 'error'));
  list.replaceChildren(...cards);
}

function renderTicket(ticket) {
  const article = node('article', null, 'ticket-card');
  const link = node('a', '문의 링크');
  link.href = '/?ticket=' + ticket.id;
  article.append(
    node('span', states[ticket.state], 'badge'),
    node('h3', ticket.title),
    node('p', screenNames[ticket.screenPath] + ' · ' + formatTime(ticket.createdAt), 'meta'),
    node('p', ticket.request, 'prose'),
    link
  );
  if (ticket.reply) {
    const reply = node('div', null, 'section');
    reply.append(node('h3', '개발자 답변'), node('p', ticket.reply, 'prose'));
    article.append(reply);
  }
  return article;
}

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  const button = form.querySelector('button');
  button.disabled = true;
  showStatus(message, '접수 중…');
  try {
    const ticket = await request('/api/business/tickets', Object.fromEntries(new FormData(form)));
    const known = JSON.parse(localStorage.getItem(storageKey) || '[]');
    const updated = [...new Set([ticket.id, ...known])];
    localStorage.setItem(storageKey, JSON.stringify(updated));
    stored.splice(0, stored.length, ...updated);
    ids.add(ticket.id);
    form.reset();
    updateSampleLink();
    showStatus(message, '문의를 접수했습니다.');
    await refresh();
  } catch (error) {
    showStatus(message, error.message, true);
  } finally {
    button.disabled = false;
  }
});

refresh();
setInterval(refresh, 4500);
