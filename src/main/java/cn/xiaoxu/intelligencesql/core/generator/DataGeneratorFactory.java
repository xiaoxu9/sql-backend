package cn.xiaoxu.intelligencesql.core.generator;

import cn.xiaoxu.intelligencesql.core.model.enums.MockTypeEnum;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 数据生成器工厂
 * 工厂 + 单例模式，降低开销
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
public class DataGeneratorFactory {

	/**
	 * 模拟类型 => 生成器映射
	 */
	private static final Map<MockTypeEnum, DataGenerator> mockTypeDataGeneratorMap = new HashMap<MockTypeEnum, DataGenerator>() {{
		put(MockTypeEnum.NONE, new DefaultDataGenerator());
		put(MockTypeEnum.FIXED, new FixedDataGenerator());
		put(MockTypeEnum.RANDOM, new RandomDataGenerator());
		put(MockTypeEnum.RULE, new RuleDataGenerator());
		put(MockTypeEnum.DICT, new DictDataGenerator());
		put(MockTypeEnum.INCREASE, new IncreaseDataGenerator());
	}};

	/**
	 * 私有化构造器
	 */
	private DataGeneratorFactory() {
	}

	/**
	 * 获取实例
	 * @param mockTypeEnum
	 * @return
	 */
	public static DataGenerator getGenerator(MockTypeEnum mockTypeEnum) {
		mockTypeEnum = Optional.ofNullable(mockTypeEnum).orElse(MockTypeEnum.NONE);
		return mockTypeDataGeneratorMap.get(mockTypeEnum);
	}
}