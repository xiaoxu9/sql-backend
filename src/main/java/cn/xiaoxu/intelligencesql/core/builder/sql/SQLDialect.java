package cn.xiaoxu.intelligencesql.core.builder.sql;

/**
 * SQL 方言
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
public interface SQLDialect {

	/**
	 * 封装字段名
	 */
	String wrapFieldName(String name);

	/**
	 * 解析字段名
	 */
	String parseFieldName(String fieldName);

	/**
	 * 封装表名
	 */
	String wrapTableName(String name);

	/**
	 * 解析表名
	 */
	String parseTableName(String tableName);
}
