package io.charles.framework.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于Caffeine的缓存工具类
 *
 * @author xiongbo
 * @since 2022/3/15 18:00
 */
@ConditionalOnProperty(value = "app.cacheType", havingValue = "memory", matchIfMissing = true)
@Component
public class MemoryCacheService implements ICacheService {
    private static final Logger log = LoggerFactory.getLogger(MemoryCacheService.class);
    
    private final Cache<String, Object> cache;
    
    /** 用于存储List数据 */
    private final Map<String, List<?>> listCache = new ConcurrentHashMap<>();
    
    /** 用于存储Map数据 */
    private final Map<String, Map<String, ?>> mapCache = new ConcurrentHashMap<>();
    
    /** 存储过期时间信息 */
    private final Map<String, Long> expireTimeMap = new ConcurrentHashMap<>();

    public MemoryCacheService() {
        cache = Caffeine.newBuilder()
                .expireAfterAccess(24, TimeUnit.HOURS)
                .build();
    }

    /**
     * 支持缓存过期的缓存值
     */
    private static class ExpireCacheValue {
        private Object value;
        private Long liveDurationMs;

        private ExpireCacheValue(Object value, Long liveDurationMs) {
            this.value = value;
            this.liveDurationMs = liveDurationMs;
            this.lastAccessedTime = System.currentTimeMillis();
        }

        private long lastAccessedTime;

        private boolean hasExpired() {
            if (this.liveDurationMs == null) {
                return false;
            }
            return this.lastAccessedTime + this.liveDurationMs < System.currentTimeMillis();
        }
        
        private long getRemainingTimeMs() {
            if (this.liveDurationMs == null) {
                return -1;
            }
            long remaining = (this.lastAccessedTime + this.liveDurationMs) - System.currentTimeMillis();
            return Math.max(0, remaining);
        }
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    @Override
    public <T> void setCacheObject(final String key, final T value) {
        cache.put(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    @Override
    public <T> void setCacheObject(final String key, final T value, final Long timeout, final TimeUnit timeUnit) {
        long ms = Duration.of(timeout, timeUnit.toChronoUnit()).toMillis();
        cache.put(key, new ExpireCacheValue(value, ms));
        expireTimeMap.put(key, System.currentTimeMillis() + ms);
    }

    /**
     * 设置有效时间
     *
     * @param key     缓存键
     * @param timeout 超时时间(秒)
     * @return true=设置成功；false=设置失败
     */
    @Override
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key     缓存键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    @Override
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        Object value = cache.getIfPresent(key);
        if (value == null) {
            return false;
        }
        long ms = Duration.of(timeout, unit.toChronoUnit()).toMillis();
        // 重新包装为带过期时间的值
        Object actualValue = value;
        if (value instanceof ExpireCacheValue expireCacheValue) {
            actualValue = expireCacheValue.value;
        }
        cache.put(key, new ExpireCacheValue(actualValue, ms));
        expireTimeMap.put(key, System.currentTimeMillis() + ms);
        return true;
    }

    /**
     * 获取有效时间
     *
     * @param key 缓存键
     * @return 有效时间(秒)，-1表示永不过期，0表示已过期
     */
    @Override
    public long getExpire(final String key) {
        Object value = cache.getIfPresent(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof ExpireCacheValue expireCacheValue) {
            long remainingMs = expireCacheValue.getRemainingTimeMs();
            return remainingMs < 0 ? -1 : remainingMs / 1000;
        }
        return -1L; // 永不过期
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    @Override
    public Boolean hasKey(String key) {
        Object value = cache.getIfPresent(key);
        if (value == null) {
            return false;
        }
        if (value instanceof ExpireCacheValue expireCacheValue) {
            return !expireCacheValue.hasExpired();
        } else {
            return true;
        }
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    @Override
    public <T> T getCacheObject(final String key) {
        try {
            Object value = cache.getIfPresent(key);
            if (value == null) {
                return null;
            }
            if (value instanceof ExpireCacheValue expireCacheValue) {
                if (!expireCacheValue.hasExpired()) {
                    expireCacheValue.lastAccessedTime = System.currentTimeMillis();
                    return (T) expireCacheValue.value;
                } else {
                    // 已过期，清理缓存
                    cache.invalidate(key);
                    expireTimeMap.remove(key);
                    return null;
                }
            } else {
                return (T) value;
            }
        } catch (RuntimeException e) {
            log.warn("获取缓存对象失败, key={}: {}", key, e.getMessage());
        }
        return null;
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    @Override
    public boolean deleteObject(final String key) {
        if (key == null) {
            return false;
        }
        cache.invalidate(key);
        listCache.remove(key);
        mapCache.remove(key);
        expireTimeMap.remove(key);
        return true;
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    @Override
    public boolean deleteObject(final Collection collection) {
        for (Object key : collection) {
            deleteObject(Objects.toString(key));
        }
        return true;
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象数量
     */
    @Override
    public <T> long setCacheList(final String key, final List<T> dataList) {
        if (dataList == null) {
            return 0;
        }
        listCache.put(key, new ArrayList<>(dataList));
        return dataList.size();
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getCacheList(final String key) {
        List<?> list = listCache.get(key);
        if (list == null) {
            return Collections.emptyList();
        }
        return (List<T>) new ArrayList<>(list);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    @Override
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap == null) {
            return;
        }
        mapCache.put(key, new HashMap<>(dataMap));
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getCacheMap(final String key) {
        Map<String, ?> map = mapCache.get(key);
        if (map == null) {
            return Collections.emptyMap();
        }
        return (Map<String, T>) new HashMap<>(map);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   缓存键
     * @param hKey  Hash键
     * @param value 值
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        Map<String, Object> map = (Map<String, Object>) mapCache.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        map.put(hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  缓存键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCacheMapValue(final String key, final String hKey) {
        Map<String, ?> map = mapCache.get(key);
        if (map == null) {
            return null;
        }
        return (T) map.get(hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   缓存键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        Map<String, ?> map = mapCache.get(key);
        if (map == null) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        for (Object hKey : hKeys) {
            Object value = map.get(Objects.toString(hKey));
            if (value != null) {
                result.add((T) value);
            }
        }
        return result;
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    @Override
    public Collection<String> keys(final String pattern) {
        Set<String> keys = cache.asMap().keySet();
        final String query = pattern.endsWith("*") ? pattern.substring(0, pattern.length() - 1) : pattern;
        return keys.stream()
                .filter(k -> k.startsWith(query))
                .collect(Collectors.toList());
    }

    /**
     * 发布信息到队列（内存模式不支持，仅记录日志）
     *
     * @param channel 队列名
     * @param message 信息
     */
    @Override
    public void convertAndSend(String channel, Object message) {
        log.debug("MemoryCacheService不支持消息发布功能, channel={}, message={}", channel, message);
    }
}

