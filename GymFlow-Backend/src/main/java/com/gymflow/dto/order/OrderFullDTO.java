package com.gymflow.dto.order;

import com.gymflow.dto.member.MemberBasicDTO;
import com.gymflow.entity.PaymentRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "订单完整信息DTO")
public class OrderFullDTO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "会员信息")
    private MemberBasicDTO memberInfo;

    @Schema(description = "订单类型：0-会籍卡，1-私教课，2-团课，3-相关产品")
    private Integer orderType;

    @Schema(description = "订单类型描述")
    private String orderTypeDesc;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "实付金额")
    private BigDecimal actualAmount;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "订单项列表")
    private List<OrderItemDTO> orderItems;

    @Schema(description = "支付记录列表")
    private List<PaymentRecord> paymentRecords;

    /**
     * 订单状态：WAIT_PAY, PAID, COMPLETED, CANCELLED, REFUNDED
     */
    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "订单状态描述")
    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case "WAIT_PAY": return "待支付";
            case "PAID": return "已支付";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            case "REFUNDED": return "已退款";
            default: return status;
        }
    }

    public String getOrderTypeDesc() {
        if (orderType == null) return "未知";
        switch (orderType) {
            case 0: return "会籍卡";
            case 1: return "私教课";
            case 2: return "团课";
            case 3: return "相关产品";
            default: return "未知";
        }
    }
}