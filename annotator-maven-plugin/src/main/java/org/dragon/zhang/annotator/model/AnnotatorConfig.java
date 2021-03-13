package org.dragon.zhang.annotator.model;

import lombok.Data;

import java.lang.annotation.ElementType;
import java.util.Objects;
import java.util.Set;

/**
 * @author zhangzicheng
 * @date 2021/03/12
 */
@Data
public class AnnotatorConfig {

    /**
     * 要打在什么类型上
     */
    private ElementType annotateType;

    /**
     * 要打哪些注解
     */
    private Set<JavadocMapping> javadocMappings;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnnotatorConfig)) {
            return false;
        }
        AnnotatorConfig that = (AnnotatorConfig) o;
        return annotateType == that.annotateType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotateType);
    }
}
