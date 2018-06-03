package uk.gov.ida.hub.control.helpers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class ExponentiallyBackingOffRedisConnector {
    private static final Logger LOG = LoggerFactory.getLogger(ExponentiallyBackingOffRedisConnector.class);

    public static RedisCommands<String, String> connectToRedis(RedisClient redisClient) throws InterruptedException {
        var connectionAttempts = 0;
        var random = new Random();

        while (true) {
            try {
                return redisClient.connect().sync();
            } catch (RedisConnectionException exception) {
                connectionAttempts++;

                // Maximum sleep time of 2^8 - 1 seconds = 4.2 minutes
                if (connectionAttempts > 8) { connectionAttempts = 8; }

                // Sleep for 2^c - 1 seconds (where c is the number of connection attempts)
                var secondsToSleep = random.nextInt((1 << connectionAttempts) - 1);
                LOG.warn("Redis connection failed: " + exception.getLocalizedMessage());
                LOG.warn(String.format("Redis connection will retry in %d seconds", secondsToSleep));
                LOG.debug("Redis connection failed", exception);
                Thread.sleep(secondsToSleep * 1000);
            }
        }

    }
}
