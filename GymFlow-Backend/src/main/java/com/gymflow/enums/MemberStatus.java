package com.gymflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MemberStatus {

    ACTIVE("ACTIVE", "活跃"),
    INACTIVE("INACTIVE", "不活跃"),
    FROZEN("FROZEN", "冻结"),
    EXPIRED("EXPIRED", "过期"),
    CANCELLED("CANCELLED", "已注销");

    @EnumValue
    @JsonValue
    private final String code;
    private final String description;

    MemberStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}