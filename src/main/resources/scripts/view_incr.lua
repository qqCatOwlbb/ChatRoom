-- KEYS[1]: video_view_key
-- KEYS[2]: dirty_set_key
-- KEYs[3]: dirty_score_key
-- ARGV[1]: video_id

if redis.call("EXISTS",KEYS[1]) == 1 then
    redis.call("INCR",KEYS[1])
    redis.call("SADD",KEYS[2],ARGV[1])
    -- 数据库的脏数据队列
    redis.call("SADD", KEYS[3],ARGV[1])
    return 1    -- 成功
else
    return -1   -- key不存在，查表确认
end