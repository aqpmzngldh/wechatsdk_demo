package cn.qianyekeji.ruiji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qianyekeji.ruiji.entity.AddressBook;
import cn.qianyekeji.ruiji.mapper.AddressBookMapper;
import cn.qianyekeji.ruiji.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
