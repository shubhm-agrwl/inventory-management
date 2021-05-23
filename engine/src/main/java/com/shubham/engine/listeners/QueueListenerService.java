package com.shubham.engine.listeners;

import com.shubham.engine.common.EngineConfiguration;
import com.shubham.engine.common.SubmitForm;
import com.shubham.engine.store.IgniteConnection;
import com.shubham.engine.utils.JsonUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@Slf4j
public class QueueListenerService {

  private static boolean canRun = true;
  private static Map<String, List<String>> tableSchemas;
  private static StringBuilder insert = new StringBuilder("merge into ");
  private static StringBuilder open = new StringBuilder(" (");
  private static StringBuilder value = new StringBuilder(" values (");
  private static StringBuilder close = new StringBuilder(")");

  public static void init(EngineConfiguration configuration) {
    tableSchemas = configuration.getTableSchema();

    // Creating a threadpool to read from Kafka faster
    ExecutorService threadPoolExecutor =
        Executors.newFixedThreadPool(configuration.getQueueConfig().getThreadPoolSize());
    Integer partitionsToListen = configuration.getQueueConfig().getPartitionsToListen();
    for (int count = 0; count < partitionsToListen; count++) {
      Consumer<String, String> consumer = createConsumer(
          configuration.getQueueConfig().getQueueName(), configuration.getKafkaConsumerConfig());
      threadPoolExecutor
          .execute(new QueueListener(consumer, configuration.getKafkaConsumerPollInterval(),
              new IgniteConnection(configuration.getDataSourceFactory())));
    }
    log.info("Started AckQueueListenerService.");
  }

  private static Consumer<String, String> createConsumer(String queueName,
      Map<String, String> kafkaConsumerConfig) {
    final Properties props = new Properties();
    props.putAll(kafkaConsumerConfig);
    // Create the consumer using props.
    final Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);

    // Subscribe to the topic.
    consumer.subscribe(Collections.singletonList(queueName));
    return consumer;
  }

  static class QueueListener implements Runnable {

    private Consumer<String, String> consumer;
    private long pollInterval;
    private IgniteConnection igniteConnection;

    public QueueListener(Consumer<String, String> consumer, long pollInterval,
        IgniteConnection igniteConnection) {
      this.consumer = consumer;
      this.pollInterval = pollInterval;
      this.igniteConnection = igniteConnection;
    }

    @Override
    public void run() {
      while (canRun) {
        try {
          final ConsumerRecords<String, String> consumerRecords = consumer.poll(pollInterval);
          if (consumerRecords.count() > 0) {

            consumerRecords.forEach(record -> {
              try {
                if (log.isDebugEnabled()) {
                  log.debug("Message Read from Kafka {}", record.value());
                }
                SubmitForm submitForm = JsonUtils.fromJson(record.value(), SubmitForm.class);
                putMessage(submitForm, igniteConnection);
              } catch (Exception e) {
                log.error("exception caught while reading the message {}", record.value(), e);
              }
            });
            consumer.commitAsync();
          }
        } catch (Exception e) {
          log.error("Error occured while reading messages from kafka, continuing... ", e);
        }
      }
    }

    private void putMessage(SubmitForm submitForm, IgniteConnection igniteConnection) {

      // Dynamically generating the merge query so that it can accomodate different types of data
      for (Map.Entry<String, List<String>> entry : tableSchemas.entrySet()) {
        StringBuilder columnNames = new StringBuilder();
        StringBuilder values = new StringBuilder();
        if (entry.getKey().equals(submitForm.getType())) {
          List<String> columns = tableSchemas.get(entry.getKey());
          for (String column : columns) {
            if (null != submitForm.getSubmission().get(column)) {
              columnNames.append(column).append(",");
              values.append(submitForm.getSubmission().get(column)).append(",");
            }
          }
          columnNames.append("sync");
          values.append("false");

          StringBuilder query = new StringBuilder();
          query = query.append(insert).append(submitForm.getType()).append(open).append(columnNames)
              .append(close).append(value).append(values).append(close);
          igniteConnection.insertSubmission(query.toString());

        }
      }

    }
  }

}
