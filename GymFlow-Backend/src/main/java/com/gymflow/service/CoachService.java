package com.gymflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Coach;
import com.gymflow.entity.Member;
import com.gymflow.utils.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface CoachService {

    Result getCoachList(Page<Coach> page, String keyword, HttpServletRequest request);

    Result getActiveCoaches(HttpServletRequest request);

    Result getCoachDetail(Long id, HttpServletRequest request);

    Result addCoach(RegisterDTO registerDTO, HttpServletRequest request);

    Result updateCoach(Long id, Coach coach, HttpServletRequest request);

    Result deleteCoach(Long id, HttpServletRequest request);

    Result updateCoachStatus(Long id, Integer status, HttpServletRequest request);

    Result getCoachStudents(Long id, Page<Member> page, HttpServletRequest request);

    Result getCoachCourses(Long id, String date, String status, HttpServletRequest request);

    Result getCoachStats(Long id, String startDate, String endDate, HttpServletRequest request);
}