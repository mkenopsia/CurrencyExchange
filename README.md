# Study project
mvc rest api+ java servlets + jdbc + postgres

**GET /currencies** <br>
Получение списка валют.

**GET /currency/EUR** <br>
Получение конкретной валюты.

**POST /currencies** <br>
Добавление новой валюты в базу. Данные передаются в теле запроса в виде полей формы (x-www-form-urlencoded).
Поля формы - name, code, sign

**GET /exchangeRates** <br>
Получение списка всех обменных курсов.

**GET /exchangeRate/USDRUB** <br>
Получение конкретного обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса. 

**POST /exchangeRates** <br>
Добавление нового обменного курса в базу. Данные передаются в теле запроса в виде полей формы (x-www-form-urlencoded).
Поля формы - baseCurrencyCode, targetCurrencyCode, rate. Пример полей формы:
baseCurrencyCode - USD
targetCurrencyCode - EUR
rate - 0.99

**PATCH /exchangeRate/USDRUB** <br>
Обновление существующего в базе обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса.
Данные передаются в теле запроса в виде полей формы (x-www-form-urlencoded). Единственное поле формы - rate.

**GET /exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT** <br>
Расчёт перевода определённого количества средств из одной валюты в другую. Пример запроса - GET /exchange?from=USD&to=AUD&amount=10.
