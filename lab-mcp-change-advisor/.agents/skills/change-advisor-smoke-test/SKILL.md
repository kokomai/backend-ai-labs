---
name: change-advisor-smoke-test
description: 변경 요청 센터의 문의 접수, 개발자 분석, 담당자 배정·답변, Spring MCP 재시도나 저장을 수동 또는 API로 검증할 때 사용한다.
---

# 변경 요청 센터 스모크 검증

저장소 루트에서 두 모듈을 빌드한다.

```bash
./gradlew :lab-mcp-change-advisor:build :lab-mcp-change-advisor:sample-app:build
```

[README](../../../README.md)의 실행 명령으로 두 앱을 시작한다. 기존 문의 데이터와 분리해 테스트하려면 에이전트에 `ADVISOR_DB_URL=jdbc:h2:mem:advisor-smoke`를 설정한다. 재시작 보존을 확인할 때는 별도의 임시 파일 DB를 사용한다.

- 현업에서 `/quote.html`을 대상으로 “결합 할인을 15%로 변경” 문의를 등록한다. API 검증 시 `POST /api/business/tickets`에 `screenPath`, `title`, `request`를 보낸다. 응답은 HTTP 201이며 내부 분석 필드를 포함하지 않는다.
- 개발자 상세 `GET /api/developer/tickets/{id}`를 조회한다. 분석은 비동기이므로 `COMPLETE` 또는 `FAILED`까지 기다린다. 기본 모드에서 15% 제안과 서비스 근거가 나타나고 모든 `evidence` 줄 번호가 실제 소스와 일치하는지 확인한다.
- 같은 문의에 `POST /api/developer/tickets/{id}/claim`으로 `assignee`를 보내고, `POST /api/developer/tickets/{id}/reply`로 `reply`를 등록한다. `GET /api/business/tickets/{id}`에서 `RESOLVED` 상태와 답변을 확인한다.
- MCP 실패 처리가 변경되면 샘플 앱이 내려간 상태에서 문의를 등록해 분석이 `FAILED`가 되는지 확인한다. 샘플을 시작한 뒤 `POST /api/developer/tickets/{id}/analysis`에 빈 객체를 보내 재분석을 확인한다.
- 모델 연동 테스트는 모의 모델 연결과 실제 모델 응답의 품질 확인을 구분해서 기록한다. 브라우저 검증에서는 접수, 문의 선택, 담당자 입력과 답변 등록을 직접 수행하고 UI 갱신을 확인한다.
