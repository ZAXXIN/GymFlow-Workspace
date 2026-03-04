package com.gymflow.service.settings;

import com.gymflow.dto.settings.webUser.WebUserQueryDTO;
import com.gymflow.dto.settings.webUser.WebUserBasicDTO;
import com.gymflow.dto.settings.webUser.WebUserDetailDTO;
import com.gymflow.vo.PageResultVO;
import com.gymflow.vo.settings.WebUserListVO;

public interface WebUserService {

    /**
     * 分页查询用户列表
     */
    PageResultVO<WebUserListVO> getUserList(WebUserQueryDTO queryDTO);

    /**
     * 获取用户详情
     */
    WebUserDetailDTO getUserDetail(Long userId);

    /**
     * 新增用户
     */
    Long addUser(WebUserBasicDTO userDTO);

    /**
     * 编辑用户
     */
    void updateUser(Long userId, WebUserBasicDTO userDTO);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username, Long excludeUserId);
}