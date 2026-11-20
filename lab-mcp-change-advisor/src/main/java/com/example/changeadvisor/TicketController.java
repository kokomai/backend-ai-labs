package com.example.changeadvisor;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
class TicketController {
  private final TicketStore tickets;
  private final TicketAttachments attachments;

  TicketController(TicketStore tickets, TicketAttachments attachments) {
    this.tickets = tickets;
    this.attachments = attachments;
  }

  @PostMapping(path = "/api/business/tickets", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  ChangeTicket.BusinessView create(@RequestBody CreateRequest input, Principal principal) {
    return create(principal.getName(), input, List.of());
  }

  @PostMapping(path = "/api/business/tickets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  ChangeTicket.BusinessView upload(
      @RequestParam String screenPath,
      @RequestParam String title,
      @RequestParam String request,
      @RequestParam(name = "attachments", required = false) List<MultipartFile> files,
      Principal principal) {
    return create(
        principal.getName(),
        new CreateRequest(screenPath, title, request),
        files == null ? List.of() : TicketAttachment.fromUploads(files));
  }

  private ChangeTicket.BusinessView create(
      String owner, CreateRequest input, List<TicketAttachment> files) {
    String screenPath = required(input.screenPath(), 100, "대상 화면");
    if (LegacyScreenCatalog.find(screenPath) == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "샘플 화면을 선택해 주세요.");
    }
    return tickets
        .create(
            owner,
            screenPath,
            required(input.title(), 120, "제목"),
            required(input.request(), 3000, "변경 요청"),
            files)
        .businessView();
  }

  @GetMapping("/api/screens")
  List<LegacyScreenCatalog.Screen> screens() {
    return LegacyScreenCatalog.screens();
  }

  @GetMapping("/api/business/tickets")
  List<ChangeTicket.BusinessView> businessTickets(Principal principal) {
    return tickets.listOwned(principal.getName());
  }

  @GetMapping("/api/business/tickets/{id}")
  ChangeTicket.BusinessView businessTicket(@PathVariable UUID id, Principal principal) {
    return tickets.getOwned(id, principal.getName());
  }

  @GetMapping("/api/business/tickets/{id}/attachments/{fileId}")
  ResponseEntity<byte[]> businessDownload(
      @PathVariable UUID id, @PathVariable UUID fileId, Principal principal) {
    tickets.checkOwner(id, principal.getName());
    return download(id, fileId);
  }

  @GetMapping("/api/developer/tickets/{id}/attachments/{fileId}")
  ResponseEntity<byte[]> developerDownload(@PathVariable UUID id, @PathVariable UUID fileId) {
    return download(id, fileId);
  }

  private ResponseEntity<byte[]> download(UUID id, UUID fileId) {
    var file = attachments.get(id, fileId);
    var info = file.info();
    var disposition =
        info.isImage() ? ContentDisposition.inline() : ContentDisposition.attachment();
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(info.mediaType()))
        .contentLength(info.size())
        .cacheControl(CacheControl.noStore())
        .header("X-Content-Type-Options", "nosniff")
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            disposition.filename(info.filename(), StandardCharsets.UTF_8).build().toString())
        .body(file.content());
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
  ChangeTicket claim(@PathVariable UUID id, Principal principal) {
    return tickets.claim(id, principal.getName());
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
