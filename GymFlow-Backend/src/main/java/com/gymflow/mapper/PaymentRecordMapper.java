package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}