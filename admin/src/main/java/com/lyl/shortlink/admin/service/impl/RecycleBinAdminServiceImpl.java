package com.lyl.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyl.shortlink.admin.common.conventions.result.Result;
import com.lyl.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.lyl.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.lyl.shortlink.admin.service.RecycleBinService;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class RecycleBinAdminServiceImpl implements RecycleBinService {
    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return null;
    }
}
