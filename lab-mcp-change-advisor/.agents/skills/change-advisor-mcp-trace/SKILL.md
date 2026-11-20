---
name: change-advisor-mcp-trace
description: 변경 요청 센터의 새 샘플 화면을 연결하거나 Spring MCP 호출 추적, 소스 줄 번호, 분석 제안의 근거를 변경할 때 사용한다.
---

# 화면 추적과 MCP 소스 계약

1. 대상 화면의 폼이나 버튼에서 브라우저 핸들러를 찾아 요청 URL과 파라미터를 확인한다. 같은 URL의 컨트롤러와 서비스 로직을 따라간다.
2. `sample-app`의 `ScreenTraceTool`에서 해당 화면의 앵커를 관리한다. 실제 코드 문자열을 앵커로 사용하고 동일 문자열이 먼저 등장하는지 확인한다. 파일 추가 시 `sample-app/build.gradle`의 `processResources`에 해당 소스가 포함되는지 확인한다.
3. 화면 경로가 바뀌면 `TicketController`의 입력 검증, 현업 화면의 선택 항목, `common.js`의 화면 이름, `AnalysisWorker`에서 사용하는 경로를 점검한다. MCP 결과 필드를 바꾸면 서버 출력과 워커의 역직렬화를 함께 갱신한다.
4. 해당 화면으로 문의를 보내 개발자 응답의 `evidence`를 확인한다. 경로는 저장소 루트 기준이며, 줄 범위는 양쪽 끝을 포함한다. 인용 코드가 실제 파일의 해당 줄과 일치해야 한다.
5. 근거를 현업에 노출하거나 런타임에서 임의의 파일을 읽도록 범위를 확대하는 변경이 있는지 확인한다. 자세한 실행 흐름은 [스모크 검증](../change-advisor-smoke-test/SKILL.md)을 참고한다.
