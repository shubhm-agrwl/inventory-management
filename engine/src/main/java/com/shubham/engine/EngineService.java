package com.shubham.engine;

import com.shubham.engine.common.EngineConfiguration;
import com.shubham.engine.listeners.QueueListenerService;
import com.shubham.engine.scheduler.GoogleSheetSyncScheduler;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EngineService extends Application<EngineConfiguration> {

  public static void main(String[] args) throws Exception {
    new EngineService().run(args);
  }

  public void run(EngineConfiguration configuration, Environment environment) throws Exception {

    // Kafka Queue Listener Service
    QueueListenerService.init(configuration);

    // Scheduler which runs every 100 seconds, picks up 100 records which has not been synced with Google Sheet
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    executorService.scheduleWithFixedDelay(
        new GoogleSheetSyncScheduler(configuration),
        10, 100, TimeUnit.SECONDS);
    log.info("Scheduled Ack message Deletion...");

  }
}
