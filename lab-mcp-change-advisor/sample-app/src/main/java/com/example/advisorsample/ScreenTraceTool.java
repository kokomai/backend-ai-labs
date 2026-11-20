package com.example.advisorsample;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
class ScreenTraceTool {
  private static final String STATIC = "resources/static/";
  private static final String JAVA = "java/com/example/advisorsample/";
  private static final Map<String, List<Anchor>> TRACES =
      Map.of(
          "/contracts.html",
          List.of(
              new Anchor("화면", STATIC + "contracts.html", "<h1>", 10),
              new Anchor("브라우저 요청", STATIC + "insurance.js", "bindForm('#contract-form'", 5),
              new Anchor(
                  "API", JAVA + "InsuranceController.java", "@GetMapping(\"/api/contracts\")", 4),
              new Anchor("계약 조회", JAVA + "InsuranceService.java", "Contract findContract", 8)),
          "/quote.html",
          List.of(
              new Anchor("화면", STATIC + "quote.html", "<h1>", 10),
              new Anchor("브라우저 요청", STATIC + "insurance.js", "bindForm('#quote-form'", 8),
              new Anchor(
                  "API", JAVA + "InsuranceController.java", "@GetMapping(\"/api/quote\")", 4),
              new Anchor("보험료 산정", JAVA + "InsuranceService.java", "Quote calculateQuote", 9)));

  @McpTool(
      name = "trace_screen",
      description = "샘플 화면 경로에서 화면, 브라우저 요청, API, 서비스까지 실제 소스와 줄 번호를 조회한다.",
      annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false),
      generateOutputSchema = true)
  public ScreenTrace traceScreen(
      @McpArg(name = "screenPath", description = "샘플 화면 경로", required = true) String screenPath) {
    var anchors = TRACES.get(screenPath);
    if (anchors == null) {
      throw new IllegalArgumentException("등록된 샘플 화면 경로를 입력해 주세요.");
    }
    return new ScreenTrace(screenPath, anchors.stream().map(this::readEvidence).toList());
  }

  private Evidence readEvidence(Anchor anchor) {
    try {
      var resource = new ClassPathResource("sample-source/" + anchor.path());
      var lines = resource.getContentAsString(StandardCharsets.UTF_8).lines().toList();
      int start =
          IntStream.range(0, lines.size())
              .filter(index -> lines.get(index).contains(anchor.text()))
              .findFirst()
              .orElseThrow(() -> new IllegalStateException("소스 위치를 확인할 수 없습니다."));
      int end = Math.min(lines.size(), start + anchor.lineCount());
      return new Evidence(
          anchor.layer(),
          "lab-mcp-change-advisor/sample-app/src/main/" + anchor.path(),
          start + 1,
          end,
          String.join("\n", lines.subList(start, end)));
    } catch (IOException exception) {
      throw new IllegalStateException("패키지에 포함된 샘플 소스를 읽을 수 없습니다.", exception);
    }
  }

  private record Anchor(String layer, String path, String text, int lineCount) {}

  public record ScreenTrace(String screenPath, List<Evidence> evidence) {}

  public record Evidence(String layer, String path, int startLine, int endLine, String code) {}
}
