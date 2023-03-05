package cn.xiaoxu.intelligencesql.model.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * 创建请求
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
@Data
public class FieldInfoAddRequest implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 内容
     */
    private String content;

    /**
     * 审核状态
     */
    private Integer reviewStatus;

    private static final long serialVersionUID = 1L;
}