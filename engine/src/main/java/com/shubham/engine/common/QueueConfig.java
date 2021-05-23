package com.shubham.engine.common;

import lombok.Data;

public @Data
class QueueConfig {

  private String queueName;

  private Integer threadPoolSize;

  private Integer partitionsToListen;

}
