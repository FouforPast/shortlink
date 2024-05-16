package com.lyl.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyl.shortlink.admin.common.biz.user.UserContext;
import com.lyl.shortlink.admin.common.conventions.exception.ServiceException;
import com.lyl.shortlink.admin.common.conventions.result.Result;
import com.lyl.shortlink.admin.dao.entity.GroupDO;
import com.lyl.shortlink.admin.dao.mapper.GroupMapper;
import com.lyl.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.lyl.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.lyl.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.lyl.shortlink.admin.service.RecycleBinService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class RecycleBinAdminServiceImpl implements RecycleBinService {
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;
    private final GroupMapper groupMapper;

    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam.getGidList(), requestParam.getCurrent(), requestParam.getSize());
    }
}
