package uk.gov.ida.hub.control.clients;

import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import java.util.Map;
import java.util.UUID;

public class SessionClient {
    private final RedisCommands<String, String> redisClient;

    public SessionClient(RedisCommands<String, String> redisClient) {
        this.redisClient = redisClient;
    }

    public String initialise(Map<String, String> session) {
        var sessionId = UUID.randomUUID().toString();
        redisClient.hmset(sessionKey(sessionId), session);
        return sessionId;
    }

    public String get(String sessionId, String key) throws SessionNotFoundException {
        checkSessionExists(sessionId);
        String value = redisClient.hget(sessionKey(sessionId), key);
        return value;
    }

    public void set(String sessionId, String key, String value) throws SessionNotFoundException {
        checkSessionExists(sessionId);
        redisClient.hset(sessionKey(sessionId), key, value);
    }

    public Map<String, String> getAll(String sessionId) throws SessionNotFoundException {
        checkSessionExists(sessionId);
        return redisClient.hgetall(sessionKey(sessionId));
    }

    public VerifySessionState getState(String sessionId) throws SessionNotFoundException {
        checkSessionExists(sessionId);
        return VerifySessionState.forName(redisClient.get(stateKey(sessionId)));
    }

    public void setState(String sessionId, VerifySessionState state) throws SessionNotFoundException {
        checkSessionExists(sessionId);
        redisClient.set(stateKey(sessionId), state.getName());
    }

    private String sessionKey(String sessionId) {
        return "session:" + sessionId;
    }

    private String stateKey(String sessionId) {
        return "state:" + sessionId;
    }

    private void checkSessionExists(String sessionId) throws SessionNotFoundException {
        String sessionKey = sessionKey(sessionId);
        Long keyCount = redisClient.exists(sessionKey);
        if (keyCount != 1) {
            throw new SessionNotFoundException("Expected to find one key '" + sessionKey + "' but found " + keyCount);
        }
    }

}
