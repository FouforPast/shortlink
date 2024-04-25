package com.lyl.shortlink.project.common.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ValidDateTypeEnum {
    PERMANENT(0, "永久有效"),

    CUSTOM(1, "自定义");


    private final Integer code;

    private final String desc;
}
