package com.gymflow.service.mini;

import com.gymflow.dto.member.HealthRecordDTO;
import com.gymflow.dto.member.MemberFullDTO;
import com.gymflow.dto.mini.MiniReminderDTO;

public interface MiniMemberService {

    /**
     * 获取当前会员完整信息（包含会员卡、健康档案、课程包）
     */
    MemberFullDTO getMyInfo(Long memberId);

    /**
     * 获取当前时段提醒
     */
    MiniReminderDTO getCurrentReminder(Long memberId);

    /**
     * 添加健康档案
     */
    void addHealthRecord(Long memberId, HealthRecordDTO healthRecordDTO);

    /**
     * 修改密码
     */
    void modifyPassword(Long memberId, String oldPassword, String newPassword);
}