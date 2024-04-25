package com.lyl.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyl.shortlink.project.dao.entity.ShortLinkDO;
import com.lyl.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.lyl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.lyl.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.lyl.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

public interface ShortLinkService extends IService<ShortLinkDO> {
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);


    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    ShortLinkCreateRespDTO createShortLinkByLock(ShortLinkCreateReqDTO requestParam);

    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);

    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);
}

