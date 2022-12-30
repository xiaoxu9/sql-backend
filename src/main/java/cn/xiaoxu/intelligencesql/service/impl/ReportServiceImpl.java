package cn.xiaoxu.intelligencesql.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.xiaoxu.intelligencesql.model.entity.Report;
import cn.xiaoxu.intelligencesql.service.ReportService;
import cn.xiaoxu.intelligencesql.mapper.ReportMapper;
import org.springframework.stereotype.Service;

/**
* @author XiaoXuTongXue
* @description 针对表【report(举报)】的数据库操作Service实现
* @createDate 2022-12-30 16:35:09
*/
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report>
    implements ReportService{

}




