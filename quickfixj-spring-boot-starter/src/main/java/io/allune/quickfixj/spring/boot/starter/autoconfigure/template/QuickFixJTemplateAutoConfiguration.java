package io.allune.quickfixj.spring.boot.starter.autoconfigure.template;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuickFixJTemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QuickFixJTemplate quickFixJTemplate() {
        return new QuickFixJTemplate();
    }
}
