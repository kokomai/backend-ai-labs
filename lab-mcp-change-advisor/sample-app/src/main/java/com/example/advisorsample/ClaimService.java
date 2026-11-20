package com.example.advisorsample;

import java.time.LocalDate;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
class ClaimService {
  private final InsuranceService contracts;
  private final ClaimRepository repository;
  private final ClaimIntakePolicy intakePolicy;
  private final ClaimProgressPolicy progressPolicy;

  ClaimService(
      InsuranceService contracts,
      ClaimRepository repository,
      ClaimIntakePolicy intakePolicy,
      ClaimProgressPolicy progressPolicy) {
    this.contracts = contracts;
    this.repository = repository;
    this.intakePolicy = intakePolicy;
    this.progressPolicy = progressPolicy;
  }

  ClaimProgressPolicy.Progress report(long contractId, LocalDate accidentDate, String description) {
    LocalDate today = LocalDate.now();
    contracts.findActiveContract(contractId);
    String normalizedDescription = intakePolicy.validate(accidentDate, description, today);
    var report = repository.save(contractId, accidentDate, normalizedDescription, today);
    return progressPolicy.describe(report);
  }

  ClaimProgressPolicy.Progress progress(String id) {
    return progressPolicy.describe(repository.find(id.strip().toUpperCase(Locale.ROOT)));
  }
}
