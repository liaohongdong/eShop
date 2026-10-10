package org.eu.liaohongdong.mpg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class UmsAdminTest {

    @Test
    void tableName_shouldBeUmsAdmin() {
        TableName tableName = UmsAdmin.class.getAnnotation(TableName.class);
        assertNotNull(tableName, "缺少 @TableName");
        assertEquals("ums_admin", tableName.value());
    }

    @Test
    void id_shouldMapToAutoIncrementPrimaryKey() {
        Field id = field("id");
        TableId tableId = id.getAnnotation(TableId.class);
        assertNotNull(tableId, "id 缺少 @TableId");
        assertEquals("id", tableId.value());
        assertEquals(IdType.AUTO, tableId.type());
    }

    @Test
    void fields_shouldMapToColumns() {
        assertColumn("username", "username");
        assertColumn("password", "password");
        assertColumn("icon", "icon");
        assertColumn("email", "email");
        assertColumn("nickName", "nick_name");
        assertColumn("note", "note");
        assertColumn("createTime", "create_time");
        assertColumn("loginTime", "login_time");
        assertColumn("status", "status");
    }

    @Test
    void everyField_shouldBeMapped() {
        for (Field field : UmsAdmin.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            boolean mapped = field.isAnnotationPresent(TableId.class)
                    || field.isAnnotationPresent(TableField.class);
            assertTrue(mapped, field.getName() + " 未映射为表字段");
        }
    }

    @Test
    void gettersAndSetters_shouldWork() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 10, 12, 0);
        UmsAdmin admin = new UmsAdmin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("pwd");
        admin.setIcon("icon.png");
        admin.setEmail("admin@eShop.com");
        admin.setNickName("管理员");
        admin.setNote("remark");
        admin.setCreateTime(now);
        admin.setLoginTime(now);
        admin.setStatus(1);

        assertEquals(1L, admin.getId());
        assertEquals("admin", admin.getUsername());
        assertEquals("pwd", admin.getPassword());
        assertEquals("icon.png", admin.getIcon());
        assertEquals("admin@eShop.com", admin.getEmail());
        assertEquals("管理员", admin.getNickName());
        assertEquals("remark", admin.getNote());
        assertEquals(now, admin.getCreateTime());
        assertEquals(now, admin.getLoginTime());
        assertEquals(1, admin.getStatus());
    }

    @Test
    void toString_shouldContainFieldValues() {
        UmsAdmin admin = new UmsAdmin();
        admin.setUsername("admin");
        admin.setStatus(1);
        String text = admin.toString();
        assertTrue(text.contains("admin"), "toString 应包含 username");
        assertTrue(text.contains("status"), "toString 应包含字段名 status");
    }

    @Test
    void shouldBeSerializable() {
        assertTrue(Serializable.class.isAssignableFrom(UmsAdmin.class));
    }

    private static Field field(String name) {
        try {
            return UmsAdmin.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            fail("字段不存在: " + name);
            throw new IllegalStateException(e);
        }
    }

    private static void assertColumn(String fieldName, String column) {
        TableField tableField = field(fieldName).getAnnotation(TableField.class);
        assertNotNull(tableField, fieldName + " 缺少 @TableField");
        assertEquals(column, tableField.value(), fieldName + " 列名不匹配");
    }
}
