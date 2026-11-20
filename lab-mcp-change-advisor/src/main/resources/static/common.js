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

export const screenNames = {
  '/contracts.html': '계약 조회',
  '/quote.html': '보험료 계산'
};

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

export async function request(url, body) {
  const options = body === undefined ? {} : {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(body)
  };
  const response = await fetch(url, options);
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.detail || '요청을 처리할 수 없습니다. 다시 시도해 주세요.');
  }
  return response.json();
}

export function showStatus(element, text, failed = false) {
  element.classList.toggle('error', failed);
  element.textContent = text;
}
