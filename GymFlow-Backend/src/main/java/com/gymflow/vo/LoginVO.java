package com.gymflow.vo;

import com.gymflow.enums.UserRole;
import lombok.Data;

@Data
public class LoginVO {

    private Long id;

    private String username;

    private String phone;

    private String email;

    private String avatar;

    private UserRole role;

    private String token;

    private Long memberId;

    private Long coachId;

    private Long adminId;

    private String realName;
}