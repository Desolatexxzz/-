package com.zhouyue.waimai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhouyue.waimai.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
