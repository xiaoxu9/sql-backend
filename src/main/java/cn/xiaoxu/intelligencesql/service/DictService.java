package cn.xiaoxu.intelligencesql.service;

import cn.xiaoxu.intelligencesql.model.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author XiaoXuTongXue
* @description 针对表【dict(词库)】的数据库操作Service
* @createDate 2022-12-30 16:34:31
*/
public interface DictService extends IService<Dict> {
	/**
	 * 校验并处理
	 *
	 * @param dict
	 * @param add 是否为创建校验
	 */
	void validAndHandleDict(Dict dict, boolean add);
}
