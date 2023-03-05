package cn.xiaoxu.intelligencesql.service.impl;

import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.core.GeneratorFacade;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.mapper.TableInfoMapper;
import cn.xiaoxu.intelligencesql.model.entity.TableInfo;
import cn.xiaoxu.intelligencesql.model.enums.ReviewStatusEnum;
import cn.xiaoxu.intelligencesql.service.TableInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author XiaoXuTongXue
* @description 针对表【table_info(表信息)】的数据库操作Service实现
* @createDate 2022-12-30 16:35:15
*/
@Service
public class TableInfoServiceImpl extends ServiceImpl<TableInfoMapper, TableInfo> implements TableInfoService{

	private final static Gson GSON = new Gson();

	@Override
	public void validAndHandleTableInfo(TableInfo tableInfo, boolean add) {
		if (tableInfo == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String content = tableInfo.getContent();
		String name = tableInfo.getName();
		Integer reviewStatus = tableInfo.getReviewStatus();
		// 创建时，所有参数必须为空
		if (add && StringUtils.isAnyBlank(name, content)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		if (StringUtils.isNotBlank(name) && name.length() > 30) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
		}
		if (StringUtils.isNotBlank(content)) {
			if (content.length() > 20000) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
			}
			// 校验字段内容
			try {
				TableSchema tableSchema = GSON.fromJson(content, TableSchema.class);
				GeneratorFacade.validSchema(tableSchema);
			} catch (Exception e) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容格式错误");
			}
		}
		if (reviewStatus != null && !ReviewStatusEnum.getValues().contains(reviewStatus)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
	}
}




