package com.lyl.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyl.shortlink.admin.common.database.BaseDO;
import lombok.Data;

/**
 * @description t_user_0
 * @author lyl.com
 * @date 2024-04-22
 */
@Data
@TableName("t_user")
public class UserDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 注销时间戳
     */
    private Long deletionTime;

    private Integer loginFlag;
}