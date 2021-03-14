# What troubles have been solved?

You had written a lot of comments in the `.java` file, and you want to use `swagger`, so you have to write a lot of the same comment which you had just written.

In this case, it will be very nice if the `comments` could be automatically converted to `annotations`.

That's what `annotator-maven-plugin` have solved.

For example, here is the class without `swagger annotation` I wrote before:
```java
/**
 * 类上的描述-annotator
 *
 * @author zhangzicheng
 * @version 1.0.0
 * @date 2021/03/01
 * @exception Exception
 * @throws Exception
 * @link Exception
 * @see Exception
 * @since 1.0.0
 */
@RestController
public class TestController implements BeanNameAware {

    /**
     * beanName
     */
    private String name;

    /**
     * 方法上的描述
     *
     * @param param 参数
     * @return 返回值
     */
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String test(@RequestParam String param) {
        return name + " say hello, " + param;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }
}
```
After use `annotator-maven-plugin`, we can generate bytecode below with `mvn clean install` command:
```java
@RestController
@Api(
    authorizations = {@Authorization(
    scopes = {@AuthorizationScope(
    description = "",
    scope = ""
)},
    value = ""
)},
    basePath = "",
    produces = "",
    consumes = "",
    protocols = "http,https",
    description = "",
    value = "",
    position = 0,
    hidden = false,
    tags = {"类上的描述-annotator"}
)
public class TestController implements BeanNameAware {
    private String name;

    public TestController() {
    }

    @RequestMapping(
        value = {"/test"},
        method = {RequestMethod.GET}
    )
    @ApiOperation(
        nickname = "",
        authorizations = {@Authorization(
    scopes = {@AuthorizationScope(
    description = "",
    scope = ""
)},
    value = ""
)},
        ignoreJsonView = false,
        responseHeaders = {@ResponseHeader(
    responseContainer = "",
    response = Void.class,
    description = "",
    name = ""
)},
        notes = "",
        responseContainer = "",
        produces = "",
        consumes = "",
        response = Void.class,
        httpMethod = "",
        protocols = "http,https",
        code = 200,
        value = "方法上的描述",
        position = 0,
        extensions = {@Extension(
    name = "",
    properties = {@ExtensionProperty(
    name = "",
    value = ""
)}
)},
        hidden = false,
        tags = {""},
        responseReference = ""
    )
    @ApiResponse(
        examples = @Example({@ExampleProperty(
    mediaType = "",
    value = ""
)}),
        responseHeaders = {@ResponseHeader(
    responseContainer = "",
    response = Void.class,
    description = "",
    name = ""
)},
        responseContainer = "",
        response = Void.class,
        reference = "",
        code = 200,
        message = "返回值"
    )
    public String test(@RequestParam @ApiParam(collectionFormat = "",allowableValues = "",allowEmptyValue = false,allowMultiple = false,examples = @Example({@ExampleProperty(
    mediaType = "",
    value = ""
)}),defaultValue = "",readOnly = false,required = false,name = "param",type = "",value = "参数",format = "",access = "",hidden = false,example = "") String param) {
        return this.name + " say hello, " + param;
    }

    public void setBeanName(String name) {
        this.name = name;
    }
}
```

For more details, see [annotator-maven-plugin-demos](https://github.com/dragon-zhang/annotator-maven-plugin-demos).