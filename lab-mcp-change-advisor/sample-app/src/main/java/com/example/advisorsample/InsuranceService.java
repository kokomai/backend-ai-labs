package com.example.advisorsample;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
class InsuranceService {
  record Contract(long id, String product, String status, int monthlyPremium) {}

  record Quote(int basePremium, int discountPercent, int monthlyPremium) {}

  private final Map<Long, Contract> contracts =
      Map.of(
          1001L, new Contract(1001, "생활안심 보험", "유지", 32000),
          1002L, new Contract(1002, "생활안심 보험", "만료", 28000));

  Contract findContract(long id) {
    var contract = contracts.get(id);
    if (contract == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "계약을 찾을 수 없습니다.");
    }
    return contract;
  }

  Contract findActiveContract(long id) {
    var contract = findContract(id);
    if (!contract.status().equals("유지")) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "유지 중인 계약만 처리할 수 있습니다.");
    }
    return contract;
  }

  Quote calculateQuote(int age, boolean bundled) {
    if (age < 18 || age > 80) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "가입 가능 연령은 만 18~80세입니다.");
    }
    int basePremium = age >= 60 ? 45000 : 30000;
    int discountPercent = bundled ? 10 : 0;
    int monthlyPremium = basePremium * (100 - discountPercent) / 100;
    return new Quote(basePremium, discountPercent, monthlyPremium);
  }
}
