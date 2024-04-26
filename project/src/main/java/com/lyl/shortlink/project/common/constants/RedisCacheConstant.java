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
}
