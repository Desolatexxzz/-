package com.zhouyue.waimai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhouyue.waimai.pojo.AddressBook;
import com.zhouyue.waimai.pojo.R;
import com.zhouyue.waimai.service.AddressBookService;
import com.zhouyue.waimai.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址簿
     * @return
     */
    @PostMapping
    public R<String> addAddressBook(@RequestBody AddressBook addressBook){
        log.info(addressBook.toString());
        addressBook.setUserId(BaseContext.getCurrentId());
        boolean save = addressBookService.save(addressBook);
        if (save){
            return R.success("新增成功");
        }
        return R.error("出现错误, 新增失败");
    }

    /**
     * 获取用户所有地址
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> getAllAddressBook(){
        Long id = BaseContext.getCurrentId();
        System.out.println(id);
        List<AddressBook> list = addressBookService.list(new QueryWrapper<AddressBook>().eq("user_id", id).orderByDesc("update_time"));
        return R.success(list);
    }

    /**
     * 设置默认地址
     * @param addressBook1
     * @return
     */
    @PutMapping("/default")
    public R<String> setDefaultAddress(@RequestBody AddressBook addressBook1){
        Long addressId = addressBook1.getId();
        AddressBook addressBook = addressBookService.getById(addressId);
        Long userId = addressBook.getUserId();
        QueryWrapper<AddressBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<AddressBook> list = addressBookService.list(queryWrapper);
        for (AddressBook book : list) {
            if (book.getId().equals(addressId)){
                System.out.println(book.getId() + "**" + addressId);
                book.setIsDefault(1);
            }else {
                book.setIsDefault(0);
            }
        }
        boolean flag = addressBookService.updateBatchById(list);
        if (flag){
            return R.success("设置成功");
        }
        return R.error("出现错误，设置失败");
    }

    /**
     * 获取默认地址
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefaultAddress(){
        Long userId = BaseContext.getCurrentId();
        QueryWrapper<AddressBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_default", 1);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        return R.success(addressBook);
    }
}
