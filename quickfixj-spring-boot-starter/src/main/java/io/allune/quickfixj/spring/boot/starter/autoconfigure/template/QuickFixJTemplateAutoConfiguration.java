package io.allune.quickfixj.spring.boot.starter.autoconfigure.template;

import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link QuickFixJTemplate}.
 *
 * @author Eduardo Sanchez-Ros
 * @since 2.0.0
 */
@Configuration
public class QuickFixJTemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QuickFixJTemplate quickFixJTemplate() {
        return new QuickFixJTemplate();
    }
}
