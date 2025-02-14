package com.example.dao;

import com.example.model.Currency;
import com.example.model.Exchange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeDao {
    private CurrencyDao currencyDao = new CurrencyDao();
    private ObjectMapper objectMapper = new ObjectMapper();

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

    public void save(Exchange exchange) {

        String sql = "INSERT INTO exchange_rates (basecurrencyid, targetcurrencyid, rate) VALUES (?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int id = exchange.getId();
            Currency baseCurrency = exchange.getBaseCurrency();
            Currency targetCurrency = exchange.getTargetCurrency();
            double rate = exchange.getRate();

//            if(baseCurrency == null || targetCurrency == null || rate == 0) {
//                throw new IllegalArgumentException();
//            }

//            statement.setInt(1, id);
            statement.setInt(1, baseCurrency.getId());
            statement.setInt(2, targetCurrency.getId());
            statement.setDouble(3, rate);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsById(int id, String table) {
        String sql;
        if(table.equals("currencies"))
            sql = "SELECT COUNT(*) FROM currencies WHERE id = ?";
        else
            sql = "SELECT COUNT(*) FROM exchange_rates WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsPair(int base, int target) {
        String sql = "SELECT COUNT(*) FROM exchange_rates WHERE basecurrencyid = ? AND targetcurrencyid = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, base);
            statement.setInt(2, target);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getExchange(String name) throws JsonProcessingException {
        String sql;
        Currency base = new Currency();
        Currency target = new Currency();
        if(name.isEmpty()) {
            sql = "SELECT \n" +
                    "    er.id,\n" +
                    "    json_build_object(\n" +
                    "        'id', c1.id,\n" +
                    "        'code', c1.code,\n" +
                    "        'name', c1.fullname,\n" +
                    "        'sign', c1.sign\n" +
                    "    ) AS base_currency,\n" +
                    "    json_build_object(\n" +
                    "        'id', c2.id,\n" +
                    "        'code', c2.code,\n" +
                    "        'name', c2.fullname,\n" +
                    "        'sign', c2.sign\n" +
                    "    ) AS target_currency,\n" +
                    "    er.rate\n" +
                    "FROM \n" +
                    "    exchange_rates er\n" +
                    "JOIN \n" +
                    "    currencies c1 ON er.basecurrencyid = c1.id\n" +
                    "JOIN \n" +
                    "    currencies c2 ON er.targetcurrencyid = c2.id;";
        }
        else {
            base = objectMapper.readValue(currencyDao.getCurrency(name.substring(0, 3)), Currency.class);
            target = objectMapper.readValue(currencyDao.getCurrency(name.substring(3)), Currency.class);

            sql = "SELECT " +
                    "    er.id, " +
                    "    json_build_object(" +
                    "        'id', c1.id, " +
                    "        'code', c1.code, " +
                    "        'name', c1.fullname, " +
                    "        'sign', c1.sign " +
                    "    ) AS base_currency, " +
                    "    json_build_object(" +
                    "        'id', c2.id, " +
                    "        'code', c2.code, " +
                    "        'name', c2.fullname, " +
                    "        'sign', c2.sign " +
                    "    ) AS target_currency, " +
                    "    er.rate " +
                    "FROM " +
                    "    exchange_rates er " +
                    "JOIN " +
                    "    currencies c1 ON er.basecurrencyid = c1.id " +
                    "JOIN " +
                    "    currencies c2 ON er.targetcurrencyid = c2.id " +
                    "WHERE " +
                    "    er.basecurrencyid = ? AND er.targetcurrencyid = ?;";
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if(!name.isEmpty()) {
                statement.setInt(1, base.getId());
                statement.setInt(2, target.getId());
            }

            ResultSet resultSet = statement.executeQuery();
            if(!name.isEmpty()) {
                resultSet.next();
                Exchange exchange = new Exchange(resultSet.getInt("id"), base, target, resultSet.getDouble("rate"));
                resultSet.close();
                return objectMapper.writeValueAsString(exchange);
            }
            List<Exchange> exchangeList = new ArrayList<>();

            while (resultSet.next()) {
                Exchange exchange = new Exchange();
                
                exchange.setId(resultSet.getInt("id"));
                exchange.setBaseCurrency(objectMapper.readValue(resultSet.getString("base_currency"), Currency.class));
                exchange.setTargetCurrency(objectMapper.readValue(resultSet.getString("target_currency"), Currency.class));
                exchange.setRate(resultSet.getDouble("rate"));
                exchangeList.add(exchange);
            }

            resultSet.close();
            if(exchangeList.isEmpty())
                return "";
            if(exchangeList.size() == 1)
                return objectMapper.writeValueAsString(exchangeList.get(0));
            return objectMapper.writeValueAsString(exchangeList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String name, double rate) throws JsonProcessingException {
        String sql = "UPDATE exchange_rates SET rate = ? WHERE basecurrencyid = ? AND targetcurrencyid = ?";
        int base = objectMapper.readValue(currencyDao.getCurrency(name.substring(0,3)), Currency.class).getId();
        int target = objectMapper.readValue(currencyDao.getCurrency(name.substring(3)), Currency.class).getId();

        if(!existsPair(base, target))
            throw new AssertionError();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, rate);
            statement.setInt(2, base);
            statement.setInt(3, target);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
