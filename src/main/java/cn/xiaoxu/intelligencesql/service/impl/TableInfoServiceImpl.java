package cn.xiaoxu.intelligencesql.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.xiaoxu.intelligencesql.model.entity.TableInfo;
import cn.xiaoxu.intelligencesql.service.TableInfoService;
import cn.xiaoxu.intelligencesql.mapper.TableInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author XiaoXuTongXue
* @description 针对表【table_info(表信息)】的数据库操作Service实现
* @createDate 2022-12-30 16:35:15
*/
@Service
public class TableInfoServiceImpl extends ServiceImpl<TableInfoMapper, TableInfo>
    implements TableInfoService{

}




