package com.example.controller;

import com.example.dao.CurrencyDao;
import com.example.dao.ExchangeDao;
import com.example.model.*;
import com.example.utils.CheckUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    CurrencyDao currencyDao = new CurrencyDao();
    ExchangeDao exchangeDao = new ExchangeDao();
    ObjectMapper objectMapper = new ObjectMapper();
    CheckUtils checkUtils = new CheckUtils();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Exchange exchange = new Exchange();

        String base = req.getParameter("base");
        String target = req.getParameter("target");
        String rateParam = req.getParameter("rate");

        if(checkUtils.isAnyParameterMissing(base, target, rateParam)) {
            checkUtils.sendErrorResponse(resp, 400, "Отсутствует нужное поле формы");
            return;
        }

        double rate = Double.parseDouble(rateParam);
        String baseJSON = currencyDao.getCurrency(base);
        String targetJSON = currencyDao.getCurrency(target);

        if(baseJSON.isEmpty() || targetJSON.isEmpty()) {
            checkUtils.sendErrorResponse(resp, 404, "Одна (или обе) валюта из валютной пары не существует в БД");
            return;
        }

        int baseId = objectMapper.readValue(baseJSON, Currency.class).getId();
        int targetId = objectMapper.readValue(targetJSON, Currency.class).getId();

        exchange.setBaseCurrency(objectMapper.readValue(currencyDao.getCurrency(base), Currency.class));
        exchange.setTargetCurrency(objectMapper.readValue(currencyDao.getCurrency(target), Currency.class));
        exchange.setRate(rate);

        if(exchangeDao.existsPair(baseId, targetId)) {
            checkUtils.sendErrorResponse(resp, 409, "Валютная пара с таким кодом уже существует");
            return;
        }

        try {
            exchangeDao.save(exchange);
            resp.setStatus(201);
            resp.getWriter().write("OK");
        } catch (RuntimeException e) {
            checkUtils.sendErrorResponse(resp, 500, "Ошибка при подключении к БД");

        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String json ="";
        try {
            json = exchangeDao.getExchange("");
        } catch (RuntimeException e) {
            checkUtils.sendErrorResponse(resp, 500, "Ошибка при подключении к БД");
            return;
        }

        try {
            resp.setStatus(200);
            resp.getWriter().write(json);
        } catch (IOException e) {
            throw new RuntimeException("Error writing response", e);
        }
    }

}
