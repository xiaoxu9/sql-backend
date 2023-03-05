package cn.xiaoxu.intelligencesql.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

///**
// * @author xiaoxu9
// * @version 1.0
// * @description: tomcat配置http转htttps
// */
//@Configuration
public class TomcatConfig {
	//@Value("${my.httpServer.port}")
	//private Integer httpServerPort; //http的端口
	//@Value("${server.port}")
	//private Integer serverPort;//https的端口，也是配置文件中配置的端口
	//
	//@Bean
	//public ServletWebServerFactory servletContainer() {
	//	TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
	//		@Override
	//		protected void postProcessContext(Context context) {
	//			SecurityConstraint securityConstraint = new SecurityConstraint();
	//			securityConstraint.setUserConstraint("CONFIDENTIAL");
	//			SecurityCollection collection = new SecurityCollection();
	//			collection.addPattern("/*");
	//			securityConstraint.addCollection(collection);
	//			context.addConstraint(securityConstraint);
	//		}
	//	};
	//	tomcat.addAdditionalTomcatConnectors(redirectConnector());
	//	return tomcat;
	//}
	//
	//private Connector redirectConnector() {
	//	Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
	//	connector.setScheme("http");
	//	connector.setPort(httpServerPort);
	//	connector.setSecure(false);
	//	connector.setRedirectPort(serverPort);
	//	return connector;
	//}
}
