package com.gymflow.service;

import com.gymflow.dto.member.*;
import com.gymflow.vo.MemberListVO;
import com.gymflow.vo.PageResultVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MemberService {

    /**
     * 分页查询会员列表
     */
    PageResultVO<MemberListVO> getMemberList(MemberQueryDTO queryDTO);

    /**
     * 获取会员详情
     */
    MemberFullDTO getMemberDetail(Long memberId);

    /**
     * 添加会员
     */
    Long addMember(MemberBasicDTO basicDTO, HealthRecordDTO healthRecordDTO, MemberCardDTO cardDTO);

    /**
     * 更新会员
     */
    void updateMember(Long memberId, MemberBasicDTO basicDTO, MemberCardDTO cardDTO);
//    void updateMember(Long memberId, MemberBasicDTO basicDTO, HealthRecordDTO healthRecordDTO, MemberCardDTO cardDTO);

    /**
     * 删除会员（软删除）
     */
    void deleteMember(Long memberId);

    /**
     * 批量删除会员
     */
    void batchDeleteMember(List<Long> memberIds);

    /**
     * 续费会员卡
     */
    void renewMemberCard(Long memberId, MemberCardDTO cardDTO);

    /**
     * 获取会员健康档案列表
     */
    List<HealthRecordDTO> getHealthRecords(Long memberId);

    /**
     * 添加健康档案
     */
    void addHealthRecord(Long memberId, HealthRecordDTO healthRecordDTO);
}
