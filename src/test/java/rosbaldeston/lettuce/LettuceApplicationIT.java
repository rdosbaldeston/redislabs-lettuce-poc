//
// Copyright (c) 2020 Resonate Group Ltd.  All Rights Reserved.
//

package rosbaldeston.lettuce;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.SocketUtils;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;


/**
 * Cucumber integration tests. This is simply the entry point for all the Cucumber tests and should remain empty. The Cucumber glue classes should be
 * automatically detected.
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features", strict = true, tags = "", plugin = { "pretty", "json:target/cucumber.json" })
@CucumberContextConfiguration
@SpringBootTest(classes = { LettuceApplication.class, LettuceApplicationIT.Configuration.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(locations = "classpath:/test-application.properties")
@ActiveProfiles("cucumber")
public final class LettuceApplicationIT
{
    private LettuceApplicationIT()
    {
        // Don't instantiate
    }

    /**
     * Test configuration.
     */
    @TestConfiguration
    public static class Configuration
    {
        public static final int REDIS_PORT = SocketUtils.findAvailableTcpPort();

        /**
         * Configure the Redis connection factory.
         * @return the Redis connection factory
         */
        @Primary
        @Bean
        public LettuceConnectionFactory testRedisConnectionFactory()
        {
            return new LettuceConnectionFactory(
                    new RedisStandaloneConfiguration("localhost", REDIS_PORT));
        }
    }
}
