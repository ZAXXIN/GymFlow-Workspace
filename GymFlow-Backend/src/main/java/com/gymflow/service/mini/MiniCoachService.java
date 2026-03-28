package com.gymflow.service.mini;

import com.gymflow.dto.coach.CoachFullDTO;
import com.gymflow.dto.member.HealthRecordDTO;
import com.gymflow.dto.member.MemberFullDTO;
import com.gymflow.dto.mini.*;

import java.time.LocalDate;
import java.util.List;

public interface MiniCoachService {

    /**
     * 获取当前教练信息
     */
    CoachFullDTO getMyInfo(Long coachId);

    /**
     * 获取我的课表
     */
    List<MiniScheduleDTO> getMySchedule(Long coachId, LocalDate date);

    /**
     * 获取课程学员列表
     */
    List<MiniCourseStudentDTO> getCourseStudents(Long coachId, Long courseId);

    /**
     * 获取课程学员列表（根据排课ID）
     */
    List<MiniCourseStudentDTO> getCourseStudentsBySchedule(Long coachId, Long scheduleId);

    /**
     * 获取会员详情（教练视角）
     */
    MemberFullDTO getMemberDetail(Long coachId, Long memberId);

    /**
     * 为会员添加健康档案
     */
    void addHealthRecord(Long coachId, Long memberId, HealthRecordDTO healthRecordDTO);

    /**
     * 获取财务统计数据
     */
    MiniFinanceStatsDTO getFinanceStats(Long coachId, String period);

    /**
     * 获取当前时段提醒
     */
    MiniReminderDTO getCurrentReminder(Long coachId);

    /**
     * 修改密码
     */
    void modifyPassword(Long coachId, String oldPassword, String newPassword);
}