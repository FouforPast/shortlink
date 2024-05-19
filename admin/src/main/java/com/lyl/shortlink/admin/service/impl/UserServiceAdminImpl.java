package com.lyl.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyl.shortlink.admin.common.biz.user.UserContext;
import com.lyl.shortlink.admin.common.conventions.exception.ClientException;
import com.lyl.shortlink.admin.common.conventions.exception.ServiceException;
import com.lyl.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.lyl.shortlink.admin.dao.entity.UserDO;
import com.lyl.shortlink.admin.dao.mapper.UserMapper;
import com.lyl.shortlink.admin.dto.req.UserLoginReqDTO;
import com.lyl.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.lyl.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.lyl.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.lyl.shortlink.admin.dto.resp.UserRespDto;
import com.lyl.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.lyl.shortlink.admin.common.constants.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.lyl.shortlink.admin.common.constants.RedisCacheConstant.USER_LOGIN_KEY;
import static com.lyl.shortlink.admin.common.enums.UserErrorCodeEnum.*;

@Service
@RequiredArgsConstructor
public class UserServiceAdminImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupServiceImpl groupService;
    @Override
    public UserRespDto getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDo = baseMapper.selectOne(wrapper);
        return Optional.ofNullable(userDo).map(userDO -> {
            UserRespDto result = new UserRespDto();
            BeanUtils.copyProperties(userDO, result);
            return result;
        }).orElseThrow(() -> new ClientException(UserErrorCodeEnum.USER_NULL));
    }

    @Override
    public Boolean hasUsername(String username){
//        LambdaQueryWrapper<UserDo> wrapper = Wrappers.lambdaQuery(UserDo.class)
//                .eq(UserDo::getUsername, username);
//        UserDo userDo = baseMapper.selectOne(wrapper);
//        return userDo != null;
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        if (!lock.tryLock()) {
            throw new ClientException(USER_NAME_EXIST);
        }
        try {
            int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
            if (inserted < 1) {
                throw new ClientException(USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
            groupService.saveGroup(requestParam.getUsername(), "默认分组");
        } catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST);
        } catch(Exception ex){
            throw new ServiceException("执行出错");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO reqDTO) {
        if (!Objects.equals(reqDTO.getUsername(), UserContext.getUsername())) {
            throw new ClientException("当前登录用户修改请求异常");
        }
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, reqDTO.getUsername());
        baseMapper.update(BeanUtil.toBean(reqDTO, UserDO.class), updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + requestParam.getUsername());
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + requestParam.getUsername());
//            stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
//            String token = hasLoginMap.keySet().stream()
//                    .findFirst()
//                    .map(Object::toString)
//                    .orElseThrow(() -> new ClientException("用户登录错误"));
//            return new UserLoginRespDTO(token);
        }
        /**
         * Hash
         * Key：login_用户名
         * Value：
         *  Key：token标识
         *  Val：JSON 字符串（用户信息）
         */
        String uuid = UUID.randomUUID().toString();
//        if (requestParam.getUsername().equals("admin")){
//            uuid = "4b312f0b-aa6a-474a-8c20-70b2544c8f97";
//        }
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + requestParam.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }
}
