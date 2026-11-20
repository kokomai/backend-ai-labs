package com.example.changeadvisor;

import com.example.changeadvisor.LegacyScreenCatalog.Anchor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
class ScreenTraceTool {
  @McpResource(
      uri = "context://legacy/screens",
      name = "legacy-screens",
      description = "추적할 수 있는 레거시 보험 화면과 경로")
  public String screens() {
    return LegacyScreenCatalog.screens().stream()
        .map(screen -> screen.name() + " " + screen.path())
        .collect(Collectors.joining("\n"));
  }

  @McpTool(
      name = "trace_screen",
      description = "샘플 화면 경로에서 화면, 브라우저 요청, API, 서비스까지 실제 소스와 줄 번호를 조회한다.",
      annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false),
      generateOutputSchema = true)
  public ScreenTrace traceScreen(
      @McpArg(name = "screenPath", description = "샘플 화면 경로", required = true) String screenPath) {
    var screen = LegacyScreenCatalog.find(screenPath);
    if (screen == null) {
      throw new IllegalArgumentException("등록된 샘플 화면 경로를 입력해 주세요.");
    }
    return new ScreenTrace(screenPath, screen.anchors().stream().map(this::readEvidence).toList());
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

  public record ScreenTrace(String screenPath, List<Evidence> evidence) {}

  public record Evidence(String layer, String path, int startLine, int endLine, String code) {}
}
