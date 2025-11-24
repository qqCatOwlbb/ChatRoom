-- KEY[1]: dirty key (源)
-- KEY[2]: processing key (目标)

-- 1. 如果 dirty 集合是空的，没必要搬运，直接返回 0
if redis.call('SCARD', KEYS[1]) == 0 then
    return 0
end

-- 2. 如果 processing 集合还有数据（说明上一次任务挂了或者是积压了），
--    为了防止覆盖导致数据丢失，我们这次不搬运，先让消费者把旧的处理完。
if redis.call('SCARD', KEYS[2]) > 0 then
    return 2 -- 返回特殊状态码，表示“阻塞中”
end

-- 3. 执行原子重命名
redis.call('RENAME', KEYS[1], KEYS[2])
return 1 -- 成功