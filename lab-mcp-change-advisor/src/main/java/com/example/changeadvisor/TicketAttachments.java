package com.example.changeadvisor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
class TicketAttachments {
  private final JdbcClient jdbc;

  TicketAttachments(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  void insert(UUID ticketId, List<TicketAttachment> attachments) {
    for (var attachment : attachments) {
      var info = attachment.info();
      jdbc.sql(
              """
          insert into ticket_attachment
            (id, ticket_id, filename, media_type, size_bytes, content)
          values (?, ?, ?, ?, ?, ?)
          """)
          .params(
              info.id(),
              ticketId,
              info.filename(),
              info.mediaType(),
              info.size(),
              attachment.content())
          .update();
    }
  }

  List<TicketAttachment.Info> list(UUID ticketId) {
    return jdbc.sql(
            """
        select id, filename, media_type, size_bytes
        from ticket_attachment where ticket_id = ? order by filename, id
        """)
        .param(ticketId)
        .query((row, index) -> info(row))
        .list();
  }

  List<TicketAttachment> images(UUID ticketId) {
    return jdbc.sql(
            """
        select id, filename, media_type, size_bytes, content
        from ticket_attachment
        where ticket_id = ? and media_type in ('image/png', 'image/jpeg')
        order by filename, id
        """)
        .param(ticketId)
        .query((row, index) -> read(row))
        .list();
  }

  TicketAttachment get(UUID ticketId, UUID attachmentId) {
    return jdbc.sql(
            """
        select id, filename, media_type, size_bytes, content
        from ticket_attachment where ticket_id = ? and id = ?
        """)
        .params(ticketId, attachmentId)
        .query((row, index) -> read(row))
        .optional()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부 파일을 찾을 수 없습니다."));
  }

  private TicketAttachment read(ResultSet row) throws SQLException {
    return new TicketAttachment(info(row), row.getBytes("content"));
  }

  private TicketAttachment.Info info(ResultSet row) throws SQLException {
    return new TicketAttachment.Info(
        row.getObject("id", UUID.class), row.getString("filename"),
        row.getString("media_type"), row.getLong("size_bytes"));
  }
}
