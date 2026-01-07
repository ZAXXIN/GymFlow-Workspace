package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.entity.CheckIn;
import com.gymflow.entity.Course;
import com.gymflow.mapper.CheckInMapper;
import com.gymflow.mapper.CourseMapper;
import com.gymflow.service.CheckInService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class CheckInServiceImpl implements CheckInService {

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public Result getCheckInList(Page<CheckIn> page, Long memberId, Long courseId, String status, HttpServletRequest request) {
        try {
            QueryWrapper<CheckIn> queryWrapper = new QueryWrapper<>();

            if (memberId != null) {
                queryWrapper.eq("member_id", memberId);
            }

            if (courseId != null) {
                queryWrapper.eq("course_id", courseId);
            }

            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }

            queryWrapper.orderByDesc("checkin_time");

            Page<CheckIn> result = checkInMapper.selectPage(page, queryWrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result checkIn(String signCode, HttpServletRequest request) {
        try {
            // 验证签到码
            Course course = courseMapper.selectBySignCode(signCode);
            if (course == null) {
                return Result.error("签到码无效或已过期");
            }

            // TODO: 需要获取当前会员ID
            Long memberId = 1L; // 临时写死，实际应从token获取

            // 检查是否已签到
            int alreadyCheckedIn = checkInMapper.checkMemberCourseCheckIn(memberId, course.getId());
            if (alreadyCheckedIn > 0) {
                return Result.error("今日已签到该课程");
            }

            // 创建签到记录
            CheckIn checkIn = new CheckIn();
            checkIn.setMemberId(memberId);
            checkIn.setCourseId(course.getId());
            checkIn.setSignCode(signCode);
            checkIn.setCheckinTime(LocalDateTime.now());
            checkIn.setCheckinType("自助签到");
            checkIn.setStatus(com.gymflow.enums.CheckInStatus.SUCCESS);
            checkIn.setCreateTime(LocalDateTime.now());
            checkIn.setUpdateTime(LocalDateTime.now());

            int result = checkInMapper.insert(checkIn);
            if (result > 0) {
                // 更新会员签到次数
                // TODO: 需要会员Mapper
                // memberMapper.incrementCheckins(memberId);
                return Result.success("签到成功");
            } else {
                return Result.error("签到失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("签到失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result batchCheckIn(Long courseId, String memberIds, HttpServletRequest request) {
        try {
            // TODO: 实现批量签到逻辑
            // 1. 解析memberIds
            // 2. 批量更新签到状态
            // 3. 更新会员签到次数

            return Result.success("批量签到功能待实现");
        } catch (Exception e) {
            throw new RuntimeException("批量签到失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMemberCheckIns(String startDate, String endDate, HttpServletRequest request) {
        try {
            // TODO: 需要获取当前会员ID
            Long memberId = 1L; // 临时写死，实际应从token获取

            return Result.success(checkInMapper.selectMemberCheckIns(memberId,
                    startDate != null ? java.time.LocalDate.parse(startDate) : null,
                    endDate != null ? java.time.LocalDate.parse(endDate) : null));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getCourseCheckIns(Long id, HttpServletRequest request) {
        try {
            return Result.success(checkInMapper.selectCourseCheckIns(id));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateCheckInStatus(Long id, String status, String notes, HttpServletRequest request) {
        try {
            int result = checkInMapper.updateCheckInStatus(id, status, notes);
            if (result > 0) {
                return Result.success("签到状态更新成功");
            } else {
                return Result.error("签到状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result getTodayCheckInStats(HttpServletRequest request) {
        try {
            Integer count = checkInMapper.selectTodayCheckInCount();
            return Result.success(count != null ? count : 0);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getCheckInTrend(Integer days, HttpServletRequest request) {
        try {
            return Result.success(checkInMapper.selectCheckInStatsByDay(days));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getActiveMembers(Integer days, HttpServletRequest request) {
        try {
            return Result.success(checkInMapper.selectActiveMembers(days));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result exportCheckIns(String startDate, String endDate, HttpServletRequest request) {
        // TODO: 实现导出签到记录逻辑
        return Result.success("导出签到记录功能待实现");
    }
}