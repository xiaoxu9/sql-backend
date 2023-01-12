package cn.xiaoxu.intelligencesql.controller;

import cn.xiaoxu.intelligencesql.annotation.AuthCheck;
import cn.xiaoxu.intelligencesql.common.BaseResponse;
import cn.xiaoxu.intelligencesql.common.DeleteRequest;
import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.common.ResultUtils;
import cn.xiaoxu.intelligencesql.constant.CommonConstant;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.model.dto.ReportAddRequest;
import cn.xiaoxu.intelligencesql.model.dto.ReportQueryRequest;
import cn.xiaoxu.intelligencesql.model.dto.ReportUpdateRequest;
import cn.xiaoxu.intelligencesql.model.entity.*;
import cn.xiaoxu.intelligencesql.model.enums.ReportStatusEnum;
import cn.xiaoxu.intelligencesql.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 举报接口
 *
 * @author https://github.com/xiaoxu9
 */
@RestController
@Slf4j
@RequestMapping("/report")
public class ReportController {
	@Data
	class Obj{
		private Long reportedId;
		private Long userId;
	}

	@Resource
	private ReportService reportService;

	@Resource
	private DictService dictService;

	@Resource
	private TableInfoService tableInfoService;

	@Resource
	private FieldInfoService fieldInfoService;

	@Resource
	private UserService userService;

	// region 增删改查

	/**
	 * 创建
	 *
	 * @param reportAddRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addReport(@RequestBody ReportAddRequest reportAddRequest, HttpServletRequest request) {
		if (reportAddRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Report report = new Report();
		BeanUtils.copyProperties(reportAddRequest, report);
		// 校验
		reportService.validReport(report, true);
		User loginUser = userService.getLoginUser(request);
		// 判断审核类型（0-词库 1-表信息 2-字段信息）
		Obj obj = new Obj();
		if (reportAddRequest.getType() != null){
				switch (reportAddRequest.getType()) {
					// 举报的是词库
					case 0:
						Dict dict = dictService.getById(reportAddRequest.getReportedId());
						if (dict.getUserId() != null){
							obj.setUserId(dict.getUserId());
						}
						break;
					// 举报的是表大全
					case 1:
						TableInfo tableInfo = tableInfoService.getById(reportAddRequest.getReportedId());
						if (tableInfo.getUserId() != null){
							obj.setUserId(tableInfo.getUserId());
						}
						break;
					// 举报的是字段信息
					case 2:
						FieldInfo fieldInfo = fieldInfoService.getById(reportAddRequest.getReportedId());
						if (fieldInfo.getUserId() != null){
							obj.setUserId(fieldInfo.getUserId());
						}
						break;
					default:
						obj = null;
				}
			if (obj == null) {
				throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
			}
			obj.setReportedId(reportAddRequest.getReportedId());
		}
		// 设置被举报用户id
		report.setReportedUserId(obj.getUserId());
		// 设置举报用户id
		report.setUserId(loginUser.getId());
		// 设置举报状态
		report.setStatus(ReportStatusEnum.DEFAULT.getValue());
		boolean result = reportService.save(report);
		if (!result) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR);
		}
		Long id = report.getId();
		return ResultUtils.success(id);
	}

	/**
	 * 删除
	 *
	 * @param deleteRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteReport(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		Long id = deleteRequest.getId();
		// 判断是否存在
		Report oldReport = reportService.getById(id);
		if (oldReport == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 仅本人和管理员能删除
		if (!(oldReport.getUserId().equals(loginUser.getId()) || userService.isAdmin(request))) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		boolean result = reportService.removeById(id);
		return ResultUtils.success(result);
	}

	/**
	 * 更新（仅管理员）
	 *
	 * @param reportUpdateRequest
	 * @return
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = "admin")
	public BaseResponse<Boolean> updateReport(@RequestBody ReportUpdateRequest reportUpdateRequest) {
		if (reportUpdateRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Report report = new Report();
		BeanUtils.copyProperties(reportUpdateRequest, report);
		// 校验
		reportService.validReport(report, false);
		// 判断是否存在
		Long id = reportUpdateRequest.getId();
		Report oldReport = reportService.getById(id);
		if (oldReport == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		boolean result = reportService.updateById(report);
		return ResultUtils.success(result);
	}

	/**
	 * 根据 id 获取
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	public BaseResponse<Report> getReportById(long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Report report = reportService.getById(id);
		return ResultUtils.success(report);
	}

	/**
	 * 获取列表（仅管理员可使用）
	 *
	 * @param reportQueryRequest
	 * @return
	 */
	@AuthCheck(mustRole = "admin")
	@GetMapping("/list")
	public BaseResponse<List<Report>> listReport(ReportQueryRequest reportQueryRequest) {
		if (reportQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Report report = new Report();
		BeanUtils.copyProperties(reportQueryRequest, report);
		QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>(report);
		List<Report> reportList = reportService.list(reportQueryWrapper);
		return ResultUtils.success(reportList);
	}

	/**
	 * 分页获取列表
	 *
	 * @param reportQueryRequest
	 * @return
	 */
	@GetMapping("/list/page")
	public BaseResponse<Page<Report>> listReportByPage(ReportQueryRequest reportQueryRequest) {
		if (reportQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Report report = new Report();
		BeanUtils.copyProperties(reportQueryRequest,report);
		// 获取当前页
		long current = reportQueryRequest.getCurrent();
		// 获取页面大小
		long pageSize = reportQueryRequest.getPageSize();
		// 获取需要排序字段
		String sortField = reportQueryRequest.getSortField();
		// 获取排序顺序
		String sortOrder = reportQueryRequest.getSortOrder();
		// 获取举报内容
		String content = reportQueryRequest.getContent();
		// content 需支持模糊查询
		// 限制爬虫
		if (pageSize > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 创建条件构造器对象
		QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
		// 构造哪个字段模糊查询条件
		reportQueryWrapper.like(StringUtils.isNotBlank(content), "content", content);
		// 构造字段排序方式条件
		reportQueryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		// 获取分页列表
		Page<Report> reportPage = reportService.page(new Page<>(current, pageSize), reportQueryWrapper);
		return ResultUtils.success(reportPage);
	}
}
