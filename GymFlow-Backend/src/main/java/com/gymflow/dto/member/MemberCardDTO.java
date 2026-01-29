package com.gymflow.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MemberCardDTO {

    private Long productId;           // 商品ID
    private String cardName;          // 卡名称
    private Integer cardType;         // 卡类型：0-私教课，1-团课，2-月卡，3-年卡
    private LocalDate startDate;      // 开始日期
    private LocalDate endDate;        // 结束日期
    private Integer totalSessions;    // 总节数
    private Integer usedSessions;     // 已用节数
    private Integer remainingSessions;// 剩余节数
    private BigDecimal amount;        // 金额
}