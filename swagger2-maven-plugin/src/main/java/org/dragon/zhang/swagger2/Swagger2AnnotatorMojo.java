package org.dragon.zhang.swagger2;

import com.google.common.collect.Sets;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.dragon.zhang.annotator.AnnotatorMojo;
import org.dragon.zhang.annotator.model.AnnotatorConfig;
import org.dragon.zhang.annotator.model.JavadocMapping;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author zhangzicheng
 * @date 2021/03/13
 */
@Mojo(name = "swagger", defaultPhase = LifecyclePhase.COMPILE)
public class Swagger2AnnotatorMojo extends AnnotatorMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (CollectionUtils.isEmpty(super.configs)) {
            log.warn("you have not configured it, it will be configured for you automatically...");
            super.configs = new LinkedHashSet<>();
            //打在类上的注解
            Map<String, String> apiMapping = new HashMap<>();
            apiMapping.put("tags", "description");
            Map<String, Object> apiDefaultValues = new HashMap<>();
            apiDefaultValues.put("protocols", "http,https");
            super.configs.add(AnnotatorConfig.builder()
                    .annotateType(ElementType.TYPE)
                    .javadocMappings(Sets.newHashSet(JavadocMapping.builder()
                            .conditions(Sets.newHashSet("org.springframework.stereotype.Controller"))
                            .annotationClassName("io.swagger.annotations.Api")
                            .mapping(apiMapping)
                            .defaultValues(apiDefaultValues)
                            .build()))
                    .build());

            //打在方法上的注解
            Map<String, String> apiOperationMapping = new HashMap<>();
            apiOperationMapping.put("value", "description");
            Map<String, Object> apiOperationDefaultValues = new HashMap<>();
            apiOperationDefaultValues.put("protocols", "http,https");
            super.configs.add(AnnotatorConfig.builder()
                    .annotateType(ElementType.METHOD)
                    .javadocMappings(Sets.newHashSet(JavadocMapping.builder()
                            .conditions(Sets.newHashSet("org.springframework.web.bind.annotation.RequestMapping"))
                            .annotationClassName("io.swagger.annotations.ApiOperation")
                            .mapping(apiOperationMapping)
                            .defaultValues(apiOperationDefaultValues)
                            .build()))
                    .build());
            Map<String, String> apiResponseMapping = new HashMap<>();
            apiResponseMapping.put("message", "@return");
            Map<String, Object> apiResponseDefaultValues = new HashMap<>();
            apiResponseDefaultValues.put("code", 200);
            super.configs.add(AnnotatorConfig.builder()
                    .annotateType(ElementType.METHOD)
                    .javadocMappings(Sets.newHashSet(JavadocMapping.builder()
                            .conditions(Sets.newHashSet("org.springframework.web.bind.annotation.RequestMapping"))
                            .annotationClassName("io.swagger.annotations.ApiResponse")
                            .mapping(apiResponseMapping)
                            .defaultValues(apiResponseDefaultValues)
                            .build()))
                    .build());

            //打在参数上的注解
            Map<String, String> apiParam = new HashMap<>();
            apiParam.put("name", "paramName");
            apiParam.put("value", "description");
            super.configs.add(AnnotatorConfig.builder()
                    .annotateType(ElementType.PARAMETER)
                    .javadocMappings(Sets.newHashSet(JavadocMapping.builder()
                            .conditions(Sets.newHashSet("org.springframework.web.bind.annotation.RequestParam"))
                            .annotationClassName("io.swagger.annotations.ApiParam")
                            .mapping(apiParam)
                            .build()))
                    .build());
            log.warn("automatic configuration is complete !");
        }
        super.execute();
    }
}
