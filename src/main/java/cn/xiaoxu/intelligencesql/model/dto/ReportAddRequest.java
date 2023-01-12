package cn.xiaoxu.intelligencesql.model.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * 举报创建请求
 *
 * @TableName report
 */
@Data
public class ReportAddRequest implements Serializable {

    /**
     * 内容
     */
    private String content;

    /**
     * 举报实体类型（0-词库 1-表信息 2-字段信息）
     */
    private Integer type;

    /**
     * 被举报对象 id
     */
    private Long reportedId;

    private static final long serialVersionUID = 1L;
}