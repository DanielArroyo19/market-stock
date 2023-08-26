FROM python:3.9-slim

WORKDIR /app

COPY ./services/yfinance-poll/requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

RUN pip list


COPY ./services/yfinance-poll/. .

CMD ["pip", "list"]

CMD ["python", "app/app.py"]