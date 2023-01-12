package cn.xiaoxu.intelligencesql.core.builder;

import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.core.builder.sql.MySQLDialect;
import cn.xiaoxu.intelligencesql.core.builder.sql.SQLDialect;
import cn.xiaoxu.intelligencesql.core.builder.sql.SQLDialectFactory;
import cn.xiaoxu.intelligencesql.core.model.enums.MockTypeEnum;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema.Field;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.core.model.enums.FieldTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SQL 生成器
 * 支持方言，策略模式
 * 
 * @author: https://github.com/xiaoxu9
 */
@Slf4j
public class SqlBuilder {

	/**
	 * 方言
	 */
	private SQLDialect sqlDialect;

	public SqlBuilder() {
		this.sqlDialect = SQLDialectFactory.getDialect(MySQLDialect.class.getName());
	}

	public SqlBuilder(SQLDialect sqlDialect) {
		this.sqlDialect = sqlDialect;
	}

	/**
	 * 设置方言
	 */
	public void setSqlDialect(SQLDialect sqlDialect) {
		this.sqlDialect = sqlDialect;
	}

	/**
	 * 构造建表 SQL
	 *
	 * @param tableSchema 表概要
	 * @return 生成的 SQL
	 */
	public String buildCreateTableSql(TableSchema tableSchema) {
		// 构造模板
		String template = "%s\n"
				+ "create table if not exists %s\n"
				+ "(\n"
				+ "%s\n"
				+ ") %s;";
		// 构造表名
		String tableName = sqlDialect.wrapTableName(tableSchema.getTableName());
		// 构造数据库名
		String dbName = tableSchema.getDbName();
		if (StringUtils.isNotBlank(dbName)) {
			tableName = String.format("%s.%s", dbName, tableName);  // 库名.表名
		}
		// 构造表前缀注释
		String tableComment = tableSchema.getTableComment();
		// 当表注释为空时，表前缀注释则为表名
		if (StringUtils.isBlank(tableComment)) {
			tableComment = tableName;
		}
		String tablePrefixComment = String.format("--%s", tableComment);
		// 构造表后缀注释
		String tableSuffixComment = String.format("comment '%s'", tableComment);
		// 构造表字段
		List<Field> fieldList = tableSchema.getFieldList();
		StringBuilder fieldStrBuilder = new StringBuilder();
		int fieldSize = fieldList.size();
		for (int i = 0; i < fieldSize; i++) {
			Field field = fieldList.get(i);
			fieldStrBuilder.append(buildCreateFieldSql(field));
			// 最后一个字段后没有逗号和换行
			if (i != fieldSize - 1) {
				fieldStrBuilder.append(",");
				fieldStrBuilder.append("\n");
			}
		}
		String fieldStr = fieldStrBuilder.toString();
		// 填充模板
		String result = String.format(template, tablePrefixComment, tableName, fieldStr, tableSuffixComment);
		log.info("sql result = ", result);
		return result;
	}

