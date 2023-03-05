package cn.xiaoxu.intelligencesql.core.builder.sql;

import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.exception.BusinessException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL 方言工厂
 * 工厂 + 单例模式，降低开销
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
public class SQLDialectFactory {

	/**
	 * className => 方言实例映射
	 */
	private static final Map<String, SQLDialect> DIALECT_POOL = new ConcurrentHashMap<>();

	private SQLDialectFactory() {
	}

	/**
	 * 获取方言实例
	 *
	 * @param className 类名
	 * @return
	 */
	public static SQLDialect getDialect(String className) {
		SQLDialect dialect = DIALECT_POOL.get(className);
		if (null == dialect) {
			synchronized (className.intern()) {
				dialect = DIALECT_POOL.computeIfAbsent(className,
					key -> {
						try {
							return (SQLDialect) Class.forName(className).newInstance();
						} catch (Exception e) {
							throw new BusinessException(ErrorCode.SYSTEM_ERROR);
						}
					});
			}
		}
		return dialect;
	}
}
