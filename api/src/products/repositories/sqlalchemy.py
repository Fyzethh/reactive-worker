from src.products.models.product import Product


class SQLAlchemyProductsRepository:
    def __init__(self, sqlalchemy_client):
        self.client = sqlalchemy_client
        self.session_factory = sqlalchemy_client.session_factory

    def get_products(self):
        with self.session_factory() as session:
            query = session.query(Product).all()

            return query


