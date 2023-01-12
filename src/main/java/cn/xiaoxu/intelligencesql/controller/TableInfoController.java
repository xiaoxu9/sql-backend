package cn.xiaoxu.intelligencesql.controller;

import cn.xiaoxu.intelligencesql.annotation.AuthCheck;
import cn.xiaoxu.intelligencesql.common.BaseResponse;
import cn.xiaoxu.intelligencesql.common.DeleteRequest;
import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.common.ResultUtils;
import cn.xiaoxu.intelligencesql.constant.CommonConstant;
import cn.xiaoxu.intelligencesql.core.builder.SqlBuilder;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.model.dto.TableInfoAddRequest;
import cn.xiaoxu.intelligencesql.model.dto.TableInfoQueryRequest;
import cn.xiaoxu.intelligencesql.model.dto.TableInfoUpdateRequest;
import cn.xiaoxu.intelligencesql.model.entity.TableInfo;
import cn.xiaoxu.intelligencesql.model.entity.User;
import cn.xiaoxu.intelligencesql.model.enums.ReviewStatusEnum;
import cn.xiaoxu.intelligencesql.service.TableInfoService;
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
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/table_info")
public class TableInfoController {

	@Resource
	private TableInfoService tableInfoService;

	@Resource
	private UserService userService;

	private final static Gson GSON = new Gson();

	/**
	 * 创建
	 *
	 * @param tableInfoAddRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addTableInfo(@RequestBody TableInfoAddRequest tableInfoAddRequest,
	                                       HttpServletRequest request) {
		if (tableInfoAddRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		TableInfo tableInfo = new TableInfo();
		BeanUtils.copyProperties(tableInfoAddRequest, tableInfo);
		// 校验
		tableInfoService.validAndHandleTableInfo(tableInfo, true);
		User loginUser = userService.getLoginUser(request);
		// 表信息和当前用户关联
		tableInfo.setUserId(loginUser.getId());
		// 保存
		boolean result = tableInfoService.save(tableInfo);
		if (!result) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR);
		}
		return ResultUtils.success(tableInfo.getId());
	}

	/**
	 * 删除
	 *
	 * @param deleteRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteTableInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Long id = deleteRequest.getId();
		// 判断存不存在
		TableInfo oldTableInfo = tableInfoService.getById(id);
		if (oldTableInfo == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 仅本人和管理员能删除
		User loginUser = userService.getLoginUser(request);
		if (!(oldTableInfo.getUserId().equals(loginUser.getId()) || userService.isAdmin(request))) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		boolean b = tableInfoService.removeById(id);
		return ResultUtils.success(b);
	}

	/**
	 * 更新（仅管理员）
	 *
	 * @param tableInfoUpdateRequest
	 * @return
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = "admin")
	public BaseResponse<Boolean> updateTableInfo(@RequestBody TableInfoUpdateRequest tableInfoUpdateRequest) {
		if (tableInfoUpdateRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		TableInfo tableInfo = new TableInfo();
		BeanUtils.copyProperties(tableInfoUpdateRequest, tableInfo);
		// 参数校验
		tableInfoService.validAndHandleTableInfo(tableInfo, false);
		// 判断是否存在
		long id = tableInfoUpdateRequest.getId();
		TableInfo oldTableInfo = tableInfoService.getById(id);
		if (oldTableInfo == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 更新
		boolean result = tableInfoService.updateById(tableInfo);
		return ResultUtils.success(result);
	}

	/**
	 * 根据 id 获取
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	public BaseResponse<TableInfo> getTableInfoById(long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		TableInfo tableInfo = tableInfoService.getById(id);
		return ResultUtils.success(tableInfo);
	}

	/**
	 * 获取列表（仅管理员可使用）
	 *
	 * @param tableInfoQueryRequest
	 * @return
	 */
	@AuthCheck(mustRole = "admin")
	@GetMapping("/list")
	public BaseResponse<List<TableInfo>> listTableInfo(TableInfoQueryRequest tableInfoQueryRequest) {
		List<TableInfo> tableInfoList = tableInfoService.list(getQueryWrapper(tableInfoQueryRequest));
		return ResultUtils.success(tableInfoList);
	}

