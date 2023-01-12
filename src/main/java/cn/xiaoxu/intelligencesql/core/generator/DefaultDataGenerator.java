package cn.xiaoxu.intelligencesql.core.generator;

import cn.hutool.core.date.DateUtil;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 固定值数据生成器
 *
 * @author: https://github.com/xiaoxu9
 */
public class DefaultDataGenerator implements DataGenerator {

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
		List<String> list = new ArrayList<>(rowNum);
		// 主键递增策略
		if (field.isPrimaryKey()) {
			// id是空赋予一个默认值 1
			if (StringUtils.isBlank(mockParams)) {
				mockParams = "1";
			}
			int initValue = Integer.parseInt(mockParams);
			for (int i = 0; i < rowNum; i++) {
				list.add(String.valueOf(initValue + i));
			}
		}
		// 使用默认值
		String defaultValue = field.getDefaultValue();
		// 特殊逻辑，日期要伪造数据
		if ("CURRENT_TIMESTAMP".equals(defaultValue)) {
			defaultValue = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		}
		if (StringUtils.isNotBlank(defaultValue)) {
			for (int i = 0; i < rowNum; i++) {
				list.add(defaultValue);
			}
		}
		return list;
	}
}
