//
// Copyright (c) 2020 Resonate Group Ltd.  All Rights Reserved.
//
package rosbaldeston.lettuce;

import java.time.Duration;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.stream.StreamReceiver;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Slf4j
public class LettuceSteps
{
    private static final String STREAM_KEY = "test-stream";
    
    private static final String MESSAGE = "test-message";
    
    private FixedHostPortGenericContainer<?> redisContainer;

    @Autowired
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Value("${spring.redis.port}")
    private int redisPort;
    
    private Flux<ObjectRecord<String, String>> messages;

    /**
     * Stop the Redis container.
     */
    @After
    public void after()
    {
        LOG.info("Stopping Redis container");
        stopRedisContainer();
    }

    private void stopRedisContainer()
    {
        if (redisContainer != null)
        {
            redisContainer.stop();
        }
    }

    /**
     * Make the Redis container unavailable.
     */
    @Given("^Redis is unavailable$")
    public void redisUnavailable()
    {
        stopRedisContainer();
        Assertions.assertThrows(RedisConnectionFailureException.class, () -> redisTemplate.delete(StringUtils.EMPTY).block(Duration.ofSeconds(1)));
    }

    /**
     * Make the Redis container unavailable.
     */
    @Given("^Redis is available$")
    public void redisAvailable()
    {
        startRedisContainer();
        Assertions.assertEquals(0, redisTemplate.delete(StringUtils.EMPTY).block(Duration.ofSeconds(1)));
    }
    
    private void startRedisContainer()
    {
        LOG.info("Starting Redis container");
        redisContainer = new FixedHostPortGenericContainer<>("redis:6.0.6");
        redisContainer.withFixedExposedPort(LettuceApplicationIT.Configuration.REDIS_PORT, redisPort);
        redisContainer.start();
        redisContainer.waitingFor(new HttpWaitStrategy().forPort(LettuceApplicationIT.Configuration.REDIS_PORT));
    }

    /**
     * Create a stream receiver.
     */
    @Given("^a stream receiver is connected to Redis$")
    public void streamReceiver()
    {
        messages = StreamReceiver
                .create(reactiveRedisConnectionFactory,
                        StreamReceiver.StreamReceiverOptions.builder()
                              .targetType(String.class)
                              .pollTimeout(Duration.ofSeconds(1))
                              .build())
                    .receive(StreamOffset.fromStart(STREAM_KEY));
    }

    /**
     * Make the Redis container available.
     */
    @When("^Redis becomes available$")
    public void redisBecomesAvailable()
    {
        startRedisContainer();
    }

    /**
     * Make the Redis container unavailable.
     */
    @When("^Redis becomes unavailable$")
    public void redisBecomesUnavailable()
    {
        stopRedisContainer();
    }

    /**
     * Assert a message is published.
     */
    @Then("^a message can be published to a Redis stream$")
    public void assertPublished()
    {
        Assertions.assertNotNull(redisTemplate.opsForStream()
                .add(StreamRecords.newRecord()
                        .ofObject(MESSAGE)
                        .withStreamKey(STREAM_KEY))
                .block(Duration.ofSeconds(1)));
    }

    /**
     * Assert a message is not published.
     */
    @Then("^a message can not be published to a Redis stream$")
    public void assertNotPublished()
    {
        StepVerifier.create(redisTemplate.opsForStream()
                        .add(StreamRecords.newRecord()
                                .ofObject(MESSAGE)
                                .withStreamKey(STREAM_KEY)))
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

    /**
     * Assert the message is consumed.
     */
    @Then("^the message can be consumed from the Redis stream$")
    public void assertConsumed()
    {
        StepVerifier.create(messages)
                .expectNextMatches(record -> Objects.equals(MESSAGE, record.getValue()))
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

}
