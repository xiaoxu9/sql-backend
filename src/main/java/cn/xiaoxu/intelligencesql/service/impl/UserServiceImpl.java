package cn.xiaoxu.intelligencesql.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.xiaoxu.intelligencesql.model.entity.User;
import cn.xiaoxu.intelligencesql.service.UserService;
import cn.xiaoxu.intelligencesql.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author XiaoXuTongXue
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2022-12-30 16:35:19
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




