from src.products.models.product import Product

class SQLAlchemyProductsRepository:
    def __init__(self, sqlalchemy_client):
        self.client = sqlalchemy_client
        self.session_factory = sqlalchemy_client.session_factory

    def get_products_by_ids(self, ids):
        with self.session_factory() as session:
            query = session.query(Product).filter(Product.id.in_(ids)).all()
            return query
