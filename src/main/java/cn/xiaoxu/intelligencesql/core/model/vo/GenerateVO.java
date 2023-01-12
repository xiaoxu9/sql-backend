package cn.xiaoxu.intelligencesql.core.model.vo;

import cn.xiaoxu.intelligencesql.core.schema.TableSchema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 生成的返回值
 *
 * @author: https://github.com/xiaoxu9
 */
@Data
public class GenerateVO implements Serializable {
	private TableSchema tableSchema;

	private String createSql;

	private List<Map<String, Object>> dataList;

	private String insertSql;

	private String dataJson;

	private String javaEntityCode;

	private String javaObjectCode;

	private String typeScriptTypeCode;

	private static final long SerialVersionUID = 7122637163626243606L;
}
