package com.example.changeadvisor;

import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@Repository
@DependsOnDatabaseInitialization
class TicketStore {
  private final JdbcClient jdbc;
  private final JsonMapper json;

  TicketStore(JdbcClient jdbc, JsonMapper json) {
    this.jdbc = jdbc;
    this.json = json;
  }

  @PostConstruct
  void requeueInterruptedAnalysis() {
    jdbc.sql("update change_ticket set analysis_state = 'QUEUED' where analysis_state = 'RUNNING'")
        .update();
  }

  ChangeTicket create(String screenPath, String title, String request) {
    var id = UUID.randomUUID();
    jdbc.sql(
            """
        insert into change_ticket (id, created_at, screen_path, title, request_text)
        values (?, ?, ?, ?, ?)
        """)
        .params(id, OffsetDateTime.now(ZoneOffset.UTC), screenPath, title, request)
        .update();
    return get(id);
  }

  List<ChangeTicket.Summary> list() {
    return jdbc.sql(
            """
        select id, created_at, screen_path, title, state, analysis_state
        from change_ticket order by created_at desc
        """)
        .query(
            (row, index) ->
                new ChangeTicket.Summary(
                    row.getObject("id", UUID.class),
                    row.getObject("created_at", OffsetDateTime.class),
                    row.getString("screen_path"),
                    row.getString("title"),
                    ChangeTicket.State.valueOf(row.getString("state")),
                    ChangeTicket.AnalysisState.valueOf(row.getString("analysis_state"))))
        .list();
  }

  ChangeTicket get(UUID id) {
    return jdbc.sql("select * from change_ticket where id = ?")
        .param(id)
        .query(this::read)
        .optional()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다."));
  }

  ChangeTicket claim(UUID id, String assignee) {
    return updateState(
        id,
        "update change_ticket set assignee = ?, state = 'IN_PROGRESS' where id = ? and state = 'OPEN'",
        assignee,
        id);
  }

  ChangeTicket reply(UUID id, String reply) {
    return updateState(
        id,
        "update change_ticket set reply = ?, state = 'RESOLVED' where id = ? and state = 'IN_PROGRESS'",
        reply,
        id);
  }

  ChangeTicket requeue(UUID id) {
    return updateState(
        id,
        """
        update change_ticket set analysis_state = 'QUEUED', analysis_json = null
        where id = ? and analysis_state in ('COMPLETE', 'FAILED')
        """,
        id);
  }

  Optional<ChangeTicket> startNextAnalysis() {
    var next =
        jdbc.sql(
                """
        select * from change_ticket where analysis_state = 'QUEUED'
        order by created_at fetch first row only
        """)
            .query(this::read)
            .optional();
    return next.filter(
        ticket ->
            jdbc.sql(
                        """
                update change_ticket set analysis_state = 'RUNNING'
                where id = ? and analysis_state = 'QUEUED'
                """)
                    .param(ticket.id())
                    .update()
                == 1);
  }

  void finish(UUID id, ChangeTicket.AnalysisState state, ChangeTicket.Analysis analysis) {
    jdbc.sql("update change_ticket set analysis_state = ?, analysis_json = ? where id = ?")
        .params(state.name(), json.writeValueAsString(analysis), id)
        .update();
  }

  private ChangeTicket updateState(UUID id, String statement, Object... parameters) {
    int updated = jdbc.sql(statement).params(parameters).update();
    var ticket = get(id);
    if (updated == 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "문의 상태가 변경되었습니다.");
    }
    return ticket;
  }

  private ChangeTicket read(ResultSet row, int index) throws SQLException {
    String data = row.getString("analysis_json");
    return new ChangeTicket(
        row.getObject("id", UUID.class),
        row.getObject("created_at", OffsetDateTime.class),
        row.getString("screen_path"),
        row.getString("title"),
        row.getString("request_text"),
        ChangeTicket.State.valueOf(row.getString("state")),
        row.getString("assignee"),
        row.getString("reply"),
        ChangeTicket.AnalysisState.valueOf(row.getString("analysis_state")),
        data == null ? null : json.readValue(data, ChangeTicket.Analysis.class));
  }
}
