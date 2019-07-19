package cn.stylefeng.guns.conf;

import cn.stylefeng.roses.core.config.properties.DruidProperties;
import cn.stylefeng.roses.core.config.properties.MutiDataSourceProperties;
import cn.stylefeng.roses.core.mutidatasource.DynamicDataSource;
import com.alibaba.druid.pool.DruidDataSource;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author: fumin
 * @Description:
 * @Date: Create in 2019/5/20 10:26
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    @Autowired
    DruidProperties druidProperties;

    @Autowired
    GunsFlowableProperties gunsFlowableProperties;

    @Autowired
    MutiDataSourceProperties mutiDataSourceProperties;

    //flowable单独的数据源
    private DruidDataSource flowableDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        druidProperties.config(dataSource);
        gunsFlowableProperties.config(dataSource);
        return dataSource;
    }
    @Override
    public void configure(SpringProcessEngineConfiguration springProcessEngineConfiguration) {

        springProcessEngineConfiguration.setDataSource(flowableDataSource());

        springProcessEngineConfiguration.setActivityFontName("宋体");
        springProcessEngineConfiguration.setLabelFontName("宋体");
        springProcessEngineConfiguration.setAnnotationFontName("宋体");



        //重写flowable主键生成策略
        springProcessEngineConfiguration.setIdGenerator(() -> InviteCodeGenerator.generateInviteCode());
    }
}
