package cn.xiaoxu.intelligencesql.core.generator;

import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.model.entity.Dict;
import cn.xiaoxu.intelligencesql.service.DictService;
import cn.xiaoxu.intelligencesql.utils.SpringContextUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 词库数据生成器
 *
 * @author: https://github.com/xiaoxu9
 */
public class DictDataGenerator implements DataGenerator{

	private static final DictService dictService = SpringContextUtils.getBean(DictService.class);

	private static final Gson GSON = new Gson();

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
		long id = Integer.parseInt(mockParams);
		// 根据id从数据库中获取词库
		Dict dict = dictService.getById(id);
		if (dict == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "词库不存在");
		}
		// 该方法将指定的Json反序列化为指定类型 List<String> 的对象
		List<String> wordList = GSON.fromJson(dict.getContent(), new TypeToken<List<String>>() {}.getType());
		List<String> list = new ArrayList<>();
		// 遍历随机获取词库中的数据
		for (int i = 0; i < rowNum; i++) {
			// RandomUtils.nextInt(0, wordList.size()) 设置随机数范围
			String randomStr = wordList.get(RandomUtils.nextInt(0, wordList.size()));
			// 把获取到的词库数据添加到list集合中
			list.add(randomStr);
		}
		return list;
	}
}
