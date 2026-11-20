---
name: change-advisor-smoke-test
description: 변경 요청 센터의 문의 접수, 개발자 분석, 담당자 배정·답변, Spring MCP 재시도나 저장을 수동 또는 API로 검증할 때 사용한다.
---

# 변경 요청 센터 스모크 검증

저장소 루트에서 두 모듈을 빌드한다.

```bash
./gradlew :lab-mcp-change-advisor:build :lab-mcp-change-advisor:sample-app:build
```

[README](../../../README.md)의 실행 명령으로 두 앱을 시작한다. 기존 문의 데이터와 분리해 테스트하려면 에이전트에 `ADVISOR_DB_URL=jdbc:h2:mem:advisor-smoke`를 설정한다. 재시작 보존을 확인할 때는 별도의 임시 파일 DB를 사용한다. 화면에서는 `biz`와 `dev` 계정으로 역할을 바꿔 테스트한다. API에서 쓰기 요청을 보낼 때는 해당 계정의 세션 쿠키와 `GET /api/csrf`의 토큰 헤더가 필요하다.

- 현업에서 `/quote.html`을 대상으로 “결합 할인을 15%로 변경” 문의를 등록한다. API 검증 시 `POST /api/business/tickets`에 `screenPath`, `title`, `request`를 보낸다. 응답은 HTTP 201이며 내부 분석 필드를 포함하지 않는다.
- 개발자 상세 `GET /api/developer/tickets/{id}`를 조회한다. 분석은 비동기이므로 `COMPLETE` 또는 `FAILED`까지 기다린다. 기본 모드에서 15% 제안과 서비스 근거가 나타나고 모든 `evidence` 줄 번호가 실제 소스와 일치하는지 확인한다.
- 추가 흐름을 볼 때는 `/claim-intake.html`에서 접수 기간을 45일로 늘리는 문의를 등록하고, 접수 정책 상수와 화면 안내 문구가 함께 수정 후보로 표시되는지 확인한다.
- 같은 문의에 개발자 세션으로 `POST /api/developer/tickets/{id}/claim`을 호출하고, `POST /api/developer/tickets/{id}/reply`로 `reply`를 등록한다. `GET /api/business/tickets/{id}`에서 `RESOLVED` 상태와 답변을 확인한다.
- 샘플 앱이 실행되지 않은 상태에서도 에이전트의 `/mcp`로 분석이 완료되는지 확인한다. MCP 실패 처리를 확인할 때는 에이전트 테스트 빌드의 소스 앵커를 의도적으로 불일치시켜 `FAILED`를 확인하고, 앵커를 복구한 빌드에서 `POST /api/developer/tickets/{id}/analysis`로 재분석을 확인한다.
- 모델 연동 테스트는 모의 모델 연결과 실제 모델 응답의 품질 확인을 구분해서 기록한다. 브라우저 검증에서는 로그인, 접수, 문의 선택, 담당 맡기와 답변 등록을 직접 수행하고 UI 갱신을 확인한다. 현업과 개발자 세션을 바꿔 반대 역할 API가 차단되는지 확인한다.
