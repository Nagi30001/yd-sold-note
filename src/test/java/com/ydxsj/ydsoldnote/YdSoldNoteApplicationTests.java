package com.ydxsj.ydsoldnote;

import com.ydxsj.ydsoldnote.bean.role.RolePermission;
import com.ydxsj.ydsoldnote.mapper.RoleMapper;
import com.ydxsj.ydsoldnote.mapper.UserTokenMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class YdSoldNoteApplicationTests {

    @Autowired
    private UserTokenMapper userTokenMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Test
    public void contextLoads() {
        List<RolePermission> r1001 = roleMapper.getPermissionByUserRole("R1005");
        System.err.println(r1001.toString());
    }

}
