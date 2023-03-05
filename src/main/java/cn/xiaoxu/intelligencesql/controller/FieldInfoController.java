package cn.xiaoxu.intelligencesql.controller;


import cn.xiaoxu.intelligencesql.annotation.AuthCheck;
import cn.xiaoxu.intelligencesql.common.BaseResponse;
import cn.xiaoxu.intelligencesql.common.DeleteRequest;
import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.common.ResultUtils;
import cn.xiaoxu.intelligencesql.constant.CommonConstant;
import cn.xiaoxu.intelligencesql.core.builder.SqlBuilder;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema.Field;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.model.dto.FieldInfoAddRequest;
import cn.xiaoxu.intelligencesql.model.dto.FieldInfoQueryRequest;
import cn.xiaoxu.intelligencesql.model.dto.FieldInfoUpdateRequest;
import cn.xiaoxu.intelligencesql.model.entity.FieldInfo;
import cn.xiaoxu.intelligencesql.model.entity.User;
import cn.xiaoxu.intelligencesql.model.enums.ReviewStatusEnum;
import cn.xiaoxu.intelligencesql.service.FieldInfoService;
import cn.xiaoxu.intelligencesql.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 字段信息接口
 *
 * @author https://github.com/xiaoxu9
 */
@RestController
@Slf4j
@RequestMapping("/field_info")
public class FieldInfoController {

	@Resource
	private FieldInfoService fieldInfoService;

	@Resource
	private UserService userService;

	private final static Gson GSON = new Gson();

	/**
	 * 创建
	 *
	 * @param fieldInfoAddRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addFieldInfo(@RequestBody FieldInfoAddRequest fieldInfoAddRequest, HttpServletRequest request) {
		if (fieldInfoAddRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		FieldInfo fieldInfo = new FieldInfo();
		BeanUtils.copyProperties(fieldInfoAddRequest, fieldInfo);
		// 校验
		fieldInfoService.validAndHandleFieldInfo(fieldInfo, true);
		// 默认审核状态为未提交(3-未提交)
		if (fieldInfo.getReviewStatus() != 3) {
			fieldInfo.setReviewStatus(3);
		}
		User loginUser = userService.getLoginUser(request);
		fieldInfo.setUserId(loginUser.getId());
		boolean result = fieldInfoService.save(fieldInfo);
		if (!result) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR);
		}
		return ResultUtils.success(fieldInfo.getId());
	}

	/**
	 * 删除
	 *
	 * @param deleteRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteFieldInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		Long id = deleteRequest.getId();
		// 判断是否存在
		FieldInfo fieldInfo = fieldInfoService.getById(id);
		if (fieldInfo == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 仅本人和管理员可以删除
		if (!fieldInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		boolean b = fieldInfoService.removeById(id);
		return ResultUtils.success(b);
	}

	/**
	 * 更新（仅管理员）
	 *
	 * @param fieldInfoUpdateRequest
	 * @return
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = "admin")
	public BaseResponse<Boolean> updateFieldInfo(@RequestBody FieldInfoUpdateRequest fieldInfoUpdateRequest) {
		if (fieldInfoUpdateRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		FieldInfo fieldInfo = new FieldInfo();
		BeanUtils.copyProperties(fieldInfoUpdateRequest, fieldInfo);
		// 参数校验
		fieldInfoService.validAndHandleFieldInfo(fieldInfo, false);
		// 判断是否存在
		long id = fieldInfoUpdateRequest.getId();
		FieldInfo oldFieldInfo = fieldInfoService.getById(id);
		if (oldFieldInfo == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 更新数据
		boolean b = fieldInfoService.updateById(fieldInfo);
		return ResultUtils.success(b);
	}

	/**
	 * 根据 id 获取
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	public BaseResponse<FieldInfo> getFieldInfoById(long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		FieldInfo fieldInfo = fieldInfoService.getById(id);
		return ResultUtils.success(fieldInfo);
	}

	/**
	 * 获取列表（仅管理员可使用）
	 *
	 * @param fieldInfoQueryRequest
	 * @return
	 */
	@AuthCheck(mustRole = "admin")
	@GetMapping("/list")
	public BaseResponse<List<FieldInfo>> listFieldInfo(FieldInfoQueryRequest fieldInfoQueryRequest) {
		List<FieldInfo> fieldInfoList = fieldInfoService.list(getQueryWrapper(fieldInfoQueryRequest));
		return ResultUtils.success(fieldInfoList);
	}

