package cn.xiaoxu.intelligencesql.aop;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import cn.xiaoxu.intelligencesql.annotation.AuthCheck;
import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.model.entity.User;
import cn.xiaoxu.intelligencesql.service.UserService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验 AOP
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
@Aspect
@Component
public class AuthInterceptor {

	@Resource
	private UserService userService;

	/**
	 * 执行拦截
	 *
	 */
	@Around("@annotation(authCheck)")
	public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable{
		// 拦截全部权限角色封装到集合中
		List<String> anyRole = Arrays.stream(authCheck.anyRole()).filter(StringUtils::isNotBlank).collect(Collectors.toList());
		// 获取必须角色
		String mustRole = authCheck.mustRole();
		// 通过请求头 获取当前请求属性
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		// 通过请求属性获取请求链接
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
		// 通过请求链接获取当前登录用户
		User user = userService.getLoginUser(request);
		// 拥有任意权限即通过
		if(CollectionUtils.isNotEmpty(anyRole)){
			String userRole = user.getUserRole();
			// 不存任何一个权限
			if(!anyRole.contains(userRole)){
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
			}
		}
		// 必须拥有某个权限才通过
		if(StringUtils.isNotEmpty(mustRole)){
			// 获取当前用户权限
			String userRole = user.getUserRole();
			// 不存在必须权限
			if(!mustRole.equals(userRole)){
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
			}
		}
		// 通过权限校验，放行
		return joinPoint.proceed();
	}
}
