package com.redhat.kafka.client.controller;

import com.redhat.kafka.client.service.KafkaSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class ClientController {

    @Value("${kafka.topic.name}")
    private String topicName;

    @Autowired
    private KafkaSender kafkaSender;

    @GetMapping(value = "/send")
    public String send(@RequestParam(required = true) String message) {

        LOG.info(message);

        kafkaSender.sendMessage(topicName, message);

        return "<h2>Message sent</h2>";
    }
}
