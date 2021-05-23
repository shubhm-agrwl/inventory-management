package com.shubham.ingestor.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

public @Data
class IngestorConfiguration extends Configuration {

  @NotNull
  @Valid
  @JsonProperty
  private DataSourceFactory dataSourceFactory = new DataSourceFactory();

  @Valid
  @NotNull
  @JsonProperty
  private String queueName;

  @NotNull
  @Valid
  @JsonProperty
  private Map<String, String> kafkaProducerConfig;

  @Valid
  @NotNull
  @JsonProperty
  private Boolean authenticationEnabled;

  @Valid
  @NotNull
  @JsonProperty
  private String ingestorId;

  @NotNull
  @Valid
  @JsonProperty
  private Map<String, List<String>> checkpointOrder;

  @NotNull
  @Valid
  @JsonProperty
  private long canStartTripInDays;

}
