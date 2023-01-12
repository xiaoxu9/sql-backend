package cn.xiaoxu.intelligencesql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解定义
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

	/**
	 * 有任何一个角色
	 */
	String[] anyRole() default "";

	/**
	 * 必须有莫个角色
	 */
	String mustRole() default "";
}
