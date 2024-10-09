import pytest
from fastapi.testclient import TestClient
from src.main import app 

client = TestClient(app)

def test_get_home():
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "Hello, Kafka & FastAPI!"}

def test_get_get_client():
    pass

def test_get_get_products():
    pass

