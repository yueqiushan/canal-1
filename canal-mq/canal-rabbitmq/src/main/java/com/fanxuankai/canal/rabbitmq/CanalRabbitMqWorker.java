package com.fanxuankai.canal.rabbitmq;

import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.fanxuankai.canal.core.CanalWorker;
import com.fanxuankai.canal.core.ConsumerConfigFactory;
import com.fanxuankai.canal.core.EntryConsumerFactory;
import com.fanxuankai.canal.core.config.CanalConfiguration;
import com.fanxuankai.canal.core.config.CanalWorkConfiguration;
import com.fanxuankai.canal.mq.core.config.CanalMqConfiguration;
import com.fanxuankai.canal.rabbitmq.consumer.DeleteConsumer;
import com.fanxuankai.canal.rabbitmq.consumer.InsertConsumer;
import com.fanxuankai.canal.rabbitmq.consumer.UpdateConsumer;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * @author fanxuankai
 */
public class CanalRabbitMqWorker extends CanalWorker {

    public CanalRabbitMqWorker(CanalWorkConfiguration canalWorkConfiguration) {
        super(canalWorkConfiguration);
    }

    public static CanalRabbitMqWorker newCanalWorker(CanalConfiguration canalConfiguration,
                                                     @Nullable CanalMqConfiguration canalMqConfiguration,
                                                     RabbitTemplate rabbitTemplate,
                                                     AmqpAdmin amqpAdmin) {
        ConsumerConfigFactory consumerConfigFactory = new ConsumerConfigFactory();
        canalMqConfiguration = Optional.ofNullable(canalMqConfiguration)
                .orElse(new CanalMqConfiguration());
        canalMqConfiguration.getConsumerConfigMap().forEach((schema, consumerConfigMap) ->
                consumerConfigMap.forEach((table, consumerConfig) ->
                        consumerConfigFactory.put(schema, table, consumerConfig)));
        EntryConsumerFactory entryConsumerFactory = new EntryConsumerFactory();
        Exchange exchange = new DirectExchange("canal2Mq.exchange");
        amqpAdmin.declareExchange(exchange);
        entryConsumerFactory.put(EventType.INSERT, new InsertConsumer(canalMqConfiguration, rabbitTemplate
                , amqpAdmin, exchange));
        entryConsumerFactory.put(EventType.UPDATE, new UpdateConsumer(canalMqConfiguration, rabbitTemplate
                , amqpAdmin, exchange));
        entryConsumerFactory.put(EventType.DELETE, new DeleteConsumer(canalMqConfiguration, rabbitTemplate
                , amqpAdmin, exchange));
        return new CanalRabbitMqWorker(new CanalWorkConfiguration()
                .setCanalConfiguration(canalConfiguration)
                .setConsumerConfigFactory(consumerConfigFactory)
                .setEntryConsumerFactory(entryConsumerFactory));
    }

}
