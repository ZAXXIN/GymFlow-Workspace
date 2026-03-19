// MiniCheckInService.java
package com.gymflow.service.mini;

import com.gymflow.dto.mini.MiniCheckinCodeDTO;
import com.gymflow.dto.mini.MiniReminderDTO;
import com.gymflow.dto.mini.MiniScanResultDTO;

public interface MiniCheckInService {

    /**
     * 获取签到码（会员端）
     */
    MiniCheckinCodeDTO getCheckinCode(Long memberId, Long bookingId);

    /**
     * 扫码核销（教练端）
     */
    MiniScanResultDTO scanCheckin(Long coachId, String qrCode);

    /**
     * 数字码核销（PC端）
     */
    MiniScanResultDTO verifyCode(String digitalCode, Integer checkinMethod, String notes);

    /**
     * 检查签到码是否有效
     */
    Boolean checkCodeValid(Long userId, Long bookingId);

    /**
     * 获取当前时段提醒
     * @param userId 用户ID
     * @param userType 用户类型：0-会员，1-教练
     */
    MiniReminderDTO getCurrentReminder(Long userId, Integer userType);
}