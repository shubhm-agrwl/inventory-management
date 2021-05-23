package com.shubham.engine.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

public @Data
class EngineConfiguration extends Configuration {

  @NotNull
  String engineId;

  @NotNull
  @Valid
  @JsonProperty
  private Map<String, String> kafkaConsumerConfig;

  @NotNull
  @Valid
  @JsonProperty
  private long kafkaConsumerPollInterval;


  @NotNull
  @Valid
  @JsonProperty
  private QueueConfig queueConfig;

  @NotNull
  @Valid
  @JsonProperty
  private DataSourceFactory dataSourceFactory = new DataSourceFactory();

  @NotNull
  @Valid
  @JsonProperty
  private Map<String, List<String>> tableSchema;

}
