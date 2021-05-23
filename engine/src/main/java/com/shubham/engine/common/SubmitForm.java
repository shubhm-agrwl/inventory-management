package com.shubham.engine.common;

import java.util.Map;
import lombok.Data;

@Data
public class SubmitForm {

  private String type;

  private Map<String, Object> submission;

}
