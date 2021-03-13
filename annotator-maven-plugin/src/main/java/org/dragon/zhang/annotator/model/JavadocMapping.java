package org.dragon.zhang.annotator.model;

import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * @author zhangzicheng
 * @date 2021/03/06
 */
@Data
public class JavadocMapping {

    /**
     * 已经打了某些注解，才会打注解
     */
    private Set<String> conditions;

    /**
     * 注解的类全名
     */
    private String annotationClassName;

    /**
     * 注解的默认值
     */
    private Map<String, Object> defaultValues;

    /**
     * key注解的方法名, value注释key或者注释tag key；
     */
    private Map<String, String> mapping;

    public String getCommentKey(String key) {
        return mapping.get(key);
    }

    public Object getDefaultValue(String methodName) {
        if (null == defaultValues) {
            return null;
        }
        return defaultValues.get(methodName);
    }
}
