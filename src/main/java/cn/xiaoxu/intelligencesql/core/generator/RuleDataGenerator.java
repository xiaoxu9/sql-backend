package cn.xiaoxu.intelligencesql.core.generator;

import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import com.mifmif.common.regex.Generex;

import java.util.ArrayList;
import java.util.List;

/**
 * 正则表达式数据生成器
 * 
 * @author: https://github.com/xiaoxu9
 */
public class RuleDataGenerator implements DataGenerator{
	/**
	 * 生成
	 *
	 * @param field  字段信息
	 * @param rowNum 行数
	 * @return 生成的数据列表
	 */
	@Override
	public List<String> doGenerate(TableSchema.Field field, int rowNum) {
		// 获取前端的模拟参数
		String mockParams = field.getMockParams();
		List<String> list = new ArrayList<String>(rowNum);
		// 根据正则表达式创建 Generex 对象
		Generex generex = new Generex(mockParams);
		for (int i = 0; i < rowNum; i++) {
			// 根据正则表达式随机生成数据
			String randomStr = generex.random();
			list.add(randomStr);
		}
		return list;
	}
}
