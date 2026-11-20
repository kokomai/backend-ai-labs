import {formatTime, loadScreens, node, renderAttachments, request, screenName, showStatus, states} from './common.js';

const form = document.querySelector('#create-ticket');
const message = document.querySelector('#form-message');
const list = document.querySelector('#business-tickets');
const screen = document.querySelector('#screen');
const sampleLink = document.querySelector('#sample-link');
const sampleHome = new URL(sampleLink.href);
const codespaceHost = location.hostname.match(/^(.+)-[0-9]+(\.app\.github\.dev)$/);
if (codespaceHost) {
  sampleHome.protocol = 'https:';
  sampleHome.hostname = codespaceHost[1] + '-' + sampleHome.port + codespaceHost[2];
  sampleHome.port = '';
}
const suppliedId = new URLSearchParams(location.search).get('ticket');
let lastRender = '';
let revision = 0;

screen.addEventListener('change', updateSampleLink);
updateSampleLink();

function updateSampleLink() {
  sampleLink.href = new URL(screen.value || '/', sampleHome).href;
}

async function refresh() {
  const current = ++revision;
  try {
    const [tickets, availableScreens] = await Promise.all([
      request('/api/business/tickets'),
      loadScreens()
    ]);
    if (screen.options.length === 1) {
      screen.append(...availableScreens.map(({path, name}) => {
        const option = node('option', name);
        option.value = path;
        return option;
      }));
    }
    if (current !== revision) return;
    const snapshot = JSON.stringify(tickets);
    if (snapshot === lastRender) return;
    lastRender = snapshot;
    const linked = tickets.find((ticket) => ticket.id === suppliedId);
    const ordered = linked ? [linked, ...tickets.filter((ticket) => ticket !== linked)] : tickets;
    const cards = ordered.map(renderTicket);
    list.replaceChildren(...(cards.length ? cards : [node('p', '아직 접수된 문의가 없습니다.', 'hint')]));
  } catch (error) {
    if (current === revision) {
      lastRender = '';
      list.replaceChildren(node('p', error.message, 'error'));
    }
  }
}

function renderTicket(ticket) {
  const article = node('article', null, 'ticket-card');
  const link = node('a', '문의 링크');
  link.href = '/?ticket=' + ticket.id;
  article.append(
    node('span', states[ticket.state], 'badge'),
    node('h3', ticket.title),
    node('p', screenName(ticket.screenPath) + ' · ' + formatTime(ticket.createdAt), 'meta'),
    node('p', ticket.request, 'prose'),
    link
  );
  if (ticket.attachments?.length) {
    article.append(renderAttachments(ticket.attachments, '/api/business/tickets/' + ticket.id));
  }
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
    const payload = new FormData(form);
    const files = payload.getAll('attachments').filter((file) => file.name);
    if (files.length > 3 || files.some((file) => file.size === 0 || file.size > 5 * 1024 * 1024)) {
      throw new Error('비어 있지 않은 5MB 이하 파일을 최대 3개 첨부할 수 있습니다.');
    }
    payload.delete('attachments');
    for (const file of files) payload.append('attachments', file);
    await request('/api/business/tickets', payload);
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
