package com.gymflow.service.mini;

import com.gymflow.dto.mini.MemberStatisticsDTO;

public interface MemberStatisticsService {

    /**
     * 获取会员统计数据（签到记录 + 课时明细）
     * @param memberId 会员ID
     * @param orderItemId 订单项ID（卡片的唯一标识）
     */
    MemberStatisticsDTO getMemberStatistics(Long memberId, Long orderItemId);
}