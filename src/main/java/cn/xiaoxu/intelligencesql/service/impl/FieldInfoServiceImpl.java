package cn.xiaoxu.intelligencesql.service.impl;

import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.core.GeneratorFacade;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.mapper.FieldInfoMapper;
import cn.xiaoxu.intelligencesql.model.entity.FieldInfo;
import cn.xiaoxu.intelligencesql.model.enums.ReviewStatusEnum;
import cn.xiaoxu.intelligencesql.service.FieldInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author XiaoXuTongXue
* @description 针对表【field_info(字段信息)】的数据库操作Service实现
* @createDate 2022-12-30 16:34:58
*/
@Service
public class FieldInfoServiceImpl extends ServiceImpl<FieldInfoMapper, FieldInfo> implements FieldInfoService{

	private static final Gson GSON = new Gson();

	@Override
	public void validAndHandleFieldInfo(FieldInfo fieldInfo, boolean add) {
		if (fieldInfo == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String content = fieldInfo.getContent();
		String name = fieldInfo.getName();
		Integer reviewstatus = fieldInfo.getReviewStatus();
		// 创建时，所有参数必须为空
		if (add && StringUtils.isAnyBlank(name, content)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 名称长度不能大于30
		if (StringUtils.isNotBlank(name) && name.length() > 30) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
		}
		// 内容
		if (StringUtils.isNotBlank(content)) {
			if (content.length() > 20000) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
			}
			// 校验内容
			try {
				TableSchema.Field field = GSON.fromJson(content, TableSchema.Field.class);
				GeneratorFacade.validField(field);
				// 填充 fieldName
				fieldInfo.setFieldName(field.getFieldName());
			} catch (Exception e) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容格式错误");
			}
		}
		if (reviewstatus != null && !ReviewStatusEnum.getValues().contains(reviewstatus)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
	}
}




