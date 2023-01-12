package cn.xiaoxu.intelligencesql.service.impl;

import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.mapper.DictMapper;
import cn.xiaoxu.intelligencesql.model.entity.Dict;
import cn.xiaoxu.intelligencesql.model.enums.ReviewStatusEnum;
import cn.xiaoxu.intelligencesql.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author https://github.com/xiaoxu9
* @description 针对表【dict(词库)】的数据库操作Service实现
* @createDate 2022-12-30 16:34:31
*/
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService{
	private final static Gson GSON = new Gson();

	@Override
	public void validAndHandleDict(Dict dict, boolean add) {
		if (dict == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String content = dict.getContent();
		String name = dict.getName();
		Integer reviewstatus = dict.getReviewStatus();
		// 创建时，任意参数为空时，抛出异常
		if (add && StringUtils.isAnyBlank(name, content)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// name不空，长度超过30抛出异常
		if (StringUtils.isNotBlank(name) && name.length() > 30) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
		}
		// 内容不空且长度不超过20000
		if (StringUtils.isNotBlank(content)) {
			if (content.length() > 20000) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
			}
			// 对content 进行转换
			try {
				String[] words = content.split("[,，]");
				// 移除开头结尾空格
				for (int i = 0; i < words.length; i++) {
					words[i] = words[i].trim();
				}
				// 过滤空单词
				List<String> wordList = Arrays.stream(words).filter(StringUtils::isNotBlank).collect(Collectors.toList());
				// 转换为Json格式
				dict.setContent(GSON.toJson(wordList));
			} catch (Exception e) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容格式错误");
			}
		}
		// 审核状态为不存在的数据
		if (reviewstatus != null && !ReviewStatusEnum.getValues().contains(reviewstatus)){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
	}
}




