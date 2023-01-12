package cn.xiaoxu.intelligencesql.model.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户创建请求
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    /**
     * 密码
     */
    private String userPassword;

    private static final long serialVersionUID = 1L;
}