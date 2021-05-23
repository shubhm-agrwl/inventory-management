package com.shubham.ingestor.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IgniteConfig {

  private String url;

  private String userName;

  private String password;
}

