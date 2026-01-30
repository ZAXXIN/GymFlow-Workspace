package com.gymflow.service;

import com.gymflow.dto.coach.*;
import com.gymflow.vo.CoachListVO;
import com.gymflow.vo.PageResultVO;

import java.util.List;

public interface CoachService {

    /**
     * 分页查询教练列表
     */
    PageResultVO<CoachListVO> getCoachList(CoachQueryDTO queryDTO);

    /**
     * 获取教练详情
     */
    CoachFullDTO getCoachDetail(Long coachId);

    /**
     * 添加教练
     */
    Long addCoach(CoachBasicDTO basicDTO);

    /**
     * 更新教练
     */
    void updateCoach(Long coachId, CoachBasicDTO basicDTO);

    /**
     * 删除教练（软删除）
     */
    void deleteCoach(Long coachId);

    /**
     * 批量删除教练
     */
    void batchDeleteCoach(List<Long> coachIds);

    /**
     * 更新教练状态（离职/在职）
     */
    void updateCoachStatus(Long coachId, Integer status);

    /**
     * 获取教练排班列表
     */
    List<CoachScheduleDTO> getCoachSchedules(Long coachId);

    /**
     * 添加教练排班
     */
    void addCoachSchedule(Long coachId, CoachScheduleDTO scheduleDTO);

    /**
     * 更新教练排班
     */
    void updateCoachSchedule(Long scheduleId, CoachScheduleDTO scheduleDTO);

    /**
     * 删除教练排班
     */
    void deleteCoachSchedule(Long scheduleId);

    /**
     * 获取教练关联课程列表
     */
    List<CoachCourseDTO> getCoachCourses(Long coachId);
}