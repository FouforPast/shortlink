package com.lyl.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.lyl.shortlink.admin.common.conventions.result.Result;
import com.lyl.shortlink.admin.common.conventions.result.Results;
import com.lyl.shortlink.admin.dto.req.UserLoginReqDTO;
import com.lyl.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.lyl.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.lyl.shortlink.admin.dto.resp.UserActualRespDto;
import com.lyl.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.lyl.shortlink.admin.dto.resp.UserRespDto;
import com.lyl.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(value = "/api/short-link/admin/v1/user/{username}")
    public Result<UserRespDto> getUserByUsername(@PathVariable("username") String username){
        return Results.success(userService.getUserByUsername(username));
    }

    @GetMapping(value = "/api/short-link/admin/v1/actual/user/{username}")
    public Result<UserActualRespDto> getActualUserByUsername(@PathVariable("username") String username){
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDto.class));
    }

    @GetMapping(value = "/api/short-link/admin/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username){
        return Results.success(userService.hasUsername(username));
    }

    @PostMapping("/api/short-link/admin/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }

    @PutMapping("/api/short-link/admin/v1/user")
    public Result<Void> modify(@RequestBody UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Results.success();
    }

    @PostMapping("/api/short-link/admin/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.login(requestParam));
    }

    @GetMapping("/api/short-link/admin/v1/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username, @RequestParam("token") String token) {
        return Results.success(userService.checkLogin(username, token));
    }

    @DeleteMapping("/api/short-link/admin/v1/user/logout")
    public Result<Void> logout(@RequestParam("username") String username, @RequestParam("token") String token) {
        userService.logout(username, token);
        return Results.success();
    }
}
