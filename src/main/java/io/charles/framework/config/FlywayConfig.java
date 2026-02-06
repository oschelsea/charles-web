package io.charles.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway 数据库迁移配置
 * 根据数据库类型动态设置迁移脚本位置
 *
 * @author charles
 */
@Slf4j
@Configuration
public class FlywayConfig {

    @Value("${app.database.type:sqlite}")
    private String databaseType;

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        log.info("Flyway: 使用 {} 数据库迁移脚本", databaseType);
        String location = "classpath:db/migration/" + databaseType;
        
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(location)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .validateOnMigrate(true)
                .outOfOrder(false)
                .mixed(true)
                .load();
    }
}
