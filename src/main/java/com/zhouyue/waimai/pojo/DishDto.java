package com.zhouyue.waimai.pojo;

import com.zhouyue.waimai.pojo.Dish;
import com.zhouyue.waimai.pojo.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
