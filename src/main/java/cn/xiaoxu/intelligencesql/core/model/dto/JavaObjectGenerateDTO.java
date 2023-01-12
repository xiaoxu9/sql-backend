package cn.xiaoxu.intelligencesql.core.model.dto;

import lombok.Data;

import java.util.List;

/**
 * Java 对象生成封装类
 *
 * @author https://github.com/xiaoxu9
 */
@Data
public class JavaObjectGenerateDTO {

    /**
     * 类名
     */
    private String className;

    /**
     * 对象名
     */
    private String objectName;

    /**
     * 列信息列表
     */
    private List<FieldDTO> fieldList;

    /**
     * 列信息
     */
    @Data
    public static class FieldDTO {
        /**
         * set 方法名
         */
        private String setMethod;

        /**
         * 值
         */
        private String value;
    }

}