	/**
	 * 生成创建字段的 SQL
	 *
	 * @param field
	 * @return
	 */
	public String buildCreateFieldSql(Field field) {
		if (field == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 封装字段名
		String fieldName = sqlDialect.wrapFieldName(field.getFieldName());
		String fieldType = field.getFieldType();
		String defaultValue = field.getDefaultValue();
		String comment = field.getComment();
		boolean notNull = field.isNotNull();
		String onUpdate = field.getOnUpdate();
		boolean primaryKey = field.isPrimaryKey();
		boolean autoIncrement = field.isAutoIncrement();
		// e.g. column_name int default 0 not null auto_increment comment '注释' primary key,
		StringBuilder fieldStrBuilder = new StringBuilder();
		// 字段名
		fieldStrBuilder.append(fieldName);
		// 字段类型
		fieldStrBuilder.append(" ").append(fieldType);
		// 默认值
		if (StringUtils.isNotBlank(defaultValue)){
			fieldStrBuilder.append(" ").append("default ").append(getValueStr(field, defaultValue));
		}
		// 是否非空
		fieldStrBuilder.append(" ").append(notNull ? "not null" : "null");
		// 是否自增
		if (autoIncrement) {
			fieldStrBuilder.append(" ").append("auto_increment");
		}
		// 附加条件
		if (StringUtils.isNotBlank(onUpdate)) {
			fieldStrBuilder.append(" ").append("on update ").append(onUpdate);
		}
		// 注释
		if (StringUtils.isNotBlank(comment)) {
			fieldStrBuilder.append(" ").append(String.format("comment '%s'",comment));
		}
		// 是否为主键
		if (primaryKey) {
			fieldStrBuilder.append(" ").append("primary key");
		}
		return fieldStrBuilder.toString();
	}

	/**
	 * 构造插入数据 SQL
	 * e.g. INSERT INTO report (id, content) VALUES (1, '这个有点问题吧');
	 *
	 * @param tableSchema 表概要
	 * @param dataList 数据列表
	 * @return 生成的 SQL 列表字符串
	 */
	public String buildInsertSql(TableSchema tableSchema, List<Map<String, Object>> dataList) {
		// 构造模板
		String template = "insert into %s (%s) values (%s);";
		// 构建表名
		String tableName = sqlDialect.wrapTableName(tableSchema.getTableName());
		// 数据库名
		String dbName = tableSchema.getDbName();
		if (StringUtils.isNotBlank(dbName)) {
			tableName = String.format("%s.%s", dbName, tableName);
		}
		// 构造表字段
		List<Field> fieldList = tableSchema.getFieldList();
		// 过滤掉不模拟的字段
		fieldList.stream().filter(field -> {
			MockTypeEnum mockTypeEnum = Optional.ofNullable(MockTypeEnum.getEnumByValue(field.getFieldType())).orElse(MockTypeEnum.NONE);
			return !MockTypeEnum.NONE.equals(mockTypeEnum);
		}).collect(Collectors.toList());
		StringBuilder resultStringBuilder = new StringBuilder();
		int size = dataList.size();
		// 每一行数据中有多个字段
		for (int i = 0; i < size; i++) {
			Map<String, Object> dataRow = dataList.get(i);
			// 封装所有字段名为  (字段1, 字段2, 字段3...)
			String keyStr = fieldList.stream()
					.map(field -> sqlDialect.wrapFieldName(field.getFieldName()))
					.collect(Collectors.joining(", "));
			// 封装所有字段对应的数据  (数据1, 数据2, 数据3...)
			String valueStr = fieldList.stream()
					.map(field -> getValueStr(field, dataRow.get(field.getFieldName())))
					.collect(Collectors.joining(", "));
			// 填充模板  insert into %s (%s) values (%s);
			String result = String.format(template, tableName, keyStr, valueStr);
			resultStringBuilder.append(result);
			// 最后一行不换行
			if (i != size - 1) {
				resultStringBuilder.append("\n");
			}
		}
		return resultStringBuilder.toString();
	}

	/**
	 * 根据列的属性获取值字符串
	 *
	 * @param field
	 * @param value
	 * @return
	 */
	public static String getValueStr(Field field, Object value) {
		if (field == null || value == null) {
			return "''";
		}
		FieldTypeEnum fieldTypeEnum = Optional.ofNullable(FieldTypeEnum.getEnumByValue(field.getFieldType()))
				.orElse(FieldTypeEnum.TEXT);
		switch (fieldTypeEnum) {
			case DATE:
			case TIME:
			case DATETIME:
			case CHAR:
			case VARCHAR:
			case TINYTEXT:
			case TEXT:
			case MEDIUMTEXT:
			case LONGTEXT:
			case TINYBLOB:
			case BLOB:
			case MEDIUMBLOB:
			case LONGBLOB:
			case BINARY:
			case VARBINARY:
				return String.format("'%s'", value);
			default:
				return String.valueOf(value);
		}
	}
}
