package com.olineshop.dao;

import com.olineshop.model.Role;
import com.olineshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с ролями в базе данных
 */
public class RoleDAO {

    /**
     * Получить все роли из базы данных
     * 
     * @return список ролей
     */
    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                roles.add(new Role(
                        rs.getInt("id"),
                        rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Получить роль по идентификатору
     * 
     * @param id идентификатор роли
     * @return роль или null, если роль не найдена
     */
    public Role getRoleById(int id) {
        String sql = "SELECT * FROM roles WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Role(
                            rs.getInt("id"),
                            rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получить роль по названию
     * 
     * @param name название роли
     * @return роль или null, если роль не найдена
     */
    public Role getRoleByName(String name) {
        String sql = "SELECT * FROM roles WHERE name = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Role(
                            rs.getInt("id"),
                            rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Добавить новую роль в базу данных
     * 
     * @param role роль для добавления
     * @return true, если роль успешно добавлена, иначе false
     */
    public boolean addRole(Role role) {
        String sql = "INSERT INTO roles (name) VALUES (?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.getName());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Обновить роль в базе данных
     * 
     * @param role роль для обновления
     * @return true, если роль успешно обновлена, иначе false
     */
    public boolean updateRole(Role role) {
        String sql = "UPDATE roles SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.getName());
            pstmt.setInt(2, role.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Удалить роль из базы данных
     * 
     * @param id идентификатор роли для удаления
     * @return true, если роль успешно удалена, иначе false
     */
    public boolean deleteRole(int id) {
        String sql = "DELETE FROM roles WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 