package com.example.advisorsample;

import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
class ContractChangeService {
  private final InsuranceService contracts;
  private final ContractChangePolicy policy;

  ContractChangeService(InsuranceService contracts, ContractChangePolicy policy) {
    this.contracts = contracts;
    this.policy = policy;
  }

  Preview preview(long contractId, ContractChangePolicy.Coverage coverage) {
    var contract = contracts.findActiveContract(contractId);
    int surcharge = policy.monthlySurcharge(coverage);
    return new Preview(
        contract.id(),
        coverage,
        contract.monthlyPremium(),
        surcharge,
        contract.monthlyPremium() + surcharge,
        policy.effectiveDate(LocalDate.now()));
  }

  record Preview(
      long contractId,
      ContractChangePolicy.Coverage coverage,
      int currentMonthlyPremium,
      int monthlySurcharge,
      int estimatedMonthlyPremium,
      LocalDate effectiveDate) {}
}
