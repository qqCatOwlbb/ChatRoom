package com.catowl.chatroom.init;

import com.catowl.chatroom.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: ChatRoom
 * @description: 项目启动是初始化并预热布隆过滤器
 * @author: qqCatOwlbb
 * @create: 2025-11-21 20:00
 **/
@Component
@Slf4j
public class BloomFilterInit implements CommandLineRunner {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserMapper userMapper;

    public static final String USER_ID_BLOOM_KEY = "bloom:user:id";
    public static final String USER_NAME_BLOOM_KEY = "bloom:user:name";


    @Override
    public void run(String... args) throws Exception {
        log.info("正在初始化用户ID布隆过滤器");
        // 初始化 ID 布隆过滤器
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(USER_ID_BLOOM_KEY);

        bloomFilter.tryInit(1000000L,0.01);

        if(bloomFilter.count() == 0){
            List<Long> allUserIds = userMapper.findAllIds();
            if(allUserIds != null && !allUserIds.isEmpty()){
                for(Long id : allUserIds){
                    bloomFilter.add(id);
                }
                log.info("布隆过滤器预热完成，加载用户数量：{}", allUserIds.size());
            }
        }else {
            log.info("布隆过滤器已存在，无需加载。当前估算元素数，{}", bloomFilter.count());
        }

        log.info("正在初始化Username布隆过滤器");
        // 初始化 Username 布隆过滤器
        RBloomFilter<String> nameBloomFilter = redissonClient.getBloomFilter(USER_NAME_BLOOM_KEY);

        nameBloomFilter.tryInit(1000000L,0.01);
        if (nameBloomFilter.count() == 0) {
            List<String> allUsernames = userMapper.findAllUsernames();
            if (allUsernames != null && !allUsernames.isEmpty()) {
                // 批量添加 (Redisson 支持 addAll 但通常循环添加更稳健以免阻塞)
                // 或者使用 Batch 操作优化性能，这里演示简单循环
                for (String username : allUsernames) {
                    nameBloomFilter.add(username);
                }
                log.info("用户名预热完成，加载数量: {}", allUsernames.size());
            }
        } else {
            log.info("用户名布隆过滤器已存在，估算元素: {}", nameBloomFilter.count());
        }

    }
}
