package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Coach;
import com.gymflow.entity.Course;
import com.gymflow.entity.User;
import com.gymflow.enums.UserRole;
import com.gymflow.mapper.CoachMapper;
import com.gymflow.mapper.UserMapper;
import com.gymflow.service.CoachService;
import com.gymflow.utils.Result;
import com.gymflow.vo.CoachVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CoachServiceImpl implements CoachService {

    @Autowired
    private CoachMapper coachMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result getCoachList(Page<Coach> page, String keyword, HttpServletRequest request) {
        try {
            QueryWrapper<Coach> queryWrapper = new QueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like("real_name", keyword)
                        .or()
                        .like("coach_no", keyword)
                        .or()
                        .like("phone", keyword);
            }

            queryWrapper.orderByDesc("create_time");

            Page<Coach> result = coachMapper.selectPage(page, queryWrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getActiveCoaches(HttpServletRequest request) {
        try {
            List<Coach> coaches = coachMapper.selectActiveCoaches();
            return Result.success(coaches);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getCoachDetail(Long id, HttpServletRequest request) {
        try {
            CoachVO coachVO = coachMapper.selectCoachDetail(id);
            if (coachVO == null) {
                return Result.error("教练不存在");
            }
            return Result.success(coachVO);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result addCoach(RegisterDTO registerDTO, HttpServletRequest request) {
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
            user.setRole(UserRole.COACH);
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            int userResult = userMapper.insert(user);
            if (userResult <= 0) {
                throw new RuntimeException("创建用户失败");
            }

            // 创建教练
            Coach coach = new Coach();
            coach.setUserId(user.getId());
            coach.setCoachNo("C" + System.currentTimeMillis());
            coach.setRealName(registerDTO.getRealName());
            coach.setPhone(registerDTO.getPhone());
            coach.setEmail(registerDTO.getEmail());
            coach.setSpecialty(registerDTO.getSpecialty());
            coach.setCertifications(registerDTO.getCertifications());
            coach.setStatus(1);
            coach.setCreateTime(LocalDateTime.now());
            coach.setUpdateTime(LocalDateTime.now());

            int coachResult = coachMapper.insert(coach);
            if (coachResult <= 0) {
                throw new RuntimeException("创建教练失败");
            }

            return Result.success("教练添加成功");
        } catch (Exception e) {
            throw new RuntimeException("添加教练失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateCoach(Long id, Coach coach, HttpServletRequest request) {
        try {
            Coach existingCoach = coachMapper.selectById(id);
            if (existingCoach == null) {
                return Result.error("教练不存在");
            }

            coach.setId(id);
            coach.setUpdateTime(LocalDateTime.now());
            int result = coachMapper.updateById(coach);

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
    public Result deleteCoach(Long id, HttpServletRequest request) {
        try {
            Coach coach = coachMapper.selectById(id);
            if (coach == null) {
                return Result.error("教练不存在");
            }

            // 删除教练记录
            int coachResult = coachMapper.deleteById(id);
            if (coachResult <= 0) {
                return Result.error("删除教练失败");
            }

            // 删除对应的用户记录
            int userResult = userMapper.deleteById(coach.getUserId());
            if (userResult <= 0) {
                throw new RuntimeException("删除用户失败");
            }

            return Result.success("删除成功");
        } catch (Exception e) {
            throw new RuntimeException("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateCoachStatus(Long id, Integer status, HttpServletRequest request) {
        try {
            int result = coachMapper.updateStatus(id, status);
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
    public Result getCoachStudents(Long id, Page<com.gymflow.entity.Member> page, HttpServletRequest request) {
        try {
            // 查询教练的学员数
            Integer studentCount = coachMapper.countStudents(id);
            if (studentCount == null) {
                studentCount = 0;
            }
            return Result.success("教练学员数：" + studentCount);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getCoachCourses(Long id, String date, String status, HttpServletRequest request) {
        try {
            List<Course> courses = coachMapper.selectCoachCourses(id, date, status);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getCoachStats(Long id, String startDate, String endDate, HttpServletRequest request) {
        try {
            // 查询教练的课程数
            Integer courseCount = coachMapper.countCourses(id);
            if (courseCount == null) {
                courseCount = 0;
            }

            // 查询教练的收入统计
            Map<String, Object> incomeStats = coachMapper.selectIncomeStats(id, startDate, endDate);

            // 构建返回结果
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("courseCount", courseCount);
            stats.put("incomeStats", incomeStats);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}