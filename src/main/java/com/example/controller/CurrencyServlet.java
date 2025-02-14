package com.example.controller;

import com.example.dao.CurrencyDao;
import com.example.utils.CheckUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    CurrencyDao currencyDao = new CurrencyDao();
    CheckUtils checkUtils = new CheckUtils();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String name = req.getPathInfo().substring(1);
        if(name.isEmpty()) {
            checkUtils.sendErrorResponse(resp, 400, "Код валюты отсутствует в адресе");
            return;
        }
        String json;
        try {
            json = currencyDao.getCurrency(name);
        } catch (RuntimeException e) {
            checkUtils.sendErrorResponse(resp, 500, "Проблема с базой данных");
            return;
        }
        if(json.isEmpty()) {
            checkUtils.sendErrorResponse(resp, 404, "Валюта не найдена");
            return;
        }

        resp.setStatus(200);
        resp.getWriter().write(json);
    }
}
