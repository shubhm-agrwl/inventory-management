package com.shubham.ingestor.store;

import io.dropwizard.db.DataSourceFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IgniteConnection {

  private Connection jdbcConn;
  private Statement stmt;
  private ResultSet rs;

  public IgniteConnection(DataSourceFactory dataSourceFactory) {
    try {
      jdbcConn = DriverManager
          .getConnection(dataSourceFactory.getUrl(), dataSourceFactory.getUser(),
              dataSourceFactory.getPassword());
      stmt = jdbcConn.createStatement();
    } catch (Exception e) {
      log.error("Error creating JDBC Connection in Ingestor", e);
    }

  }

  public List<String> getTruckDetailsForCheckpoint(String tableName, String prevColName,
      String curColName) {
    List<String> truckIds = new ArrayList<String>();
    String sql =
        "select truck_id from " + tableName + " where " + prevColName + " > 0 and " + curColName
            + " is null";
    try {
      rs = stmt.executeQuery(sql);
      while (rs.next()) {
        truckIds.add(rs.getString("truck_id"));
      }
    } catch (SQLException e) {
      log.error("Error while fetching checkpoint details ", e);
    }
    return truckIds;
  }

  public List<String> getStartTripTrucks(String tableName, long canStartTrip) {
    List<String> truckIds = new ArrayList<String>();
    String sql =
        "select truck_id from " + tableName + " where site_entry is null or site_entry + " + canStartTrip + " < " + System
            .currentTimeMillis();
    try {
      rs = stmt.executeQuery(sql);
      while (rs.next()) {
        truckIds.add(rs.getString("truck_id"));
      }
    } catch (SQLException e) {
      log.error("Error while fetching checkpoint details ", e);
    }
    return truckIds;
  }
}
