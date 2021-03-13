package org.dragon.zhang.annotator.annotator;

import net.bytebuddy.dynamic.DynamicType;
import org.dragon.zhang.annotator.model.JavadocMapping;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.lang.annotation.ElementType;
import java.util.Set;

/**
 * @author zhangzicheng
 * @date 2021/03/10
 */
public interface Annotator {

    String DESCRIPTION_KEY = "description";

    String PARAM_TAG = "@param";

    String NAME_KEY = "name";

    /**
     * 打注解
     *
     * @param builder   用它来实现打注解
     * @param needToTag 哪些注解要打
     * @param source    注释来源
     * @return byte-buddy建造者
     */
    DynamicType.Builder<?> annotate(DynamicType.Builder<?> builder,
                                    Set<JavadocMapping> needToTag,
                                    JavaClassSource source);

    /**
     * 注解打在什么类型上，比如只打在类上
     */
    ElementType annotateType();
}
