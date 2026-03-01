package com.gymflow.service;

import com.gymflow.dto.checkin.CheckInQueryDTO;
import com.gymflow.dto.checkin.CheckInReportDTO;
import com.gymflow.dto.checkin.CheckInStatsDTO;
import com.gymflow.vo.CheckInListVO;
import com.gymflow.vo.CheckInDetailVO;
import com.gymflow.vo.PageResultVO;

import java.util.List;

public interface CheckInService {

    /**
     * 分页查询签到列表
     */
    PageResultVO<CheckInListVO> getCheckInList(CheckInQueryDTO queryDTO);

    /**
     * 获取签到详情
     */
    CheckInDetailVO getCheckInDetail(Long checkInId);

    /**
     * 会员签到
     */
    void memberCheckIn(Long memberId, Integer checkinMethod, String notes);

    /**
     * 课程签到（关联课程预约）
     */
    void courseCheckIn(Long bookingId, Integer checkinMethod, String notes);

    /**
     * 更新签到信息
     */
    void updateCheckIn(Long checkInId, String notes);

    /**
     * 删除签到记录
     */
    void deleteCheckIn(Long checkInId);

    /**
     * 批量删除签到记录
     */
    void batchDeleteCheckIns(List<Long> checkInIds);

    /**
     * 获取会员签到记录
     */
    PageResultVO<CheckInListVO> getMemberCheckIns(Long memberId, CheckInQueryDTO queryDTO);

    /**
     * 获取今日签到统计
     */
    CheckInStatsDTO getTodayCheckInStats();

    /**
     * 获取签到统计报表
     */
    CheckInReportDTO getCheckInReport(CheckInQueryDTO queryDTO);
}