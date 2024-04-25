package com.lyl.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyl.shortlink.project.dao.entity.ShortLinkDO;
import com.lyl.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.lyl.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.lyl.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.lyl.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkPageRespDTO;

public interface RecycleBinService extends IService<ShortLinkDO> {
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);

    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
