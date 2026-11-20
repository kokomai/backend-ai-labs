const result = document.querySelector('#result');
const won = new Intl.NumberFormat('ko-KR');

function bindForm(id, calculate) {
  document.querySelector(id)?.addEventListener('submit', async (event) => {
    event.preventDefault();
    result.textContent = '조회 중…';
    try {
      result.textContent = await calculate(new FormData(event.currentTarget));
    } catch {
      result.textContent = '결과를 조회할 수 없습니다. 입력값을 확인해 주세요.';
    }
  });
}

async function lookup(path, params) {
  const response = await fetch(path + '?' + new URLSearchParams(params));
  if (!response.ok) throw new Error('조회 실패');
  return response.json();
}

bindForm('#contract-form', async (data) => {
  const contract = await lookup('/api/contracts', {id: data.get('id')});
  return contract.product + ' · ' + contract.status + ' · 월 ' +
    won.format(contract.monthlyPremium) + '원';
});

bindForm('#quote-form', async (data) => {
  const quote = await lookup('/api/quote', {
    age: data.get('age'),
    bundled: data.has('bundled')
  });
  return '기본 보험료 ' + won.format(quote.basePremium) + '원 · 결합 할인 ' +
    quote.discountPercent + '% · 월 ' + won.format(quote.monthlyPremium) + '원';
});
