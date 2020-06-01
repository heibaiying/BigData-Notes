# Spring/Spring Boot 整合 Mybatis + Phoenix

<nav>
<a href="#一前言">一、前言</a><br/>
<a href="#二Spring-+-Mybatis-+-Phoenix">二、Spring + Mybatis + Phoenix</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-项目结构">2.1 项目结构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-主要依赖">2.2 主要依赖</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23--数据库配置文件">2.3  数据库配置文件</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24--配置数据源和会话工厂">2.4  配置数据源和会话工厂</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25-Mybtais参数配置">2.5 Mybtais参数配置</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#26-查询接口">2.6 查询接口</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#27-单元测试">2.7 单元测试</a><br/>
<a href="#三SpringBoot-+-Mybatis-+-Phoenix">三、SpringBoot + Mybatis + Phoenix</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-项目结构">3.1 项目结构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-主要依赖">3.2 主要依赖</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-配置数据源">3.3 配置数据源</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#34-新建查询接口">3.4 新建查询接口</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#35-单元测试">3.5 单元测试</a><br/>
<a href="#附建表语句">附：建表语句</a><br/>
</nav>

## 一、前言

使用 Spring+Mybatis 操作 Phoenix 和操作其他的关系型数据库（如 Mysql，Oracle）在配置上是基本相同的，下面会分别给出 Spring/Spring Boot 整合步骤，完整代码见本仓库：

