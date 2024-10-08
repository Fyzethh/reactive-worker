FROM python:3.9-slim

WORKDIR /app

COPY ./requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY ./src /app

COPY ./wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

CMD ["./wait-for-it.sh", "kafka:9092","--","uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]

