# Swagger Dubbo
- swagger document for dubbo
- support swagger-annotations v2 and v3
- such as io.swagger.v3.oas.annotations.media.Schema  and io.swagger.annotations.ApiModel

  recommend use v3
# Version
| **swagger-dubbo version** | **dubbo version** | support  custom registration dubbo services |
| --- | --- | --- |
| 3.0.0 | 2.7.x | yes |

## Maven
```
<dependency>
	  <groupId>com.come2future.boot</groupId>
	  <artifactId>swagger-dubbo</artifactId>
	  <version>${swagger-dubbo-project.version}</version>
</dependency>
```
# Integration
use @EnableDubboSwagger enable swagger-dubbo
```
@SpringBootApplication
@EnableDubboSwagger
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
```
## Swagger Html
swagger-ui can be integrated within any container that can host pages

**api-docs**: http://ip:port/context/swagger-dubbo/api-docs

**ui**: http://ip:port/context/doc-dubbo.html
![image.png](https://cdn.nlark.com/yuque/0/2021/png/1672473/1625301191512-9ae894d0-99b8-4ef8-9f37-ee9803ac1d8f.png#clientId=u043db260-0b11-4&from=paste&height=781&id=ue35bfea3&margin=%5Bobject%20Object%5D&name=image.png&originHeight=1562&originWidth=2872&originalType=binary&ratio=1&size=846411&status=done&style=none&taskId=uccfa204b-f11f-48e0-a67c-0427ffb7c67&width=1436)
# Configuration Item Description
```
swagger:
  dubbo:
     enable: true
     context-path: ares
     doc: swagger-dubbo
     group: dubbo interface document
     application:
       groupId: com.come2future.boot
       artifactId: swagger-dubbo
       version: 1.0.0
```

# Cross Domain
```
@Component
public class CorsFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse res = (HttpServletResponse) response;
    res.addHeader("Access-Control-Allow-Credentials", "true");
    res.addHeader("Access-Control-Allow-Origin", "*");
    res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
    res.addHeader("Access-Control-Allow-Headers",
        "Content-Type,X-CAF-Authorization-Token,sessionToken,X-TOKEN");
    if ("OPTIONS".equals(((HttpServletRequest) request).getMethod())) {
      response.getWriter().println("ok");
      return;
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }
}
```
# Remark

- referenced project [https://github.com/Sayi/swagger-dubbo.git](https://github.com/Sayi/swagger-dubbo.git) ideas
- method in same class if has the same name, last one is reserved, please modify method name or designation operationId
- use knife ui from project [https://github.com/xiaoymin/swagger-bootstrap-ui.git](https://github.com/xiaoymin/swagger-bootstrap-ui.git)
