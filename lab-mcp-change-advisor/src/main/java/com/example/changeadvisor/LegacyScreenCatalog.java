package com.example.changeadvisor;

import java.util.List;

final class LegacyScreenCatalog {
  private static final String LEGACY_JAVA = "java/com/example/advisorsample/";
  private static final String STATIC = "resources/static/";

  private static final List<ScreenDefinition> SCREENS =
      List.of(
          new ScreenDefinition(
              new Screen("/contracts.html", "계약 조회"),
              List.of(
                  html("contracts.html", 10),
                  script("insurance.js", "bindForm('#contract-form'", 5),
                  java("API", "InsuranceController.java", "@GetMapping(\"/api/contracts\")", 4),
                  java("계약 조회", "InsuranceService.java", "Contract findContract", 8)),
              List.of()),
          new ScreenDefinition(
              new Screen("/quote.html", "보험료 계산"),
              List.of(
                  html("quote.html", 10),
                  script("insurance.js", "bindForm('#quote-form'", 8),
                  java("API", "InsuranceController.java", "@GetMapping(\"/api/quote\")", 4),
                  java("보험료 산정", "InsuranceService.java", "Quote calculateQuote", 9)),
              List.of()),
          new ScreenDefinition(
              new Screen("/contract-change.html", "계약 변경 시뮬레이션"),
              List.of(
                  html("contract-change.html", 14),
                  script("operations.js", "attachForm('#contract-change-form'", 7),
                  java("API", "ContractChangeController.java", "@GetMapping", 5),
                  java("변경 견적", "ContractChangeService.java", "Preview preview", 13),
                  java("변경 정책", "ContractChangePolicy.java", "EXTENDED_COVERAGE_SURCHARGE", 17),
                  java("계약 유효성", "InsuranceService.java", "Contract findActiveContract", 8)),
              List.of(
                  new Hint(
                      List.of("할증", "추가 보험료", "확장 보장", "5천", "5000"),
                      List.of(new Target("변경 정책", "EXTENDED_COVERAGE_SURCHARGE =")),
                      "확장 보장에 가산할 월 보험료를 변경하세요. 견적 서비스가 기존 월 보험료에 이 값을 더합니다.",
                      "유지 계약 1001에서 기본·확장 보장 각각의 예상 보험료를 비교하세요."),
                  new Hint(
                      List.of("적용일", "적용 예정일", "다음 달", "즉시"),
                      List.of(new Target("변경 정책", "LocalDate effectiveDate")),
                      "요구한 적용일을 계산하도록 effectiveDate를 변경하세요.",
                      "월말과 연말에 조회해 적용 예정일을 확인하세요."),
                  new Hint(
                      List.of("만료", "유지 계약", "유지 중", "활성 계약"),
                      List.of(new Target("계약 유효성", "contract.status()")),
                      "허용할 계약 상태를 확인하세요. 계약 유효성 검사는 사고 접수에도 사용되므로 화면 전용 변경이면 변경 견적 서비스에서 분리하세요.",
                      "유지 계약 1001과 만료 계약 1002에서 변경 견적과 사고 접수를 각각 확인하세요."))),
          new ScreenDefinition(
              new Screen("/claim-intake.html", "사고 접수"),
              List.of(
                  html("claim-intake.html", 16),
                  script("operations.js", "attachForm('#claim-intake-form'", 13),
                  java("API", "ClaimController.java", "@PostMapping", 7),
                  java("사고 접수 서비스", "ClaimService.java", "Progress report", 11),
                  java("접수 정책", "ClaimIntakePolicy.java", "MAX_REPORT_AGE_DAYS", 26),
                  java("접수 저장소", "ClaimRepository.java", "Report save", 8),
                  java("계약 유효성", "InsuranceService.java", "Contract findActiveContract", 8)),
              List.of(
                  new Hint(
                      List.of("접수 기간", "접수 기한", "사고일", "일 전", "30일", "45일"),
                      List.of(
                          new Target("접수 정책", "MAX_REPORT_AGE_DAYS ="),
                          new Target("화면", "id=\"date-hint\"")),
                      "허용할 접수 기간을 MAX_REPORT_AGE_DAYS에 반영하고 사고일 안내 문구를 맞추세요.",
                      "변경한 허용 일수의 경계 날짜는 접수되고 그보다 하루 앞선 사고일은 거절되는지 확인하세요."),
                  new Hint(
                      List.of("사고 내용", "최소 글자", "최대 글자", "글자 수", "내용 길이"),
                      List.of(
                          new Target("접수 정책", "MIN_DESCRIPTION_LENGTH ="),
                          new Target("화면", "minlength=")),
                      "사고 내용 길이 제한을 정책 상수와 입력창의 minlength·maxlength에 함께 반영하세요.",
                      "최소·최대 길이와 각 경계에서 한 글자 벗어난 입력을 확인하세요."),
                  new Hint(
                      List.of("접수 번호", "번호 형식", "번호 생성"),
                      List.of(new Target("접수 저장소", "String id ="), new Target("브라우저 요청", "접수 번호")),
                      "저장소의 접수 번호 생성 방법을 조정하고 화면에 노출되는 형식을 확인하세요.",
                      "두 건을 접수해 번호가 서로 다르고 각각의 진행 조회 링크가 동작하는지 확인하세요."))),
          new ScreenDefinition(
              new Screen("/claim-progress.html", "보상 진행 조회"),
              List.of(
                  html("claim-progress.html", 11),
                  script("operations.js", "attachForm('#claim-progress-form'", 6),
                  java("API", "ClaimController.java", "@GetMapping", 5),
                  java("진행 조회 서비스", "ClaimService.java", "Progress progress", 5),
                  java("진행 정책", "ClaimProgressPolicy.java", "REVIEW_DAYS", 26),
                  java("접수 저장소", "ClaimRepository.java", "Report find", 8)),
              List.of(
                  new Hint(
                      List.of("예정일", "검토일", "검토 기간", "검토 기한", "5일"),
                      List.of(
                          new Target("진행 정책", "REVIEW_DAYS ="), new Target("브라우저 요청", "검토 예정일")),
                      "검토 예정일 산정에 사용하는 REVIEW_DAYS를 변경하고 브라우저의 표시 조건을 확인하세요.",
                      "CLM-1001의 예정일과 CLM-1002의 예정일 미표시를 확인하세요."),
                  new Hint(
                      List.of("상태", "서류 검토", "추가 서류", "안내", "다음 조치"),
                      List.of(
                          new Target("진행 정책", "var statusLabel"),
                          new Target("브라우저 요청", "claim.statusLabel")),
                      "상태별 표시 문구와 다음 조치를 정하는 switch 분기에서 요청한 상태의 응답을 수정하세요.",
                      "접수 완료·서류 검토 중·처리 완료의 응답과 화면 표시를 각각 확인하세요."))));

  private LegacyScreenCatalog() {}

  static List<Screen> screens() {
    return SCREENS.stream().map(ScreenDefinition::screen).toList();
  }

  static ScreenDefinition find(String path) {
    return SCREENS.stream()
        .filter(item -> item.screen().path().equals(path))
        .findFirst()
        .orElse(null);
  }

  private static Anchor html(String file, int lines) {
    return new Anchor("화면", STATIC + file, "<h1>", lines);
  }

  private static Anchor script(String file, String anchor, int lines) {
    return new Anchor("브라우저 요청", STATIC + file, anchor, lines);
  }

  private static Anchor java(String layer, String file, String anchor, int lines) {
    return new Anchor(layer, LEGACY_JAVA + file, anchor, lines);
  }

  record Screen(String path, String name) {}

  record ScreenDefinition(Screen screen, List<Anchor> anchors, List<Hint> hints) {}

  record Anchor(String layer, String path, String text, int lineCount) {}

  record Hint(List<String> keywords, List<Target> targets, String suggestion, String validation) {}

  record Target(String layer, String marker) {}
}
