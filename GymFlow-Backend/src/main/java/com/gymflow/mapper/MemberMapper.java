package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.entity.Member;
import com.gymflow.vo.MemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {

    /**
     * 根据用户ID查询会员
     */
    @Select("SELECT * FROM member WHERE user_id = #{userId}")
    Member selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询会员列表（带条件）
     */
    IPage<MemberVO> selectMemberPage(Page<MemberVO> page, @Param("keyword") String keyword);

    /**
     * 获取会员详情（包含用户信息）
     */
    MemberVO selectMemberDetail(@Param("memberId") Long memberId);

    /**
     * 更新会员状态
     */
    @Update("UPDATE member SET status = #{status} WHERE id = #{memberId}")
    int updateStatus(@Param("memberId") Long memberId, @Param("status") String status);

    /**
     * 更新会员会籍信息
     */
    @Update("UPDATE member SET membership_type = #{membershipType}, " +
            "membership_start_date = #{startDate}, membership_end_date = #{endDate} " +
            "WHERE id = #{memberId}")
    int updateMembership(@Param("memberId") Long memberId,
                         @Param("membershipType") String membershipType,
                         @Param("startDate") String startDate,
                         @Param("endDate") String endDate);

    /**
     * 更新教练分配
     */
    @Update("UPDATE member SET personal_coach_id = #{coachId} WHERE id = #{memberId}")
    int updatePersonalCoach(@Param("memberId") Long memberId, @Param("coachId") Long coachId);

    /**
     * 增加签到次数
     */
    @Update("UPDATE member SET total_checkins = total_checkins + 1 WHERE id = #{memberId}")
    int incrementCheckins(@Param("memberId") Long memberId);

    /**
     * 增加课程小时数
     */
    @Update("UPDATE member SET total_course_hours = total_course_hours + #{hours} WHERE id = #{memberId}")
    int addCourseHours(@Param("memberId") Long memberId, @Param("hours") Integer hours);

    /**
     * 根据手机号查询会员
     */
    @Select("SELECT m.* FROM member m " +
            "LEFT JOIN user u ON m.user_id = u.id " +
            "WHERE u.phone = #{phone}")
    Member selectByPhone(@Param("phone") String phone);
}