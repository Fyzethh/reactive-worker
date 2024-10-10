import logging

from src.products.models.product import Product

class SQLAlchemyProductsRepository:
    def __init__(self, sqlalchemy_client):
        self.client = sqlalchemy_client
        self.session_factory = sqlalchemy_client.session_factory

    def get_products_by_ids(self, ids):
        with self.session_factory() as session:
            query = session.query(Product).filter(Product.id.in_(ids)).all()
            return query

    def update_product_stock(self, stock_updates):
        updated_products = []
        with self.session_factory() as session:
            try:
                for update in stock_updates:
                    product = session.query(Product).filter(Product.id == update.product_id).one_or_none()
                    if not product:
                        continue
                    
                    new_stock = product.stock - update.quantity
                    if new_stock < 0:
                        raise ValueError(f"Not enough stock for product ID {update.product_id}")

                    product.stock = new_stock
                    session.add(product)
                    updated_products.append(product.id)

                session.commit()
                return updated_products
            except Exception as e:
                logging.error(f"Failed to update product stock: {str(e)}")
                session.rollback()
                raise e
