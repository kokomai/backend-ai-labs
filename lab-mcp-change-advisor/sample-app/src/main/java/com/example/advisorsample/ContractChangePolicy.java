package com.example.advisorsample;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
class ContractChangePolicy {
  private static final int EXTENDED_COVERAGE_SURCHARGE = 5000;

  enum Coverage {
    STANDARD,
    EXTENDED
  }

  int monthlySurcharge(Coverage coverage) {
    return coverage == Coverage.EXTENDED ? EXTENDED_COVERAGE_SURCHARGE : 0;
  }

  LocalDate effectiveDate(LocalDate today) {
    return today.plusMonths(1).withDayOfMonth(1);
  }
}
