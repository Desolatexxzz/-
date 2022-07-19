package com.zhouyue.waimai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhouyue.waimai.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
