package cn.xiaoxu.intelligencesql.controller;

import cn.xiaoxu.intelligencesql.annotation.AuthCheck;
import cn.xiaoxu.intelligencesql.common.*;
import cn.xiaoxu.intelligencesql.constant.CommonConstant;
import cn.xiaoxu.intelligencesql.core.GeneratorFacade;
import cn.xiaoxu.intelligencesql.core.model.enums.MockTypeEnum;
import cn.xiaoxu.intelligencesql.core.model.vo.GenerateVO;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema.Field;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.model.dto.DictAddRequest;
import cn.xiaoxu.intelligencesql.model.dto.DictQueryRequest;
import cn.xiaoxu.intelligencesql.model.dto.DictUpdateRequest;
import cn.xiaoxu.intelligencesql.model.entity.Dict;
import cn.xiaoxu.intelligencesql.model.entity.User;
import cn.xiaoxu.intelligencesql.model.enums.ReviewStatusEnum;
import cn.xiaoxu.intelligencesql.service.DictService;
import cn.xiaoxu.intelligencesql.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.List;

/**
 * 词条接口
 *
 * @author https://github.com/xiaoxu9
 */
@RestController
@RequestMapping("/dict")
@Slf4j
public class DictController {

	@Resource
	private DictService dictService;

	@Resource
	private UserService userService;

	// region 增删改查

