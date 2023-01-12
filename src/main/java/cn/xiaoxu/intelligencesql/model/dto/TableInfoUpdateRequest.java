package cn.xiaoxu.intelligencesql.model.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * 表格信息更新请求
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
@Data
public class TableInfoUpdateRequest implements Serializable {

    /**
     * id
     */
    private long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 内容
     */
    private String content;

    /**
     * 状态（0-待审核, 1-通过, 2-拒绝）
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}