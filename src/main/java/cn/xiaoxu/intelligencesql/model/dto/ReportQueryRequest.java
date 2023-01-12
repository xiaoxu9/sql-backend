package cn.xiaoxu.intelligencesql.model.dto;

import cn.xiaoxu.intelligencesql.common.PageRequest;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 举报查询请求
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */

// PageRequest 页面共同信息抽取类
@EqualsAndHashCode(callSuper = true)
@Data
public class ReportQueryRequest extends PageRequest implements Serializable {

    /**
     * 内容
     */
    private String content;

    /**
     * 举报实体类型（0-词库）
     */
    private Integer type;

    /**
     * 被举报对象 id
     */
    private Long reportedId;

    /**
     * 被举报用户 id
     */
    private Long reportedUserId;

    /**
     * 状态（0-未处理, 1-已处理）
     */
    private Integer status;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}