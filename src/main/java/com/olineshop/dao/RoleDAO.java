package com.olineshop.dao;

import com.olineshop.model.Role;
import com.olineshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//Класс для работы с ролями в базе данных

public class RoleDAO {

    // Флаг createDefaultRoles
    private static boolean isCreatingDefaultRoles = false;

    //Получить все роли из базы данных
    //return список ролей
    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";
        System.out.println("Получение всех ролей из базы данных");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных");
                return roles;
            }
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Role role = new Role(
                        rs.getInt("id"),
                        rs.getString("name"));
                roles.add(role);
                System.out.println("Найдена роль: ID=" + role.getId() + ", Название=" + role.getName());
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении ролей: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();

            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
       
        if (roles.isEmpty() && !isCreatingDefaultRoles) {
            System.out.println("Роли не найдены. Создаем стандартные роли.");
            isCreatingDefaultRoles = true;
            boolean created = createDefaultRoles();
            if (created) {
                // Повторно получаем роли
                List<Role> newRoles = new ArrayList<>();
                try {
                    conn = DatabaseManager.getConnection();
                    if (conn == null) {
                        System.out.println("Ошибка: не удалось получить соединение с базой данных");
                        isCreatingDefaultRoles = false;
                        return newRoles;
                    }
                    
                    pstmt = conn.prepareStatement(sql);
                    rs = pstmt.executeQuery();
                    
                    while (rs.next()) {
                        Role role = new Role(
                                rs.getInt("id"),
                                rs.getString("name"));
                        newRoles.add(role);
                        System.out.println("Найдена роль после создания: ID=" + role.getId() + ", Название=" + role.getName());
                    }
                } catch (SQLException e) {
                    System.out.println("Ошибка при повторном получении ролей: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (pstmt != null) pstmt.close();
                    } catch (SQLException e) {
                        System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
                        e.printStackTrace();
                    }
                    isCreatingDefaultRoles = false;
                }
                return newRoles;
            }
            isCreatingDefaultRoles = false;
        }
        
        System.out.println("Всего найдено ролей: " + roles.size());
        return roles;
    }

    //Получить роль по идентификатору
    //id идентификатор роли
    //return роль или null, если роль не найдена
    public Role getRoleById(int id) {
        String sql = "SELECT * FROM roles WHERE id = ?";
        System.out.println("Получение роли по ID: " + id);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных");
                return null;
            }
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Role role = new Role(
                        rs.getInt("id"),
                        rs.getString("name"));
                System.out.println("Найдена роль: ID=" + role.getId() + ", Название=" + role.getName());
                return role;
            } else {
                System.out.println("Роль с ID=" + id + " не найдена");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении роли по ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                // Не закрываем соединение, так как оно управляется DatabaseManager
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    //Получить роль по названию
    //name название роли
    //return роль или null, если роль не найдена
    public Role getRoleByName(String name) {
        String sql = "SELECT * FROM roles WHERE name = ?";
        System.out.println("Получение роли по названию: " + name);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных");
                return null;
            }
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Role role = new Role(
                        rs.getInt("id"),
                        rs.getString("name"));
                System.out.println("Найдена роль: ID=" + role.getId() + ", Название=" + role.getName());
                return role;
            } else {
                System.out.println("Роль с названием '" + name + "' не найдена");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении роли по названию: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                // Не закрываем соединение, так как оно управляется DatabaseManager
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    //Добавить новую роль в базу данных
    //role роль для добавления
    //return true, если роль успешно добавлена, иначе false
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

    //Обновить роль в базе данных
    //role роль для обновления
    //return true, если роль успешно обновлена, иначе false
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

    //Удалить роль из базы данных
    //id идентификатор роли для удаления
    //return true, если роль успешно удалена, иначе false
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
    

    private boolean createDefaultRoles() {
        System.out.println("Создание стандартных ролей в базе данных");
        

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных");
                return false;
            }
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS `roles` (" +
                                   "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                                   "`name` VARCHAR(50) NOT NULL UNIQUE)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(createTableSQL)) {
                pstmt.executeUpdate();
                System.out.println("Таблица ролей создана или уже существует");
            }
            
            // Проверяем, существует ли уже роль "Администратор"
            String checkAdminSQL = "SELECT COUNT(*) FROM roles WHERE name = 'Администратор'";
            boolean adminExists = false;
            try (PreparedStatement pstmt = conn.prepareStatement(checkAdminSQL);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    adminExists = true;
                    System.out.println("Роль 'Администратор' уже существует");
                }
            }
            
            if (!adminExists) {
                Role adminRole = new Role();
                adminRole.setName("Администратор");
                addRole(adminRole);
                System.out.println("Роль 'Администратор' добавлена");
            }
            
            // Проверяем, существует ли уже роль "Клиент"
            String checkClientSQL = "SELECT COUNT(*) FROM roles WHERE name = 'Клиент'";
            boolean clientExists = false;
            try (PreparedStatement pstmt = conn.prepareStatement(checkClientSQL);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    clientExists = true;
                    System.out.println("Роль 'Клиент' уже существует");
                }
            }
            
            if (!clientExists) {
                Role clientRole = new Role();
                clientRole.setName("Клиент");
                addRole(clientRole);
                System.out.println("Роль 'Клиент' добавлена");
            }
            
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при создании стандартных ролей: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 