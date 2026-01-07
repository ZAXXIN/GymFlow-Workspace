package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Coach;
import com.gymflow.entity.Member;
import com.gymflow.entity.User;
import com.gymflow.enums.UserRole;
import com.gymflow.mapper.CoachMapper;
import com.gymflow.mapper.MemberMapper;
import com.gymflow.mapper.UserMapper;
import com.gymflow.service.MemberService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CoachMapper coachMapper;  // 添加CoachMapper依赖

    @Override
    public Result getMemberList(Page<Member> page, String keyword, HttpServletRequest request) {
        try {
            Page<Member> result = memberMapper.selectPage(page, new QueryWrapper<Member>()
                    .like(keyword != null, "real_name", keyword)
                    .or()
                    .like(keyword != null, "member_no", keyword)
                    .orderByDesc("create_time"));
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMemberDetail(Long id, HttpServletRequest request) {
        try {
            Member member = memberMapper.selectById(id);
            if (member == null) {
                return Result.error("会员不存在");
            }
            return Result.success(member);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result addMember(RegisterDTO registerDTO, HttpServletRequest request) {
        try {
            // 检查用户名是否已存在
            if (userMapper.countByUsername(registerDTO.getUsername()) > 0) {
                return Result.error("用户名已存在");
            }

            // 检查手机号是否已存在
            if (userMapper.countByPhone(registerDTO.getPhone()) > 0) {
                return Result.error("手机号已存在");
            }

            // 创建用户
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setPassword(registerDTO.getPassword()); // 实际应该加密
            user.setPhone(registerDTO.getPhone());
            user.setEmail(registerDTO.getEmail());
            user.setRole(UserRole.MEMBER);
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            int userResult = userMapper.insert(user);
            if (userResult <= 0) {
                throw new RuntimeException("创建用户失败");
            }

            // 创建会员
            Member member = new Member();
            member.setUserId(user.getId());
            member.setMemberNo("M" + System.currentTimeMillis());
            member.setRealName(registerDTO.getRealName());
            member.setGender(registerDTO.getGender());
            if (registerDTO.getBirthday() != null) {
                member.setBirthday(java.time.LocalDate.parse(registerDTO.getBirthday()));
            }
            member.setMembershipType("体验会员");
            member.setStatus(com.gymflow.enums.MemberStatus.ACTIVE);
            member.setCreateTime(LocalDateTime.now());
            member.setUpdateTime(LocalDateTime.now());

            int memberResult = memberMapper.insert(member);
            if (memberResult <= 0) {
                throw new RuntimeException("创建会员失败");
            }

            return Result.success("会员添加成功");
        } catch (Exception e) {
            throw new RuntimeException("添加会员失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateMember(Long id, Member member, HttpServletRequest request) {
        try {
            Member existingMember = memberMapper.selectById(id);
            if (existingMember == null) {
                return Result.error("会员不存在");
            }

            member.setId(id);
            member.setUpdateTime(LocalDateTime.now());
            int result = memberMapper.updateById(member);

            if (result > 0) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result deleteMember(Long id, HttpServletRequest request) {
        try {
            Member member = memberMapper.selectById(id);
            if (member == null) {
                return Result.error("会员不存在");
            }

            // 删除会员记录
            int memberResult = memberMapper.deleteById(id);
            if (memberResult <= 0) {
                return Result.error("删除会员失败");
            }

            // 删除对应的用户记录
            int userResult = userMapper.deleteById(member.getUserId());
            if (userResult <= 0) {
                throw new RuntimeException("删除用户失败");
            }

            return Result.success("删除成功");
        } catch (Exception e) {
            throw new RuntimeException("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateMemberStatus(Long id, String status, HttpServletRequest request) {
        try {
            int result = memberMapper.updateStatus(id, status);
            if (result > 0) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateMemberMembership(Long id, String membershipType, String startDate, String endDate, HttpServletRequest request) {
        try {
            int result = memberMapper.updateMembership(id, membershipType, startDate, endDate);
            if (result > 0) {
                return Result.success("会籍信息更新成功");
            } else {
                return Result.error("会籍信息更新失败");
            }
        } catch (Exception e) {
            return Result.error("会籍信息更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result assignCoachToMember(Long id, Long coachId, HttpServletRequest request) {
        try {
            // 首先检查会员是否存在
            Member member = memberMapper.selectById(id);
            if (member == null) {
                return Result.error("会员不存在");
            }

            // 检查教练是否存在
            Coach coach = coachMapper.selectById(coachId);
            if (coach == null) {
                return Result.error("教练不存在");
            }

            // 更新会员的教练ID
            int result = memberMapper.updatePersonalCoach(id, coachId);
            if (result > 0) {
                // 更新教练的学员数 - 使用CoachMapper中的正确方法
                coachMapper.incrementStudents(coachId);
                return Result.success("教练分配成功");
            } else {
                return Result.error("教练分配失败");
            }
        } catch (Exception e) {
            return Result.error("教练分配失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMemberHealthRecords(Long id, String startDate, String endDate, HttpServletRequest request) {
        // TODO: 实现获取会员健康档案逻辑
        return Result.success("获取健康档案功能待实现");
    }

    @Override
    public Result getMemberTrainingPlans(Long id, HttpServletRequest request) {
        // TODO: 实现获取会员训练计划逻辑
        return Result.success("获取训练计划功能待实现");
    }
}