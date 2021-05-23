package com.shubham.ingestor.resource;

import com.shubham.ingestor.common.CreateForm;
import com.shubham.ingestor.common.GetCheckpoint;
import com.shubham.ingestor.common.IngestorConfiguration;
import com.shubham.ingestor.store.DAOHolder;
import com.shubham.ingestor.store.IgniteConnection;
import com.shubham.ingestor.utils.JsonUtils;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Timed;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
@Path("/collect/inventory/v1")
public class IngestorResource {

  private Producer<Long, String> producer;
  private String queueName;
  private AtomicLong counter = new AtomicLong(0);
  private static Map<String, List<String>> checkpointOrder;
  private IgniteConnection igniteConnection;
  private long canStartTrip;

  public IngestorResource(IngestorConfiguration ingestorConfiguration,
      Producer<Long, String> producer) {
    this.producer = producer;
    this.queueName = ingestorConfiguration.getQueueName();
    this.checkpointOrder = ingestorConfiguration.getCheckpointOrder();
    this.igniteConnection = new IgniteConnection(ingestorConfiguration.getDataSourceFactory());
    this.canStartTrip = ingestorConfiguration.getCanStartTripInDays() * 24 * 60 * 60 * 1000;
  }

  @POST
  @Timed(name = "create.form.queue.time")
  @Counted(name = "create.form.queue.count")
  @Path("/createForm")
  @Produces(MediaType.APPLICATION_JSON)
  public Response createForm(CreateForm createForm) {

    log.info("Create Form Request {}", JsonUtils.toJson(createForm));
    try {
      DAOHolder.getInventoryDAO()
          .createForm(createForm, JsonUtils.toJson(createForm.getFormContent()));
      return Response.status(Status.ACCEPTED).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }


  }

  @POST
  @Timed(name = "submit.form.queue.time")
  @Counted(name = "submit.form.queue.count")
  @Path("/submitForm")
  @Produces(MediaType.APPLICATION_JSON)
  public Response submitForm(Map<String, Object> submitForm) {

    String submission = JsonUtils.toJson(submitForm);

    log.info("Submit Form Request {}", submission);

    final ProducerRecord<Long, String> record =
        new ProducerRecord<Long, String>(queueName, counter.getAndIncrement(), submission);

    try {
      producer.send(record).get();
      return Response.status(Status.ACCEPTED).build();
    } catch (Exception e) {
      log.error("Unable to write to Kafka", e);
    }
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  @POST
  @Timed(name = "checkpoint.queue.time")
  @Counted(name = "checkpoint.queue.count")
  @Path("/checkpoint")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> getCheckpoint(GetCheckpoint getCheckpoint) {

    log.info("Get Checkpoint Request {} {}", getCheckpoint.getTableName(),
        getCheckpoint.getColName());

    List<String> truckIds = new ArrayList<String>();
    int index = checkpointOrder.get(getCheckpoint.getTableName())
        .indexOf(getCheckpoint.getColName());
    if (index > 0) {
      truckIds = igniteConnection.getTruckDetailsForCheckpoint(getCheckpoint.getTableName(),
          checkpointOrder.get(getCheckpoint.getTableName()).get(index - 1),
          getCheckpoint.getColName());
    }
    return truckIds;
  }

  @GET
  @Timed(name = "create.form.queue.time")
  @Counted(name = "create.form.queue.count")
  @Path("/canStart")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> canStart(@QueryParam("tableName") String tableName) {

    log.info("Can Start Trip Request Check");
    return igniteConnection.getStartTripTrucks(tableName, canStartTrip);

  }

}
