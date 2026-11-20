export const states = {
  OPEN: '접수',
  IN_PROGRESS: '처리 중',
  RESOLVED: '답변 완료'
};

export const analysisStates = {
  QUEUED: '분석 대기',
  RUNNING: '분석 중',
  COMPLETE: '분석 완료',
  FAILED: '분석 실패'
};

const screenNames = new Map();
let screensRequest;

export function loadScreens() {
  screensRequest ??= request('/api/screens').then((screens) => {
    for (const screen of screens) screenNames.set(screen.path, screen.name);
    return screens;
  }).catch((error) => {
    screensRequest = undefined;
    throw error;
  });
  return screensRequest;
}

export function screenName(path) {
  return screenNames.get(path) ?? path;
}

const dateTime = new Intl.DateTimeFormat('ko-KR', {
  dateStyle: 'medium',
  timeStyle: 'short'
});

export function formatTime(value) {
  return dateTime.format(new Date(value));
}

export function node(tag, text, className) {
  const element = document.createElement(tag);
  if (text != null) element.textContent = text;
  if (className) element.className = className;
  return element;
}

let csrfRequest;

export async function request(url, body) {
  if (body === undefined) return sendRequest(url);
  csrfRequest ??= sendRequest('/api/csrf').catch((error) => {
    csrfRequest = undefined;
    throw error;
  });
  const csrf = await csrfRequest;
  return sendRequest(url, body, {[csrf.headerName]: csrf.token});
}

async function sendRequest(url, body, headers = {}) {
  let options = {};
  if (body instanceof FormData) {
    options = {method: 'POST', body, headers};
  } else if (body !== undefined) {
    options = {
      method: 'POST',
      headers: {...headers, 'Content-Type': 'application/json'},
      body: JSON.stringify(body)
    };
  }
  const codespace = location.hostname.endsWith('.app.github.dev');
  const signInMessage = 'Codespaces 인증이 필요합니다. 이 주소를 브라우저 새 탭에서 열어 GitHub에 로그인한 뒤 다시 시도해 주세요.';
  let response;
  try {
    response = await fetch(url, {...options, redirect: 'manual'});
  } catch {
    throw new Error('서버에 연결할 수 없습니다. 페이지를 연 주소와 네트워크 연결을 확인해 주세요.');
  }
  if (response.type === 'opaqueredirect') {
    throw new Error(codespace ? signInMessage : '다른 주소로 이동하라는 응답을 받았습니다. 화면을 다시 열어 주세요.');
  }
  if (response.status === 401) {
    throw new Error('앱 로그인 세션이 끝났습니다. 페이지를 다시 열고 로그인해 주세요.');
  }
  if (response.status === 403) {
    throw new Error('이 요청을 실행할 권한이 없거나 세션이 끝났습니다.');
  }
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.detail || '요청을 처리할 수 없습니다. 다시 시도해 주세요.');
  }
  return response.json();
}

export function renderAttachments(files, ticketUrl) {
  const group = node('section', null, 'section');
  const list = node('ul', null, 'attachments');
  group.append(node('h3', '첨부 파일'), list);
  for (const file of files) {
    const item = node('li');
    const link = node('a', file.filename);
    link.href = ticketUrl + '/attachments/' + encodeURIComponent(file.id);
    link.download = file.filename;
    item.append(link);
    if (file.mediaType === 'image/png' || file.mediaType === 'image/jpeg') {
      const image = node('img');
      image.src = link.href;
      image.alt = file.filename;
      image.loading = 'lazy';
      item.append(image);
    }
    list.append(item);
  }
  return group;
}

export function showStatus(element, text, failed = false) {
  element.classList.toggle('error', failed);
  element.textContent = text;
}
