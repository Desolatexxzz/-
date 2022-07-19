package com.zhouyue.waimai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouyue.waimai.exception.CategoryException;
import com.zhouyue.waimai.pojo.Category;
import com.zhouyue.waimai.pojo.Dish;
import com.zhouyue.waimai.pojo.R;
import com.zhouyue.waimai.pojo.Setmeal;
import com.zhouyue.waimai.service.CategoryService;
import com.zhouyue.waimai.service.DishService;
import com.zhouyue.waimai.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 分页查询所有分类信息
     * @return
     */
    @GetMapping("/page")
    public R<Page> getAllCategories(int page, int pageSize){
        Page<Category> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        Page<Category> categoryPage = categoryService.page(pageInfo, queryWrapper);
        return R.success(categoryPage);
    }


    /**
     * 新增菜单、套餐分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        boolean save = categoryService.save(category);
        if (save){
            return R.success("新增成功");
        }
        return R.error("新增失败");
    }

    /**
     * 删除菜单
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){

        QueryWrapper<Dish> queryWrapper1 = new QueryWrapper<Dish>().eq("category_id", ids);
        QueryWrapper<Setmeal> queryWrapper2 = new QueryWrapper<Setmeal>().eq("category_id", ids);
        int dishCount = dishService.count(queryWrapper1);
        if (dishCount > 0){
            throw new CategoryException("该分类下还有菜品，无法删除");
        }
        int setmealCount = setmealService.count(queryWrapper2);
        if (setmealCount > 0){
            throw new CategoryException("该分类下还有套餐，无法删除");
        }
        boolean flag = categoryService.removeById(ids);
        if (flag) {
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }

    /**
     * 修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        boolean flag = categoryService.updateById(category);
        if (flag){
            return R.success("更新分类信息成功");
        }
        return R.error("出现错误，更新失败");
    }

    /**
     * 获取菜品的所有分类
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> getDishTypes(Category category){
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(category.getType() != null,"type",category.getType());
        queryWrapper.orderByDesc("update_time");
        List<Category> categoryList = categoryService.list(queryWrapper);
        return R.success(categoryList);
    }
}
