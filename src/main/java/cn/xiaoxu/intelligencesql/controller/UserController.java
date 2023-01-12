package cn.xiaoxu.intelligencesql.controller;

import cn.xiaoxu.intelligencesql.annotation.AuthCheck;
import cn.xiaoxu.intelligencesql.common.BaseResponse;
import cn.xiaoxu.intelligencesql.common.DeleteRequest;
import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.common.ResultUtils;
import cn.xiaoxu.intelligencesql.constant.UserConstant;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.model.dto.*;
import cn.xiaoxu.intelligencesql.model.entity.User;
import cn.xiaoxu.intelligencesql.model.vo.UserVO;
import cn.xiaoxu.intelligencesql.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 * @author https://github.com/xiaoxu9
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Resource
	private UserService userService;

	/**
	 * 用户注册
	 *
	 * @param userRegisterRequest
	 * @return
	 */
	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
		if (userRegisterRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String userName = userRegisterRequest.getUserName();
		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		if (StringUtils.isAnyBlank(userName, userAccount, userPassword, checkPassword)) {
			return null;
		}
		long result = userService.userRegister(userName, userAccount, userPassword, checkPassword, UserConstant.DEFAULT_ROLE);
		return ResultUtils.success(result);
	}

	/**
	 * 用户登录
	 *
	 * @param userLoginRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/login")
	public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
		if (userLoginRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.userLogin(userAccount, userPassword, request);
		return ResultUtils.success(user);
	}

	/**
	 * 用户注销
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/logout")
	public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
		if (request == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean result = userService.userLogout(request);
		return ResultUtils.success(result);
	}

	/**
	 * 获取当前登录用户
	 *
	 * @param request
	 * @return
	 */
	@GetMapping("/get/login")
	public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
		if (request == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(loginUser, userVO);
		return ResultUtils.success(userVO);
	}

	// endregion

	// region 增删改查

	/**
	 * 创建用户
	 *
	 * @param userAddRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/add")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
		System.out.println(userAddRequest);
		if (userAddRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = new User();
		BeanUtils.copyProperties(userAddRequest, user);
		// 加密
		String password = userService.getMd5Password(user.getUserPassword());
		user.setUserPassword(password);
		boolean result = userService.save(user);
		if (!result) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR);
		}
		return ResultUtils.success(user.getId());
	}


	/**
	 * 删除用户
	 *
	 * @param deleteRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Long id = deleteRequest.getId();
		// 判断是否存在
		User user = userService.getById(id);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		boolean result = userService.removeById(deleteRequest.getId());
		return ResultUtils.success(result);
	}

	/**
	 * 更新用户
	 *
	 * @param userUpdateRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
	                                        HttpServletRequest request) {
		if (userUpdateRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = new User();
		BeanUtils.copyProperties(userUpdateRequest, user);
		// 判断是否存在
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 加密密码
		String password = userService.getMd5Password(user.getUserPassword());
		user.setUserPassword(password);
		boolean result = userService.updateById(user);
		return ResultUtils.success(result);
	}

	/**
	 * 根据 id 获取用户
	 *
	 * @param id
	 * @param request
	 * @return
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<UserVO> getUserById(int id, HttpServletRequest request) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getById(id);
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(user, userVO);
		return ResultUtils.success(userVO);
	}

	/**
	 * 获取用户列表
	 *
	 * @param userQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/list")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
		if (userQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User userQuery = new User();
		BeanUtils.copyProperties(userQueryRequest, userQuery);
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		List<User> userList = userService.list(queryWrapper);
		List<UserVO> userVOList = userList.stream().map(user -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			return userVO;
		}).collect(Collectors.toList());
		return ResultUtils.success(userVOList);
	}

	/**
	 * 分页获取用户列表
	 *
	 * @param userQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
		long current = 1;
		long pageSize = 10;
		User userQuery = new User();
		if (userQueryRequest != null) {
			BeanUtils.copyProperties(userQueryRequest, userQuery);
			current = userQueryRequest.getCurrent();
			pageSize = userQueryRequest.getPageSize();
		}
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		Page<User> userPage = userService.page(new Page<>(current, pageSize), queryWrapper);
		Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
		List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			return userVO;
		}).collect(Collectors.toList());
		userVOPage.setRecords(userVOList);
		return ResultUtils.success(userVOPage);
	}
}
