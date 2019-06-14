# 一、使用OpenID Connect保护的Spring boot应用示例

这是一个Spring boot应用程序，并使用OpenID Connect保护它。
在这个示例中，我使用了wso2 Identity Server作为OpenID身份提供者程序，但是可以使用任何OpenID连接提供程序。

## 详细过程请参考：

[OAuth2 Login for Spring Boot Application with WSO2 Identity Server](https://medium.com/@balaajanthan/oauth2-login-for-spring-boot-application-with-wso2-identity-server-da0a88893987)

[Github Code](https://github.com/ajanthan/spring-boot-oauth2-login-with-wso2is)



# 二、这个项目是如何构建出来的呢？

## 第一步

访问[https://start.spring.io](https://start.spring.com/) 或[http://192.168.3.69:9980](http://192.168.3.69:9980)生成项目的基本结构并添加依赖 `web,Security` 和`Thymeleaf`

注意：http://192.168.3.69:9980是本地的`start.spring.io`服务示例（因为start.spring.io经常访问不了）

[![start.spring.io](assets/start-spring-io.png)](https://github.com/ajanthan/spring-boot-oauth2-login-with-wso2is/blob/master/images/start-spring-io.png?raw=true)

## 第二步

在 pom.xml中添加如下依赖

```xml
		<!--Additional Dependencies-->
		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-oauth2-client</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-oauth2-jose</artifactId>
		</dependency>
		<dependency>
			<groupId>org.thymeleaf.extras</groupId>
			<artifactId>thymeleaf-extras-springsecurity4</artifactId>
            <version>3.0.4.RELEASE</version>
		</dependency>
        
```

## 第三步

添加一个controller 类来处理对index.html页面的访问.

```java
package com.github.ajanthan.spring.security.oauth2loginsample.controllers;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/")
    public String getUserName(Model model, OAuth2AuthenticationToken token) {
        model.addAttribute("userName", token.getPrincipal().getName());
        return "index";
    }
}
```

在 resource/templates文件夹下添加index.html模板文件来显示登录用户信息.

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>OAuth2 登录 </title>
</head>
<body>
<div style="float: right">
    <div style="float:left">
        <span style="font-weight:bold">用户: <span th:text=${userName}></span> </span>
    </div>
    <div style="float:none">&nbsp;</div>
    <div style="float:right">
        <form th:action="@{/logout}" method="post">
            <input type="submit" value="注销"/>
        </form>
    </div>
</div>
<h1>OAuth 2.0 登录使用 Spring Security</h1>
<div>
    欢迎你 <span style="font-weight:bold" th:text="${userName}"></span>
</div>
</body>
</html>
```

## 第四步

添加OAuth2LoginConfig类以使用特定于wso2的详细信息和将用于登录的已注册OAuth2客户端配置InMemoryCenterRegistrationRepository.

```java
package gds.is.sp.oauth2loginsample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

/**
 * 添加OAuth2LoginConfig类以使用特定于wso2的详细信息和将用于登录的已注册OAuth2客户端配置InMemoryCenterRegistrationRepository.
 * @author wangf
 *
 */
@Configuration
public class OAuth2LoginConfig {
    private static String CLIENT_PROPERTY_KEY = "spring.security.oauth2.client.registration.wso2.";
    private static String PROVIDER_PROPERTY_KEY = "spring.security.oauth2.client.provider.wso2.";
    @Autowired
    private Environment env;

    @Bean
    public ClientRegistrationRepository
    clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.WSO2ClientRegistration());
    }

    private ClientRegistration WSO2ClientRegistration() {
        return ClientRegistration.withRegistrationId("oauth2-login-sample")           //这是应用的注册Id，这个id就是回调Url中的{registrationId}，命名：应用的英文名称
                .clientId(env.getProperty(CLIENT_PROPERTY_KEY + "client-id"))
                .clientSecret(env.getProperty(CLIENT_PROPERTY_KEY + "client-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")  //IS中添加的服务提供者的入站认证配置下的OAuth/OpenId连接配置中的回调Url须根据这个模板来填写
                .scope("openid", "profile", "email", "address", "phone")              //这是本应用期望从IS获取的用户个人信息，登录成功后，系统要求用户确认是否愿意提供这些信息。
                .authorizationUri(env.getProperty(PROVIDER_PROPERTY_KEY + "authorization-uri"))
                .tokenUri(env.getProperty(PROVIDER_PROPERTY_KEY + "token-uri"))
                .userInfoUri(env.getProperty(PROVIDER_PROPERTY_KEY + "user-info-uri"))
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri(env.getProperty(PROVIDER_PROPERTY_KEY + "jwk-set-uri"))
                .clientName("Oauth登录IS的示例程序")   //这是应用的显示名称，应用注销后会显示在引导用户重新登录的页面上
                .build();
    }
}
```

这个实现将从application.properties中读取某些参数。这是一个示例应用程序属性。.

```properties
spring.thymeleaf.cache=false
spring.security.oauth2.client.registration.wso2.client-id=1D_jX0cS8I1Ut7P4fCfTvUANSJga
spring.security.oauth2.client.registration.wso2.client-secret=YhZviqZGvao_dCfnqmnxStmtLl0a
spring.security.oauth2.client.provider.wso2.authorization-uri=http://is.cd.mtn:9763/oauth2/authorize
spring.security.oauth2.client.provider.wso2.token-uri=http://is.cd.mtn:9763/oauth2/token
spring.security.oauth2.client.provider.wso2.user-info-uri=http://is.cd.mtn:9763/oauth2/userinfo
spring.security.oauth2.client.provider.wso2.jwk-set-uri=http://is.cd.mtn:9763/oauth2/jwks

```

其中：

- 1、`spring.security.oauth2.client.registration.wso2.client-id `和`spring.security.oauth2.client.registration.wso2.client-secret`属性是在is中添加了服务提供者并配置了入站身份验证的配置下的`OAuth/OpenId`后自动生成的。你需要手动复制到`application.properties`文件中。
- 2、在IS中配置服务提供者时，只需要设置回调URL即可。针对本例子的回调URL如下所示：

```
http://192.168.200.24:8080/login/oauth2/code/oauth2-login-sample
```

# 三、指南

The following guides illustrate how to use some features concretely:

- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
- [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
- [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
- [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
- [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)