	/**
	 * 分页获取列表
	 *
	 * @param tableInfoQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/list/page")
	public BaseResponse<Page<TableInfo>> listTableInfoByPage(TableInfoQueryRequest tableInfoQueryRequest,
	                                                         HttpServletRequest request) {
		if (tableInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long current = tableInfoQueryRequest.getCurrent();
		long pageSize = tableInfoQueryRequest.getPageSize();
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Page<TableInfo> tableInfoPage = tableInfoService.page(new Page<>(current, pageSize), getQueryWrapper(tableInfoQueryRequest));
		return ResultUtils.success(tableInfoPage);
	}

	/**
	 * 获取当前用户可选的全部资源列表（只返回 id 和名称）
	 *
	 * @param tableInfoQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/my/list")
	public BaseResponse<List<TableInfo>> listMyTableInfo(TableInfoQueryRequest tableInfoQueryRequest,
	                                                     HttpServletRequest request) {
		if (tableInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		TableInfo tableInfo = new TableInfo();
		BeanUtils.copyProperties(tableInfoQueryRequest, tableInfo);
		// 先获取所有审核通过的资源（包括自己上传的，也包括别人上传的）
		tableInfo.setReviewStatus(ReviewStatusEnum.PASS.getValue());
		QueryWrapper<TableInfo> queryWrapper = getQueryWrapper(tableInfoQueryRequest);
		// 设置只返回 name 和 id
		String[] fields = new String[]{"id", "name"};
		queryWrapper.select(fields);
		List<TableInfo> tableInfoList = tableInfoService.list(queryWrapper);
		// 再查询个人的
		try {
			User loginUser = userService.getLoginUser(request);
			tableInfo.setReviewStatus(null);
			tableInfo.setUserId(loginUser.getId());
			queryWrapper = new QueryWrapper<>(tableInfo);
			queryWrapper.select(fields);
			// 合并个人资源到全部中
			tableInfoList.addAll(tableInfoService.list(queryWrapper));
		} catch (Exception e) {
			// 未登录
		}
		// 根据 id 去重
		List<TableInfo> resultList = tableInfoList.stream().collect(Collectors.collectingAndThen(
				Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(TableInfo::getId))), ArrayList::new));
		return ResultUtils.success(resultList);
	}

	/**
	 * 分页获取当前用户可选的资源列表
	 *
	 * @param tableInfoQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/my/list/page")
	public BaseResponse<Page<TableInfo>> listMyTableInfoByPage(TableInfoQueryRequest tableInfoQueryRequest,
	                                                           HttpServletRequest request) {
		if (tableInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		long current = tableInfoQueryRequest.getCurrent();
		long pageSize = tableInfoQueryRequest.getPageSize();
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		QueryWrapper<TableInfo> queryWrapper = getQueryWrapper(tableInfoQueryRequest);
		// 查询当前用户 或者 审核状态为通过的
		queryWrapper.eq("userId", loginUser.getId())
				.or()
				.eq("reviewStatus", ReviewStatusEnum.PASS.getValue());
		Page<TableInfo> tableInfoPage = tableInfoService.page(new Page<>(current, pageSize), queryWrapper);
		return ResultUtils.success(tableInfoPage);
	}

	/**
	 * 分页获取当前用户创建的资源列表
	 *
	 * @param tableInfoQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/my/add/list/page")
	public BaseResponse<Page<TableInfo>> listMyAddTableInfoByPage(TableInfoQueryRequest tableInfoQueryRequest,
	                                                              HttpServletRequest request) {
		if (tableInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		tableInfoQueryRequest.setUserId(loginUser.getId());
		long current = tableInfoQueryRequest.getCurrent();
		long pageSize = tableInfoQueryRequest.getPageSize();
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Page<TableInfo> tableInfoPage = tableInfoService.page(new Page<>(current, pageSize), getQueryWrapper(tableInfoQueryRequest));
		return ResultUtils.success(tableInfoPage);

	}

	/**
	 * 生成创建表的 SQL
	 *
	 * @param id
	 * @return
	 */
	@PostMapping("/generate/sql")
	public BaseResponse<String> generateCreateSql(@RequestBody long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 根据id获取tableInfo
		TableInfo tableInfo = tableInfoService.getById(id);
		// 判断是否存在
		if (tableInfo == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		TableSchema tableSchema = GSON.fromJson(tableInfo.getContent(), TableSchema.class);
		SqlBuilder sqlBuilder = new SqlBuilder();
		String createTableSql = sqlBuilder.buildCreateTableSql(tableSchema);
		return ResultUtils.success(createTableSql);
	}

	/**
	 * 获取查询包装类
	 *
	 * @param tableInfoQueryRequest
	 * @return
	 */
	private QueryWrapper<TableInfo> getQueryWrapper(TableInfoQueryRequest tableInfoQueryRequest) {
		if (tableInfoQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		TableInfo tableInfo = new TableInfo();
		BeanUtils.copyProperties(tableInfoQueryRequest, tableInfo);
		// 获取需要排序的字段名
		String sortField = tableInfoQueryRequest.getSortField();
		// 获取排序顺序方式
		String sortOrder = tableInfoQueryRequest.getSortOrder();
		String name = tableInfo.getName();
		String content = tableInfo.getContent();
		// name、content 需支持模糊查询
		tableInfo.setName(null);
		tableInfo.setContent(null);
		QueryWrapper<TableInfo> tableInfoQueryWrapper = new QueryWrapper<>();
		// 设置模糊查询条件构造器
		tableInfoQueryWrapper.like(StringUtils.isNotBlank(name), "name", name);
		tableInfoQueryWrapper.like(StringUtils.isNotBlank(content), "content", content);
		// 设置排序顺序条件构造器
		tableInfoQueryWrapper.orderBy(StringUtils.isNotBlank(sortField),sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return tableInfoQueryWrapper;
	}
}
