package com.olineshop.dao;

import com.olineshop.model.Role;
import com.olineshop.model.User;
import com.olineshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

//Класс для работы с пользователями в базе данных

public class UserDAO {
    private RoleDAO roleDAO = new RoleDAO();

    //Получить всех пользователей из базы данных
    //return список пользователей
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    //Получить пользователя по идентификатору
    //id идентификатор пользователя
    //return пользователь или null, если пользователь не найден
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Получить пользователя по логину
    //login логин пользователя
    //return пользователь или null, если пользователь не найден
    public User getUserByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Аутентифицировать пользователя по логину и паролю
    //return пользователь или null, если аутентифик не удалась
    public User authenticate(String login, String password) {
        String sql = "SELECT * FROM users WHERE login = ? AND password_hash = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // здесь должно быть хеширование пароля
            pstmt.setString(1, login);
            pstmt.setString(2, password); 

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Добавить нового пользователя в базу данных
    //user пользователь для добавления
    //return true, если пользователь успешно добавлен, иначе false
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (login, password_hash, first_name, last_name, email, phone, discount, role_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getLogin());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhone());
            pstmt.setDouble(7, user.getDiscount());
            pstmt.setInt(8, user.getRole().getId());

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Обновить пользователя в базе данных
    //user пользователь для обновления
    //return true, если пользователь успешно обновлен, иначе false
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET login = ?, password_hash = ?, first_name = ?, last_name = ?, "
                + "email = ?, phone = ?, discount = ?, role_id = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getLogin());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhone());
            pstmt.setDouble(7, user.getDiscount());
            pstmt.setInt(8, user.getRole().getId());
            pstmt.setInt(9, user.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Обновить скидку пользователя в базе данных
    //userId идентификатор пользователя
    //discount новая скидка
    //return true, если скидка успешно обновлена, иначе false
    public boolean updateUserDiscount(int userId, double discount) {
        String sql = "UPDATE users SET discount = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, discount);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Удалить пользователя из базы данных
    //id идентификатор пользователя для удаления
    //return true, если пользователь успешно удален, иначе false
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

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

    //Извлечь пользователя из результата запроса
    //rs результат запроса
    //return пользователь

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        Role role = roleDAO.getRoleById(rs.getInt("role_id"));
        
        return new User(
                rs.getInt("id"),
                rs.getString("login"),
                rs.getString("password_hash"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getDouble("discount"),
                role);
    }
} 