package com.gymflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Member;
import com.gymflow.utils.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface MemberService {

    Result getMemberList(Page<Member> page, String keyword, HttpServletRequest request);

    Result getMemberDetail(Long id, HttpServletRequest request);

    Result addMember(RegisterDTO registerDTO, HttpServletRequest request);

    Result updateMember(Long id, Member member, HttpServletRequest request);

    Result deleteMember(Long id, HttpServletRequest request);

    Result updateMemberStatus(Long id, String status, HttpServletRequest request);

    Result updateMemberMembership(Long id, String membershipType, String startDate, String endDate, HttpServletRequest request);

    Result assignCoachToMember(Long id, Long coachId, HttpServletRequest request);

    Result getMemberHealthRecords(Long id, String startDate, String endDate, HttpServletRequest request);

    Result getMemberTrainingPlans(Long id, HttpServletRequest request);
}