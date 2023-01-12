package cn.xiaoxu.intelligencesql.core.generator;

import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 递增值数据生成器
 * 
 * @author: https://github.com/xiaoxu9
 */
public class IncreaseDataGenerator implements DataGenerator{
	/**
	 * 生成
	 *
	 * @param field  字段信息
	 * @param rowNum 行数
	 * @return 生成的数据列表
	 */
	@Override
	public List<String> doGenerate(TableSchema.Field field, int rowNum) {
		String mockParams = field.getMockParams();
		// 用户没有设置自增开始值
		if (StringUtils.isBlank(mockParams)) {
			// 给用户设置自增默认初始值为 1
			mockParams = "1";
		}
		// 用户设置了自增开始值
		List<String> list = new ArrayList<>(rowNum);
		int initValue = Integer.parseInt(mockParams);
		for (int i = 0; i < rowNum; i++) {
			list.add(String.valueOf(initValue + i));
		}
		return list;
	}
}
