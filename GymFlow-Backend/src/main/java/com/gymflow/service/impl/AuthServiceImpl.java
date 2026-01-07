package com.gymflow.service.impl;

import com.gymflow.dto.LoginDTO;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Admin;
import com.gymflow.entity.Coach;
import com.gymflow.entity.Member;
import com.gymflow.entity.User;
import com.gymflow.enums.UserRole;
import com.gymflow.mapper.AdminMapper;
import com.gymflow.mapper.CoachMapper;
import com.gymflow.mapper.MemberMapper;
import com.gymflow.mapper.UserMapper;
import com.gymflow.service.AuthService;
import com.gymflow.utils.JwtUtil;
import com.gymflow.utils.Result;
import com.gymflow.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private CoachMapper coachMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result login(LoginDTO loginDTO) {
        try {
            // 根据用户名查找用户
            User user = userMapper.selectByUsername(loginDTO.getUsername());
            if (user == null) {
                return Result.error("用户名或密码错误");
            }

            // 验证密码（实际应该加密验证）
            if (!user.getPassword().equals(loginDTO.getPassword())) {
                return Result.error("用户名或密码错误");
            }

            // 验证用户状态
            if (user.getStatus() != 1) {
                return Result.error("账号已被禁用");
            }

            // 验证角色
            if (loginDTO.getRole() != null && !user.getRole().toString().equals(loginDTO.getRole())) {
                return Result.error("角色不匹配");
            }

            // 更新最后登录时间
            userMapper.updateLastLoginTime(user.getId());

            // 生成token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().toString());

            // 构建返回结果
            LoginVO loginVO = new LoginVO();
            loginVO.setId(user.getId());
            loginVO.setUsername(user.getUsername());
            loginVO.setPhone(user.getPhone());
            loginVO.setEmail(user.getEmail());
            loginVO.setAvatar(user.getAvatar());
            loginVO.setRole(user.getRole());
            loginVO.setToken(token);
            loginVO.setRealName("");

            // 根据角色设置额外信息
            if (user.getRole() == UserRole.MEMBER) {
                Member member = memberMapper.selectByUserId(user.getId());
                if (member != null) {
                    loginVO.setMemberId(member.getId());
                    loginVO.setRealName(member.getRealName());
                }
            } else if (user.getRole() == UserRole.COACH) {
                Coach coach = coachMapper.selectByUserId(user.getId());
                if (coach != null) {
                    loginVO.setCoachId(coach.getId());
                    loginVO.setRealName(coach.getRealName());
                }
            } else if (user.getRole() == UserRole.ADMIN) {
                Admin admin = adminMapper.selectByUserId(user.getId());
                if (admin != null) {
                    loginVO.setAdminId(admin.getId());
                    loginVO.setRealName(admin.getRealName());
                    // 更新管理员登录信息
                    String ip = loginDTO.getIp() != null ? loginDTO.getIp() : "unknown";
                    adminMapper.updateLastLoginInfo(admin.getId(), ip);
                }
            }

            return Result.success("登录成功", loginVO);
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result register(RegisterDTO registerDTO) {
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
            user.setPassword(registerDTO.getPassword()); // 实际应该加密存储
            user.setPhone(registerDTO.getPhone());
            user.setEmail(registerDTO.getEmail());
            user.setRole(UserRole.fromCode(registerDTO.getRole()));
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            int userResult = userMapper.insert(user);
            if (userResult <= 0) {
                throw new RuntimeException("创建用户失败");
            }

            // 根据角色创建对应的记录
            if (registerDTO.getRole().equals("MEMBER")) {
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
            } else if (registerDTO.getRole().equals("COACH")) {
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
            } else if (registerDTO.getRole().equals("ADMIN")) {
                Admin admin = new Admin();
                admin.setUserId(user.getId());
                admin.setRealName(registerDTO.getRealName());
                admin.setDepartment("运营部");
                admin.setPosition("管理员");
                admin.setPermissions("[]");
                admin.setCreateTime(LocalDateTime.now());
                admin.setUpdateTime(LocalDateTime.now());

                int adminResult = adminMapper.insert(admin);
                if (adminResult <= 0) {
                    throw new RuntimeException("创建管理员失败");
                }
            }

            return Result.success("注册成功");
        } catch (Exception e) {
            throw new RuntimeException("注册失败：" + e.getMessage());
        }
    }

    @Override
    public Result logout(HttpServletRequest request) {
        try {
            // 实际应用中可能需要将token加入黑名单
            // 这里简单返回成功
            return Result.success("退出成功");
        } catch (Exception e) {
            return Result.error("退出失败：" + e.getMessage());
        }
    }

    @Override
    public Result getCurrentUserInfo(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }

            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);

            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            LoginVO loginVO = new LoginVO();
            loginVO.setId(user.getId());
            loginVO.setUsername(user.getUsername());
            loginVO.setPhone(user.getPhone());
            loginVO.setEmail(user.getEmail());
            loginVO.setAvatar(user.getAvatar());
            loginVO.setRole(user.getRole());
            loginVO.setRealName("");

            // 根据角色设置额外信息
            if (user.getRole() == UserRole.MEMBER) {
                Member member = memberMapper.selectByUserId(user.getId());
                if (member != null) {
                    loginVO.setMemberId(member.getId());
                    loginVO.setRealName(member.getRealName());
                }
            } else if (user.getRole() == UserRole.COACH) {
                Coach coach = coachMapper.selectByUserId(user.getId());
                if (coach != null) {
                    loginVO.setCoachId(coach.getId());
                    loginVO.setRealName(coach.getRealName());
                }
            } else if (user.getRole() == UserRole.ADMIN) {
                Admin admin = adminMapper.selectByUserId(user.getId());
                if (admin != null) {
                    loginVO.setAdminId(admin.getId());
                    loginVO.setRealName(admin.getRealName());
                }
            }

            return Result.success("获取成功", loginVO);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @Override
    public Result changePassword(String oldPassword, String newPassword, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }

            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);

            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证旧密码
            if (!user.getPassword().equals(oldPassword)) {
                return Result.error("原密码错误");
            }

            // 更新密码
            int result = userMapper.updatePassword(userId, newPassword);
            if (result > 0) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    @Override
    public Result resetPassword(Long userId, String newPassword, HttpServletRequest request) {
        try {
            // TODO: 验证当前用户是否有重置密码的权限
            // 可以检查当前登录用户是否为管理员

            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            int result = userMapper.updatePassword(userId, newPassword);
            if (result > 0) {
                return Result.success("密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            return Result.error("重置失败：" + e.getMessage());
        }
    }
}