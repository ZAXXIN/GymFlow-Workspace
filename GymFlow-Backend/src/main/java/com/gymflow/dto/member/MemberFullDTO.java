package com.gymflow.dto.member;

import com.gymflow.dto.mini.MiniMemberCardDTO;
import com.gymflow.dto.mini.MyCourseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "会员完整信息DTO")
public class MemberFullDTO {

    @NotNull(message = "会员ID不能为空")
    @Positive(message = "会员ID必须为正数")
    @Schema(description = "会员主键ID", example = "10001", required = true)
    private Long id;

    @NotBlank(message = "会员编号不能为空")
    @Size(max = 20, message = "会员编号长度不能超过20")
    @Schema(description = "会员编号", example = "GYM20260128001", required = true)
    private String memberNo;

    @Schema(description = "登录用户名（手机号）", example = "13800138000")
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000", required = true)
    private String phone;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50")
    @Schema(description = "会员真实姓名", example = "张三", required = true)
    private String realName;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别值只能是0或1")
    @Max(value = 1, message = "性别值只能是0或1")
    @Schema(description = "性别：0-女，1-男", example = "1", required = true)
    private Integer gender;

    @NotNull(message = "创建时间不能为空")
    @Schema(description = "会员创建/注册时间", example = "2026-01-01T10:00:00", required = true)
    private LocalDateTime createTime;

    @Schema(description = "会员有效期开始日期", example = "2026-01-01")
    private LocalDate membershipStartDate;

    @Schema(description = "会员有效期结束日期", example = "2027-01-01")
    private LocalDate membershipEndDate;

    @Min(value = 0, message = "总签到次数不能小于0")
    @Schema(description = "累计签到次数", example = "25")
    private Integer totalCheckins;

    @Min(value = 0, message = "总课程时长不能小于0")
    @Schema(description = "累计课程时长（小时）", example = "80")
    private Integer totalCourseHours;

    @DecimalMin(value = "0.00", message = "总消费金额不能小于0")
    @Schema(description = "累计消费金额（元）", example = "5999.00")
    private BigDecimal totalSpent;

    // 健康档案列表
    @Schema(description = "会员健康档案列表")
    private List<HealthRecordDTO> healthRecords;

    // 会员卡列表（会籍卡 + 课程包）
    @Schema(description = "会员名下会员卡列表")
    private List<MiniMemberCardDTO> memberCards;

    // 课程包列表（单独返回，方便使用）
    @Schema(description = "会员购买的课程包列表")
    private List<MyCourseDTO> courses;

    // 课程记录列表
    @Schema(description = "会员课程记录列表")
    private List<CourseRecordDTO> courseRecords;

    // 签到记录列表
    @Schema(description = "会员签到记录列表")
    private List<CheckInRecordDTO> checkinRecords;

    public String getUsername() {
        return this.phone;
    }
}