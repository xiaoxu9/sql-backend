package cn.xiaoxu.intelligencesql.service;

import cn.xiaoxu.intelligencesql.model.entity.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author XiaoXuTongXue
* @description 针对表【table_info(表信息)】的数据库操作Service
* @createDate 2022-12-30 16:35:15
*/
public interface TableInfoService extends IService<TableInfo> {

	/**
	 * 校验并处理
	 *
	 * @param tableInfo
	 * @param add 是否为创建校验
	 */
	void validAndHandleTableInfo(TableInfo tableInfo, boolean add);
}
