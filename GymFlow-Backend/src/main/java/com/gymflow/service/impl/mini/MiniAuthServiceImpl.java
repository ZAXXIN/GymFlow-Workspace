package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.mini.MiniLoginDTO;
import com.gymflow.dto.mini.MiniLoginResultDTO;
import com.gymflow.entity.Coach;
import com.gymflow.entity.Member;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.CoachMapper;
import com.gymflow.mapper.MemberMapper;
import com.gymflow.service.mini.MiniAuthService;
import com.gymflow.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAuthServiceImpl implements MiniAuthService {

    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;
    private final JwtTokenUtil jwtTokenUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public MiniLoginResultDTO login(MiniLoginDTO loginDTO) {
        // 1. 参数校验
        if (!StringUtils.hasText(loginDTO.getPhone())) {
            throw new BusinessException("手机号不能为空");
        }
        if (!StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        // 2. 先查询是否为会员
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getPhone, loginDTO.getPhone());
        Member member = memberMapper.selectOne(memberQuery);

        if (member != null) {
            // 会员登录验证
            if (!passwordEncoder.matches(loginDTO.getPassword(), member.getPassword())) {
                log.warn("会员登录失败，密码错误，手机号: {}", loginDTO.getPhone());
                throw new BusinessException("手机号或密码错误");
            }

            // 生成token
            String token = jwtTokenUtil.generateToken(member.getId(), member.getPhone(), 3); // 3-会员角色

            // 构建返回结果
            MiniLoginResultDTO result = new MiniLoginResultDTO();
            result.setUserId(member.getId());
            result.setUserType(0); // 0-会员
            result.setPhone(member.getPhone());
            result.setRealName(member.getRealName());
            result.setMemberNo(member.getMemberNo());
            result.setToken(token);
            result.setLoginTime(LocalDateTime.now());

            log.info("会员登录成功: {}, 姓名: {}", member.getPhone(), member.getRealName());
            return result;
        }

        // 3. 如果不是会员，查询是否为教练
        LambdaQueryWrapper<Coach> coachQuery = new LambdaQueryWrapper<>();
        coachQuery.eq(Coach::getPhone, loginDTO.getPhone());
        Coach coach = coachMapper.selectOne(coachQuery);

        if (coach != null) {
            // 教练登录验证
            if (!passwordEncoder.matches(loginDTO.getPassword(), coach.getPassword())) {
                log.warn("教练登录失败，密码错误，手机号: {}", loginDTO.getPhone());
                throw new BusinessException("手机号或密码错误");
            }

            // 检查教练状态
            if (coach.getStatus() == 0) {
                throw new BusinessException("教练账号已被禁用");
            }

            // 生成token
            String token = jwtTokenUtil.generateToken(coach.getId(), coach.getPhone(), 2); // 2-教练角色

            // 构建返回结果
            MiniLoginResultDTO result = new MiniLoginResultDTO();
            result.setUserId(coach.getId());
            result.setUserType(1); // 1-教练
            result.setPhone(coach.getPhone());
            result.setRealName(coach.getRealName());
            result.setToken(token);
            result.setLoginTime(LocalDateTime.now());

            log.info("教练登录成功: {}, 姓名: {}", coach.getPhone(), coach.getRealName());
            return result;
        }

        // 4. 都不存在
        log.warn("登录失败，用户不存在，手机号: {}", loginDTO.getPhone());
        throw new BusinessException("手机号或密码错误");
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtTokenUtil.validateToken(token)) {
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            log.info("小程序用户登出: {}", phone);
        }
    }
}