package com.redhat.kafka.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaReceiver {

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${kafka.consumer.group-id}")
    public void listen(String message) {

        LOG.info(message);
    }
}
