package cn.xiaoxu.intelligencesql.model.dto;

import lombok.Data;

import java.io.Serializable;

/***
 * 创建请求
 *
 * @author: xiaoxu   https://github.com/xiaoxu9
 */
@Data
public class DictAddRequest implements Serializable {
	/**
	 * 名称
	 */
	private String name;

	/**
	 * 内容
	 */
	private String Content;

	/**
	 * 审核状态
	 */
	private Integer reviewStatus;

	private static final long serialVersionUID = 1L;
}
