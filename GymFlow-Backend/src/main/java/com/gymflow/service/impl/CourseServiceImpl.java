package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.CourseDTO;
import com.gymflow.entity.Course;
import com.gymflow.mapper.CourseMapper;
import com.gymflow.service.CourseService;
import com.gymflow.utils.JwtUtil;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result getCourseList(Page<Course> page, String courseType, String status, String keyword, HttpServletRequest request) {
        try {
            QueryWrapper<Course> queryWrapper = new QueryWrapper<>();

            if (courseType != null && !courseType.isEmpty()) {
                queryWrapper.eq("course_type", courseType);
            }

            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }

            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like("course_name", keyword)
                        .or()
                        .like("coach_name", keyword)
                        .or()
                        .like("location", keyword);
            }

            queryWrapper.orderByDesc("course_date").orderByAsc("start_time");

            Page<Course> result = courseMapper.selectPage(page, queryWrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getCourseDetail(Long id, HttpServletRequest request) {
        try {
            Course course = courseMapper.selectById(id);
            if (course == null) {
                return Result.error("课程不存在");
            }
            return Result.success(course);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result addCourse(CourseDTO courseDTO, HttpServletRequest request) {
        try {
            Course course = new Course();
            course.setCourseName(courseDTO.getCourseName());
            course.setCourseType(courseDTO.getCourseType());
            course.setDescription(courseDTO.getDescription());
            course.setCoachId(courseDTO.getCoachId());
            course.setMaxCapacity(courseDTO.getMaxCapacity());
            course.setCourseDate(courseDTO.getCourseDate());
            course.setStartTime(courseDTO.getStartTime());
            course.setEndTime(courseDTO.getEndTime());
            course.setDuration(courseDTO.getDuration() != null ? courseDTO.getDuration() : 60);
            course.setPrice(courseDTO.getPrice());
            course.setLocation(courseDTO.getLocation());
            course.setStatus(com.gymflow.enums.CourseStatus.UPCOMING);
            course.setCreateTime(LocalDateTime.now());
            course.setUpdateTime(LocalDateTime.now());

            // TODO: 需要查询教练姓名并设置
            course.setCoachName("教练");

            int result = courseMapper.insert(course);
            if (result > 0) {
                return Result.success("课程添加成功");
            } else {
                return Result.error("课程添加失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("添加课程失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateCourse(Long id, CourseDTO courseDTO, HttpServletRequest request) {
        try {
            Course existingCourse = courseMapper.selectById(id);
            if (existingCourse == null) {
                return Result.error("课程不存在");
            }

            Course course = new Course();
            course.setId(id);
            course.setCourseName(courseDTO.getCourseName());
            course.setCourseType(courseDTO.getCourseType());
            course.setDescription(courseDTO.getDescription());
            course.setCoachId(courseDTO.getCoachId());
            course.setMaxCapacity(courseDTO.getMaxCapacity());
            course.setCourseDate(courseDTO.getCourseDate());
            course.setStartTime(courseDTO.getStartTime());
            course.setEndTime(courseDTO.getEndTime());
            course.setDuration(courseDTO.getDuration() != null ? courseDTO.getDuration() : 60);
            course.setPrice(courseDTO.getPrice());
            course.setLocation(courseDTO.getLocation());
            course.setUpdateTime(LocalDateTime.now());

            // TODO: 需要查询教练姓名并设置
            course.setCoachName("教练");

            int result = courseMapper.updateById(course);
            if (result > 0) {
                return Result.success("课程更新成功");
            } else {
                return Result.error("课程更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result deleteCourse(Long id, HttpServletRequest request) {
        try {
            int result = courseMapper.deleteById(id);
            if (result > 0) {
                return Result.success("课程删除成功");
            } else {
                return Result.error("课程删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateCourseStatus(Long id, String status, HttpServletRequest request) {
        try {
            int result = courseMapper.updateStatus(id, status);
            if (result > 0) {
                return Result.success("课程状态更新成功");
            } else {
                return Result.error("课程状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result getAvailableCourses(String courseType, String date, HttpServletRequest request) {
        try {
            // TODO: 需要获取当前会员ID
            Long memberId = 1L; // 临时写死，实际应从token获取

            return Result.success(courseMapper.selectAvailableCourses(memberId,
                    courseType,
                    date != null ? java.time.LocalDate.parse(date) : null));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getTodayCourses(HttpServletRequest request) {
        try {
            return Result.success(courseMapper.selectTodayCourses());
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result bookCourse(Long id, HttpServletRequest request) {
        try {
            Course course = courseMapper.selectById(id);
            if (course == null) {
                return Result.error("课程不存在");
            }

            if (course.getStatus() != com.gymflow.enums.CourseStatus.UPCOMING) {
                return Result.error("课程不可预约");
            }

            if (course.getMaxCapacity() != null &&
                    course.getCurrentEnrollment() >= course.getMaxCapacity()) {
                return Result.error("课程已满");
            }

            // TODO: 实现课程预约逻辑
            // 1. 检查是否已预约
            // 2. 创建预约记录
            // 3. 更新课程报名人数

            return Result.success("预约成功");
        } catch (Exception e) {
            return Result.error("预约失败：" + e.getMessage());
        }
    }

    @Override
    public Result cancelBooking(Long id, String reason, HttpServletRequest request) {
        // TODO: 实现取消预约逻辑
        return Result.success("取消预约功能待实现");
    }

    @Override
    public Result getCourseBookings(Long id, Page<Object> page, HttpServletRequest request) {
        // TODO: 实现获取课程预约列表逻辑
        return Result.success("获取预约列表功能待实现");
    }

    @Override
    public Result generateSignCode(Long id, HttpServletRequest request) {
        try {
            Course course = courseMapper.selectById(id);
            if (course == null) {
                return Result.error("课程不存在");
            }

            // 生成6位随机签到码
            String signCode = String.format("%06d", new Random().nextInt(1000000));
            LocalDateTime expireTime = LocalDateTime.now().plusMinutes(30); // 30分钟后过期

            int result = courseMapper.updateSignCode(id, signCode, expireTime.toString());
            if (result > 0) {
                return Result.success("签到码生成成功", signCode);
            } else {
                return Result.error("签到码生成失败");
            }
        } catch (Exception e) {
            return Result.error("生成失败：" + e.getMessage());
        }
    }

    @Override
    public Result verifySignCode(String signCode, HttpServletRequest request) {
        try {
            Course course = courseMapper.selectBySignCode(signCode);
            if (course == null) {
                return Result.error("签到码无效或已过期");
            }

            return Result.success("签到码有效", course);
        } catch (Exception e) {
            return Result.error("验证失败：" + e.getMessage());
        }
    }
}