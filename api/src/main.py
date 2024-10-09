from src.frameworks.db.sqlalchemy import SQLAlchemyClient
from src.frameworks.http.fastapi import create_fastapi_app
from src.products.repositories.sqlalchemy import SQLAlchemyProductsRepository
from src.products.usecases.products import ProductsUsecase
from src.products.routers.products import create_products_routers

from src.clients.repositories.sqlalchemy import SQLAlchemyClientsRepository
from src.clients.usecases.clients_usecase import ClientsUsecase
from src.clients.routers.clients import create_clients_routers 

# Init clients
sqlalchemy_client = SQLAlchemyClient()
sqlalchemy_client.create_tables()


# Respositories
clients_repository = SQLAlchemyClientsRepository(sqlalchemy_client)
products_repository = SQLAlchemyProductsRepository(sqlalchemy_client)

# Usecases
products_usecase = ProductsUsecase(products_repository)
clients_usecase = ClientsUsecase(clients_repository)


routers = [
    create_products_routers(products_usecase),
    create_clients_routers(clients_usecase)
]

app = create_fastapi_app(routers)



@app.get("/")
def read_root():
    return {"message": "Hello, Kafka & FastAPI!"}
