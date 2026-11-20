package com.example.advisorsample;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
class ClaimIntakePolicy {
  private static final int MAX_REPORT_AGE_DAYS = 30;
  private static final int MIN_DESCRIPTION_LENGTH = 10;
  private static final int MAX_DESCRIPTION_LENGTH = 250;

  String validate(LocalDate accidentDate, String description, LocalDate today) {
    if (accidentDate == null
        || accidentDate.isAfter(today)
        || accidentDate.isBefore(today.minusDays(MAX_REPORT_AGE_DAYS))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "사고일은 오늘부터 " + MAX_REPORT_AGE_DAYS + "일 전까지 입력할 수 있습니다.");
    }
    String normalized = description == null ? "" : description.strip();
    if (normalized.length() < MIN_DESCRIPTION_LENGTH
        || normalized.length() > MAX_DESCRIPTION_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "사고 내용을 " + MIN_DESCRIPTION_LENGTH + "~" + MAX_DESCRIPTION_LENGTH + "자로 입력해 주세요.");
    }
    return normalized;
  }
}
