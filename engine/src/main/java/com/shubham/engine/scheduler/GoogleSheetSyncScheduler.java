package com.shubham.engine.scheduler;

import com.shubham.engine.common.EngineConfiguration;
import com.shubham.engine.store.IgniteConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleSheetSyncScheduler implements Runnable {

  private IgniteConnection igniteConnection;

  public GoogleSheetSyncScheduler(EngineConfiguration engineConfiguration) {
    this.igniteConnection = new IgniteConnection(engineConfiguration.getDataSourceFactory());
  }

  @Override
  public void run() {

    log.info("Running Google Sheet Sync Scheduler");
    igniteConnection.getAsyncRows();

  }
}
