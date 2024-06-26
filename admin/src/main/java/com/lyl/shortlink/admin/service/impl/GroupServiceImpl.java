package com.lyl.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyl.shortlink.admin.common.biz.user.UserContext;
import com.lyl.shortlink.admin.common.conventions.exception.ClientException;
import com.lyl.shortlink.admin.dao.entity.GroupDO;
import com.lyl.shortlink.admin.dao.mapper.GroupMapper;
import com.lyl.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.lyl.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.lyl.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.lyl.shortlink.admin.service.GroupService;
import com.lyl.shortlink.admin.util.RandomGenerator;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.lyl.shortlink.admin.common.constants.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;


@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

//    private final ShortLinkActualRemoteService shortLinkActualRemoteService;
    private final RedissonClient redissonClient;

    @Value("${short-link.group.max-num}")
    private Integer groupMaxNum;

    @Override
    public void saveGroup(String groupName) {
        String username = UserContext.getUsername();
        if (StrUtil.isEmpty(username)) {
            throw new ClientException("用户未登录");
        }
        saveGroup(username, groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0);
            List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(groupDOList) && groupDOList.size() == groupMaxNum) {
                throw new ClientException(String.format("已超出最大分组数：%d", groupMaxNum));
            }
            String gid;
            do {
                gid = RandomGenerator.generateRandom();
            } while (!hasGid(username, gid));
            GroupDO groupDO = GroupDO.builder()
                    .gid(gid)
                    .sortOrder(0)
                    .username(username)
                    .name(groupName)
                    .build();
            baseMapper.insert(groupDO);
        } finally {
            lock.unlock();
        }
    }

    private boolean hasGid(String username, String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return hasGroupFlag == null;
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
//        LambdaQueryWrapper<GroupDO> listGroupWrapper = Wrappers.lambdaQuery(GroupDO.class)
//                .eq(GroupDO::getDelFlag, 0)
//                .eq(GroupDO::getUsername, UserContext.getUsername())
//                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
//        List<GroupDO> groups = baseMapper.selectList(listGroupWrapper);
//        Result<Map<String, ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkRemoteService.groupCount(groups.stream().map(GroupDO::getGid).toList());
//        List<ShortLinkGroupRespDTO> shortLinkGroupResp = BeanUtil.copyToList(groups, ShortLinkGroupRespDTO.class);
//        shortLinkGroupResp.forEach(each -> each.setShortLinkCount(listResult.getData().get(each.getGid()).getShortLinkCount()));
//        return shortLinkGroupResp;
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
//        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkActualRemoteService
//                .listGroupShortLinkCount(groupDOList.stream().map(GroupDO::getGid).toList());
//        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOList = BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
//        shortLinkGroupRespDTOList.forEach(each -> {
//            Optional<ShortLinkGroupCountQueryRespDTO> first = listResult.getData().stream()
//                    .filter(item -> Objects.equals(item.getGid(), each.getGid()))
//                    .findFirst();
//            first.ifPresent(item -> each.setShortLinkCount(first.get().getShortLinkCount()));
//        });
//        return shortLinkGroupRespDTOList;
        return BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        baseMapper.update(groupDO, updateWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getDelFlag, 0)
                .set(GroupDO::getDelFlag, 1);
        baseMapper.update(null, updateWrapper);
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
//        requestParam.forEach(each -> {
//            GroupDO groupDO = GroupDO.builder()
//                    .sortOrder(each.getSortOrder())
//                    .build();
//            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
//                    .eq(GroupDO::getUsername, UserContext.getUsername())
//                    .eq(GroupDO::getGid, each.getGid())
//                    .eq(GroupDO::getDelFlag, 0);
//            baseMapper.update(groupDO, updateWrapper);
//        });
        requestParam.forEach(each -> {
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .set(GroupDO::getSortOrder, each.getSortOrder())
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            update(updateWrapper);
        });
    }
}
