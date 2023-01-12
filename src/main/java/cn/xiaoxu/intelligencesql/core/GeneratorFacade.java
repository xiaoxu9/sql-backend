package cn.xiaoxu.intelligencesql.core;

import cn.xiaoxu.intelligencesql.core.builder.*;
import cn.xiaoxu.intelligencesql.core.model.vo.GenerateVO;
import cn.xiaoxu.intelligencesql.core.schema.SchemaException;
import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 集中数据生成器
 * 门面模式，统一生成
 *
 * @author: https://github.com/xiaoxu9
 */
@Component
@Slf4j
public class GeneratorFacade {

	/**
	 * 生成所有内容
	 *
	 * @param tableSchema
	 * @return
	 */
	public static GenerateVO generateAll(TableSchema tableSchema) {
		// 校验
		validSchema(tableSchema);
		SqlBuilder sqlBuilder = new SqlBuilder();
		// 构建建表SQL
		String createSql = sqlBuilder.buildCreateTableSql(tableSchema);
		// 建表数据条数
		Integer mockNum = tableSchema.getMockNum();
		// 生成模拟数据
		List<Map<String, Object>> dataList = DataBuilder.generateData(tableSchema, mockNum);
		// 生成插入 SQL
		String insertSql = sqlBuilder.buildInsertSql(tableSchema, dataList);
		// 生成数据 Json
		String dataJson = JsonBuilder.builderJson(dataList);
		// 生成 Java 实体代码
		String javaEntityCode = JavaCodeBuilder.buildJavaEntityCode(tableSchema);
		// 生成 Java 对象代码
		String javaObjectCode = JavaCodeBuilder.buildJavaObjectCode(tableSchema, dataList);
		// 生成 typeScript 类型代码
		String typeScriptTypeCode = FrontendCodeBuilder.buildTypeScriptTypeCode(tableSchema);
		// 封装返回
		GenerateVO generateVO = new GenerateVO();
		generateVO.setTableSchema(tableSchema);
		generateVO.setCreateSql(createSql);
		generateVO.setDataList(dataList);
		generateVO.setInsertSql(insertSql);
		generateVO.setDataJson(dataJson);
		generateVO.setJavaEntityCode(javaEntityCode);
		generateVO.setJavaObjectCode(javaObjectCode);
		generateVO.setTypeScriptTypeCode(typeScriptTypeCode);
		return generateVO;
	}

	/**
	 * 验证 schema
	 *
	 * @param tableSchema 表概要
	 */
	public static void validSchema(TableSchema tableSchema) {
		if (tableSchema == null) {
			throw new SchemaException("数据为空");
		}
		String tableName = tableSchema.getTableName();
		if (tableName == null) {
			throw new SchemaException("表名不能为空");
		}
		Integer mockNum = tableSchema.getMockNum();
		// 默认生成 20 条
		if (mockNum == null) {
			tableSchema.setMockNum(20);
			mockNum = 20;
		}
		if (mockNum > 100 || mockNum < 10) {
			throw new SchemaException("生成条数设置失误");
		}
		List<TableSchema.Field> fieldList = tableSchema.getFieldList();
		if (CollectionUtils.isEmpty(fieldList)) {
			throw new SchemaException("字段列表不能为空");
		}
		// 遍历检验字段
		for (TableSchema.Field field : fieldList) {
			validField(field);
		}
	}

	/**
	 * 校验字段
	 *
	 * @param field
	 */
	public static void validField(TableSchema.Field field) {
		String fieldName = field.getFieldName();
		String fieldType = field.getFieldType();
		if (StringUtils.isBlank(fieldName)) {
			throw new SchemaException("字段名不能为空");
		}
		if (StringUtils.isBlank(fieldType)) {
			throw new SchemaException("字段类型不能为空");
		}
	}
}
