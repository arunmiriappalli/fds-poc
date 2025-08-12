
package com.example.fds.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Collections;

@Service
public class RedisWindows {
  private final StringRedisTemplate redis;
  private final DefaultRedisScript<Long> script;

  public RedisWindows(StringRedisTemplate redis) {
    this.redis = redis;
    String lua = "redis.call('ZADD', KEYS[1], ARGV[1], ARGV[3])\n" +
        "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1]-ARGV[2])\n" +
        "return redis.call('ZCARD', KEYS[1])";
    this.script = new DefaultRedisScript<>(lua, Long.class);
  }

  public long countAndTrim(String key, long nowMs, Duration window, String member) {
    Long res = redis.execute(script, Collections.singletonList(key), String.valueOf(nowMs),
        String.valueOf(window.toMillis()), member);
    return res == null ? 0L : res;
  }

  public void blockKey(String key, Duration ttl) {
    redis.opsForValue().set(key, "1", ttl);
  }

  public boolean isBlocked(String key) {
    return redis.opsForValue().get(key) != null;
  }
}
