package cn.xiaoxu.intelligencesql.core.generator;

import cn.xiaoxu.intelligencesql.core.schema.TableSchema.Field;

import java.util.List;

/**
 * 数据生成器
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
public interface DataGenerator {

	/**
	 * 生成
	 * @param field 字段信息
	 * @param rowNum 行数
	 * @return 生成的数据列表
	 */
	List<String> doGenerate(Field field, int rowNum);
}