+ [Spring + Mybatis + Phoenix](https://github.com/heibaiying/BigData-Notes/tree/master/code/Phoenix/spring-mybatis-phoenix)
+ [SpringBoot + Mybatis + Phoenix](https://github.com/heibaiying/BigData-Notes/tree/master/code/Phoenix/spring-boot-mybatis-phoenix)

## 二、Spring + Mybatis + Phoenix

### 2.1 项目结构

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/spring-mybatis-phoenix.png"/> </div>

### 2.2 主要依赖

除了 Spring 相关依赖外，还需要导入 `phoenix-core` 和对应的 Mybatis 依赖包

```xml
<!--mybatis 依赖包-->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis-spring</artifactId>
    <version>1.3.2</version>
</dependency>
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.4.6</version>
</dependency>
<!--phoenix core-->
<dependency>
    <groupId>org.apache.phoenix</groupId>
    <artifactId>phoenix-core</artifactId>
    <version>4.14.0-cdh5.14.2</version>
</dependency>
```

### 2.3  数据库配置文件

在数据库配置文件 `jdbc.properties`  中配置数据库驱动和 zookeeper 地址

```properties
# 数据库驱动
phoenix.driverClassName=org.apache.phoenix.jdbc.PhoenixDriver
# zookeeper地址
phoenix.url=jdbc:phoenix:192.168.0.105:2181
```

### 2.4  配置数据源和会话工厂

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context" xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.1.xsd http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd">

    <!-- 开启注解包扫描-->
    <context:component-scan base-package="com.heibaiying.*"/>

    <!--指定配置文件的位置-->
    <context:property-placeholder location="classpath:jdbc.properties"/>

    <!--配置数据源-->
    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <!--Phoenix 配置-->
        <property name="driverClassName" value="${phoenix.driverClassName}"/>
        <property name="url" value="${phoenix.url}"/>
    </bean>

    <!--配置 mybatis 会话工厂 -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"/>
        <!--指定 mapper 文件所在的位置-->
        <property name="mapperLocations" value="classpath*:/mappers/**/*.xml"/>
        <property name="configLocation" value="classpath:mybatisConfig.xml"/>
    </bean>

    <!--扫描注册接口 -->
    <!--作用:从接口的基础包开始递归搜索，并将它们注册为 MapperFactoryBean(只有至少一种方法的接口才会被注册;, 具体类将被忽略)-->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!--指定会话工厂 -->
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
        <!-- 指定 mybatis 接口所在的包 -->
        <property name="basePackage" value="com.heibaiying.dao"/>
    </bean>

</beans>
```

### 2.5 Mybtais参数配置

新建 mybtais 配置文件，按照需求配置额外参数， 更多 settings 配置项可以参考[官方文档](http://www.mybatis.org/mybatis-3/zh/configuration.html)

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<!-- mybatis 配置文件 -->
<configuration>
    <settings>
        <!-- 开启驼峰命名 -->
        <setting name="mapUnderscoreToCamelCase" value="true"/>
        <!-- 打印查询 sql -->
        <setting name="logImpl" value="STDOUT_LOGGING"/>
    </settings>
</configuration>
```

### 2.6 查询接口

```java
public interface PopulationDao {

    List<USPopulation> queryAll();

    void save(USPopulation USPopulation);

    USPopulation queryByStateAndCity(@Param("state") String state, @Param("city") String city);

    void deleteByStateAndCity(@Param("state") String state, @Param("city") String city);
}
```

```xml
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.heibaiying.dao.PopulationDao">


    <select id="queryAll" resultType="com.heibaiying.bean.USPopulation">
        SELECT * FROM us_population
    </select>

    <insert id="save">
        UPSERT INTO us_population VALUES( #{state}, #{city}, #{population} )
    </insert>

    <select id="queryByStateAndCity" resultType="com.heibaiying.bean.USPopulation">
        SELECT * FROM us_population WHERE state=#{state} AND city = #{city}
    </select>

    <delete id="deleteByStateAndCity">
        DELETE FROM us_population WHERE state=#{state} AND city = #{city}
    </delete>

</mapper>
```

### 2.7 单元测试

```java
@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:springApplication.xml"})
public class PopulationDaoTest {

    @Autowired
    private PopulationDao populationDao;

    @Test
    public void queryAll() {
        List<USPopulation> USPopulationList = populationDao.queryAll();
        if (USPopulationList != null) {
            for (USPopulation USPopulation : USPopulationList) {
                System.out.println(USPopulation.getCity() + " " + USPopulation.getPopulation());
            }
        }
    }

    @Test
    public void save() {
        populationDao.save(new USPopulation("TX", "Dallas", 66666));
        USPopulation usPopulation = populationDao.queryByStateAndCity("TX", "Dallas");
        System.out.println(usPopulation);
    }

    @Test
    public void update() {
        populationDao.save(new USPopulation("TX", "Dallas", 99999));
        USPopulation usPopulation = populationDao.queryByStateAndCity("TX", "Dallas");
        System.out.println(usPopulation);
    }


    @Test
    public void delete() {
        populationDao.deleteByStateAndCity("TX", "Dallas");
        USPopulation usPopulation = populationDao.queryByStateAndCity("TX", "Dallas");
        System.out.println(usPopulation);
    }
}
```

## 三、SpringBoot + Mybatis + Phoenix

### 3.1 项目结构

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/spring-boot-mybatis-phoenix.png"/> </div>

### 3.2 主要依赖

```xml
<!--spring 1.5 x 以上版本对应 mybatis 1.3.x (1.3.1)
        关于更多 spring-boot 与 mybatis 的版本对应可以参见 <a href="http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/">-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>1.3.2</version>
</dependency>
<!--phoenix core-->
<dependency>
    <groupId>org.apache.phoenix</groupId>
    <artifactId>phoenix-core</artifactId>
    <version>4.14.0-cdh5.14.2</version>
</dependency>
<dependency>
```

spring boot 与 mybatis 版本的对应关系：

| MyBatis-Spring-Boot-Starter 版本 | MyBatis-Spring 版本 | Spring Boot 版本 |
| -------------------------------- | ------------------- | ---------------- |
| **1.3.x (1.3.1)**                | 1.3 or higher       | 1.5 or higher    |
| **1.2.x (1.2.1)**                | 1.3 or higher       | 1.4 or higher    |
| **1.1.x (1.1.1)**                | 1.3 or higher       | 1.3 or higher    |
| **1.0.x (1.0.2)**                | 1.2 or higher       | 1.3 or higher    |

### 3.3 配置数据源

在 application.yml 中配置数据源，spring boot 2.x 版本默认采用 Hikari 作为数据库连接池，Hikari 是目前 java 平台性能最好的连接池，性能好于 druid。

```yaml
spring:
  datasource:
    #zookeeper 地址
    url: jdbc:phoenix:192.168.0.105:2181
    driver-class-name: org.apache.phoenix.jdbc.PhoenixDriver

    # 如果不想配置对数据库连接池做特殊配置的话,以下关于连接池的配置就不是必须的
    # spring-boot 2.X 默认采用高性能的 Hikari 作为连接池 更多配置可以参考 https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby
    type: com.zaxxer.hikari.HikariDataSource
    hikari:
      # 池中维护的最小空闲连接数
      minimum-idle: 10
      # 池中最大连接数，包括闲置和使用中的连接
      maximum-pool-size: 20
      # 此属性控制从池返回的连接的默认自动提交行为。默认为 true
      auto-commit: true
      # 允许最长空闲时间
      idle-timeout: 30000
      # 此属性表示连接池的用户定义名称，主要显示在日志记录和 JMX 管理控制台中，以标识池和池配置。 默认值：自动生成
      pool-name: custom-hikari
      #此属性控制池中连接的最长生命周期，值 0 表示无限生命周期，默认 1800000 即 30 分钟
      max-lifetime: 1800000
      # 数据库连接超时时间,默认 30 秒，即 30000
      connection-timeout: 30000
      # 连接测试 sql 这个地方需要根据数据库方言差异而配置 例如 oracle 就应该写成  select 1 from dual
      connection-test-query: SELECT 1

# mybatis 相关配置
mybatis:
  configuration:
    # 是否打印 sql 语句 调试的时候可以开启
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 3.4 新建查询接口

上面 Spring+Mybatis 我们使用了 XML 的方式来写 SQL，为了体现 Mybatis 支持多种方式，这里使用注解的方式来写 SQL。

```java
@Mapper
public interface PopulationDao {

    @Select("SELECT * from us_population")
    List<USPopulation> queryAll();

    @Insert("UPSERT INTO us_population VALUES( #{state}, #{city}, #{population} )")
    void save(USPopulation USPopulation);

    @Select("SELECT * FROM us_population WHERE state=#{state} AND city = #{city}")
    USPopulation queryByStateAndCity(String state, String city);


    @Delete("DELETE FROM us_population WHERE state=#{state} AND city = #{city}")
    void deleteByStateAndCity(String state, String city);
}
```

### 3.5 单元测试

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class PopulationTest {

    @Autowired
    private PopulationDao populationDao;

    @Test
    public void queryAll() {
        List<USPopulation> USPopulationList = populationDao.queryAll();
        if (USPopulationList != null) {
            for (USPopulation USPopulation : USPopulationList) {
                System.out.println(USPopulation.getCity() + " " + USPopulation.getPopulation());
            }
        }
    }

    @Test
    public void save() {
        populationDao.save(new USPopulation("TX", "Dallas", 66666));
        USPopulation usPopulation = populationDao.queryByStateAndCity("TX", "Dallas");
        System.out.println(usPopulation);
    }

    @Test
    public void update() {
        populationDao.save(new USPopulation("TX", "Dallas", 99999));
        USPopulation usPopulation = populationDao.queryByStateAndCity("TX", "Dallas");
        System.out.println(usPopulation);
    }


    @Test
    public void delete() {
        populationDao.deleteByStateAndCity("TX", "Dallas");
        USPopulation usPopulation = populationDao.queryByStateAndCity("TX", "Dallas");
        System.out.println(usPopulation);
    }

}

```



## 附：建表语句

上面单元测试涉及到的测试表的建表语句如下：

```sql
CREATE TABLE IF NOT EXISTS us_population (
      state CHAR(2) NOT NULL,
      city VARCHAR NOT NULL,
      population BIGINT
      CONSTRAINT my_pk PRIMARY KEY (state, city));
      
-- 测试数据
UPSERT INTO us_population VALUES('NY','New York',8143197);
UPSERT INTO us_population VALUES('CA','Los Angeles',3844829);
UPSERT INTO us_population VALUES('IL','Chicago',2842518);
UPSERT INTO us_population VALUES('TX','Houston',2016582);
UPSERT INTO us_population VALUES('PA','Philadelphia',1463281);
UPSERT INTO us_population VALUES('AZ','Phoenix',1461575);
UPSERT INTO us_population VALUES('TX','San Antonio',1256509);
UPSERT INTO us_population VALUES('CA','San Diego',1255540);
UPSERT INTO us_population VALUES('CA','San Jose',912332);
```



<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>