package storm.redis;

import redis.clients.jedis.JedisPoolConfig;

import java.io.Serializable;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class JedisPoolConfigSerializable extends JedisPoolConfig implements Serializable  {}