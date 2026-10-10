package org.eu.liaohongdong.mpg;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.eu.liaohongdong.mpg.entity.UmsAdmin;
import org.eu.liaohongdong.mpg.mapper.UmsAdminMapper;
import org.eu.liaohongdong.mpg.service.IUmsAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 直接连本机真实 MySQL 的集成测试。
 * 每个用例运行在同一个事务里（@Transactional），方法结束自动回滚，不污染库中现有数据。
 */
@SpringBootTest(classes = TestApplication.class)
//@Transactional // 开启事务就不会落库，用例结束时整体回滚
class UmsAdminTest {

    private static final String TAG = "it_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

    @Autowired
    private UmsAdminMapper umsAdminMapper;

    @Autowired
    private IUmsAdminService umsAdminService;

    @Test
    @Commit // 落库
    void insertAndSelectById() {
        UmsAdmin admin = newAdmin("admin");

        int rows = umsAdminMapper.insert(admin);

        assertEquals(1, rows);
        assertNotNull(admin.getId(), "自增主键应回填到实体");

        UmsAdmin loaded = umsAdminMapper.selectById(admin.getId());
        assertNotNull(loaded);
        assertEquals(uname("admin"), loaded.getUsername());
        assertEquals("pwd", loaded.getPassword());
        assertEquals(1, loaded.getStatus());
        assertNotNull(loaded.getCreateTime());
    }

    @Test
    @Rollback(false) // 不回滚
    void updateById_shouldModifyRow() {
        UmsAdmin admin = newAdmin("admin");
        umsAdminMapper.insert(admin);

        admin.setUsername(uname("admin-updated"));
        admin.setStatus(0);
        int rows = umsAdminMapper.updateById(admin);

        assertEquals(1, rows);
        UmsAdmin loaded = umsAdminMapper.selectById(admin.getId());
        assertEquals(uname("admin-updated"), loaded.getUsername());
        assertEquals(0, loaded.getStatus());
    }

    @Test
    void deleteById_shouldRemoveRow() {
        UmsAdmin admin = newAdmin("to-delete");
        umsAdminMapper.insert(admin);

        int rows = umsAdminMapper.deleteById(admin.getId());

        assertEquals(1, rows);
        assertNull(umsAdminMapper.selectById(admin.getId()));
    }

    @Test
    void selectList_withQueryWrapper_shouldFilter() {
        umsAdminMapper.insert(newAdmin("filter-alice"));
        umsAdminMapper.insert(newAdmin("filter-bob"));

        List<UmsAdmin> tagged = umsAdminMapper.selectList(
                Wrappers.<UmsAdmin>lambdaQuery().likeRight(UmsAdmin::getUsername, TAG + "_filter"));
        assertEquals(2, tagged.size());

        List<UmsAdmin> bob = umsAdminMapper.selectList(
                Wrappers.<UmsAdmin>lambdaQuery().eq(UmsAdmin::getUsername, uname("filter-bob")));
        assertEquals(1, bob.size());
        assertEquals(uname("filter-bob"), bob.get(0).getUsername());
    }

    @Test
    void serviceSaveAndGetById() {
        UmsAdmin admin = newAdmin("svc-admin");

        assertTrue(umsAdminService.save(admin));

        UmsAdmin loaded = umsAdminService.getById(admin.getId());
        assertNotNull(loaded);
        assertEquals(uname("svc-admin"), loaded.getUsername());
        assertEquals(uname("svc-admin") + "@eShop.com", loaded.getEmail());
    }

    private static UmsAdmin newAdmin(String name) {
        UmsAdmin admin = new UmsAdmin();
        admin.setUsername(uname(name));
        admin.setPassword("pwd");
        admin.setEmail(uname(name) + "@eShop.com");
        admin.setNickName(name + "-nick");
        admin.setCreateTime(LocalDateTime.now());
        admin.setStatus(1);
        return admin;
    }

    private static String uname(String name) {
        return TAG + "_" + name;
    }
}
