package com.shubham.ingestor.store;

import com.shubham.ingestor.store.dao.InventoryDAO;
import lombok.Getter;
import org.skife.jdbi.v2.DBI;

public class DAOHolder {

  @Getter
  private static DBI jdbiHolder;
  @Getter
  private static InventoryDAO inventoryDAO;

  public static void init(DBI jdbi) {

    jdbiHolder = jdbi;
    inventoryDAO = jdbi.onDemand(InventoryDAO.class);

  }

}
