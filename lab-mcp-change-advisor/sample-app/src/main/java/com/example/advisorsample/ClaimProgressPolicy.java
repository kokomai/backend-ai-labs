package com.example.advisorsample;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
class ClaimProgressPolicy {
  private static final int REVIEW_DAYS = 5;

  Progress describe(ClaimRepository.Report report) {
    var statusLabel =
        switch (report.status()) {
          case RECEIVED -> "접수 완료";
          case IN_REVIEW -> "서류 검토 중";
          case COMPLETED -> "처리 완료";
        };
    var nextAction =
        switch (report.status()) {
          case RECEIVED -> "담당자 배정을 기다려 주세요.";
          case IN_REVIEW -> "추가 서류 요청이 있는지 확인해 주세요.";
          case COMPLETED -> "보상 처리가 완료되었습니다.";
        };
    var expectedReviewOn =
        report.status() == ClaimRepository.Status.COMPLETED
            ? null
            : report.reportedOn().plusDays(REVIEW_DAYS);
    return new Progress(
        report.id(),
        report.contractId(),
        report.accidentDate(),
        report.reportedOn(),
        report.status(),
        statusLabel,
        nextAction,
        expectedReviewOn);
  }

  record Progress(
      String id,
      long contractId,
      LocalDate accidentDate,
      LocalDate reportedOn,
      ClaimRepository.Status status,
      String statusLabel,
      String nextAction,
      LocalDate expectedReviewOn) {}
}
