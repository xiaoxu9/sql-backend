package cn.xiaoxu.intelligencesql.core.builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Map;

/**
 * 数据 Json 生成器
 *
 * @author: https://github.com/xiaoxu9
 */
public class JsonBuilder {
	public static String builderJson(List<Map<String, Object>> dataList) {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()  // 配置Gson以输出适合页面打印的Json模式
				.create();
		return gson.toJson(dataList);
	}
}
