package cn.xiaoxu.intelligencesql.core.schema;

import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.core.builder.sql.MySQLDialect;
import cn.xiaoxu.intelligencesql.core.model.enums.FieldTypeEnum;
import cn.xiaoxu.intelligencesql.core.model.enums.MockTypeEnum;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.model.entity.FieldInfo;
import cn.xiaoxu.intelligencesql.service.FieldInfoService;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlCreateTableParser;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 表概要生成器
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
@Component
@Slf4j
public class TableSchemaBuilder {

	private final static Gson Gson = new Gson();

	private static FieldInfoService fieldInfoService;

	private static final MySQLDialect sqlDialect = new MySQLDialect();

	@Resource
	public void setFieldInfoService(FieldInfoService fieldInfoService) {
		TableSchemaBuilder.fieldInfoService = fieldInfoService;
	}

	/**
	 * 日期格式
	 */
	private static final String[] DATE_PATTERNS = {"yyyy-MM-dd", "yyyy年MM月dd日", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyyMMdd"};

	/**
	 * 智能构建
	 */
	public static TableSchema buildFromAuto(String content) {
		if (StringUtils.isBlank(content)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 切分单词
		String[] words = content.split("[,，]");  // 只要是英文逗号或者是中文逗号就进行拆分
		// 判断数组如果为空、null或者字段个数长度大于20，抛出异常
		if (ArrayUtils.isEmpty(words) || words.length > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 根据单词去词库里匹配列信息，未匹配到的使用默认值, 条件构造器
		QueryWrapper<FieldInfo> queryWrapper = new QueryWrapper<>();
		// 根据集合对象的 name 或者 fieldName字段 作为查询条件
		queryWrapper.in("name", Arrays.asList(words)).or().in("fieldName", Arrays.asList(words));
		// 根据条件构造器查询全部字段信息
		List<FieldInfo> fieldInfoList = fieldInfoService.list(queryWrapper);
		// 名称 => 字段信息
		Map<String, List<FieldInfo>> nameFieldInfoMap = fieldInfoList.stream().collect(Collectors.groupingBy(FieldInfo::getName));
		// 字段名称 => 字段信息 （sql导入时是fieldName）
		Map<String, List<FieldInfo>> fieldNameFieldInfoMap = fieldInfoList.stream().collect(Collectors.groupingBy(FieldInfo::getFieldName));
		// 创建Schema模板对象
		TableSchema tableSchema = new TableSchema();
		// 设置表名
		tableSchema.setTableName("my_table");
		// 设置表内容
		tableSchema.setTableComment("自动生成的表");
		List<TableSchema.Field> fieldList = new ArrayList<>();
		// 遍历所有字段，以json格式添加到集合中
		for (String word : words) {
			TableSchema.Field field;
			// Optional.ofNullable 过滤掉 null 的值，返回不为null的值
			List<FieldInfo> infoList = Optional.ofNullable(nameFieldInfoMap.get(word))
					.orElse(fieldNameFieldInfoMap.get(word));
			// 不为空
			if (CollectionUtils.isNotEmpty(infoList)){
				// 转换为json格式
				field = Gson.fromJson(infoList.get(0).getContent(), TableSchema.Field.class);
			} else {
				// 未匹配到的使用默认值
				field = getDefaultField(word);
			}
			// 将json数据添加到字段集合中
			fieldList.add(field);
		}
		// 设置到tableSchema模板 的字段集合对象中
		tableSchema.setFieldList(fieldList);
		return tableSchema;
	}

	/**
	 * 根据建表 SQL 构建
	 *
	 * @param sql 建表 SQL
	 * @return 生成的 TableSchema
	 */
	public static TableSchema buildFromSql(String sql) {
		// isBlank 如果是空串、null、空格串返回true
		if (StringUtils.isBlank(sql)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		try {
			// 解析SQL
			// 获取解析器
			MySqlCreateTableParser parser = new MySqlCreateTableParser(sql);
			// 通过解析器获取SQL创建表语句
			SQLCreateTableStatement sqlCreateTableStatement = parser.parseCreateTable();
			TableSchema tableSchema = new TableSchema();
			// 设置数据库名
			tableSchema.setDbName(sqlCreateTableStatement.getSchema());
			// 设置表名
			tableSchema.setTableName(sqlDialect.parseTableName(sqlCreateTableStatement.getTableName()));
			String tableComment = null;
			if (sqlCreateTableStatement.getComment() != null) {
				tableComment = sqlCreateTableStatement.getComment().toString();
				if (tableComment.length() > 2) {
					// 截取掉字符串的前后引号 ""
					tableComment = tableComment.substring(1, tableComment.length() - 1);
				}
			}
			// 设置表注释
			tableSchema.setTableComment(tableComment);
			// 用于存储表字段集合
			List<TableSchema.Field> fieldList = new ArrayList<>();
			// 解析列
			for (SQLTableElement sqlTableElement : sqlCreateTableStatement.getTableElementList()) {
				// 检查字段的``是否存在，存在则删除
				SQLColumnDefinition columnDefinition = (SQLColumnDefinition) sqlTableElement;
				String fieldName = sqlDialect.parseFieldName(columnDefinition.getNameAsString());
				fieldName = fieldName.replace(" ", "");
				fieldName = fieldName.replace("`", "");
				if (!fieldName.equals(sqlDialect.parseFieldName(columnDefinition.getNameAsString()))) {
					if (fieldName.length() > 2) {
						columnDefinition.setName(fieldName);
					}
				}
				// 主键约束
				if (sqlTableElement instanceof SQLPrimaryKey){
					SQLPrimaryKey sqlPrimaryKey = (SQLPrimaryKey) sqlTableElement;
					String primaryFieldName = sqlDialect.parseFieldName(sqlPrimaryKey.getColumns().get(0).toString());
					// fieldList 集合为空时， 跳过该语句，如果不为空，则校验所有字段，当是主键值时，设置该字段为主键
					fieldList.forEach(field -> {
						if (field.getFieldName().equals(primaryFieldName)){
							field.setPrimaryKey(true);
						}
					});
				} else if (sqlTableElement instanceof SQLColumnDefinition) {
					// 非主键字段 (列)
					TableSchema.Field field = new TableSchema.Field();
					// 设置字段名
					field.setFieldName(sqlDialect.parseFieldName(columnDefinition.getNameAsString()));
					// 设置字段类型
					field.setFieldType(columnDefinition.getDataType().toString());
					// 如果字段默认值不等于null，则把该值赋值给定义的默认值
					String defaultValue = null;
					if (columnDefinition.getDefaultExpr() != null) {
						defaultValue = columnDefinition.getDefaultExpr().toString();
					}
					// 设置默认值
					field.setDefaultValue(defaultValue);
					// 设置不空属性
					field.setNotNull(columnDefinition.containsNotNullConstaint());
					// 如果字段内容属性不等于null，则把该值赋值给定义的内容属性
					String comment = null;
					if (columnDefinition.getComment() != null) {
						comment = columnDefinition.getComment().toString();
						// 截取掉双引号 ""
						if (comment.length() > 2) {
							comment = comment.substring(1, comment.length() - 1);
						}
					}
					// 设置内容属性
					field.setComment(comment);
					// 设置主键
					field.setPrimaryKey(columnDefinition.isPrimaryKey());
					// 设置是否自增
					field.setAutoIncrement(columnDefinition.isAutoIncrement());
					// 设置更新时间
					String onUpdate = null;
					if (columnDefinition.getOnUpdate() != null) {
						onUpdate = columnDefinition.getOnUpdate().toString();
					}
					field.setOnUpdate(onUpdate);
					// 设置模拟数据类型
					field.setMockType(MockTypeEnum.NONE.getValue());
					fieldList.add(field);
				}
			}
			tableSchema.setFieldList(fieldList);
			return tableSchema;
		}catch (Exception e) {
			log.error("SQL 解析错误", e);
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请确认 SQL 语句正确");
		}
	}

	/**
	 * 根据 Excel 文件构建
	 *
	 * @param file Excel 文件
	 * @return 生成的 TableSchema
	 */
	public static TableSchema buildFromExcel(MultipartFile file) {
		try {
			// 读取表头
			List<Map<Integer, String>> dataList = EasyExcel.read(file.getInputStream()).sheet().headRowNumber(0).doReadSync();
			// 第一行表头
			Map<Integer, String> map = dataList.get(0);
			List<TableSchema.Field> fieldList = map.values().stream().map(name -> {
				TableSchema.Field field = new TableSchema.Field();
				field.setFieldName(name);
				field.setComment(name);
				field.setFieldType(FieldTypeEnum.TEXT.getValue());
				return field;
			}).collect(Collectors.toList());
			// 第二行为值
			if (dataList.size() > 1){
				Map<Integer, String> dataMap = dataList.get(1);
				for (int i = 0; i < fieldList.size(); i++) {
					String value = dataMap.get(1);
					// 根据判断类型
					String fieldType = getFieldTypeByValue(value);
					fieldList.get(i).setFieldType(fieldType);
				}
			}
			TableSchema tableSchema = new TableSchema();
			tableSchema.setFieldList(fieldList);
			return tableSchema;
		}catch (Exception e) {
			log.error("buildFromExcel error",e);
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "表格解析错误");
		}
	}

	/**
	 * 判断字段类型
	 *
	 * @param value
	 * @return
	 */
	public static String getFieldTypeByValue(String value) {
		if (StringUtils.isBlank(value)) {
			return FieldTypeEnum.TEXT.getValue();
		}
		// 布尔
		if ("false".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
			return FieldTypeEnum.TINYINT.getValue();
		}
		// 整数
		if (StringUtils.isNumeric(value)) {
			// 转换为十进度整数
			long number = Long.parseLong(value);
			// 判断是否超过int的最大值范围
			if (number > Integer.MAX_VALUE) {
				return FieldTypeEnum.BIGINT.getValue();
			}
			return FieldTypeEnum.INT.getValue();
		}
		// 小数
		if (isDouble(value)) {
			return FieldTypeEnum.DOUBLE.getValue();
		}
		// 日期
		if (isDate(value)){
			return FieldTypeEnum.DATE.getValue();
		}
		return FieldTypeEnum.TEXT.getValue();
	}

	/**
	 * 判断是否为日期
	 *
	 * @param str
	 * @return
	 */
	private static boolean isDate(String str) {
		if (StringUtils.isBlank(str)){
			return false;
		}
		try {
			DateUtils.parseDate(str, DATE_PATTERNS);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * 判断字符串是不是 double 型
	 *
	 * @param str
	 * @return
	 */
	private static boolean isDouble(String str) {
		// 通过正则表达式得到特定模式
		Pattern pattern = Pattern.compile("[0-9]+[.]{0,1}[0-9]*[dD]{0,1}");
		// 创建一个匹配给定输入与此模式的匹配器。
		Matcher isNum = pattern.matcher(str);
		return isNum.matches();
	}

	/**
	 * 获取默认字段
	 *
	 * @param word
	 * @return
	 */
	private static TableSchema.Field getDefaultField(String word) {
		final TableSchema.Field field = new TableSchema.Field();
		field.setFieldName(word);
		field.setFieldType("text");
		field.setDefaultValue("");
		field.setNotNull(false);
		field.setComment(word);
		field.setPrimaryKey(false);
		field.setAutoIncrement(false);
		field.setMockType("");
		field.setMockParams("");
		field.setOnUpdate("");
		return field;
	}
}
