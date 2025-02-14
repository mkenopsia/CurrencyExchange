package com.example.controller;

import com.example.dao.CurrencyDao;
import com.example.utils.CheckUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    CurrencyDao currencyDao = new CurrencyDao();
    ObjectMapper objectMapper = new ObjectMapper();
    CheckUtils checkUtils = new CheckUtils();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String sign = req.getParameter("sign");

        if(checkUtils.isAnyParameterMissing(code, name, sign)) {
            checkUtils.sendErrorResponse(resp, 400, "Отсутствует нужное поле формы");
            return;
        }

        if(!currencyDao.getCurrency(code).isEmpty()) {
            checkUtils.sendErrorResponse(resp, 409, "Валюта с таким кодом уже существует");
            return;
        }

        try {
            currencyDao.save(code, name, sign);
            resp.setStatus(201);
            resp.getWriter().write("OK");
        } catch (Exception e) {
            checkUtils.sendErrorResponse(resp, 500, "Проблема с базой данных");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String json = currencyDao.getCurrency("");

        try {
            resp.getWriter().write(json);
        } catch (IOException e) {
            checkUtils.sendErrorResponse(resp, 500, "Проблема с записью json'а");
        }
    }
}
