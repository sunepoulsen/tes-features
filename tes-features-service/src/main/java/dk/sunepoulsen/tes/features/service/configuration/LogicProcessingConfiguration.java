package dk.sunepoulsen.tes.features.service.configuration;

import dk.sunepoulsen.tes.springboot.rest.logic.configuration.ExecutorConfig;
import dk.sunepoulsen.tes.springboot.rest.logic.configuration.ThreadPoolExecutorFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConfigurationProperties(prefix = "logic.processing")
@Getter
@Setter
public class LogicProcessingConfiguration {

    private ExecutorConfig executor;

    @Bean
    public ThreadPoolTaskExecutor logicExecutor() {
        return ThreadPoolExecutorFactory.createExecutor(executor);
    }

}
