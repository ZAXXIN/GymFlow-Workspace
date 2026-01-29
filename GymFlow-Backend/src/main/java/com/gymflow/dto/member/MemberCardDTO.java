package com.gymflow.dto.member;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MemberCardDTO {

    private Long productId;
    private String productName;
    private Integer productType; // 商品类型：0-会籍卡，1-私教课，2-团课，3-相关产品
    private Integer cardType;    // 卡类型：0-私教课，1-团课，2-月卡，3-年卡，4-周卡，5-其他
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalSessions;    // 总课时数（仅私教课、团课有效）
    private Integer usedSessions;     // 已用课时数（仅私教课、团课有效）
    private Integer remainingSessions;// 剩余课时数（仅私教课、团课有效）
    private BigDecimal amount;
    private String status; // 状态：ACTIVE-有效，EXPIRED-过期，USED_UP-用完
}