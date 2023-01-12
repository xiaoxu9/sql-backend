package cn.xiaoxu.intelligencesql.service.impl;

import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.model.enums.ReportStatusEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.xiaoxu.intelligencesql.model.entity.Report;
import cn.xiaoxu.intelligencesql.service.ReportService;
import cn.xiaoxu.intelligencesql.mapper.ReportMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author XiaoXuTongXue
* @description 针对表【report(举报)】的数据库操作Service实现
* @createDate 2022-12-30 16:35:09
*/
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService{

	@Override
	public void validReport(Report report, boolean add) {
		if (report == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String content = report.getContent();
		Long reportedId = report.getReportedId();
		Integer status = report.getStatus();
		if (StringUtils.isNotBlank(content) && content.length() > 1024) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
		}
		// 创建时必须指定
		if (add) {
			if (reportedId == null || reportedId <= 0) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR);
			}
		} else {
			if (status != null && !ReportStatusEnum.getValues().contains(status)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR);
			}
		}
	}
}




