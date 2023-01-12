package cn.xiaoxu.intelligencesql.core.builder;

import cn.hutool.core.util.StrUtil;
import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.core.model.dto.JavaEntityGenerateDTO;
import cn.xiaoxu.intelligencesql.core.model.dto.JavaEntityGenerateDTO.FieldDTO;
import cn.xiaoxu.intelligencesql.core.model.dto.JavaObjectGenerateDTO;
import cn.xiaoxu.intelligencesql.core.model.enums.MockTypeEnum;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema.Field;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.core.model.enums.FieldTypeEnum;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Java 代码生成器
 *
 * @author: https://github.com/xiaoxu9
 */
@Component
@Slf4j
public class JavaCodeBuilder {

	private static Configuration configuration;

	@Resource
	public void setConfiguration(Configuration configuration) {
		JavaCodeBuilder.configuration = configuration;
	}

	/**
	 * 构造 Java 实体代码
	 *
	 * @param tableSchema 表概要
	 * @return 生成的 java 代码
	 */
	@SneakyThrows
	public static String buildJavaEntityCode(TableSchema tableSchema) {
		// 传递参数
		JavaEntityGenerateDTO javaEntityGenerateDTO = new JavaEntityGenerateDTO();
		String tableName = tableSchema.getTableName();
		String tableComment = tableSchema.getTableComment();
		// 把表名转为大驼峰写法
		String upperCamelTableName = StringUtils.capitalize(StrUtil.toCamelCase(tableName));
		javaEntityGenerateDTO.setClassName(upperCamelTableName);
		// 类注释为表注释 > 为空时则为表名
		javaEntityGenerateDTO.setClassComment(Optional.ofNullable(tableComment).orElse(upperCamelTableName));
		// 依次填写每一列
		List<FieldDTO> fieldDTOList = new ArrayList<>();
		for (Field field : tableSchema.getFieldList()) {
			FieldDTO fieldDTO = new FieldDTO();
			// 设置Java对象字段名
			fieldDTO.setFieldName(field.getFieldName());
			// 统一类型枚举
			FieldTypeEnum fieldTypeEnum = Optional.ofNullable(FieldTypeEnum.getEnumByValue(field.getFieldType())).orElse(FieldTypeEnum.TEXT);
			// 设置Java对象类型
			fieldDTO.setJavaType(fieldTypeEnum.getJavaType());
			// 设置Java类注释
			fieldDTO.setComment(field.getComment());
			fieldDTOList.add(fieldDTO);
		}
		javaEntityGenerateDTO.setFieldList(fieldDTOList);
		StringWriter stringWriter = new StringWriter();
		Template template = configuration.getTemplate("java_entity.ftl");
		template.process(javaEntityGenerateDTO, stringWriter);
		return stringWriter.toString();
	}

	/**
	 * 构造 Java 对象代码
	 *
	 * @param tableSchema 表概要
	 * @param dataList    数据列表
	 * @return 生成的 java 代码
	 */
	@SneakyThrows
	public static String buildJavaObjectCode(TableSchema tableSchema, List<Map<String, Object>> dataList) {
		if(CollectionUtils.isEmpty(dataList)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "缺少实例数据");
		}
		// 传递参数
		JavaObjectGenerateDTO javaObjectGenerateDTO = new JavaObjectGenerateDTO();
		String tableName = tableSchema.getTableName();
		String tableComment = tableSchema.getTableComment();
		// 转换为大驼峰写法
		String upperCamelTableName = StringUtils.capitalize(StrUtil.toCamelCase(tableName));
		javaObjectGenerateDTO.setClassName(upperCamelTableName);
		// 对象名 首字母小写
		String head = upperCamelTableName.substring(0, 1).toLowerCase();
		javaObjectGenerateDTO.setObjectName(head + upperCamelTableName.substring(1, upperCamelTableName.length() - 1));
		// 依次填写每一列
		Map<String, Object> fillData = dataList.get(0);
		List<JavaObjectGenerateDTO.FieldDTO> fieldDTOList = new ArrayList<>();
		List<Field> fieldList = tableSchema.getFieldList();
		// 过滤掉不模拟的字段
		fieldList = fieldList.stream().filter(field -> {
			MockTypeEnum mockTypeEnum = Optional.ofNullable(MockTypeEnum.getEnumByValue(field.getMockType())).orElse(MockTypeEnum.NONE);
			// 过滤返回不模拟的字段
			return !MockTypeEnum.NONE.equals(mockTypeEnum);
		}).collect(Collectors.toList());
		for (Field field : fieldList) {
			JavaObjectGenerateDTO.FieldDTO fieldDTO = new JavaObjectGenerateDTO.FieldDTO();
			// 驼峰字段名
			String fieldName = field.getFieldName();
			fieldDTO.setSetMethod(StrUtil.toCamelCase("set_" + fieldName));
			fieldDTO.setValue(getValueStr(field, fillData.get(fieldName)));
			fieldDTOList.add(fieldDTO);
		}
		javaObjectGenerateDTO.setFieldList(fieldDTOList);
		StringWriter stringWriter = new StringWriter();
		Template temp = configuration.getTemplate("java_object.ftl");
		temp.process(javaObjectGenerateDTO, stringWriter);
		return stringWriter.toString();
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
				return String.format("\"%s\"", value);  // 格式化输出字符串  输出格式： "value"
			default:
				return String.valueOf(value);  // 转换为字符串
		}
	}
}
