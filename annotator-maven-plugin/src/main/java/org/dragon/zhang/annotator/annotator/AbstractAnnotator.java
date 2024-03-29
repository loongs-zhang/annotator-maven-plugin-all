package org.dragon.zhang.annotator.annotator;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import org.codehaus.plexus.logging.Logger;
import org.dragon.zhang.annotator.model.JavadocMapping;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangzicheng
 * @date 2021/03/10
 */
public abstract class AbstractAnnotator implements Annotator {

    protected static Logger log;

    public AbstractAnnotator(Logger log) {
        AbstractAnnotator.log = log;
    }

    @Override
    public final DynamicType.Builder<?> annotate(DynamicType.Builder<?> builder,
                                                 Set<JavadocMapping> needToTag,
                                                 JavaClassSource source) {
        if (null == builder) {
            throw new NullPointerException("builder is null !");
        }
        if (CollectionUtils.isEmpty(needToTag)) {
            return builder;
        }
        Map<String, Set<JavadocMapping>> tagMap = needToTag(needToTag, source);
        Map<String, Map<String, Object>> comment = initComment(source);
        for (Map.Entry<String, Map<String, Object>> entry : comment.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> value = entry.getValue();
            Set<JavadocMapping> realNeedToTag = tagMap.get(key);
            if (CollectionUtils.isEmpty(realNeedToTag)) {
                continue;
            }
            Collection<AnnotationDescription> descriptions = buildAnnotationDescriptions(realNeedToTag, value);
            builder = tagAnnotations(builder, descriptions, key, 0);
        }
        return builder;
    }

