package cn.xiaoxu.intelligencesql.core.builder.sql;

/**
 * MySQL 方言
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
public class MySQLDialect implements SQLDialect{
	/**
	 * 封装字段名
	 *
	 * @param name
	 */
	@Override
	public String wrapFieldName(String name) {
		return String.format("`%s`", name);
	}

	/**
	 * 解析字段名
	 *
	 * @param fieldName
	 */
	@Override
	public String parseFieldName(String fieldName) {
		if(fieldName.startsWith("`") && fieldName.endsWith("`")) {
			return fieldName.substring(1, fieldName.length() - 1);
		}
		return fieldName;
	}

	/**
	 * 封装表名
	 *
	 * @param name
	 */
	@Override
	public String wrapTableName(String name) {
		return String.format("`%s`", name);
	}

	/**
	 * 解析表名
	 *
	 * @param tableName
	 */
	@Override
	public String parseTableName(String tableName) {
		// 如果字符串存在 `  则截取掉
		if (tableName.startsWith("`") && tableName.endsWith("`")) {
			return tableName.substring(1, tableName.length() - 1);
		}
		return tableName;
	}
}
