package cn.xiaoxu.intelligencesql.core.builder;

import cn.hutool.core.util.StrUtil;
import cn.xiaoxu.intelligencesql.core.model.dto.TypescriptTypeGenerateDTO;
import cn.xiaoxu.intelligencesql.core.model.dto.TypescriptTypeGenerateDTO.FieldDTO;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema.Field;
import cn.xiaoxu.intelligencesql.core.model.enums.FieldTypeEnum;
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
import java.util.Optional;

/**
 * Type Script 代码生成器
 *
 * @author: https://github.com/xiaoxu9
 */
@Component
@Slf4j
public class FrontendCodeBuilder {

	private static Configuration configuration;

	@Resource
	public void setConfiguration(Configuration configuration){
		FrontendCodeBuilder.configuration = configuration;
	}

	/**
	 * 构造 Typescript 类型代码
	 *
	 * @param tableSchema 表概要
	 * @return 生成的代码
	 */
	@SneakyThrows
	public static String buildTypeScriptTypeCode(TableSchema tableSchema) {
		// 传递参数
		TypescriptTypeGenerateDTO generateDTO = new TypescriptTypeGenerateDTO();
		// 获取表名
		String tableName = tableSchema.getTableName();
		// 获取内容
		String tableComment = tableSchema.getTableComment();
		// 转换为驼峰表名
		String upperCamelTableName = StringUtils.capitalize(StrUtil.toCamelCase(tableName));
		// 设置typescript 的类名
		generateDTO.setClassName(upperCamelTableName);
		// 类注释为表注释 > 为空时则为表名
		generateDTO.setClassComment(Optional.ofNullable(tableComment).orElse(upperCamelTableName));
		// 依次填写每一列
		List<FieldDTO> fieldDTOList = new ArrayList<>();
		for (Field field : tableSchema.getFieldList()) {
			FieldDTO fieldDTO = new FieldDTO();
			// 设置类注释到前端对象中
			fieldDTO.setComment(field.getComment());
			// 把java的数据类型转换为通用枚举类型
			FieldTypeEnum fieldTypeEnum = Optional.ofNullable(FieldTypeEnum.getEnumByValue(field.getFieldType())).orElse(FieldTypeEnum.TEXT);
			// 根据匹配通用枚举类型 设置类型到前端对象中
			fieldDTO.setTypescriptType(fieldTypeEnum.getTypescriptType());
			// 设置字段名到前端对象中
			fieldDTO.setFieldName(field.getFieldName());
			fieldDTOList.add(fieldDTO);
		}
		generateDTO.setFieldList(fieldDTOList);
		// 加载String文件流
		StringWriter stringWriter = new StringWriter();
		// 通过模板配置 获取文件模板
		Template template = configuration.getTemplate("typescript_type.ftl");
		// 根据模板来填写数据到stringWriter中
		template.process(generateDTO, stringWriter);
		// 返回填写完成的 stringWriter
		return stringWriter.toString();
	}
}
