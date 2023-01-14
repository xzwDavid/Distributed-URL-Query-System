package edu.lantonia.tinyurl.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Notifier {
  @Value("${tiny-url.counter.batch-size:inappropriate-url}")
  private String inappropriateUrlTopic;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  public void signalInappropriateUrl(String url) throws Exception {
    // We do not wait for the result of asynchronous send() method on purpose.
    // I would like to show retry performed by Kafka producer's internal buffer.
    // Depending on your use-case, in majority of cases, you would want to wait
    // for successful acknowledgement from send() method.
    kafkaTemplate.send(inappropriateUrlTopic, url);
  }
}