	/**
	 * 创建
	 *
	 * @param dictAddRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addDict(@RequestBody DictAddRequest dictAddRequest, HttpServletRequest request) {
		if (dictAddRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Dict dict = new Dict();
		BeanUtils.copyProperties(dictAddRequest, dict);
		// 校验
		dictService.validAndHandleDict(dict, true);
		User loginUser = userService.getLoginUser(request);
		dict.setUserId(loginUser.getId());
		boolean result = dictService.save(dict);
		if (!result) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR);
		}
		return ResultUtils.success(dict.getId());
	}

	/**
	 * 删除
	 *
	 * @param deleteRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteDict(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		Long id = deleteRequest.getId();
		// 判断是否存在
		Dict oldDict = dictService.getById(id);
		if (oldDict == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 仅本人和管理员可删除
		if (!oldDict.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		boolean result = dictService.removeById(id);
		return ResultUtils.success(result);
	}

	/**
	 * 更新（仅管理员）
	 *
	 * @param dictUpdateRequest
	 * @return
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = "admin")
	public BaseResponse<Boolean> updateDict(@RequestBody DictUpdateRequest dictUpdateRequest) {
		if (dictUpdateRequest == null || dictUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Dict dict = new Dict();
		BeanUtils.copyProperties(dictUpdateRequest, dict);
		// 参数校验
		dictService.validAndHandleDict(dict, false);
		// 判断是否存在
		long id = dictUpdateRequest.getId();
		Dict oldDict = dictService.getById(id);
		if (oldDict == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		boolean result = dictService.updateById(dict);
		return ResultUtils.success(result);
	}

	/**
	 * 根据 id 获取
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	public BaseResponse<Dict> getDictById(long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Dict dict = dictService.getById(id);
		return ResultUtils.success(dict);
	}

	/**
	 * 获取列表（仅管理员可使用）
	 *
	 * @param dictQueryRequest
	 * @return
	 */
	@GetMapping("/list")
	@AuthCheck(mustRole = "admin")
	public BaseResponse<List<Dict>> listDict(DictQueryRequest dictQueryRequest) {
		if (dictQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		List<Dict> dictList = dictService.list(getQueryWrapper(dictQueryRequest));
		return ResultUtils.success(dictList);
	}

	/**
	 * 分页获取列表
	 *
	 * @param dictQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/list/page")
	public BaseResponse<Page<Dict>> listDictByPage(DictQueryRequest dictQueryRequest, HttpServletRequest request) {
		long current = dictQueryRequest.getCurrent();
		long pageSize = dictQueryRequest.getPageSize();
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 封装页面词典集合
		Page<Dict> dictPage = dictService.page(new Page<>(current, pageSize), getQueryWrapper(dictQueryRequest));
		return ResultUtils.success(dictPage);
	}

	/**
	 * 获取当前用户可选的全部资源列表（只返回 id 和名称）
	 *
	 * @param dictQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/my/list")
	public BaseResponse<List<Dict>> listMyDict(DictQueryRequest dictQueryRequest, HttpServletRequest request) {
		if (dictQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 先查询所有审核通过的
		dictQueryRequest.setReviewStatus(ReviewStatusEnum.PASS.getValue());
		Dict dictQuery = new Dict();
		QueryWrapper<Dict> queryWrapper = getQueryWrapper(dictQueryRequest);
		// 设置只返回id和name
		final String[] fields = {"id", "name"};
		queryWrapper.select(fields);
		List<Dict> dictList = dictService.list(queryWrapper);
		// 再查询本人
		try {
			User loginUser = userService.getLoginUser(request);
			dictQuery.setReviewStatus(null);
			dictQuery.setUserId(loginUser.getId());
			queryWrapper = new QueryWrapper<>(dictQuery);
			queryWrapper.select(fields);
			dictList.addAll(dictService.list(queryWrapper));
		} catch (Exception e) {
			// 未登录
		}
		// 根据 id 去重
		List<Dict> resultList = dictList.stream().collect(Collectors.collectingAndThen(
				Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Dict::getId))), ArrayList::new));
		return ResultUtils.success(resultList);
	}

	/**
	 * 分页获取当前用户可选的资源列表
	 *
	 * @param dictQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/my/list/page")
	public BaseResponse<Page<Dict>> listMyDictByPage(DictQueryRequest dictQueryRequest, HttpServletRequest request) {
		if (dictQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		dictQueryRequest.setUserId(loginUser.getId());
		long current = dictQueryRequest.getCurrent();
		long pageSize = dictQueryRequest.getPageSize();
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		QueryWrapper<Dict> queryWrapper = getQueryWrapper(dictQueryRequest);
		// 根据用户id或者审核状态通过的条件进行查询
		queryWrapper.eq("userId", loginUser.getId())
				.or()
				.eq("reviewStatus", ReviewStatusEnum.PASS.getValue());
		Page<Dict> page = dictService.page(new Page<>(current, pageSize), queryWrapper);
		return ResultUtils.success(page);
	}

	/**
	 * 分页获取当前用户创建的资源列表
	 *
	 * @param dictQueryRequest
	 * @param request
	 * @return
	 */
	@GetMapping("/my/add/list/page")
	public BaseResponse<Page<Dict>> listMyAddDictByPage(DictQueryRequest dictQueryRequest, HttpServletRequest request) {
		if (dictQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		dictQueryRequest.setUserId(loginUser.getId());
		long current = dictQueryRequest.getCurrent();
		long pageSize = dictQueryRequest.getPageSize();
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Page<Dict> page = dictService.page(new Page<>(current, pageSize), getQueryWrapper(dictQueryRequest));
		return ResultUtils.success(page);
	}

	// endregion

	/**
	 * 生成创建表的 SQL
	 *
	 * @param id
	 * @return
	 */
	@PostMapping("/generate/sql")
	public BaseResponse<GenerateVO> generateCreateSql(@RequestBody long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Dict dict = dictService.getById(id);
		if (dict == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 根据词库生成Schema
		TableSchema tableSchema = new TableSchema();
		String name = dict.getName();
		tableSchema.setTableName("dict");
		tableSchema.setTableComment(name);
		List<TableSchema.Field> fieldList = new ArrayList<>();
		Field idField = new Field();
		idField.setFieldName("id");
		idField.setFieldType("bigint");
		idField.setNotNull(true);
		idField.setComment("id");
		idField.setPrimaryKey(true);
		idField.setAutoIncrement(true);
		Field dataField = new Field();
		dataField.setFieldName("data");
		dataField.setFieldType("text");
		dataField.setComment("数据");
		dataField.setMockType(MockTypeEnum.DICT.getValue());
		dataField.setMockParams(String.valueOf(id));
		fieldList.add(idField);
		fieldList.add(dataField);
		tableSchema.setFieldList(fieldList);
		return ResultUtils.success(GeneratorFacade.generateAll(tableSchema));
	}

	/**
	 * 获取查询包装类
	 *
	 * @param dictQueryRequest
	 * @return
	 */
	private QueryWrapper<Dict> getQueryWrapper(DictQueryRequest dictQueryRequest) {
		if (dictQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Dict dict = new Dict();
		// 根据属性匹配转换为目标对象
		BeanUtils.copyProperties(dictQueryRequest, dict);
		// 获取需要排序的字段
		String sortField = dictQueryRequest.getSortField();
		// 获取排序方式
		String sortOrder = dictQueryRequest.getSortOrder();
		// 获取词典名
		String name = dict.getName();
		// 获取词典内容
		String content = dict.getContent();
		// name、content 需要支持模糊查询
		dict.setName(null);
		dict.setContent(null);
		QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
		// 当StringUtils.isNotBlank(name) 为true时，才会拼接后面的查询条件
		dictQueryWrapper.like(StringUtils.isNotBlank(name), "name", name);
		dictQueryWrapper.like(StringUtils.isNotBlank(content), "content", content);
		dictQueryWrapper.orderBy(StringUtils.isNotBlank(sortField),sortOrder.equals(CommonConstant.SORT_ORDER_ASC),sortField);
		return dictQueryWrapper;
	}

}