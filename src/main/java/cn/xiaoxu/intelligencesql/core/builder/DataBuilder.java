package cn.xiaoxu.intelligencesql.core.builder;

import cn.xiaoxu.intelligencesql.core.generator.DataGenerator;
import cn.xiaoxu.intelligencesql.core.generator.DataGeneratorFactory;
import cn.xiaoxu.intelligencesql.core.model.enums.MockTypeEnum;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.util.*;

/**
 * 数据生成器
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
public class DataBuilder {

	public static List<Map<String, Object>> generateData(TableSchema tableSchema, int rowNum) {
		// rowNum 要生成的数据条数
		List<TableSchema.Field> fieldList = tableSchema.getFieldList();
		// 初始化结果数据
		List<Map<String, Object>> resultList = new ArrayList<>(rowNum);
		for (int i = 0; i < rowNum; i++) {
			resultList.add(new HashMap<>());
		}
		// 依次生成每一列
		for (TableSchema.Field field : fieldList) {
			// 判断参数1 是否为空，不为空，则赋值，为空则赋值参数2，避免了空指针异常
			// 获取 模拟数据类型枚举
			MockTypeEnum mockTypeEnum = Optional.ofNullable(MockTypeEnum.getEnumByValue(field.getMockType()))
					.orElse(MockTypeEnum.NONE);
			// 根据 模拟数据类型枚举 获取对应的数据生成器
			DataGenerator dataGenerator = DataGeneratorFactory.getGenerator(mockTypeEnum);
			// 调用生成器的生成数据方法 生成数据
			List<String> mockDataList = dataGenerator.doGenerate(field, rowNum);
			// 获取字段名
			String fieldName = field.getFieldName();
			// 填充结果列表
			if (CollectionUtils.isNotEmpty(mockDataList)) {
				for (int i = 0; i < rowNum; i++) {
					// 填充rowNum条数据的该字段的值
					resultList.get(i).put(fieldName, mockDataList.get(i));
				}
			}
		}
		return resultList;
	}

}
