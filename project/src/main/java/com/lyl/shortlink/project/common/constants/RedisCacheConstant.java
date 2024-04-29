package com.lyl.shortlink.project.common.constants;

public class RedisCacheConstant {
    public static final String LOCK_USER_REGISTER_KEY = "short-link_lock_user-register";
    public static final String USER_LOGIN_KEY = "short-link_lock_user-login";

    public static final String LOCK_GROUP_CREATE_KEY = "short-link_group_cache";

    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:is-null:goto_%s";

    public static final String GOTO_SHORT_LINK_KEY = "short-link:lock:goto:%s";

    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock:goto:%s";
    public static final String UV_SHORT_LINK_KEY = "short-link:uv:%s";
    public static final String UIP_SHORT_LINK_KEY = "short-link:uip:%s:%s";

    public static final String DELAY_QUEUE_STATS_KEY = "short-link:delay-queue:stats";

    public static final String LOCK_GID_UPDATE_KEY = "short-link:lock:update-gid:%s";

    /**
     * 短链接统计判断是否新用户缓存标识
     */
    public static final String SHORT_LINK_STATS_UV_KEY = "short-link:stats:uv:";

    /**
     * 短链接统计判断是否新 IP 缓存标识
     */
    public static final String SHORT_LINK_STATS_UIP_KEY = "short-link:stats:uip:";

    /**
     * 短链接监控消息保存队列 Topic 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_TOPIC_KEY = "short-link:stats-stream";

    /**
     * 短链接监控消息保存队列 Group 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_GROUP_KEY = "short-link:stats-stream:only-group";

    /**
     * 创建短链接锁标识
     */
    public static final String SHORT_LINK_CREATE_LOCK_KEY = "short-link:lock:create";
}
