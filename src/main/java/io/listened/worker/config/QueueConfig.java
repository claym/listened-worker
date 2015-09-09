package io.listened.worker.config;

import io.listened.common.constants.JobQueues;
import io.listened.worker.delegate.ITunesGenreDelegate;
import io.listened.worker.delegate.PodcastRefreshDelegate;
import io.listened.worker.delegate.PodcastSubmitDelegate;
import io.listened.worker.delegate.PodcastUpdateDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Created by Clay on 6/21/2015.
 * Sets up queues and if necessary, creates them
 */
@Configuration
@EnableRabbit
public class QueueConfig {

    private static final Logger log = LoggerFactory.getLogger(QueueConfig.class);

    @Autowired
    PodcastSubmitDelegate podcastSubmitDelegate;

    @Autowired
    PodcastUpdateDelegate podcastUpdateDelegate;

    @Autowired
    PodcastRefreshDelegate podcastRefreshDelegate;

    @Autowired
    ITunesGenreDelegate iTunesGenreDelegate;


    @Bean
    SimpleMessageListenerContainer podcastUpdateContainer(ConnectionFactory connectionFactory) {
        verifyQueue(JobQueues.JOB_PODCAST_UPDATE, connectionFactory);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(JobQueues.JOB_PODCAST_UPDATE);
        container.setMessageListener(new MessageListenerAdapter(podcastUpdateDelegate));
        return container;
    }

    @Bean
    SimpleMessageListenerContainer podcastRefreshContainer(ConnectionFactory connectionFactory) {
        verifyQueue(JobQueues.JOB_PODCAST_REFRESH, connectionFactory);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(JobQueues.JOB_PODCAST_REFRESH);
        container.setMessageListener(new MessageListenerAdapter(podcastRefreshDelegate));
        return container;
    }

    @Bean
    SimpleMessageListenerContainer podcastSubmitContainer(ConnectionFactory connectionFactory) {
        verifyQueue(JobQueues.JOB_PODCAST_ADD, connectionFactory);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(JobQueues.JOB_PODCAST_ADD);
        container.setMessageListener(new MessageListenerAdapter(podcastSubmitDelegate));
        return container;
    }

    @Bean
    SimpleMessageListenerContainer iTunesGenreContainer(ConnectionFactory connectionFactory) {
        verifyQueue(JobQueues.JOB_ITUNES_GENRE, connectionFactory);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(JobQueues.JOB_ITUNES_GENRE);
        container.setMessageListener(new MessageListenerAdapter(iTunesGenreDelegate));
        return container;
    }

    private void verifyQueue(String queueName, ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        log.info("Checking for existence of queue: {}", queueName);
        Properties properties = admin.getQueueProperties(queueName);
        if (properties == null) {
            log.info("Unable to find queue {}, creating", queueName);
            Queue queue = new Queue(queueName);
            admin.declareQueue(queue);
            properties = admin.getQueueProperties(queueName);
            if (properties == null) {
                throw new RuntimeException("Unable to create queue " + queueName + ", aborting");
            }
        }
        log.info("Queue {} exists", queueName);
    }

}
