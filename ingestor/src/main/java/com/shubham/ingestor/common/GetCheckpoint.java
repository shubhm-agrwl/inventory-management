package com.shubham.ingestor.common;

import lombok.Data;

@Data
public class GetCheckpoint {

  private String tableName;

  private String colName;

}
