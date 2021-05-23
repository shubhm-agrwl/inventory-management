package com.shubham.engine.store;

import io.dropwizard.db.DataSourceFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IgniteConnection {

  private Connection jdbcConn;
  private Statement stmt;
  private Statement updateStmt;
  private ResultSet rs;

  public IgniteConnection(DataSourceFactory dataSourceFactory) {
    try {
      jdbcConn = DriverManager
          .getConnection(dataSourceFactory.getUrl(), dataSourceFactory.getUser(),
              dataSourceFactory.getPassword());
      stmt = jdbcConn.createStatement();
      updateStmt = jdbcConn.createStatement();
    } catch (Exception e) {
      log.error("Error creating JDBC Connection in Ingestor", e);
    }

  }

  public void insertSubmission(String query) {

    try {
      if (log.isDebugEnabled()) {
        log.debug("Query: {}", query);
      }
      stmt.execute(query);
    } catch (SQLException e) {
      log.error("Unable to query {}", query, e);
    }
  }

  public void getAsyncRows() {
    //TODO make the table name to be generic
    // Can make it ordered so that the rows inserted first gets sync at the earliest
    // Picking only 100 records since 100 transactions can be done within 100 seconds
    String sql = "select * from truck_inventory where sync = false LIMIT 100";
    try {
      rs = stmt.executeQuery(sql);
      while (rs.next()) {
        try {
          //Write to Google Sheet. If successful, update the sync to true
          String update =
              "update truck_inventory set sync = true where id = '" + rs.getString("id") + "'";
          updateStmt.execute(update);
        } catch (Exception e) {
          // Not do anything if exception has occured. will retry with the next batch
          log.error("Unable to sync to Google Sheet");
        }
      }
    } catch (SQLException e) {
      log.error("Error while fetching checkpoint details ", e);
    }
  }
}
