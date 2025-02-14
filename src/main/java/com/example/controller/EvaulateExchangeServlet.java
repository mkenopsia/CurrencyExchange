package com.example.controller;

import com.example.dao.CurrencyDao;
import com.example.dao.ExchangeDao;
import com.example.model.*;
import com.example.utils.CheckUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/exchange")
public class EvaulateExchangeServlet extends HttpServlet {
    ObjectMapper objectMapper = new ObjectMapper();
    CurrencyDao currencyDao = new CurrencyDao();
    ExchangeDao exchangeDao = new ExchangeDao();
    CheckUtils checkUtils = new CheckUtils();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String base = req.getParameter("from");
        String target = req.getParameter("to");
        double amount = Double.parseDouble(req.getParameter("amount"));

        Currency baseCurrency, targetCurrency;

        try {
            baseCurrency = objectMapper.readValue(currencyDao.getCurrency(base), Currency.class);
            targetCurrency = objectMapper.readValue(currencyDao.getCurrency(target), Currency.class);
        } catch (JsonParseException e) {
            checkUtils.sendErrorResponse(resp, 400, "Одной из валют нет в базе данных");
            return;
        }

        ExchangeResult exchangeResult = new ExchangeResult();
        exchangeResult.setBaseCurrency(baseCurrency);
        exchangeResult.setTargetCurrency(targetCurrency);
        exchangeResult.setAmount(amount);

        double rate = 0;
        double convertedAmount = 0;

        if(exchangeDao.existsPair(baseCurrency.getId(), targetCurrency.getId())) {
            rate = getExchangeRate(base + target);
            convertedAmount = (exchangeResult.getRate() * exchangeResult.getAmount());
        }
        else if (exchangeDao.existsPair(targetCurrency.getId(), baseCurrency.getId())) {
            rate = getExchangeRate(base + target);
            convertedAmount = (amount / exchangeResult.getRate());
        }
        else {
            double baseToUSD = (getExchangeRate(base + "USD"));
            double USDToTarget = getExchangeRate("USD" + target);
            double result = baseToUSD * amount;
            rate = result / amount;
            convertedAmount = (result * USDToTarget);
        }

        exchangeResult.setRate(rate);
        exchangeResult.setConvertedAmount(convertedAmount);

        try {
            resp.getWriter().write(objectMapper.writeValueAsString(exchangeResult));
        } catch (IOException e) {
            throw new RuntimeException("Error writing response", e);
        }
    }

    private double getExchangeRate(String pair) throws IOException {
        return objectMapper.readValue(exchangeDao.getExchange(pair), Exchange.class).getRate();
    }
}
