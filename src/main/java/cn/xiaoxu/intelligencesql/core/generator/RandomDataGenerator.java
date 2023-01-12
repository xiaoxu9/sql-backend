package cn.xiaoxu.intelligencesql.core.generator;

import cn.xiaoxu.intelligencesql.core.model.enums.MockParamsRandomTypeEnum;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import cn.xiaoxu.intelligencesql.core.utils.FakerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 随机值数据生成器
 * 
 * @author: https://github.com/xiaoxu9
 */
public class RandomDataGenerator implements DataGenerator {
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
		List<String> list = new ArrayList<String>(rowNum);
		// 遍历 根据类型生成随机值
		for (int i = 0; i < rowNum; i++) {
			// 根据要生成的随机值类型匹配返回对应的类型
			// 有值时走Optional.ofNullable()，没值时走 .orElse()
			MockParamsRandomTypeEnum randomTypeEnum = Optional.ofNullable(MockParamsRandomTypeEnum.getEnumByValue(mockParams))
					.orElse(MockParamsRandomTypeEnum.STRING);
			// 根据数据类型生成对应类型的数据
			String randomValue = FakerUtils.getRandomValue(randomTypeEnum);
			list.add(randomValue);
		}
		return list;
	}
}
