package com.example.advisorsample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ContractChangeController {
  private final ContractChangeService service;

  ContractChangeController(ContractChangeService service) {
    this.service = service;
  }

  @GetMapping("/api/contract-changes/preview")
  ContractChangeService.Preview preview(
      @RequestParam long contractId, @RequestParam ContractChangePolicy.Coverage coverage) {
    return service.preview(contractId, coverage);
  }
}
