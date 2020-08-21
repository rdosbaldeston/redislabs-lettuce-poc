Feature: Lettuce connection retry

  Scenario: Connection gained for stream receiver
    Given Redis is unavailable
    And a stream receiver is connected to Redis
    When Redis becomes available
    Then a message can be published to a Redis stream
    And the message can be consumed from the Redis stream

  Scenario: Connection lost for stream receiver
    Given Redis is available
    And a stream receiver is connected to Redis
    When Redis becomes unavailable
    Then a message can not be published to a Redis stream

  Scenario: Connection lost and gained for stream receiver
    Given Redis is available
    And a stream receiver is connected to Redis
    When Redis becomes unavailable
    When Redis becomes available
    Then a message can be published to a Redis stream
    And the message can be consumed from the Redis stream

  Scenario: Connection stable for stream receiver
    Given Redis is available
    And a stream receiver is connected to Redis
    Then a message can be published to a Redis stream
    And the message can be consumed from the Redis stream