package org.dragon.zhang.annotator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.ElementType;
import java.util.Set;

/**
 * @author zhangzicheng
 * @date 2021/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotatorConfig {

    /**
     * 要打在什么类型上
     */
    private ElementType annotateType;

    /**
     * 要打哪些注解
     */
    private Set<JavadocMapping> javadocMappings;
}
