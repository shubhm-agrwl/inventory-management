package com.shubham.ingestor;

import com.shubham.ingestor.common.IngestorConfiguration;
import com.shubham.ingestor.resource.IngestorResource;
import com.shubham.ingestor.store.DAOHolder;
import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
import java.util.Properties;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;

@Slf4j
public class IngestorService extends Application<IngestorConfiguration> {

  public static void main(String[] args) throws Exception {
    new IngestorService().run(args);
  }

  public void run(IngestorConfiguration configuration, Environment environment) throws Exception {

    final DBIFactory factory = new DBIFactory();
    final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "ignite");

    // init DAOs
    DAOHolder.init(jdbi);

    Producer<Long, String> producer = createProducer(configuration);

    final FilterRegistration.Dynamic cors =
        environment.servlets().addFilter("CORS", CrossOriginFilter.class);

    // Configure CORS parameters
    cors.setInitParameter("allowedOrigins", "*");
    cors.setInitParameter("allowedHeaders",
        "X-Requested-With,Content-Type,Accept,Origin,Authorization");
    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

    // Add URL mapping
    cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

    environment.jersey().register(new IngestorResource(configuration, producer));

    log.info("Ingestor Started Successfully");

  }

  private Producer<Long, String> createProducer(IngestorConfiguration configuration) {
    Properties props = new Properties();
    props.putAll(configuration.getKafkaProducerConfig());
    return new KafkaProducer<Long, String>(props);
  }
}
