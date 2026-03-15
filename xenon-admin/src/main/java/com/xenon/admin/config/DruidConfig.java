package com.xenon.admin.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot3.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;
import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.sqlite.SQLiteConfig;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * druid 配置
 *
 * @author charles
 */
@Slf4j
@Configuration
public class DruidConfig {
    @Value("${app.database.type:sqlite}")
    private String databaseType;

    @Value("${app.database.name:charles}")
    private String databaseName;

    @Value("${app.database.host:127.0.0.1}")
    private String host;

    @Value("${app.database.port:5432}")
    private String port;

    /**
     * 配置数据源，通过 @ConfigurationProperties 直接将 spring.datasource.druid 下的属性绑定到 DruidDataSource
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid")
    public DataSource dataSource() {
        boolean isSqlite = "sqlite".equalsIgnoreCase(databaseType);

        // 重写 init()，在 @ConfigurationProperties 绑定之后、连接池启动之前强制覆盖 SQLite 连接池参数
        DruidDataSource dataSource = new DruidDataSource() {
            @Override
            public void init() throws SQLException {
                if (isSqlite) {
                    super.setInitialSize(1);
                    super.setMinIdle(1);
                    super.setMaxActive(1);
                }
                super.init();
            }
        };

        if (isSqlite) {
            // SQLite 并发优化配置
            Path dbPath = Paths.get("data", databaseName + ".db").toAbsolutePath();
            File dir = dbPath.toFile().getParentFile();
            if (!dir.exists() && !dir.mkdirs()) {
                log.error("生成data目录失败，无法创建数据库");
            }

            SQLiteConfig config = new SQLiteConfig();
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
            config.setSynchronous(SQLiteConfig.SynchronousMode.FULL);
            config.setBusyTimeout(60000);
            config.enforceForeignKeys(true);

            dataSource.setUrl("jdbc:sqlite:" + dbPath);
            dataSource.setDriverClassName("org.sqlite.JDBC");
            dataSource.setConnectProperties(config.toProperties());
        } else if ("postgresql".equalsIgnoreCase(databaseType)) {
            dataSource.setUrl("jdbc:postgresql://" + host + ":" + port + "/" + databaseName);
            dataSource.setDriverClassName("org.postgresql.Driver");
        }

        return dataSource;
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.druid")
    public DruidStatProperties druidStatProperties() {
        return new DruidStatProperties();
    }

    /**
     * 去除监控页面底部的广告
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.druid.statViewServlet.enabled", havingValue = "true")
    public FilterRegistrationBean removeDruidFilterRegistrationBean(DruidStatProperties properties) {
        // 获取web监控页面的参数
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        // 提取common.js的配置路径
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        final String filePath = "support/http/resources/js/common.js";
        // 创建filter进行过滤
        Filter filter = new Filter() {
            public void init(FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                chain.doFilter(request, response);
                // 重置缓冲区，响应头不会被重置
                response.resetBuffer();
                // 获取common.js
                String text = Utils.readFromResource(filePath);
                // 正则替换banner, 除去底部的广告信息
                text = text.replaceAll("<a.*?banner\"></a><br/>", "");
                text = text.replaceAll("powered.*?shrek.wang</a>", "");
                response.getWriter().write(text);
            }

            @Override
            public void destroy() {
            }
        };
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(commonJsPattern);
        return registrationBean;
    }
}
