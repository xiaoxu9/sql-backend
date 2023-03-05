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
import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
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
		// 判断用户是否已存在，账号不能重复
		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		userQueryWrapper.eq(StringUtils.isNotBlank(userAccount), "userAccount", userAccount);
		if(userService.getOne(userQueryWrapper) != null) {  // 用户已存在
			throw new BusinessException(ErrorCode.ACCOUNT_EXTSIS);
		}
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
		// System.out.println(userAddRequest);
		if (userAddRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = new User();
		BeanUtils.copyProperties(userAddRequest, user);
		// 判断用户是否存在
		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		userQueryWrapper.eq(StringUtils.isNotBlank(user.getUserAccount()), "userAccount", user.getUserAccount());
		if(userService.getOne(userQueryWrapper) != null) {  // 用户已存在
			throw new BusinessException(ErrorCode.ACCOUNT_EXTSIS);
		}
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
	//@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
	                                        HttpServletRequest request) {
		if (userUpdateRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User newUser = new User();
		BeanUtils.copyProperties(userUpdateRequest, newUser);
		// 判断是否为空
		if (newUser == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 判断传过来的用户是否存在
		User oldUser = userService.getById(newUser.getId());
		if (oldUser == null){
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 由于没有把密码传过来，所以得从就对象中获取
		String oldPassword = oldUser.getUserPassword();
		// 密码已加密过的，所以不需再加密
		newUser.setUserPassword(oldPassword);
		boolean result = userService.updateById(newUser);
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
	public BaseResponse<UserVO> getUserById(Long id, HttpServletRequest request) {
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
	//@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
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

	@PostMapping("/update/password")
	public BaseResponse<Boolean> updatePassword(@RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest) {
		if(userUpdatePasswordRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getById(userUpdatePasswordRequest.getId());
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 验证密码是否正确
		String oldPassword = userService.getMd5Password(userUpdatePasswordRequest.getOldPassword());
		if (!user.getUserPassword().equals(oldPassword)) {
			System.out.println(user.getUserPassword());
			throw new BusinessException(ErrorCode.PASSWORD_ERROR);
		}
		if (!userUpdatePasswordRequest.getUserPassword().equals(userUpdatePasswordRequest.getCheckPassword())){
			throw new BusinessException(ErrorCode.PASSWORD_ATYPISM);
		}
		String md5Password = userService.getMd5Password(userUpdatePasswordRequest.getUserPassword());
		user.setUserPassword(md5Password);
		boolean result = userService.updateById(user);
		return ResultUtils.success(result);
	}

	/**
	 * 头像上传
	 */
	@RequestMapping("/avatarUpload")
	@ResponseBody
	public BaseResponse<String> avatarUpload(@RequestParam("avatar") MultipartFile pic, HttpServletRequest request) throws IOException {
		/**
		 * 编码为字符串
		 */
		String s = Base64Utils.encodeToString(pic.getBytes());

		/**
		 * 解码成字节数组
		 */
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] bytes = decoder.decode(s);

		/**
		 * 字节流转文件
		 */
		String filename = pic.getOriginalFilename();
		// 截取文件后缀  .jpg
		String suffix = filename.substring(filename.lastIndexOf("."));
		// 截取文件名
		String caselsh = filename.substring(0, filename.lastIndexOf(".") - 1);
		// 设置文件上传所在目录  windows
		//File file = new File("E:\\avatarUpload");
		ApplicationHome ah = new ApplicationHome(getClass());
		// linux
		// 获取jar包所在目录，并在此目录下创建一个 avatarUpload 文件夹
		String filePath = ah.getSource().getParentFile().getAbsolutePath() + "/avatarUpload";
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdir();
		}
		String id = request.getParameter("id");
		User user = userService.getById(id);
		// 删除原来的图片,再重新写入新的
		if (StringUtils.isNotBlank(user.getUserAvatar())) {
			//File file2 = new File(file.getPath() + "\\" + user.getUserAvatar());
			File file1 = new File(file.getPath() + "/" + user.getUserAvatar());
			if (file1.exists()) {
				file1.delete();
			}
			// 不管最后存不存在，都重新把头像文件名置空
			user.setUserAvatar(null);
		}
		// 使用时间戳加原文件名来做文件名，防止文件冲突
		Date date = new Date();
		SimpleDateFormat sim = new SimpleDateFormat("yyyyMMdd");
		String time = sim.format(date);
		// 设置文件名
		//String pathName = file.getPath() + "\\" + caselsh + time + suffix;
		String pathName = file.getPath() + "/" + caselsh + time + suffix;
		File newFile = new File(pathName);
		if (newFile.exists()) {
			// 存在文件内容一样的文件，则创建一个新的
			newFile.createNewFile();
		}
		FileOutputStream fos = null;
		// 把图片写入对应路径
		try {
			fos = new FileOutputStream(newFile);
			fos.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fos.close();
		}
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		user.setUserAvatar(caselsh + time + suffix);
		userService.updateById(user);
		return ResultUtils.success(user.getUserAvatar());
	}

	/**
	 * 获取登录用户头像图片
	 * @return
	 */
	@PostMapping("/get/avatar")
	public void getLoginUserAvatar(Long id, HttpServletResponse response) throws Exception {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getById(id);
		String fileName = user.getUserAvatar();
		// linux
		ApplicationHome ah = new ApplicationHome(getClass());
		// 获取jar包所在目录，并在此目录下创建一个 avatarUpload 文件夹
		String filePath = ah.getSource().getParentFile().getAbsolutePath() + "/avatarUpload/" + fileName;
		// 绝对
		//String filePath = "/usr/local/project/sql/avatarUpload/" + fileName;
		File file = new File(filePath);
		if (file.exists()) {
			BufferedImage tempImg = ImageIO.read(file);
			// 将图片输出给浏览器
			// 获取文件后缀名
			String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
			response.setContentType("image/"+suffix);
			OutputStream os = response.getOutputStream();
			try {
				ImageIO.write(tempImg, suffix, os);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				os.close();
			}
		}
	}
}
