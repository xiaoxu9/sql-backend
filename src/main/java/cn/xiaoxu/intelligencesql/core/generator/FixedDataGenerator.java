package cn.xiaoxu.intelligencesql.core.generator;

import cn.xiaoxu.intelligencesql.core.schema.TableSchema.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 固定值生成器
 *
 * @author: https://github.com/xiaoxu9
 */
public class FixedDataGenerator implements DataGenerator {

	/**
	 * 生成
	 *
	 * @param field  字段信息
	 * @param rowNum 行数
	 * @return 生成的数据列表
	 */
	@Override
	public List<String> doGenerate(Field field, int rowNum) {
		String mockParams = field.getMockParams();
		// 用户没有设置固定值
		if (StringUtils.isBlank(mockParams)) {
			// 添加一个 6 固定值
			mockParams = "6";
		}
		// 遍历为所有字段的mockParams添加固定值
		List<String> list = new ArrayList<>(rowNum);
		for (int i = 0; i < rowNum; i++) {
			list.add(mockParams);
		}
		return list;
	}
}
