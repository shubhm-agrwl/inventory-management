package com.shubham.ingestor.common;

import java.util.Map;
import lombok.Data;

@Data
public class CreateForm {

  private String id;

  private Map<String, String> formContent;

  private long ts;

}