	/**
	 * 分页获取列表
	 *
	 * @param fieldInfoQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/list/page")
	public BaseResponse<Page<FieldInfo>> listFieldInfoByPage(FieldInfoQueryRequest fieldInfoQueryRequest,
	                                                         HttpServletRequest request) {
		if (fieldInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long current = fieldInfoQueryRequest.getCurrent();
		long pageSize = fieldInfoQueryRequest.getPageSize();
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Page<FieldInfo> fieldInfoPage = fieldInfoService.page(new Page<>(current, pageSize),
				getQueryWrapper(fieldInfoQueryRequest));
		return ResultUtils.success(fieldInfoPage);
	}

	/**
	 * 获取当前用户可选的全部资源列表（只返回 id 和名称）
	 *
	 * @param fieldInfoQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/my/list")
	public BaseResponse<List<FieldInfo>> listMyFieldInfo(FieldInfoQueryRequest fieldInfoQueryRequest,
	                                                     HttpServletRequest request) {
		if (fieldInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		FieldInfo fieldInfo = new FieldInfo();
		// 先查询所有审核通过的
		fieldInfo.setReviewStatus(ReviewStatusEnum.PASS.getValue());
		QueryWrapper<FieldInfo> queryWrapper = getQueryWrapper(fieldInfoQueryRequest);
		// 只查询 id 和 name
		final String[] fields = new String[]{"id", "name"};
		// 设置查询条件构造
		queryWrapper.select(fields);
		List<FieldInfo> fieldInfoList = fieldInfoService.list(queryWrapper);
		// 再查询本人的
		try {
			User loginUser = userService.getLoginUser(request);
			// 置空，防止干扰
			fieldInfo.setReviewStatus(null);
			// 关联当前用户id
			fieldInfo.setUserId(loginUser.getId());
			// 把fieldInfo作为查询构造条件
			queryWrapper = new QueryWrapper<>(fieldInfo);
			// 设置查询返回哪些字段
			queryWrapper.select(fields);
			// 合并所有通过审核以及本人的审核通过的  (会导致重复)
			fieldInfoList.addAll(fieldInfoService.list(queryWrapper));
		} catch (Exception e) {
			// 未登录
		}
		// 根据id去重
		ArrayList<FieldInfo> resultList = fieldInfoList.stream().collect(Collectors.collectingAndThen(
				Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(FieldInfo::getId))), ArrayList::new));
		return ResultUtils.success(resultList);
	}

	/**
	 * 分页获取当前用户可选的资源列表
	 *
	 * @param fieldInfoQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/my/list/page")
	public BaseResponse<Page<FieldInfo>> listMyFieldInfoByPage(FieldInfoQueryRequest fieldInfoQueryRequest,
	                                                           HttpServletRequest request) {
		if (fieldInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		long current = fieldInfoQueryRequest.getCurrent();
		long pageSize = fieldInfoQueryRequest.getPageSize();
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		QueryWrapper<FieldInfo> queryWrapper = getQueryWrapper(fieldInfoQueryRequest);
		// 查询userId是当前用户的(自己创建的)，或者 审核状态为通过的（其它人创建的）
		queryWrapper.eq("userId", loginUser.getId())
				.or()
				.eq("reviewStatus", ReviewStatusEnum.PASS.getValue());
		Page<FieldInfo> fieldInfoPage = fieldInfoService.page(new Page<>(current, pageSize), queryWrapper);
		return ResultUtils.success(fieldInfoPage);
	}

	/**
	 * 分页获取当前用户创建的资源列表
	 *
	 * @param fieldInfoQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/my/add/list/page")
	public BaseResponse<Page<FieldInfo>> listMyAddFieldInfoByPage(FieldInfoQueryRequest fieldInfoQueryRequest,
	                                                              HttpServletRequest request) {
		if (fieldInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		fieldInfoQueryRequest.setUserId(loginUser.getId());
		long current = fieldInfoQueryRequest.getCurrent();
		long pageSize = fieldInfoQueryRequest.getPageSize();
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Page<FieldInfo> fieldInfoPage = fieldInfoService.page(new Page<>(current, pageSize), getQueryWrapper(fieldInfoQueryRequest));
		return ResultUtils.success(fieldInfoPage);
	}

	/**
	 * 生成创建字段的 SQL
	 *
	 * @param id
	 * @return
	 */
	@PostMapping("/generate/sql")
	public BaseResponse<String> generateCreateSql(@RequestBody long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		FieldInfo fieldInfo = fieldInfoService.getById(id);
		if (fieldInfo == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 把json格式转换为自定格式的对象
		Field field = GSON.fromJson(fieldInfo.getContent(), Field.class);
		SqlBuilder sqlBuilder = new SqlBuilder();
		return ResultUtils.success(sqlBuilder.buildCreateFieldSql(field));
	}

	/**
	 * 获取查询包装类
	 *
	 * @param fieldInfoQueryRequest
	 * @return
	 */
	private QueryWrapper<FieldInfo> getQueryWrapper(FieldInfoQueryRequest fieldInfoQueryRequest) {
		if (fieldInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		FieldInfo fieldInfo = new FieldInfo();
		BeanUtils.copyProperties(fieldInfoQueryRequest, fieldInfo);
		String searchName = fieldInfoQueryRequest.getSearchName();
		String sortField = fieldInfoQueryRequest.getSortField();
		String sortOrder = fieldInfoQueryRequest.getSortOrder();
		String name = fieldInfo.getName();
		String content = fieldInfo.getContent();
		String fieldName = fieldInfo.getFieldName();
		// name、fieldName、content需要支持模糊查询
		fieldInfo.setFieldName(null);
		fieldInfo.setContent(null);
		fieldInfo.setFieldName(null);
		QueryWrapper<FieldInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
		queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
		queryWrapper.like(StringUtils.isNotBlank(fieldName), "fieldName", fieldName);
		// 同时按 name、fieldName 搜索
		if (StringUtils.isNotBlank(searchName)) {
			queryWrapper.like("name", searchName).or().like("fieldName", searchName);
		}
		queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
		return queryWrapper;
	}
}
