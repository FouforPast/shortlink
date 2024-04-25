package com.lyl.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyl.shortlink.admin.dao.entity.UserDO;
import com.lyl.shortlink.admin.dto.req.UserLoginReqDTO;
import com.lyl.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.lyl.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.lyl.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.lyl.shortlink.admin.dto.resp.UserRespDto;


public interface UserService extends IService<UserDO> {

    public UserRespDto getUserByUsername(String username);

    default public Boolean hasUsername(String username){
        return false;
    }

    void register(UserRegisterReqDTO requestParam);

    void update(UserUpdateReqDTO reqDTO);

    public UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO);

    Boolean checkLogin(String username, String token);

    void logout(String username, String token);
}
