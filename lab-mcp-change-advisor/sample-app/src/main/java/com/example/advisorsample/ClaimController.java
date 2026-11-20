package com.example.advisorsample;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ClaimController {
  private final ClaimService claims;

  ClaimController(ClaimService claims) {
    this.claims = claims;
  }

  @PostMapping("/api/claims")
  @ResponseStatus(HttpStatus.CREATED)
  ClaimProgressPolicy.Progress report(@RequestBody Intake intake) {
    return claims.report(intake.contractId(), intake.accidentDate(), intake.description());
  }

  @GetMapping("/api/claims/{id}")
  ClaimProgressPolicy.Progress progress(@PathVariable String id) {
    return claims.progress(id);
  }

  record Intake(long contractId, LocalDate accidentDate, String description) {}
}
