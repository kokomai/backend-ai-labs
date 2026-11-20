# 변경 요청 센터 작업 규칙

- [PROJECT.md](PROJECT.md)에서 제품 범위, 현재 상태, 우선순위, 수용 기준을 확인한다. 제품 범위나 백로그 상태를 바꾸는 작업이면 해당 항목을 함께 갱신한다.
- 에이전트는 이 디렉터리의 Spring Boot 앱이다. `sample-app/`은 독립 실행되는 보험 샘플이자 MCP 서버다. Gradle 검증 명령은 저장소 루트에서 실행한다.
- 문의 처리 상태는 `OPEN → IN_PROGRESS → RESOLVED`다. 분석 상태는 `QUEUED → RUNNING → COMPLETE/FAILED`다. 각 상태가 독립적으로 동작하는지 확인한다.
- 현업 응답은 문의 내용, 처리 상태, 개발자 답변을 제공한다. 개발자 응답은 여기에 분석 상태, 제안, 소스 근거를 제공한다. 두 API의 응답 계약을 각각 검증한다.
- 화면 추적 근거는 샘플에 포함된 소스에서 재현할 수 있어야 한다. 화면 경로, 브라우저 요청, 컨트롤러, 서비스 또는 리소스 패키징이 바뀌면 연결된 근거도 확인한다.
- 기본 분석에는 API 키가 필요 없다. 모델 연동을 수정하는 경우 기본 모드와 `llm` 프로필의 검증 범위를 각각 기록한다.
- 리뷰에는 실행한 검증과 결과, 환경 제약을 남긴다. 반복 검증은 [$change-advisor-smoke-test](.agents/skills/change-advisor-smoke-test/SKILL.md)를 참고한다.

## Codex 역할

저장소의 `.codex/agents/`에 `change_advisor_product`, `change_advisor_mcp`, `change_advisor_ui`, `change_advisor_qa`를 정의한다. 여러 역할이 필요한 작업에서 위임 요청이 있으면 역할과 변경 범위를 먼저 나눈다. 한 사람이 처리하는 작은 작업에도 해당 역할의 체크포인트를 사용할 수 있다.
