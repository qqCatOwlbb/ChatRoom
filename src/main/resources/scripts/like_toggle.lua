-- KEYS[1]: user_like_set: 用户点赞表
-- KEYS[2]: video_like_count: 视频点赞表
-- KEYS[3]: dirty_set_key: 热榜zset脏数据集合
-- KEYS[4]: dirty_score_key: db脏数据
-- ARGV[1]: video_id

if redis.call("EXISTS", KEYS[2]) == 0 then
    return -1   -- 去查表
end

if redis.cal("SISMEMBRE", KEYS[1], ARGV[1]) == 1 then
    -- user_like_set中有这个video，说明点赞过
    redis.call("SREM", KEYS[1], ARGV[1])
    redis.call("DECR", KEYS[2])
    redis.call("SADD", KEYS[3], ARGV[1])
    redis.call("SADD", KEYS[4], ARGV[1])
    redis
    return 0    -- 取消点赞
else
    redis.call("SADD", KEYS[1], ARGV[1])
    redis.call("INCR", KEYS[2])
    redis.call("SADD", KEYS[3], ARGV[1])
    redis.call("SADD", KEYS[4], ARGV[1])
    return 1    -- 成功点赞
end