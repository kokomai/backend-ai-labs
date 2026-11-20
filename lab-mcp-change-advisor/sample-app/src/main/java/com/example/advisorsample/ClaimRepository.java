package com.example.advisorsample;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
class ClaimRepository {
  private final Map<String, Report> reports = new ConcurrentHashMap<>();
  private final AtomicLong nextNumber = new AtomicLong(1003);

  ClaimRepository() {
    var today = LocalDate.now();
    reports.put(
        "CLM-1001",
        new Report(
            "CLM-1001",
            1001,
            today.minusDays(2),
            "휴대품 파손 샘플",
            today.minusDays(1),
            Status.IN_REVIEW));
    reports.put(
        "CLM-1002",
        new Report(
            "CLM-1002",
            1001,
            today.minusDays(12),
            "수리비 청구 샘플",
            today.minusDays(10),
            Status.COMPLETED));
  }

  Report save(long contractId, LocalDate accidentDate, String description, LocalDate reportedOn) {
    String id = "CLM-" + nextNumber.getAndIncrement();
    var report = new Report(id, contractId, accidentDate, description, reportedOn, Status.RECEIVED);
    reports.put(id, report);
    return report;
  }

  Report find(String id) {
    var report = reports.get(id);
    if (report == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "접수 번호를 찾을 수 없습니다.");
    }
    return report;
  }

  enum Status {
    RECEIVED,
    IN_REVIEW,
    COMPLETED
  }

  record Report(
      String id,
      long contractId,
      LocalDate accidentDate,
      String description,
      LocalDate reportedOn,
      Status status) {}
}
