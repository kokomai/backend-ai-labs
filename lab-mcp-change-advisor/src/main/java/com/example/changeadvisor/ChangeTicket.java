package com.example.changeadvisor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record ChangeTicket(
    UUID id,
    OffsetDateTime createdAt,
    String screenPath,
    String title,
    String request,
    State state,
    String assignee,
    String reply,
    AnalysisState analysisState,
    Analysis analysis) {

  enum State {
    OPEN,
    IN_PROGRESS,
    RESOLVED
  }

  enum AnalysisState {
    QUEUED,
    RUNNING,
    COMPLETE,
    FAILED
  }

  record Analysis(String mode, String proposal, List<Evidence> evidence) {}

  record Evidence(String layer, String path, int startLine, int endLine, String code) {}

  record BusinessView(
      UUID id,
      OffsetDateTime createdAt,
      String screenPath,
      String title,
      String request,
      State state,
      String reply) {}

  record Summary(
      UUID id,
      OffsetDateTime createdAt,
      String screenPath,
      String title,
      State state,
      AnalysisState analysisState) {}

  BusinessView businessView() {
    return new BusinessView(id, createdAt, screenPath, title, request, state, reply);
  }
}