    public static Set<JavadocMapping> buildNeedToTag(AnnotationTargetSource<JavaClassSource, ?> source, Set<JavadocMapping> needToTag) {
        Set<Class<? extends Annotation>> declared = source.getAnnotations().stream()
                .map(annotation -> {
                    try {
                        return (Class<? extends Annotation>) Class.forName(annotation.getQualifiedName());
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return needToTag.stream()
                //满足条件才需要打tag
                .filter(mapping -> {
                    Set<Class<? extends Annotation>> conditions = mapping.getConditions()
                            .stream().map(condition -> {
                                try {
                                    return (Class<? extends Annotation>) Class.forName(condition);
                                } catch (Exception e) {
                                    return null;
                                }
                            }).filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(conditions)) {
                        return true;
                    }
                    for (Class<? extends Annotation> annotation : declared) {
                        for (Class<? extends Annotation> condition : conditions) {
                            if (annotation.equals(condition) ||
                                    AnnotationUtils.isAnnotationMetaPresent(annotation, condition)) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                //已经打了的注解不需要再打
                .filter(mapping -> {
                    try {
                        Class<? extends Annotation> tagType = (Class<? extends Annotation>) Class.forName(mapping.getAnnotationClassName());
                        for (Class<? extends Annotation> annotation : declared) {
                            if (annotation.equals(tagType) ||
                                    AnnotationUtils.isAnnotationMetaPresent(annotation, tagType)) {
                                return false;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    return true;
                })
                .collect(Collectors.toSet());
    }

    protected abstract Map<String, Set<JavadocMapping>> needToTag(Set<JavadocMapping> needToTag, JavaClassSource source);

    protected abstract Map<String, Map<String, Object>> initComment(JavaClassSource source);

    public static Collection<AnnotationDescription> buildAnnotationDescriptions(Set<JavadocMapping> needToTag, Map<String, Object> comment) {
        List<AnnotationDescription> result = new ArrayList<>(needToTag.size());
        for (JavadocMapping mapping : needToTag) {
            String annotationClassName = mapping.getAnnotationClassName();
            try {
                Class<? extends Annotation> annotationType = (Class<? extends Annotation>) Class.forName(annotationClassName);
                AnnotationDescription.Builder annotationBuilder = AnnotationDescription.Builder
                        .ofType(annotationType);
                for (Method memberMethod : annotationType.getDeclaredMethods()) {
                    String methodName = memberMethod.getName();
                    try {
                        //优先取用户指定的默认值
                        Object value = mapping.getDefaultValue(methodName);
                        if (null == value) {
                            //注释的key
                            String tagKey = mapping.getCommentKey(methodName);
                            if (null != tagKey) {
                                value = comment.get(tagKey);
                            }
                        }
                        if (null == value) {
                            //没有映射，取注解定义的默认值
                            value = memberMethod.getDefaultValue();
                        }
                        annotationBuilder = defineMemberValue(annotationBuilder, memberMethod, methodName, value);
                    } catch (Exception e) {
                        log.error("define member:" + memberMethod + " failed, " + e.getMessage());
                    }
                }
                result.add(annotationBuilder.build());
            } catch (Exception e) {
                log.error("define annotation:" + annotationClassName + " failed, " + e.getMessage());
            }
        }
        return result;
    }

    public static AnnotationDescription.Builder defineMemberValue(AnnotationDescription.Builder annotationBuilder, Method memberMethod, String methodName, Object value) {
        Class<?> returnType = memberMethod.getReturnType();
        if (returnType.isArray()) {
            Class<?> componentType = returnType.getComponentType();
            if (Annotation.class.isAssignableFrom(componentType)) {
                Annotation[] array = value instanceof Annotation[] ? (Annotation[]) value : new Annotation[]{(Annotation) value};
                annotationBuilder = annotationBuilder.defineAnnotationArray(methodName, (Class<Annotation>) componentType, array);
            } else if (Class.class.isAssignableFrom(componentType)) {
                Class<?>[] array = value instanceof Class<?>[] ? (Class<?>[]) value : new Class<?>[]{(Class<?>) value};
                annotationBuilder = annotationBuilder.defineTypeArray(methodName, array);
            } else if (Enum.class.isAssignableFrom(componentType)) {
                Set<String> enums = new HashSet<>();
                for (Enum<?> anEnum : ((Enum<?>[]) value)) {
                    enums.add(anEnum.name());
                }
                annotationBuilder = annotationBuilder.defineEnumerationArray(methodName,
                        TypeDescription.ForLoadedType.of(componentType), enums.toArray(new String[0]));
            } else if (Boolean.class.isAssignableFrom(componentType)) {
                boolean[] array = value instanceof boolean[] ? (boolean[]) value : new boolean[]{(boolean) value};
                annotationBuilder = annotationBuilder.defineArray(methodName, array);
            } else if (Byte.class.isAssignableFrom(componentType)) {
                byte[] array = value instanceof byte[] ? (byte[]) value : new byte[]{(byte) value};
                annotationBuilder = annotationBuilder.defineArray(methodName, array);
            } else if (Short.class.isAssignableFrom(componentType)) {
                short[] array = value instanceof short[] ? (short[]) value : new short[]{(short) value};
                annotationBuilder = annotationBuilder.defineArray(methodName, array);
            } else if (Character.class.isAssignableFrom(componentType)) {
                char[] array = value instanceof char[] ? (char[]) value : new char[]{(char) value};
                annotationBuilder = annotationBuilder.defineArray(methodName, array);
            } else if (Integer.class.isAssignableFrom(componentType)) {
                int[] array = value instanceof int[] ? (int[]) value : new int[]{(int) value};
                annotationBuilder = annotationBuilder.defineArray(methodName, array);
            } else if (Long.class.isAssignableFrom(componentType)) {
                long[] array = value instanceof long[] ? (long[]) value : new long[]{(long) value};
                annotationBuilder = annotationBuilder.defineArray(methodName, array);
            } else if (Float.class.isAssignableFrom(componentType)) {
                float[] array = value instanceof float[] ? (float[]) value : new float[]{(float) value};
                annotationBuilder = annotationBuilder.defineArray(methodName, array);
            } else if (Double.class.isAssignableFrom(componentType)) {
                double[] array = value instanceof double[] ? (double[]) value : new double[]{(double) value};
                annotationBuilder = annotationBuilder.defineArray(methodName, array);
            } else if (String.class.isAssignableFrom(componentType)) {
                String[] array = value instanceof String[] ? (String[]) value : new String[]{(String) value};
                annotationBuilder = annotationBuilder.defineArray(methodName, array);
            }
        } else if (Class.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, (Class<?>) value);
        } else if (Annotation.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, (Annotation) value);
        } else if (Enum.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, (Enum<?>) value);
        } else if (boolean.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, Boolean.parseBoolean(value.toString()));
        } else if (byte.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, Byte.parseByte(value.toString()));
        } else if (short.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, Short.parseShort(value.toString()));
        } else if (char.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, value.toString().charAt(0));
        } else if (int.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, Integer.parseInt(value.toString()));
        } else if (long.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, Long.parseLong(value.toString()));
        } else if (float.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, Float.parseFloat(value.toString()));
        } else if (double.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, Double.parseDouble(value.toString()));
        } else if (String.class.isAssignableFrom(returnType)) {
            annotationBuilder = annotationBuilder.define(methodName, value.toString());
        }
        return annotationBuilder;
    }

    protected abstract DynamicType.Builder<?> tagAnnotations(DynamicType.Builder<?> builder, Collection<AnnotationDescription> descriptions, String name, int index);

}
