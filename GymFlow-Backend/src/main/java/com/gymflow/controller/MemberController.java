package com.gymflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Member;
import com.gymflow.service.MemberService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * 获取会员列表（分页）
     */
    @GetMapping("/list")
    public Result getMemberList(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) String keyword,
                                HttpServletRequest request) {
        Page<Member> page = new Page<>(pageNum, pageSize);
        return memberService.getMemberList(page, keyword, request);
    }

    /**
     * 获取会员详情
     */
    @GetMapping("/detail/{id}")
    public Result getMemberDetail(@PathVariable Long id, HttpServletRequest request) {
        return memberService.getMemberDetail(id, request);
    }

    /**
     * 添加会员
     */
    @PostMapping("/add")
    public Result addMember(@Validated @RequestBody RegisterDTO registerDTO, HttpServletRequest request) {
        return memberService.addMember(registerDTO, request);
    }

    /**
     * 更新会员信息
     */
    @PutMapping("/update/{id}")
    public Result updateMember(@PathVariable Long id, @RequestBody Member member, HttpServletRequest request) {
        return memberService.updateMember(id, member, request);
    }

    /**
     * 删除会员
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteMember(@PathVariable Long id, HttpServletRequest request) {
        return memberService.deleteMember(id, request);
    }

    /**
     * 更新会员状态
     */
    @PutMapping("/status/{id}")
    public Result updateMemberStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     HttpServletRequest request) {
        return memberService.updateMemberStatus(id, status, request);
    }

    /**
     * 更新会员会籍信息
     */
    @PutMapping("/membership/{id}")
    public Result updateMemberMembership(@PathVariable Long id,
                                         @RequestParam String membershipType,
                                         @RequestParam String startDate,
                                         @RequestParam String endDate,
                                         HttpServletRequest request) {
        return memberService.updateMemberMembership(id, membershipType, startDate, endDate, request);
    }

    /**
     * 分配教练给会员
     */
    @PutMapping("/assign-coach/{id}")
    public Result assignCoachToMember(@PathVariable Long id,
                                      @RequestParam Long coachId,
                                      HttpServletRequest request) {
        return memberService.assignCoachToMember(id, coachId, request);
    }

    /**
     * 获取会员健康档案
     */
    @GetMapping("/health/{id}")
    public Result getMemberHealthRecords(@PathVariable Long id,
                                         @RequestParam(required = false) String startDate,
                                         @RequestParam(required = false) String endDate,
                                         HttpServletRequest request) {
        return memberService.getMemberHealthRecords(id, startDate, endDate, request);
    }

    /**
     * 获取会员训练计划
     */
    @GetMapping("/training-plan/{id}")
    public Result getMemberTrainingPlans(@PathVariable Long id, HttpServletRequest request) {
        return memberService.getMemberTrainingPlans(id, request);
    }
}