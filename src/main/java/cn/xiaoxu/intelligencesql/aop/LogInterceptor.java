package cn.xiaoxu.intelligencesql.aop;

import cn.hutool.core.date.StopWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 请求响应日志 AOP
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
@Aspect
@Component
@Slf4j
public class LogInterceptor {

	/**
	 * 执行拦截
	 */
	@Around("execution(* cn.xiaoxu.intelligencesql.controller.*.*(..))")
	public Object doInterceptor(ProceedingJoinPoint point) throws Throwable{
		// 计时
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		// 获取请求路径
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		// 生成请求唯一id
		String requestId = UUID.randomUUID().toString();
		String url = httpServletRequest.getRequestURI();
		// 获取请求参数
		Object[] args = point.getArgs();
		// 把数组转为字符串
		String reqParam = "[" + StringUtils.join(args, ",") + "]";
		// 输出请求日志
		log.info("request start, id: {}, ip: {}, param: {}", requestId, url, httpServletRequest.getRemoteHost(), reqParam);
		// 执行原方法
		Object result = point.proceed();
		// 输出响应日志
		stopWatch.stop();
		Long totalTimeMillis = stopWatch.getTotalTimeMillis();
		log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis);
		return result;
	}
}
