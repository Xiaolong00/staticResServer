package com.ice.statics;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpEncodingAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@Configuration
//@EnableAutoConfiguration(exclude = { HttpEncodingAutoConfiguration.class })
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public EmbeddedServletContainerFactory createEmbeddedServletContainerFactory() {
		TomcatEmbeddedServletContainerFactory tomcatFactory = new TomcatEmbeddedServletContainerFactory();
		tomcatFactory.setPort(8081);
		tomcatFactory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
			public void customize(Connector connector) {
				Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
				// 设置最大连接数
				protocol.setMaxConnections(2000);
				// 设置最大线程数
				protocol.setMaxThreads(2000);
				protocol.setConnectionTimeout(30000);
			}
		});
		return tomcatFactory;
	}

}
