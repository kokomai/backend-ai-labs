const result = document.querySelector('#result');
const won = new Intl.NumberFormat('ko-KR');

function attachForm(id, operation) {
  document.querySelector(id)?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const button = form.querySelector('button');
    button.disabled = true;
    result.textContent = '처리 중…';
    try {
      result.textContent = await operation(new FormData(form));
    } catch (error) {
      result.textContent = error.message;
    } finally {
      button.disabled = false;
    }
  });
}

async function request(path, body) {
  const options = body ? {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(body)
  } : {};
  const response = await fetch(path, options);
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.detail || '요청을 처리할 수 없습니다. 입력값을 확인해 주세요.');
  return data;
}

attachForm('#contract-change-form', async (data) => {
  const query = new URLSearchParams(data);
  const quote = await request('/api/contract-changes/preview?' + query);
  return '현재 보험료 ' + won.format(quote.currentMonthlyPremium) + '원 → 예상 보험료 ' +
    won.format(quote.estimatedMonthlyPremium) + '원 · 적용 예정일 ' + quote.effectiveDate;
});

const accidentDate = document.querySelector('[name="accidentDate"]');
if (accidentDate) {
  accidentDate.max = new Date().toISOString().slice(0, 10);
  accidentDate.value = accidentDate.max;
}

attachForm('#claim-intake-form', async (data) => {
  const link = document.querySelector('#progress-link');
  link.hidden = true;
  const report = await request('/api/claims', {
    contractId: Number(data.get('contractId')),
    accidentDate: data.get('accidentDate'),
    description: data.get('description')
  });
  link.href = '/claim-progress.html?' + new URLSearchParams({id: report.id});
  link.hidden = false;
  return report.statusLabel + ' · 접수 번호 ' + report.id;
});

const claimId = new URLSearchParams(location.search).get('id');
const claimInput = document.querySelector('#claim-progress-form [name="id"]');
if (claimInput && claimId) claimInput.value = claimId;

attachForm('#claim-progress-form', async (data) => {
  const claim = await request('/api/claims/' + encodeURIComponent(data.get('id').trim()));
  const estimate = claim.expectedReviewOn ? ' · 검토 예정일 ' + claim.expectedReviewOn : '';
  return claim.id + ' · ' + claim.statusLabel + estimate + ' · ' + claim.nextAction;
});
