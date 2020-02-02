package com.ydxsj.ydsoldnote.config.durid;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DruidConfig {
    //指定配置文件属性加载到Druid
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean(initMethod = "init", destroyMethod = "close")//Druid启动和销毁时执行的方法
    public DruidDataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        //将日志功能加载到了DruidDataSource
        dataSource.setProxyFilters(Lists.newArrayList(statFilter()));
        return dataSource;
    }

    //日志功能 可以加相关配置最终返回给Druid
    @Bean
    public Filter statFilter() {
        StatFilter statFilter = new StatFilter();
        statFilter.setSlowSqlMillis(5000);
        //是否打印日志
        statFilter.setLogSlowSql(true);
        //是否合并起来
        statFilter.setMergeSql(true);
        return statFilter;
    }
    //配置Druid特有的视图工具 并且设置访问路径
    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        Map<String, String> initParams = new HashMap<>();
        initParams.put("loginUsername", "admin"); //设置登陆账户
        initParams.put("loginPassword", "123");   //设置登陆密码
        initParams.put("allow", "");//默认就是允许所有访问  设置登陆访问ip
        initParams.put("deny", "192.168.15.21");    //设置谁不能访问
        //将设置参数传递过去
        servletRegistrationBean.setInitParameters(initParams);
        return servletRegistrationBean;
    }
}
