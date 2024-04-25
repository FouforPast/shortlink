package com.lyl.shortlink.project.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lyl.shortlink.project.common.conventions.result.Result;
import com.lyl.shortlink.project.common.conventions.result.Results;
import com.lyl.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.lyl.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.lyl.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.lyl.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.lyl.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecycleBinController {
    private final RecycleBinService recycleBinService;

    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }

    /**
     * 恢复短链接
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        recycleBinService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 移除短链接
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        recycleBinService.removeRecycleBin(requestParam);
        return Results.success();
    }

}
