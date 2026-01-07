package com.gymflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CheckInStatus {

    SUCCESS("SUCCESS", "签到成功"),
    FAILED("FAILED", "签到失败"),
    PENDING("PENDING", "待签到");

    @EnumValue
    @JsonValue
    private final String code;
    private final String description;

    CheckInStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}