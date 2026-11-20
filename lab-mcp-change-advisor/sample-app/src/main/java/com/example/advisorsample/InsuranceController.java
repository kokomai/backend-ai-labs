package com.example.advisorsample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class InsuranceController {
  private final InsuranceService service;

  InsuranceController(InsuranceService service) {
    this.service = service;
  }

  @GetMapping("/api/contracts")
  InsuranceService.Contract contract(@RequestParam long id) {
    return service.findContract(id);
  }

  @GetMapping("/api/quote")
  InsuranceService.Quote quote(@RequestParam int age, @RequestParam boolean bundled) {
    return service.calculateQuote(age, bundled);
  }
}
