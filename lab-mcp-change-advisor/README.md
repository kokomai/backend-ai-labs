# Spring MCP 변경 요청 센터

현업이 샘플 보험 앱의 화면과 변경 요청을 선택해 1:1 문의를 접수합니다. 개발자 문의함에는 자동 분석 결과와 화면 → 브라우저 요청 → API → 서비스의 소스 경로, 줄 번호, 코드가 표시됩니다. 개발자가 문의를 맡아 답변하면 현업 화면에서 답변을 확인할 수 있습니다.

## 실행

저장소 루트에서 Java 21로 실행합니다. 각 명령은 별도 터미널에서 실행합니다.

```bash
./gradlew :lab-mcp-change-advisor:sample-app:bootRun
```

```bash
./gradlew :lab-mcp-change-advisor:bootRun
```

| 화면 | 주소 |
| --- | --- |
| 현업 문의 | [localhost:8091](http://localhost:8091/) |
| 개발자 문의함 | [localhost:8091/developer.html](http://localhost:8091/developer.html) |
| 분석 대상 샘플 | [localhost:8092](http://localhost:8092/) |

문의는 에이전트 모듈의 `data/` 아래 H2 파일에 저장됩니다. 현업 화면은 이 브라우저에서 접수한 문의와 문의 링크로 연 내역을 표시합니다. 이 실습은 로컬에서 실행하며, 현업과 개발자 화면은 역할별 사용 흐름을 보여 줍니다. 운영 환경에서는 사용자 인증과 문의별 접근 제어를 추가해야 합니다.

## 예시

1. 샘플 앱의 **보험료 계산**에서 만 40세, 결합 할인 적용으로 조회합니다. 현재 할인율은 10%입니다.
2. **현업**에서 대상 화면으로 **보험료 계산**을 선택하고, 제목에 “결합 할인율 변경”, 변경 요청에 “결합 할인을 적용하면 할인율이 15%가 되도록 변경해 주세요.”를 입력합니다.
3. **개발자**에서 문의를 열면 자동 분석 결과가 나타납니다. `InsuranceService.calculateQuote`의 할인율 설정 줄과 앞단의 화면, 브라우저 요청, API를 확인할 수 있습니다.
4. 담당자 이름을 입력해 문의를 맡고 답변을 등록합니다. **현업**의 내 문의에 답변이 나타납니다. 담당자는 기존 분석을 다시 실행할 수 있습니다.

다른 예로 **계약 조회**에서 상품명과 월 보험료의 표시 문구 변경을 요청할 수 있습니다. 샘플 계약 번호는 1001과 1002입니다.

## 분석 모드

기본 모드에서는 API 키 없이 Spring MCP로 소스를 조회하고, 샘플 규칙에 따라 수정 후보와 확인 방법을 제시합니다. 화면과 코드 근거는 샘플 앱의 `trace_screen` 도구에서 가져옵니다. 도구는 샘플 화면별 호출 흐름을 매핑하고, 빌드 시 패키징된 소스에서 근거 코드와 줄 번호를 읽습니다.

LLM 모드에서는 Spring AI가 문의 본문과 MCP 소스 일부를 OpenAI 모델에 전달해 제안을 생성합니다. 환경에 `OPENAI_API_KEY`를 설정한 후 실행합니다. `ADVISOR_MODEL`로 모델 이름을 바꿀 수 있습니다.

```bash
SPRING_PROFILES_ACTIVE=llm ./gradlew :lab-mcp-change-advisor:bootRun
```

분석은 저장된 문의를 순차 처리합니다. 실패한 분석은 개발자 화면에서 다시 실행할 수 있습니다. 에이전트가 재시작되면 진행 중이던 분석은 대기 상태로 돌아갑니다.

[Spring AI MCP 클라이언트](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html) · [Spring AI MCP 서버](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)

## 프로젝트 운영

[프로젝트 현황과 백로그](PROJECT.md)에서 목표, MVP 검증 내역, 다음 작업 후보를 확인합니다. [에이전트 작업 규칙](AGENTS.md)에 상태 전이와 역할별 응답 계약을 정리했습니다.

Codex의 작업 디렉터리를 `lab-mcp-change-advisor`로 열면 프로젝트 스킬을 사용할 수 있습니다. 저장소 루트에서 CLI를 쓰는 경우 `codex -C lab-mcp-change-advisor`로 시작합니다.

| Codex 에이전트 | 담당 |
| --- | --- |
| [`change_advisor_product`](../.codex/agents/change_advisor_product.toml) | 요구사항과 수용 기준, 백로그 |
| [`change_advisor_mcp`](../.codex/agents/change_advisor_mcp.toml) | Spring MCP, 분석 워커, API와 저장 |
| [`change_advisor_ui`](../.codex/agents/change_advisor_ui.toml) | 현업 화면, 개발자 화면, 보험 샘플 UI |
| [`change_advisor_qa`](../.codex/agents/change_advisor_qa.toml) | 독립 리뷰와 수용 기준 검증 |

| 프로젝트 스킬 | 사용 시점 |
| --- | --- |
| [`$change-advisor-planning`](.agents/skills/change-advisor-planning/SKILL.md) | 요구사항 정리와 백로그 갱신 |
| [`$change-advisor-mcp-trace`](.agents/skills/change-advisor-mcp-trace/SKILL.md) | 새 화면 연결과 소스 경로·줄 번호 검증 |
| [`$change-advisor-smoke-test`](.agents/skills/change-advisor-smoke-test/SKILL.md) | 문의 접수부터 답변 확인까지 검증 |
| [`$change-advisor-systematic-debugging`](.agents/skills/change-advisor-systematic-debugging/SKILL.md) | 실패 재현과 원인 분석 |
| [`$change-advisor-verify-completion`](.agents/skills/change-advisor-verify-completion/SKILL.md) | 검증 결과를 확인하고 완료 보고 |

디버깅과 완료 검증 스킬은 [obra/superpowers의 고정 리비전](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7)을 이 프로젝트의 흐름에 맞게 적용했습니다. 두 스킬 폴더에 원본 MIT 라이선스를 포함했습니다.

요청 예시 `$change-advisor-planning으로 CA-01의 수용 기준을 정리하고, change_advisor_qa 에이전트에게 독립 검토를 맡겨줘.`

[Codex 커스텀 에이전트](https://developers.openai.com/codex/subagents) · [Codex 스킬 탐색](https://developers.openai.com/codex/skills)
