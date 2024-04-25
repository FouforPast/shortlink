package com.lyl.shortlink.admin.remote;


//@Service
//public class ShortLinkActualRemoteService {
//}

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lyl.shortlink.admin.common.conventions.result.Result;
import com.lyl.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.lyl.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.lyl.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.lyl.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.Map;

public interface ShortLinkActualRemoteService {
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        String post = HttpUtil.post("http://localhost:8080/api/short-link/admin/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(post, new TypeReference<>() {
        });
    }

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("gid", requestParam.getGid());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
        String post = HttpUtil.post("http://localhost:8080/api/short-link/admin/v1/page", JSON.toJSONString(map));
        return JSON.parseObject(post, new TypeReference<>() {
        });
    }
}