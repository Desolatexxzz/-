package com.zhouyue.waimai.pojo;

import com.zhouyue.waimai.pojo.Setmeal;
import com.zhouyue.waimai.pojo.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
