package cn.xiaoxu.intelligencesql.common;

/**
 * 返回工具类
 *
 * @author: https://github.com/xiaoxu9
 */
public class ResultUtils {

	/**
	 * 成功
	 *
	 * @param data
	 * @param <T>
	 * @return
	 */
	public static <T> BaseResponse<T> success(T data) {
		return new BaseResponse<>(0, data, "ok");
	}

	/**
	 * 失败
	 *
	 * @param errorCode
	 * @return
	 */
	public static BaseResponse error(ErrorCode errorCode) {
		return new BaseResponse<>(errorCode);
	}

	/**
	 * 失败
	 *
	 * @param code
	 * @param message
	 * @return
	 */
	public static BaseResponse error(int code, String message) {
		return new BaseResponse(code, message);
	}

	/**
	 * 失败
	 *
	 * @param errorCode
	 * @return
	 */
	public static BaseResponse error(ErrorCode errorCode, String message) {
		return new BaseResponse(errorCode.getCode(), null, message);
	}
}
