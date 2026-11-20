package com.example.changeadvisor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class TicketController {
  private final TicketStore tickets;

  TicketController(TicketStore tickets) {
    this.tickets = tickets;
  }

  @PostMapping("/api/business/tickets")
  @ResponseStatus(HttpStatus.CREATED)
  ChangeTicket.BusinessView create(@RequestBody CreateRequest input) {
    String screenPath = required(input.screenPath(), 100, "대상 화면");
    if (!screenPath.equals("/contracts.html") && !screenPath.equals("/quote.html")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "샘플 화면을 선택해 주세요.");
    }
    return tickets
        .create(
            screenPath,
            required(input.title(), 120, "제목"),
            required(input.request(), 3000, "변경 요청"))
        .businessView();
  }

  @GetMapping("/api/business/tickets/{id}")
  ChangeTicket.BusinessView businessTicket(@PathVariable UUID id) {
    return tickets.get(id).businessView();
  }

  @GetMapping("/api/developer/tickets")
  List<ChangeTicket.Summary> developerTickets() {
    return tickets.list();
  }

  @GetMapping("/api/developer/tickets/{id}")
  ChangeTicket developerTicket(@PathVariable UUID id) {
    return tickets.get(id);
  }

  @PostMapping("/api/developer/tickets/{id}/claim")
  ChangeTicket claim(@PathVariable UUID id, @RequestBody Map<String, String> input) {
    return tickets.claim(id, required(input.get("assignee"), 60, "담당자"));
  }

  @PostMapping("/api/developer/tickets/{id}/reply")
  ChangeTicket reply(@PathVariable UUID id, @RequestBody Map<String, String> input) {
    return tickets.reply(id, required(input.get("reply"), 3000, "답변"));
  }

  @PostMapping("/api/developer/tickets/{id}/analysis")
  ChangeTicket retryAnalysis(@PathVariable UUID id) {
    return tickets.requeue(id);
  }

  private String required(String value, int max, String name) {
    if (value == null || value.isBlank() || value.strip().length() > max) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, name + " 항목은 1~" + max + "자로 입력해 주세요.");
    }
    return value.strip();
  }

  record CreateRequest(String screenPath, String title, String request) {}
}
