FROM python:3.9-slim

WORKDIR /app

COPY ./requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

COPY ./wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

CMD ["./wait-for-it.sh", "mysql:3306", "--", "uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]

