package com.example.dao;

import com.example.model.Currency;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDao {
    ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Postgres Driver not found");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/currency", "postgres", "123123");
    }

    public void save(String code, String name, String sign) throws IOException {
        String sql = "INSERT INTO currencies (code, fullname, sign) VALUES(?, ?, ?);";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, sign);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Проблема с базой данных");
        }
    }

    public String getCurrency(String name) {
        List<Currency> currencyList = new ArrayList<>();

        String st;
        if (name.isEmpty()) {
            st = "SELECT * FROM currencies";
        } else {
            st = "SELECT * FROM currencies WHERE code = ?";
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(st)) {

            if (!name.isEmpty()) {
                statement.setString(1, name);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Currency currency = new Currency();
                    currency.setId(resultSet.getInt("id"));
                    currency.setName(resultSet.getString("fullname"));
                    currency.setCode(resultSet.getString("code"));
                    currency.setSign(resultSet.getString("sign"));

                    currencyList.add(currency);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(currencyList.isEmpty())
                return "";
            if(currencyList.size() == 1)
                return objectMapper.writeValueAsString(currencyList.get(0));

            return objectMapper.writeValueAsString(currencyList);
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrencyById(int id) {
        String sql = "SELECT * FROM currencies where id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            Currency currency = new Currency();

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    currency.setId(resultSet.getInt("id"));
                    currency.setName(resultSet.getString("fullname"));
                    currency.setCode(resultSet.getString("code"));
                    currency.setSign(resultSet.getString("sign"));
                }

                return objectMapper.writeValueAsString(currency);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        }
    }
}
