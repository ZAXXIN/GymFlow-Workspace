package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.checkin.CheckInQueryDTO;
import com.gymflow.dto.checkin.CheckInReportDTO;
import com.gymflow.dto.checkin.CheckInStatsDTO;
import com.gymflow.entity.*;
import com.gymflow.entity.mini.MiniCheckinCode;
import com.gymflow.entity.mini.MiniMessage;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.mapper.mini.MiniCheckinCodeMapper;
import com.gymflow.mapper.mini.MiniMessageMapper;
import com.gymflow.service.CheckInService;
import com.gymflow.utils.SystemConfigValidator;
import com.gymflow.vo.CheckInDetailVO;
import com.gymflow.vo.CheckInListVO;
import com.gymflow.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRecordMapper checkInRecordMapper;
    private final MemberMapper memberMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CoachMapper coachMapper;
    private final MiniCheckinCodeMapper miniCheckinCodeMapper;
    private final MiniMessageMapper miniMessageMapper;
    private final SystemConfigValidator configValidator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public PageResultVO<CheckInListVO> getCheckInList(CheckInQueryDTO queryDTO) {
        log.info("查询签到列表，查询条件：{}", queryDTO);

        // 创建分页对象
        Page<CheckinRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();

        // 会员ID查询
        if (queryDTO.getMemberId() != null) {
            queryWrapper.eq(CheckinRecord::getMemberId, queryDTO.getMemberId());
        }

        // 会员姓名查询
        if (StringUtils.hasText(queryDTO.getMemberName())) {
            // 先查询匹配的会员ID
            LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
            memberQuery.like(Member::getRealName, queryDTO.getMemberName());
            List<Member> members = memberMapper.selectList(memberQuery);

            if (!CollectionUtils.isEmpty(members)) {
                List<Long> memberIds = members.stream()
                        .map(Member::getId)
                        .collect(Collectors.toList());
                queryWrapper.in(CheckinRecord::getMemberId, memberIds);
            } else {
                // 如果没有匹配的会员，返回空结果
                queryWrapper.eq(CheckinRecord::getMemberId, -1L);
            }
        }

        // 会员手机号查询
        if (StringUtils.hasText(queryDTO.getMemberPhone())) {
            LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
            memberQuery.like(Member::getPhone, queryDTO.getMemberPhone());
            List<Member> members = memberMapper.selectList(memberQuery);

            if (!CollectionUtils.isEmpty(members)) {
                List<Long> memberIds = members.stream()
                        .map(Member::getId)
                        .collect(Collectors.toList());
                queryWrapper.in(CheckinRecord::getMemberId, memberIds);
            } else {
                queryWrapper.eq(CheckinRecord::getMemberId, -1L);
            }
        }

        // 签到方式查询
        if (queryDTO.getCheckinMethod() != null) {
            queryWrapper.eq(CheckinRecord::getCheckinMethod, queryDTO.getCheckinMethod());
        }

        // 签到时间范围查询
        if (queryDTO.getStartDate() != null) {
            queryWrapper.ge(CheckinRecord::getCheckinTime, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            queryWrapper.le(CheckinRecord::getCheckinTime, queryDTO.getEndDate().atTime(23, 59, 59));
        }

        // 是否关联课程查询
        if (queryDTO.getHasCourseBooking() != null) {
            if (queryDTO.getHasCourseBooking()) {
                queryWrapper.isNotNull(CheckinRecord::getCourseBookingId);
            } else {
                queryWrapper.isNull(CheckinRecord::getCourseBookingId);
            }
        }

        // 按签到时间倒序排列
        queryWrapper.orderByDesc(CheckinRecord::getCheckinTime);

        // 执行分页查询
        IPage<CheckinRecord> checkInPage = checkInRecordMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        List<CheckInListVO> voList = checkInPage.getRecords().stream()
                .map(this::convertToCheckInListVO)
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, checkInPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public CheckInDetailVO getCheckInDetail(Long checkInId) {
        CheckinRecord record = checkInRecordMapper.selectById(checkInId);
        if (record == null) {
            throw new BusinessException("签到记录不存在");
        }

        CheckInDetailVO vo = new CheckInDetailVO();
        vo.setId(record.getId());
        vo.setMemberId(record.getMemberId());
        vo.setCheckinTime(record.getCheckinTime());
        vo.setCheckinMethod(record.getCheckinMethod());
        vo.setNotes(record.getNotes());
        vo.setCreateTime(record.getCreateTime());

        // 查询会员信息
        Member member = memberMapper.selectById(record.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getRealName());
            vo.setMemberPhone(member.getPhone());
            vo.setMemberNo(member.getMemberNo());
            vo.setGender(member.getGender());
        }

        // 如果是课程签到，查询预约和排课信息
        if (record.getCourseBookingId() != null) {
            vo.setCourseCheckIn(true);
            vo.setCourseBookingId(record.getCourseBookingId());

            CourseBooking booking = courseBookingMapper.selectById(record.getCourseBookingId());
            if (booking != null) {
                vo.setBookingTime(booking.getBookingTime());
                vo.setBookingStatus(booking.getBookingStatus());
                vo.setSignCode(booking.getSignCode());

                // 查询排课信息
                if (booking.getScheduleId() != null) {
                    CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
                    if (schedule != null) {
                        vo.setScheduleId(schedule.getId());
                        vo.setCourseDate(schedule.getScheduleDate());
                        vo.setStartTime(schedule.getStartTime());
                        vo.setEndTime(schedule.getEndTime());

                        // 查询课程信息
                        Course course = courseMapper.selectById(schedule.getCourseId());
                        if (course != null) {
                            vo.setCourseId(course.getId());
                            vo.setCourseName(course.getCourseName());
                            vo.setCourseType(course.getCourseType());
                        }

                        // 查询教练信息
                        Coach coach = coachMapper.selectById(schedule.getCoachId());
                        if (coach != null) {
                            vo.setCoachName(coach.getRealName());
                            vo.setCoachPhone(coach.getPhone());
                        }
                    }
                }
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void memberCheckIn(Long memberId, Integer checkinMethod, String notes) {
        log.info("会员签到，会员ID：{}，签到方式：{}", memberId, checkinMethod);

        // 验证会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 验证签到方式
        if (checkinMethod != 0 && checkinMethod != 1) {
            throw new BusinessException("签到方式值只能是0（教练签到）或1（前台签到）");
        }

        // ========== 新增：验证营业时间（只能在营业时间内签到） ==========
        LocalTime now = LocalTime.now();
        configValidator.validateBusinessHours(now, now.plusMinutes(1));

        // 检查今日是否已签到（自由训练签到）
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckinRecord::getMemberId, memberId);
        queryWrapper.isNull(CheckinRecord::getCourseBookingId); // 自由训练签到
        queryWrapper.between(CheckinRecord::getCheckinTime, startOfDay, endOfDay);

        Long todayCheckInCount = checkInRecordMapper.selectCount(queryWrapper);
        if (todayCheckInCount > 0) {
            throw new BusinessException("今日已进行过自由训练签到");
        }

        // 创建签到记录
        CheckinRecord checkinRecord = new CheckinRecord();
        checkinRecord.setMemberId(memberId);
        checkinRecord.setCourseBookingId(null); // 自由训练，不关联课程预约
        checkinRecord.setCheckinTime(LocalDateTime.now());
        checkinRecord.setCheckinMethod(checkinMethod);
        checkinRecord.setNotes(notes != null ? notes : "自由训练签到");

        int result = checkInRecordMapper.insert(checkinRecord);
        if (result <= 0) {
            throw new BusinessException("签到失败");
        }

        // 更新会员签到次数
        updateMemberCheckinStats(memberId);

        log.info("会员签到成功，签到ID：{}，会员：{}", checkinRecord.getId(), member.getRealName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void courseCheckIn(Long bookingId, Integer checkinMethod, String notes) {
        log.info("课程签到，预约ID：{}，签到方式：{}", bookingId, checkinMethod);

        // 验证预约记录是否存在
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("课程预约不存在");
        }

        // 检查预约状态
        if (booking.getBookingStatus() != 0) {
            throw new BusinessException("预约状态不正确，无法签到");
        }

        // 验证签到方式
        if (checkinMethod != 0 && checkinMethod != 1) {
            throw new BusinessException("签到方式值只能是0（教练签到）或1（前台签到）");
        }

        // ========== 新增：验证营业时间（只能在营业时间内签到） ==========
        LocalTime now = LocalTime.now();
        configValidator.validateBusinessHours(now, now.plusMinutes(1));

        // 检查是否已签到
        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckinRecord::getCourseBookingId, bookingId);

        Long checkInCount = checkInRecordMapper.selectCount(queryWrapper);
        if (checkInCount > 0) {
            throw new BusinessException("该预约已签到");
        }

        // 创建签到记录
        CheckinRecord checkinRecord = new CheckinRecord();
        checkinRecord.setMemberId(booking.getMemberId());
        checkinRecord.setCourseBookingId(bookingId);
        checkinRecord.setCheckinTime(LocalDateTime.now());
        checkinRecord.setCheckinMethod(checkinMethod);
        checkinRecord.setNotes(notes != null ? notes : "课程签到");

        int result = checkInRecordMapper.insert(checkinRecord);
        if (result <= 0) {
            throw new BusinessException("签到失败");
        }

        // 更新预约状态为已签到
        booking.setBookingStatus(1); // 已签到
        booking.setCheckinTime(LocalDateTime.now());

        // 设置自动完成时间
        int autoCompleteHours = configValidator.getAutoCompleteHours();
        if (autoCompleteHours > 0) {
            booking.setAutoCompleteTime(LocalDateTime.now().plusHours(autoCompleteHours));
        }

        courseBookingMapper.updateById(booking);

        // 更新会员签到次数
        updateMemberCheckinStats(booking.getMemberId());

        log.info("课程签到成功，签到ID：{}，预约ID：{}", checkinRecord.getId(), bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void courseCheckInByCode(Long bookingId, String checkinCode, Integer checkinMethod, String notes) {
        log.info("课程签到（带数字码），预约ID：{}，数字码：{}", bookingId, checkinCode);

        // 验证预约记录是否存在
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("课程预约不存在");
        }

        // 验证数字码
        validateCheckinCode(bookingId, checkinCode);

        // 执行签到
        executeCourseCheckIn(booking, checkinMethod, notes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyByCode(String checkinCode, Integer checkinMethod, String notes) {
        log.info("通过数字码核销，数字码：{}", checkinCode);

        // 查询签到码
        LambdaQueryWrapper<MiniCheckinCode> codeQuery = new LambdaQueryWrapper<>();
        codeQuery.eq(MiniCheckinCode::getDigitalCode, checkinCode);
        codeQuery.eq(MiniCheckinCode::getStatus, 0); // 未使用

        MiniCheckinCode checkinCodeEntity = miniCheckinCodeMapper.selectOne(codeQuery);
        if (checkinCodeEntity == null) {
            throw new BusinessException("无效的签到码或已使用");
        }

        // 验证有效期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(checkinCodeEntity.getValidStartTime()) ||
                now.isAfter(checkinCodeEntity.getValidEndTime())) {
            throw new BusinessException("签到码不在有效期内");
        }

        // 获取预约记录
        CourseBooking booking = courseBookingMapper.selectById(checkinCodeEntity.getBookingId());
        if (booking == null) {
            throw new BusinessException("预约记录不存在");
        }

        // 执行签到
        executeCourseCheckIn(booking, checkinMethod, notes);
    }

    /**
     * 执行课程签到
     */
    private void executeCourseCheckIn(CourseBooking booking, Integer checkinMethod, String notes) {
        // 检查预约状态
        if (booking.getBookingStatus() != 0) {
            throw new BusinessException("预约状态不正确，无法签到");
        }

        // 获取排课信息验证签到时间
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        LocalDateTime courseDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        configValidator.validateCheckInTime(courseDateTime);

        // 检查是否已签到
        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckinRecord::getCourseBookingId, booking.getId());

        Long checkInCount = checkInRecordMapper.selectCount(queryWrapper);
        if (checkInCount > 0) {
            throw new BusinessException("该预约已签到");
        }

        // 创建签到记录
        CheckinRecord checkinRecord = new CheckinRecord();
        checkinRecord.setMemberId(booking.getMemberId());
        checkinRecord.setCourseBookingId(booking.getId());
        checkinRecord.setCheckinTime(LocalDateTime.now());
        checkinRecord.setCheckinMethod(checkinMethod);
        checkinRecord.setNotes(notes != null ? notes : "课程签到");

        int result = checkInRecordMapper.insert(checkinRecord);
        if (result <= 0) {
            throw new BusinessException("签到失败");
        }

        // 更新预约状态为已签到
        booking.setBookingStatus(1); // 已签到
        booking.setCheckinTime(LocalDateTime.now());

        // 设置自动完成时间
        int autoCompleteHours = configValidator.getAutoCompleteHours();
        if (autoCompleteHours > 0) {
            booking.setAutoCompleteTime(LocalDateTime.now().plusHours(autoCompleteHours));
        }

        courseBookingMapper.updateById(booking);

        // 更新签到码状态
        if (booking.getCheckinCodeId() != null) {
            MiniCheckinCode checkinCode = miniCheckinCodeMapper.selectById(booking.getCheckinCodeId());
            if (checkinCode != null) {
                checkinCode.setStatus(1); // 已使用
                checkinCode.setCheckinTime(LocalDateTime.now());
                miniCheckinCodeMapper.updateById(checkinCode);
            }
        }

        // 更新会员签到次数
        updateMemberCheckinStats(booking.getMemberId());

        // 发送消息通知
        sendCheckinNotification(booking, checkinMethod);

        log.info("课程签到成功，签到ID：{}，预约ID：{}", checkinRecord.getId(), booking.getId());
    }

    /**
     * 验证数字码
     */
    private void validateCheckinCode(Long bookingId, String checkinCode) {
        // 查询签到码
        LambdaQueryWrapper<MiniCheckinCode> codeQuery = new LambdaQueryWrapper<>();
        codeQuery.eq(MiniCheckinCode::getBookingId, bookingId);

        MiniCheckinCode codeEntity = miniCheckinCodeMapper.selectOne(codeQuery);
        if (codeEntity == null) {
            throw new BusinessException("预约未生成签到码");
        }

        // 验证数字码是否正确
        if (!codeEntity.getDigitalCode().equals(checkinCode)) {
            throw new BusinessException("数字码错误");
        }

        // 验证状态
        if (codeEntity.getStatus() != 0) {
            throw new BusinessException("签到码已使用或已过期");
        }

        // 验证有效期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(codeEntity.getValidStartTime()) ||
                now.isAfter(codeEntity.getValidEndTime())) {
            throw new BusinessException("签到码不在有效期内");
        }
    }

    /**
     * 发送签到成功通知
     */
    private void sendCheckinNotification(CourseBooking booking, Integer checkinMethod) {
        try {
            // 获取会员信息
            Member member = memberMapper.selectById(booking.getMemberId());
            if (member == null) return;

            // 获取课程信息
            Course course = courseMapper.selectById(booking.getCourseId());
            if (course == null) return;

            // 创建消息
            MiniMessage message = new MiniMessage();
            message.setUserId(member.getId());
            message.setUserType(0); // 会员
            message.setMessageType("CHECKIN_SUCCESS");
            message.setTitle("签到成功");
            message.setContent(String.format("您已成功签到课程：%s，签到方式：%s",
                    course.getCourseName(),
                    checkinMethod == 0 ? "教练签到" : "前台签到"));
            message.setRelatedId(booking.getId());

            miniMessageMapper.insert(message);

            log.info("发送签到成功通知，会员ID：{}", member.getId());
        } catch (Exception e) {
            log.error("发送签到通知失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheckIn(Long checkInId, String notes) {
        log.info("更新签到信息，签到ID：{}", checkInId);

        // 验证签到记录是否存在
        CheckinRecord checkinRecord = checkInRecordMapper.selectById(checkInId);
        if (checkinRecord == null) {
            throw new BusinessException("签到记录不存在");
        }

        // 只能更新当天或近期的签到记录
        LocalDateTime checkinTime = checkinRecord.getCheckinTime();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        if (checkinTime.isBefore(oneWeekAgo)) {
            throw new BusinessException("只能更新最近一周内的签到记录");
        }

        // 更新备注
        if (StringUtils.hasText(notes)) {
            checkinRecord.setNotes(notes);
        }

        int result = checkInRecordMapper.updateById(checkinRecord);
        if (result <= 0) {
            throw new BusinessException("更新签到信息失败");
        }

        log.info("更新签到信息成功，签到ID：{}", checkInId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCheckIn(Long checkInId) {
        log.info("删除签到记录，签到ID：{}", checkInId);

        // 验证签到记录是否存在
        CheckinRecord checkinRecord = checkInRecordMapper.selectById(checkInId);
        if (checkinRecord == null) {
            throw new BusinessException("签到记录不存在");
        }

        // 如果关联课程预约，不能删除
        if (checkinRecord.getCourseBookingId() != null) {
            throw new BusinessException("课程签到记录不能删除，请取消课程预约");
        }

        // 删除签到记录
        int result = checkInRecordMapper.deleteById(checkInId);
        if (result <= 0) {
            throw new BusinessException("删除签到记录失败");
        }

        // 更新会员签到次数
        if (checkinRecord.getMemberId() != null) {
            updateMemberCheckinStatsAfterDeletion(checkinRecord.getMemberId());
        }

        log.info("删除签到记录成功，签到ID：{}", checkInId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCheckIns(List<Long> checkInIds) {
        log.info("批量删除签到记录，ID列表：{}", checkInIds);

        if (CollectionUtils.isEmpty(checkInIds)) {
            log.warn("批量删除签到记录ID列表为空");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Long checkInId : checkInIds) {
            try {
                deleteCheckIn(checkInId);
                successCount++;
            } catch (Exception e) {
                log.error("删除签到记录失败，ID：{}，错误：{}", checkInId, e.getMessage());
                failCount++;
            }
        }

        log.info("批量删除完成，成功：{}，失败：{}", successCount, failCount);
    }

    @Override
    public PageResultVO<CheckInListVO> getMemberCheckIns(Long memberId, CheckInQueryDTO queryDTO) {
        log.info("获取会员签到记录，会员ID：{}", memberId);

        // 验证会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 设置会员ID查询条件
        queryDTO.setMemberId(memberId);

        // 调用通用查询方法
        return getCheckInList(queryDTO);
    }

    @Override
    public CheckInStatsDTO getTodayCheckInStats() {
        log.info("获取今日签到统计");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        CheckInStatsDTO statsDTO = new CheckInStatsDTO();

        // 查询今日总签到数
        LambdaQueryWrapper<CheckinRecord> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.between(CheckinRecord::getCheckinTime, startOfDay, endOfDay);
        Long totalCheckIns = checkInRecordMapper.selectCount(totalQuery);
        statsDTO.setTotalCheckIns(totalCheckIns.intValue());

        // 查询今日课程签到数
        LambdaQueryWrapper<CheckinRecord> courseQuery = new LambdaQueryWrapper<>();
        courseQuery.between(CheckinRecord::getCheckinTime, startOfDay, endOfDay);
        courseQuery.isNotNull(CheckinRecord::getCourseBookingId);
        Long courseCheckIns = checkInRecordMapper.selectCount(courseQuery);
        statsDTO.setCourseCheckIns(courseCheckIns.intValue());

        // 查询今日自由训练签到数
        LambdaQueryWrapper<CheckinRecord> freeQuery = new LambdaQueryWrapper<>();
        freeQuery.between(CheckinRecord::getCheckinTime, startOfDay, endOfDay);
        freeQuery.isNull(CheckinRecord::getCourseBookingId);
        Long freeCheckIns = checkInRecordMapper.selectCount(freeQuery);
        statsDTO.setFreeCheckIns(freeCheckIns.intValue());

        // 查询今日教练签到数
        LambdaQueryWrapper<CheckinRecord> coachQuery = new LambdaQueryWrapper<>();
        coachQuery.between(CheckinRecord::getCheckinTime, startOfDay, endOfDay);
        coachQuery.eq(CheckinRecord::getCheckinMethod, 0);
        Long coachCheckIns = checkInRecordMapper.selectCount(coachQuery);
        statsDTO.setCoachCheckIns(coachCheckIns.intValue());

        // 查询今日前台签到数
        LambdaQueryWrapper<CheckinRecord> frontDeskQuery = new LambdaQueryWrapper<>();
        frontDeskQuery.between(CheckinRecord::getCheckinTime, startOfDay, endOfDay);
        frontDeskQuery.eq(CheckinRecord::getCheckinMethod, 1);
        Long frontDeskCheckIns = checkInRecordMapper.selectCount(frontDeskQuery);
        statsDTO.setFrontDeskCheckIns(frontDeskCheckIns.intValue());

        // 查询今日签到会员数
        LambdaQueryWrapper<CheckinRecord> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.between(CheckinRecord::getCheckinTime, startOfDay, endOfDay);
        memberQuery.select(CheckinRecord::getMemberId);

        List<Object> memberIdObjects = checkInRecordMapper.selectObjs(memberQuery);
        Set<Long> distinctMemberIds = memberIdObjects.stream()
                .filter(Objects::nonNull)
                .map(obj -> ((Number) obj).longValue())
                .collect(Collectors.toSet());

        statsDTO.setUniqueMembers(distinctMemberIds.size());
        statsDTO.setStatDate(today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // 计算签到率（假设今日活跃会员数为10，实际应该从会员表查询）
        int activeMembers = 10; // 这里应该从业务逻辑获取
        if (activeMembers > 0) {
            double checkInRate = ((double) distinctMemberIds.size() / activeMembers) * 100;
            statsDTO.setCheckInRate(Math.round(checkInRate * 100.0) / 100.0);
        } else {
            statsDTO.setCheckInRate(0.0);
        }

        return statsDTO;
    }

    @Override
    public CheckInReportDTO getCheckInReport(CheckInQueryDTO queryDTO) {
        log.info("获取签到统计报表，查询条件：{}", queryDTO);

        CheckInReportDTO reportDTO = new CheckInReportDTO();

        // 设置查询时间范围
        LocalDateTime startTime = queryDTO.getStartDate() != null ?
                queryDTO.getStartDate().atStartOfDay() :
                LocalDate.now().minusDays(30).atStartOfDay();

        LocalDateTime endTime = queryDTO.getEndDate() != null ?
                queryDTO.getEndDate().atTime(23, 59, 59) :
                LocalDateTime.now();

        // 查询签到数据
        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(CheckinRecord::getCheckinTime, startTime, endTime);

        // 添加其他查询条件
        if (queryDTO.getMemberId() != null) {
            queryWrapper.eq(CheckinRecord::getMemberId, queryDTO.getMemberId());
        }

        if (queryDTO.getCheckinMethod() != null) {
            queryWrapper.eq(CheckinRecord::getCheckinMethod, queryDTO.getCheckinMethod());
        }

        if (queryDTO.getHasCourseBooking() != null) {
            if (queryDTO.getHasCourseBooking()) {
                queryWrapper.isNotNull(CheckinRecord::getCourseBookingId);
            } else {
                queryWrapper.isNull(CheckinRecord::getCourseBookingId);
            }
        }

        List<CheckinRecord> checkInRecords = checkInRecordMapper.selectList(queryWrapper);

        // 统计签到数据
        int totalCheckIns = checkInRecords.size();
        int courseCheckIns = 0;
        int freeCheckIns = 0;
        int coachCheckIns = 0;
        int frontDeskCheckIns = 0;

        Map<Long, Boolean> memberMap = new HashMap<>();

        for (CheckinRecord record : checkInRecords) {
            // 统计课程签到和自由训练签到
            if (record.getCourseBookingId() != null) {
                courseCheckIns++;
            } else {
                freeCheckIns++;
            }

            // 统计签到方式
            if (record.getCheckinMethod() == 0) {
                coachCheckIns++;
            } else if (record.getCheckinMethod() == 1) {
                frontDeskCheckIns++;
            }

            // 记录唯一会员
            memberMap.put(record.getMemberId(), true);
        }

        int uniqueMembers = memberMap.size();

        // 计算平均值
        double avgCheckInsPerMember = uniqueMembers > 0 ?
                (double) totalCheckIns / uniqueMembers : 0;

        // 设置报表数据
        reportDTO.setTotalCheckIns(totalCheckIns);
        reportDTO.setCourseCheckIns(courseCheckIns);
        reportDTO.setFreeCheckIns(freeCheckIns);
        reportDTO.setCoachCheckIns(coachCheckIns);
        reportDTO.setFrontDeskCheckIns(frontDeskCheckIns);
        reportDTO.setUniqueMembers(uniqueMembers);
        reportDTO.setAvgCheckInsPerMember(Math.round(avgCheckInsPerMember * 100.0) / 100.0);
        reportDTO.setReportTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 生成每日签到趋势数据
        Map<String, Integer> dailyTrend = generateDailyTrend(startTime, endTime, checkInRecords);
        reportDTO.setDailyTrend(dailyTrend);

        // 生成会员活跃度排名
        List<Map<String, Object>> memberRanking = generateMemberRanking(startTime, endTime);
        reportDTO.setMemberRanking(memberRanking);

        return reportDTO;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 将CheckinRecord实体转换为CheckInListVO
     */
    private CheckInListVO convertToCheckInListVO(CheckinRecord checkinRecord) {
        CheckInListVO vo = new CheckInListVO();

        // 设置基本信息
        vo.setId(checkinRecord.getId());
        vo.setMemberId(checkinRecord.getMemberId());
        vo.setCheckinTime(checkinRecord.getCheckinTime());
        vo.setCheckinMethod(checkinRecord.getCheckinMethod());
        vo.setCheckinMethodDesc(getCheckinMethodDesc(checkinRecord.getCheckinMethod()));
        vo.setNotes(checkinRecord.getNotes());
        vo.setCreateTime(checkinRecord.getCreateTime());

        // 获取会员信息
        Member member = memberMapper.selectById(checkinRecord.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getRealName());
            vo.setMemberPhone(member.getPhone());
            vo.setMemberNo(member.getMemberNo());
        }

        // 判断是否为课程签到
        boolean isCourseCheckIn = checkinRecord.getCourseBookingId() != null;
        vo.setCourseCheckIn(isCourseCheckIn);

        if (isCourseCheckIn) {
            vo.setCourseBookingId(checkinRecord.getCourseBookingId());

            // 获取课程预约信息
            CourseBooking booking = courseBookingMapper.selectById(checkinRecord.getCourseBookingId());
            if (booking != null) {
                // 获取排课信息
                CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
                if (schedule != null) {
                    // 获取课程信息
                    Course course = courseMapper.selectById(schedule.getCourseId());
                    if (course != null) {
                        vo.setCourseName(course.getCourseName());
                        vo.setCourseType(course.getCourseType());
                        vo.setCourseTypeDesc(getCourseTypeDesc(course.getCourseType()));
                    }

                    // 获取教练信息
                    Coach coach = coachMapper.selectById(schedule.getCoachId());
                    if (coach != null) {
                        vo.setCoachName(coach.getRealName());
                    }
                }
            }
        }

        return vo;
    }

    /**
     * 更新会员签到次数
     */
    private void updateMemberCheckinStats(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member != null) {
            // 查询会员总签到次数
            LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CheckinRecord::getMemberId, memberId);
            Long totalCheckins = checkInRecordMapper.selectCount(queryWrapper);

            member.setTotalCheckins(totalCheckins.intValue());
            memberMapper.updateById(member);

            log.debug("更新会员签到次数，会员ID：{}，总签到次数：{}", memberId, totalCheckins);
        }
    }

    /**
     * 删除签到记录后更新会员签到次数
     */
    private void updateMemberCheckinStatsAfterDeletion(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member != null) {
            // 查询删除后的总签到次数
            LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CheckinRecord::getMemberId, memberId);
            Long totalCheckins = checkInRecordMapper.selectCount(queryWrapper);

            member.setTotalCheckins(totalCheckins.intValue());
            memberMapper.updateById(member);

            log.debug("删除后更新会员签到次数，会员ID：{}，总签到次数：{}", memberId, totalCheckins);
        }
    }

    /**
     * 生成每日签到趋势数据
     */
    private Map<String, Integer> generateDailyTrend(LocalDateTime startTime, LocalDateTime endTime,
                                                    List<CheckinRecord> checkInRecords) {
        Map<String, Integer> dailyTrend = new LinkedHashMap<>();

        // 按日期分组统计
        Map<String, Integer> dateCountMap = checkInRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getCheckinTime().format(DATE_FORMATTER),
                        Collectors.summingInt(record -> 1)
                ));

        // 填充所有日期，包括没有签到的日期
        LocalDate currentDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            dailyTrend.put(dateStr, dateCountMap.getOrDefault(dateStr, 0));
            currentDate = currentDate.plusDays(1);
        }

        return dailyTrend;
    }

    /**
     * 生成会员活跃度排名
     */
    private List<Map<String, Object>> generateMemberRanking(LocalDateTime startTime, LocalDateTime endTime) {
        // 查询指定时间范围内的所有签到记录
        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(CheckinRecord::getCheckinTime, startTime, endTime);
        List<CheckinRecord> records = checkInRecordMapper.selectList(queryWrapper);

        // 按会员ID分组统计签到次数
        Map<Long, Long> checkinCountMap = records.stream()
                .collect(Collectors.groupingBy(CheckinRecord::getMemberId, Collectors.counting()));

        // 转换为List并按签到次数降序排序
        List<Map.Entry<Long, Long>> sortedEntries = new ArrayList<>(checkinCountMap.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;

        for (Map.Entry<Long, Long> entry : sortedEntries) {
            Long memberId = entry.getKey();
            Long checkinCount = entry.getValue();

            Member member = memberMapper.selectById(memberId);
            if (member != null) {
                Map<String, Object> rankingItem = new HashMap<>();
                rankingItem.put("rank", rank++);
                rankingItem.put("memberId", memberId);
                rankingItem.put("memberName", member.getRealName());
                rankingItem.put("memberPhone", member.getPhone());
                rankingItem.put("memberNo", member.getMemberNo());
                rankingItem.put("checkinCount", checkinCount);

                result.add(rankingItem);
            }
        }

        return result;
    }

    /**
     * 获取签到方式描述
     */
    private String getCheckinMethodDesc(Integer method) {
        if (method == null) return "未知";
        switch (method) {
            case 0: return "教练签到";
            case 1: return "前台签到";
            default: return "未知";
        }
    }

    /**
     * 获取课程类型描述
     */
    private String getCourseTypeDesc(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 0: return "私教课";
            case 1: return "团课";
            default: return "未知";
        }
    }
}