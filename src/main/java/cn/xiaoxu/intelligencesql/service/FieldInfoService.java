package cn.xiaoxu.intelligencesql.service;

import cn.xiaoxu.intelligencesql.model.entity.FieldInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author XiaoXuTongXue
* @description 针对表【field_info(字段信息)】的数据库操作Service
* @createDate 2022-12-30 16:34:58
*/
public interface FieldInfoService extends IService<FieldInfo> {

	/**
	 * 校验并处理
	 *
	 * @param fieldInfo
	 * @param add 是否为创建校验
	 */
	void validAndHandleFieldInfo(FieldInfo fieldInfo, boolean add);
}
