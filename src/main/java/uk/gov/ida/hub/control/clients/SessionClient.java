package uk.gov.ida.hub.control.clients;

import io.lettuce.core.api.sync.RedisCommands;
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

    public String get(String sessionId, String key) {
        return redisClient.hget(sessionKey(sessionId), key);
    }

    public void set(String sessionId, String key, String value) {
        redisClient.hset(sessionKey(sessionId), key, value);
    }

    public Map<String, String> getAll(String sessionId) {
        return redisClient.hgetall(sessionKey(sessionId));
    }

    public VerifySessionState getState(String sessionId) {
        return VerifySessionState.forName(redisClient.get(stateKey(sessionId)));
    }

    public void setState(String sessionId, VerifySessionState state) {
        redisClient.set(stateKey(sessionId), state.getName());
    }

    private String sessionKey(String sessionId) {
        return "session:" + sessionId;
    }

    private String stateKey(String sessionId) {
        return "state:" + sessionId;
    }

}
