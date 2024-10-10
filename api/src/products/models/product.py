from sqlalchemy import Column, Integer, String, Float, CheckConstraint
from src.frameworks.db.base import Base

class Product(Base):
    __tablename__ = "products"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True, nullable=False)
    description = Column(String, nullable=True)
    price = Column(Float, nullable=False)
    stock = Column(Integer, default=0, nullable=False)

    __table_args__ = (
        CheckConstraint('stock >= 0', name='check_stock_non_negative'),
    )
