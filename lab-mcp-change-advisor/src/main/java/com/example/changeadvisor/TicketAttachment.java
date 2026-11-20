package com.example.changeadvisor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

record TicketAttachment(Info info, byte[] content) {
  private static final int MAX_FILES = 3;
  private static final int MAX_BYTES = 5 * 1024 * 1024;
  private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
  private static final byte[] JPEG = {(byte) 0xff, (byte) 0xd8, (byte) 0xff};
  private static final byte[] PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);

  record Info(UUID id, String filename, String mediaType, long size) {
    boolean isImage() {
      return mediaType.startsWith("image/");
    }
  }

  static List<TicketAttachment> fromUploads(List<MultipartFile> parts) {
    var files =
        parts.stream()
            .filter(part -> !part.isEmpty() || !"".equals(part.getOriginalFilename()))
            .toList();
    if (files.size() > MAX_FILES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "첨부 파일은 최대 3개까지 등록할 수 있습니다.");
    }
    return files.stream().map(TicketAttachment::fromUpload).toList();
  }

  private static TicketAttachment fromUpload(MultipartFile part) {
    String original = part.getOriginalFilename();
    String filename = original == null ? "" : original.replace('\\', '/');
    filename = filename.substring(filename.lastIndexOf('/') + 1).strip();
    if (filename.isEmpty()
        || filename.length() > 180
        || filename.chars().anyMatch(Character::isISOControl)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "첨부 파일 이름을 확인해 주세요.");
    }
    if (part.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 파일은 첨부할 수 없습니다.");
    }
    if (part.getSize() > MAX_BYTES) {
      throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "파일은 각각 5MB까지 첨부할 수 있습니다.");
    }
    try {
      byte[] content = part.getBytes();
      String name = filename.toLowerCase(Locale.ROOT);
      String mediaType;
      if (name.endsWith(".png") && startsWith(content, PNG)) {
        mediaType = "image/png";
      } else if ((name.endsWith(".jpg") || name.endsWith(".jpeg")) && startsWith(content, JPEG)) {
        mediaType = "image/jpeg";
      } else if (name.endsWith(".pdf") && startsWith(content, PDF)) {
        mediaType = "application/pdf";
      } else if (name.endsWith(".txt") && isUtf8(content)) {
        mediaType = "text/plain";
      } else {
        throw new ResponseStatusException(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE, "PNG, JPEG, PDF 또는 UTF-8 TXT 파일을 첨부해 주세요.");
      }
      return new TicketAttachment(
          new Info(UUID.randomUUID(), filename, mediaType, content.length), content);
    } catch (IOException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "첨부 파일을 읽을 수 없습니다.", exception);
    }
  }

  private static boolean startsWith(byte[] content, byte[] signature) {
    return content.length >= signature.length
        && Arrays.equals(content, 0, signature.length, signature, 0, signature.length);
  }

  private static boolean isUtf8(byte[] content) {
    try {
      StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(content));
      return true;
    } catch (CharacterCodingException exception) {
      return false;
    }
  }
}
