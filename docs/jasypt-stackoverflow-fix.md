# Jasypt StackOverflowError 修复说明

## 问题描述

应用启动时出现 `StackOverflowError`，错误发生在创建 `dataSource` bean 时：

```
org.springframework.beans.factory.UnsatisfiedDependencyException: 
Error creating bean with name 'sqlSessionFactory'
Caused by: java.lang.StackOverflowError
at com.ulisesbocchio.jasyptspringboot.filter.DefaultPropertyFilter.isMatch
```

## 根本原因

jasypt-spring-boot 库在处理加密属性（如 `ENC(...)`）时陷入了无限递归循环：

1. jasypt 尝试解密 `mail.password: ENC(...)` 
2. 解密过程需要读取 `jasypt.encryptor.password` 配置
3. Spring 的占位符解析机制被触发去解析 `${your.jasypt.password}`
4. 占位符解析又触发了 jasypt 的属性过滤器 `DefaultPropertyFilter.isMatch()`
5. 过滤器再次调用 `getProperty()` 获取属性值
6. 形成无限循环 → StackOverflowError

**调用链循环：**
```
DefaultPropertyFilter.isMatch() 
→ CachingDelegateEncryptablePropertySource.getProperty()
→ Spring PropertyPlaceholderHelper.parseStringValue()
→ DefaultPropertyResolver.resolvePropertyValue()
→ 回到 getProperty() 
→ 再次触发 isMatch()
→ ... (无限循环)
```

## 解决方案

在 jasypt 配置中添加属性过滤规则，排除不需要解密的属性前缀，避免递归解析。

### 修改的文件

1. **center-user-web/common-user-service/src/main/resources/application.yml**
2. **for-yourself-gateway/src/main/resources/application.yml**

### 具体配置

```yaml
jasypt:
  enabled: true
  encryptor:
    password: ${your.jasypt.password}
    algorithm: PBEWithMD5AndDES
    key-obtention-iterations: 1000
  
  # 新增：配置属性过滤器
  property:
    filter:
      # 排除包含 ${} 占位符的属性名
      exclude-names:
        - spring.application.name
        - jwt.issuer
        - jasypt.encryptor.password
        - truth.truth-key
      
      # 排除不需要解密的属性前缀（关键配置）
      exclude-prefixes:
        - jasypt          # jasypt 自身配置
        - spring.cloud    # Spring Cloud 配置
        - server          # 服务器配置
        - logging         # 日志配置
        - mybatis-plus    # MyBatis-Plus 配置
        - knife4j         # API 文档配置
        - cloudflare      # Cloudflare 配置
        - gateway         # 网关配置
        - truth           # Truth 配置
        - yulgnier        # 自定义配置
```

## 工作原理

- **exclude-names**: 精确匹配属性名，这些属性不会被 jasypt 处理
- **exclude-prefixes**: 匹配属性名前缀，以这些前缀开头的属性都不会被 jasypt 处理

通过排除这些属性前缀，jasypt 只处理真正需要解密的属性（如 `spring.mail.password`、`jwt.secret-key` 等），避免了在解析配置过程中触发自身的过滤器，从而打破无限循环。

## 验证修复

重新启动应用，应该能够正常启动而不再出现 StackOverflowError。

```bash
cd center-user-web/common-user-service
mvn spring-boot:run
```

## 注意事项

1. **生产环境**: `jasypt.encryptor.password` 应该从环境变量或密钥管理系统中读取，不要硬编码在配置文件中
2. **加密属性**: 只有明确标记为 `ENC(...)` 的属性才会被解密，其他属性不受影响
3. **性能优化**: 通过 exclude-prefixes 减少不必要的属性检查，还能提升应用启动速度

## 相关版本

- jasypt-spring-boot-starter: 3.0.5
- Spring Boot: 3.1.12
- Java: 17
