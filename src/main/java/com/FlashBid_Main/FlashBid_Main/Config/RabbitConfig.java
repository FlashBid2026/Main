package com.FlashBid_Main.FlashBid_Main.Config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

  public static final String EXCHANGE_NAME = "auction.exchange";
  public static final String QUEUE_NAME = "auction.ranking.queue";
  public static final String ROUTING_KEY = "auction.bid.#";

  @Bean
  public TopicExchange auctionExchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  @Bean
  public Queue rankingQueue() {
    return new Queue(QUEUE_NAME);
  }

  @Bean
  public Binding binding(Queue rankingQueue, TopicExchange auctionExchange) {
    return BindingBuilder.bind(rankingQueue).to(auctionExchange).with(ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }
}
