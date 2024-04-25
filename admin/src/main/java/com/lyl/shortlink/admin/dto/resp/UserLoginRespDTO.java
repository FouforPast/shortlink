package com.lyl.shortlink.admin.dto.resp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserLoginRespDTO {

    String token;
}
