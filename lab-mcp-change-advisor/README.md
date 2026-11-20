# Spring MCP 변경 요청 센터

현업이 샘플 보험 앱의 화면과 변경 요청을 선택해 1:1 문의를 접수합니다. 개발자 문의함에는 자동 분석 결과와 화면 → 브라우저 요청 → API → 서비스의 소스 경로, 줄 번호, 코드가 표시됩니다. 개발자가 문의를 맡아 답변하면 현업 화면에서 답변을 확인할 수 있습니다.

## 실행

저장소 루트에서 Java 21로 실행합니다. 각 명령은 별도 터미널에서 실행합니다. 샘플 앱은 보험 업무 화면과 API를 실행합니다. 소스 분석은 에이전트 안의 MCP 서버에서 실행하고, 보험 업무를 직접 시연할 때 샘플 앱을 실행합니다.

```bash
./gradlew :lab-mcp-change-advisor:sample-app:bootRun
```

```bash
./gradlew :lab-mcp-change-advisor:bootRun
```

| 화면 | 주소 |
| --- | --- |
| 로그인 · 현업 문의 | [localhost:9090](http://localhost:9090/) |
| 개발자 문의함 | [localhost:9090/developer.html](http://localhost:9090/developer.html) |
| 분석 대상 샘플 | [localhost:8092](http://localhost:8092/) |

Codespaces에서는 Ports 탭의 **Change advisor**와 **Insurance sample** 항목에 표시되는 브라우저 주소를 사용합니다. 개발 컨테이너 설정에 두 포트가 등록되어 있습니다.

## 로그인과 내장 DB

| 역할 | 아이디 | 초기 비밀번호 |
| --- | --- | --- |
| 현업 | `biz` | `1234` |
| 개발자 | `dev` | `1234` |

로그인하면 역할에 맞는 화면이 열립니다. 현업은 로그인 계정으로 접수한 문의와 첨부를 볼 수 있습니다. 개발자는 전체 문의, 소스 분석, 첨부를 확인하고 본인 계정으로 담당을 맡습니다. 이전 버전에서 생성한 문의는 `biz` 계정으로 연결됩니다.

계정, 역할, 문의와 첨부는 별도 DB 서버 없이 에이전트에 포함된 H2에 저장됩니다. Spring Security가 비밀번호를 BCrypt 해시로 저장합니다. 기본 데이터 파일은 에이전트 프로세스의 작업 디렉터리 아래 `data/change-advisor.mv.db`에 생성됩니다. `ADVISOR_DB_URL`로 위치를 바꿀 수 있습니다. 새 DB의 초기 계정 비밀번호는 `ADVISOR_BIZ_PASSWORD`와 `ADVISOR_DEV_PASSWORD`로 지정합니다. 초기 계정이 이미 있으면 저장된 비밀번호를 사용합니다.

## 예시

1. 샘플 앱의 **보험료 계산**에서 만 40세, 결합 할인 적용으로 조회합니다. 현재 할인율은 10%입니다.
2. **현업**에서 대상 화면으로 **보험료 계산**을 선택하고, 제목에 “결합 할인율 변경”, 변경 요청에 “결합 할인을 적용하면 할인율이 15%가 되도록 변경해 주세요.”를 입력합니다.
3. 로그아웃한 뒤 **dev**로 로그인합니다. 개발자 문의함에서 문의를 열면 자동 분석 결과가 나타납니다. `InsuranceService.calculateQuote`의 할인율 설정 줄과 앞단의 화면, 브라우저 요청, API를 확인할 수 있습니다.
4. **문의 맡기**를 선택하고 답변을 등록합니다. 다시 **biz**로 로그인하면 내 문의에 답변이 나타납니다. 개발자는 기존 분석을 다시 실행할 수 있습니다.

## 레거시 시연 화면

| 화면 | 사용 예 | 문의 예시 |
| --- | --- | --- |
| 계약 조회 | 유지 계약 1001과 만료 계약 1002의 상품·보험료를 조회합니다. | 월 보험료 표시 형식을 바꿔 주세요. |
| 보험료 계산 | 만 40세에 결합 할인을 적용해 기본 보험료와 할인율을 확인합니다. | 결합 할인율을 15%로 올려 주세요. |
| 계약 변경 시뮬레이션 | 계약 1001의 확장 보장을 선택해 월 할증과 변경 적용일을 확인합니다. | 확장 보장 할증을 7,000원으로 변경해 주세요. |
| 사고 접수 | 계약 1001, 사고일, 사고 내용을 입력하고 접수 번호를 받습니다. | 사고 접수 기간을 30일에서 45일로 늘려 주세요. |
| 보상 진행 조회 | 방금 생성한 번호나 기존 샘플 CLM-1001·CLM-1002를 조회합니다. | 검토 예정일을 접수 후 7일로 변경해 주세요. |

사고 접수는 메모리 저장소에 기록되며 재시작하면 샘플 데이터로 초기화됩니다. 계약 변경 시뮬레이션은 예상 보험료와 적용일을 계산합니다. 유지 계약 검사는 계약 변경과 사고 접수 양쪽에서 공유합니다.

## MCP 및 모델 설정

| 설정 | 파일 | 키 |
| --- | --- | --- |
| MCP 서버와 내부 클라이언트 | [에이전트 application.yml](src/main/resources/application.yml) | `spring.ai.mcp.server` 및 `spring.ai.mcp.client.streamable-http.connections.advisor` |
| MCP 도구 정의 | [ScreenTraceTool.java](src/main/java/com/example/changeadvisor/ScreenTraceTool.java) | `@McpTool(name = "trace_screen")` |
| 모델과 API 키 | [application-llm.yml](src/main/resources/application-llm.yml) | `ADVISOR_MODEL`, `OPENAI_API_KEY` |

MCP 서버는 에이전트의 **`/mcp`**에서 Streamable HTTP로 동작합니다. 분석 워커는 같은 앱의 루프백 주소와 서버 포트로 접속합니다. 추적 가능한 화면 목록은 MCP 리소스 `context://legacy/screens`에서 읽을 수 있고, 현업의 문의 화면도 같은 목록을 사용합니다. 앱 내부 연결에는 시작 시 생성한 토큰을 사용합니다. 외부 MCP 클라이언트를 연결할 때는 `ADVISOR_MCP_TOKEN`에 지정한 값을 `Authorization: Bearer <token>` 헤더로 보냅니다.

## 분석 모드

기본 모드에서는 API 키 없이 Spring MCP로 소스를 조회하고, 샘플 규칙에 따라 수정 후보와 확인 방법을 제시합니다. 화면과 코드 근거는 에이전트의 `trace_screen` 도구에서 가져옵니다. 에이전트 빌드가 샘플 앱의 분석 대상 소스를 에이전트 JAR에 포함합니다. 도구는 화면별로 등록된 앵커를 사용해 화면 → 브라우저 요청 → 컨트롤러 → 서비스 → 업무 규칙·저장소의 코드와 줄 번호를 읽습니다.

LLM 모드에서는 Spring AI가 문의 본문과 MCP 소스 일부를 OpenAI 모델에 전달해 제안을 생성합니다. 환경에 `OPENAI_API_KEY`를 설정한 후 실행합니다. `ADVISOR_MODEL`로 모델 이름을 바꿀 수 있습니다.

```bash
SPRING_PROFILES_ACTIVE=llm ./gradlew :lab-mcp-change-advisor:bootRun
```

분석은 저장된 문의를 순차 처리합니다. 실패한 분석은 개발자 화면에서 다시 실행할 수 있습니다. 에이전트가 재시작되면 진행 중이던 분석은 대기 상태로 돌아갑니다.

[Spring AI MCP 클라이언트](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html) · [Spring AI MCP 서버](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)

## 첨부 파일

문의 작성 화면에서 PNG, JPEG, PDF, UTF-8 TXT를 최대 3개까지 첨부할 수 있습니다. 파일당 한도는 5MB입니다. 현업과 개발자 화면에서 첨부를 내려받을 수 있고 PNG와 JPEG는 미리 보기가 표시됩니다. 문의와 첨부는 같은 데이터베이스에 저장됩니다.

`llm` 프로필을 켜면 문의에 첨부한 PNG와 JPEG도 분석 입력으로 OpenAI에 전달됩니다. PDF와 TXT는 개발자가 다운로드해 확인할 수 있습니다.

브라우저에서는 파일 선택기를 사용합니다. `file://` URI는 파일이 있는 컴퓨터에서 Node.js 22 이상의 제출 도구로 처리합니다. 제출 도구가 로컬 파일을 읽어 `multipart/form-data`로 전송합니다.

```bash
node lab-mcp-change-advisor/scripts/submit-ticket.mjs \
  --url http://localhost:9090 \
  --screen /quote.html \
  --title "보험료 화면 변경" \
  --request "첨부 화면의 결합 할인 문구를 바꿔 주세요." \
  --attachment 'file:///absolute/path/screenshot.png'
```

`--attachment`는 반복할 수 있으며 일반 로컬 파일 경로도 받습니다. 제출 대상은 해당 컴퓨터에서 접근할 수 있는 앱 HTTP(S) 주소입니다. 제출 도구는 `biz`로 로그인하며 기본 비밀번호를 바꿨다면 같은 `ADVISOR_BIZ_PASSWORD` 값을 도구 실행 환경에 설정합니다.

## 배포 산출물과 SBOM

저장소 루트에서 Gradle Wrapper로 두 앱을 빌드합니다.

```bash
./gradlew :lab-mcp-change-advisor:build :lab-mcp-change-advisor:sample-app:build
```

| 앱 | 실행 JAR | SBOM |
| --- | --- | --- |
| 변경 요청 센터 | `lab-mcp-change-advisor/build/libs/lab-mcp-change-advisor.jar` | `lab-mcp-change-advisor/build/reports/cyclonedx-direct/bom.{json,xml}` |
| 보험 업무 샘플 | `lab-mcp-change-advisor/sample-app/build/libs/sample-app.jar` | `lab-mcp-change-advisor/sample-app/build/reports/cyclonedx-direct/bom.{json,xml}` |

Java 21이 있는 배포 환경에서 각 실행 JAR를 별도 프로세스로 실행합니다. 다음 예시는 두 JAR를 같은 디렉터리에 복사해 같은 호스트에서 띄우는 경우입니다.

```bash
java -jar sample-app.jar
```

```bash
ADVISOR_DB_URL=jdbc:h2:file:/opt/change-advisor/data/tickets java -jar lab-mcp-change-advisor.jar
```

H2 경로는 앱 프로세스가 쓸 수 있는 영속 디렉터리로 바꿉니다. 기본 포트는 문의 앱 9090, 샘플 앱 8092입니다. 네트워크에서 접근 가능한 주소에 바인딩해야 하면 `CHANGE_ADVISOR_BIND_ADDRESS`를 지정합니다. 배포 시에는 새 DB의 초기 데모 비밀번호를 환경변수로 바꾸고 HTTPS로 접속하도록 구성합니다.

각 SBOM은 해당 앱의 Gradle 런타임 의존성으로 생성됩니다. [GitHub Actions 빌드](../.github/workflows/change-advisor-build.yml)는 성공한 빌드의 실행 JAR와 SBOM JSON·XML을 앱별 아티팩트로 보관합니다. [CycloneDX Gradle Plugin](https://github.com/CycloneDX/cyclonedx-gradle-plugin)

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
