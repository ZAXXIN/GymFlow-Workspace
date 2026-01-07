package com.gymflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CourseStatus {

    UPCOMING("UPCOMING", "即将开始"),
    ONGOING("ONGOING", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    @EnumValue
    @JsonValue
    private final String code;
    private final String description;

    CourseStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}