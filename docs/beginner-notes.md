# ============懒人笔记============

## ------------构建工具-------------

### -- maven：
        1.下载解压压缩包
        2.配置环境变量到电脑，MAVEN_HOME:你的maven安装目录（M2_HOME新时代推荐变量名）编辑 Path 变量，添加：%MAVEN_HOME%\bin
        3.配置【本地仓库路径】修改 conf/settings.xml
          找到文件里的 <localRepository> 标签（默认被注释）删掉注释，改为你自己的本地仓库文件夹（随便建个空文件夹）
        4.配置【阿里云镜像】（添加新标签，不是修改原有）
            <mirrors>
              <!-- 阿里云Maven镜像，新手必加 -->
              <mirror>
                <id>aliyunmaven</id>
                <name>阿里云公共仓库</name> 起个名字
                <url>https://maven.aliyun.com/repository/public</url>
                <mirrorOf>*</mirrorOf> <!-- 国内开发推荐用*，覆盖所有仓库或者central中央仓库 --> 指定要代理的仓库
            </mirrors>
        5.IDEA集成

## ------------数据库---------------

### -- MySQL：
        1.下载解压压缩包
        2.配置环境变量到电脑，配置 MYSQL_HOME 环境变量 + Path 加 bin
        3.初始化mysql mysqld --initialize-insecure（可以先不创建my.ini文件）
        4.注册服务 mysqld --install 若提示 The service already exists（服务已存在）：先执行 sc delete mysql 删除旧服务，再重新注册。
        5.启动服务 net start mysql
        6.MySQL 初始化时默认 root 用户无密码，需手动设置：mysql -uroot -p123456
        7.IDEA集成 spring集成 配置application.yml文件

## -----------缓存相关类-------------

### -- Redis：
        1.官方无默认win版本，去别处下载压缩包 默认端口6379
        2.到根目录注册服务：redis-server --service-install redis.windows.conf --service-name Redis
                    redis-server：Redis 的主程序文件
                    service-install：告诉 Redis 要注册成 Windows 系统服务
                    redis.windows.conf：指定启动时用的配置文件（必须写对路径 / 文件名）
                    service-name Redis：给服务起个名字，默认就是 Redis，可以不改
        3.最好写一个配置类防止乱码
        4.图形化工具 RedisInsight

## -----------接口文档--------------

### --Knife4j：
        1.引依赖
        2.配置application.yml文件
        3.写配置类，对应地方加注解

## -------------工具类--------------



## ---------消息通知类---------------

### --SMTP
        1.选择一个邮箱开通POP3/SMTP服务，并生成邮箱授权码
        2.引入依赖
        3.配置application.yml文件

## -----------日志及异常类------------

### --logback：
        1.配置application.yml文件
        2.用的地方加注解 @Slf4j

### --自定义异常：
        1.自定义一个异常
        2.写全局异常处理器

## --------安全加密相关类-------------

### --Cloudflare Turnstile 人机验证完整集成步骤
        1.账号注册与开通 Turnstile，左侧菜单找到Turnstile，点击Add site（添加站点）创建 Widget
          Site name：自定义名称 Domain：填写你的域名，本地测试可选择 "Non-website"  Widget type：推荐Managed（智能分析，可信用户免验证）
          创建成功后，获取一对密钥 Site key：前端公开密钥 Secret key：后端私有密钥
        2.引入依赖
          <!-- RestTemplate -->
            <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-web</artifactId>
            </dependency> 
          <!--或使用 HTTP客户端（用于调用Turnstile验证API） -->
            <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-webflux</artifactId>
            </dependency>
        3. 配置密钥（application.yml）
        4. 创建配置类与验证服务
        5. 可以写一个工具类

### --jasypt 加密配置文件的属性原文
        1.引入依赖
        2.在application.yml文件中添加jasypt的密码配置，引用系统环境变量
        3.写一个工具类 用于加密，随用随删


### --BCrypt:
        1.引入依赖
        2.创建一个配置类注入BCryptPasswordEncoder

## --JWT:
        1.引入依赖
        2.写一个properties引入参数
        3.写一个工具类

## -----------开发辅助类-------------

### --糊涂工具包的雪花算法
        1.引入依赖
        2.创建一个配置类注入SnowFlake
        3.写一个config类注入创建雪花算法Bean


## --------服务注册与配置中心--------

### --Nacos（服务发现 + 配置管理 + 网关）：
       1.配置nacos：下载官方压缩包，解压，找到"nacos\conf\mysql-schema.sql"，创建mysql数据库，修改application.properties文件，修改nacos的配置 单机启动命令：startup.cmd -m standalone
       2.父项目引入spring cloud ,阿里巴巴 cloud 版本管理依赖
       3.服务注册与发现： 引入nacos依赖，配置application.yml文件，发现用FeignClient，引入依赖，用@FeignClient(value = "center-common-user-service")
         指定对应的服务名，里面写类似contriller的方法用注解就行，要远程调用的话，启动类添加@EnableDiscoveryClient，扫描对应的client包，然后直接注入调用方法就行。还可以配置连接池，引入依赖
         ，修改application.yml文件，添加连接池参数就行
       4.网关：
## -----------其它--------------

### --自动注入：
        1.在resources/META-INF/spring写一个org.springframework.boot.autoconfigure.AutoConfiguration.imports文件，添加你需要自动注入的类
        2.写一个配置类用@Import注解注入，首先会注入文件里提到的，然后这个类又得注入其他类
