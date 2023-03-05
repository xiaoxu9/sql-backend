package cn.xiaoxu.intelligencesql.controller;

import cn.xiaoxu.intelligencesql.common.BaseResponse;
import cn.xiaoxu.intelligencesql.common.ErrorCode;
import cn.xiaoxu.intelligencesql.common.ResultUtils;
import cn.xiaoxu.intelligencesql.exception.BusinessException;
import cn.xiaoxu.intelligencesql.utils.CheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
//测试Controller
@RestController
@RequestMapping("verify")
public class VerifyController {

	@Autowired
	RedisTemplate redisTemplate;

	/**
	 * 生成验证码的接口
	 *
	 * @param response Response对象
	 * @param request  Request对象
	 * @throws Exception
	 */
	@PostMapping("/getCode")
	public void getCode(HttpServletResponse response, HttpServletRequest request) throws Exception {
		// 获取到session
		HttpSession session = request.getSession();
		// 取到sessionid
		String id = session.getId();

		// 利用图片工具生成图片
		// 返回的数组第一个参数是生成的验证码，第二个参数是生成的图片
		Object[] objs = CheckUtils.newBuilder()
				.setWidth(120)   //设置图片的宽度
				.setHeight(35)   //设置图片的高度
				.setSize(6)      //设置字符的个数
				.setLines(5)    //设置干扰线的条数
				.setFontSize(25) //设置字体的大小
				.setTilt(true)   //设置是否需要倾斜
				.setBackgroundColor(Color.LIGHT_GRAY) //设置验证码的背景颜色
				.build()         //构建VerifyUtil项目
				.createImage();  //生成图片
		// 将验证码存入Session
		session.setAttribute("SESSION_VERIFY_CODE_" + id, objs[0]);
		// 打印验证码
		System.out.println(objs[0]);

		// 设置redis值的序列化方式
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		// 在redis中保存一个验证码最多尝试次数
		redisTemplate.opsForValue().set(("VERIFY_CODE_" + id), "3", 1 * 60, TimeUnit.SECONDS);

		// 将图片输出给浏览器
		BufferedImage image = (BufferedImage) objs[1];
		response.setContentType("image/png");
		OutputStream os = response.getOutputStream();
		try {
			ImageIO.write(image, "png", os);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			os.close();
		}
	}

	/**
	 * 业务接口包含了验证码的验证
	 *
	 * @param code    前端传入的验证码
	 * @param request Request对象
	 * @return
	 */
	@GetMapping("/checkCode")
	public BaseResponse<Boolean> checkCode(String code, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String id = session.getId();

		// 将redis中的尝试次数减一
		String verifyCodeKey = "VERIFY_CODE_" + id;
		long num = redisTemplate.opsForValue().decrement(verifyCodeKey);

		// 如果次数次数小于0 说明验证码已经失效
		if (num < 0) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码失效!");
		}

		// 将session中的取出对应session id生成的验证码
		String serverCode = (String) session.getAttribute("SESSION_VERIFY_CODE_" + id);
		// 校验验证码
		if (null == serverCode || null == code || !serverCode.toUpperCase().equals(code.toUpperCase())) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误!");
		}

		// 验证通过之后手动将验证码失效
		redisTemplate.delete(verifyCodeKey);

		// 这里做具体业务相关
		return ResultUtils.success(true);
	}
}
