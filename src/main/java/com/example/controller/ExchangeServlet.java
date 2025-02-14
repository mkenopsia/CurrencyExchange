package com.example.controller;

import com.example.dao.CurrencyDao;
import com.example.dao.ExchangeDao;
import com.example.utils.CheckUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@WebServlet("/exchangeRate/*")
public class ExchangeServlet extends HttpServlet {
    ObjectMapper objectMapper = new ObjectMapper();
    CurrencyDao currencyDao = new CurrencyDao();
    ExchangeDao exchangeDao = new ExchangeDao();
    CheckUtils checkUtils = new CheckUtils();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String name = extractCurrencyPair(req);
        if (name == null) {
            checkUtils.sendErrorResponse(resp, 400, "Коды валют пары отсутствуют в адресе");
            return;
        }

        String json = exchangeDao.getExchange(name);
        if(json.isEmpty()) {
            checkUtils.sendErrorResponse(resp, 404, "Обменный курс для пары не найден");
            return;
        }

        resp.setStatus(200);
        resp.getWriter().write(json);
    }
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String rateParam = req.getParameter("rate");
        if(rateParam == null || rateParam.isEmpty()) {
            checkUtils.sendErrorResponse(resp, 400, "Отсутствует нужное поле формы");
            return;
        }

        double rate = Double.parseDouble(rateParam);
        String name = req.getPathInfo().substring(1);

        try {
            exchangeDao.update(name, rate);
            resp.setStatus(200);
            resp.getWriter().write("OK");
        } catch (AssertionError e) {
            checkUtils.sendErrorResponse(resp, 404, "Валютная пара отсутствует в базе данных");
        }
    }

    private String extractCurrencyPair(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() == 7 ) {
            return pathInfo.substring(1).toUpperCase();
        }
        return null;
    }
}
