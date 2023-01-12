package cn.xiaoxu.intelligencesql.service;

import cn.xiaoxu.intelligencesql.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author XiaoXuTongXue
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2022-12-30 16:35:19
*/
public interface UserService extends IService<User> {

	/**
	 * 用户注册
	 *
	 * @param userName 用户名
	 * @param userAccount 用户账户
	 * @param userPassword 用户密码
	 * @param checkPassword 校验密码
	 * @param userRole 用户角色
	 * @return 新用户 id
	 */
	long userRegister(String userName, String userAccount, String userPassword, String checkPassword, String userRole);

	/**
	 * 用户登录
	 *
	 * @param userAccount 用户账户
	 * @param userPassword 用户密码
	 * @param request
	 * @return 脱敏后的用户信息
	 */
	User userLogin(String userAccount, String userPassword, HttpServletRequest request);

	/**
	 * 获取当前登录用户
	 *
	 * @param request
	 * @return
	 */
	User getLoginUser(HttpServletRequest request);

	/**
	 * 是否为管理员
	 *
	 * @param request
	 * @return
	 */
	boolean isAdmin(HttpServletRequest request);

	/**
	 * 用户注销
	 *
	 * @param request
	 * @return
	 */
	boolean userLogout(HttpServletRequest request);

	/**
	 * 加密方法
	 * @param userPassword
	 * @return
	 */
	String getMd5Password(String userPassword);
}
