package com.lyl.shortlink.admin.common.biz.user;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Optional;

public class UserContext {
    private static final ThreadLocal<UserInfoDTO> USER_INFO_DTO_THREAD_LOCAL = new TransmittableThreadLocal<>();
    public static void setUser(UserInfoDTO userInfoDTO){
        USER_INFO_DTO_THREAD_LOCAL.set(userInfoDTO);
    }

    public static String getUsername() {
        UserInfoDTO userInfoDTO = USER_INFO_DTO_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUsername).orElse(null);
    }

    public static void removeUser() {
        USER_INFO_DTO_THREAD_LOCAL.remove();
    }
}
