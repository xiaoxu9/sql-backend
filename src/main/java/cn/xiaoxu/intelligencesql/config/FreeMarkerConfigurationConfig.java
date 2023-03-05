package cn.xiaoxu.intelligencesql.config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;

import java.io.*;

/**
 * FreeMarker 模板配置
 *
 * @author: https://github.com/xiaoxu9
 */
@org.springframework.context.annotation.Configuration
public class FreeMarkerConfigurationConfig {

	@Bean
	public static Configuration configuration() throws IOException {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
		// 这里不使用设置目录，因为jar包找的绝对路径不对，所以用setClassForTemplateLoading
		cfg.setClassForTemplateLoading(FreeMarkerConfigurationConfig.class, "/templates");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);
		cfg.setFallbackOnNullLoopVariable(false);
		cfg.setClassicCompatible(true);
		return cfg;
	}

}
