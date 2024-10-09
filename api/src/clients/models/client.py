from sqlalchemy import Column, Integer, String, ForeignKey, Boolean
from sqlalchemy.orm import relationship
from src.frameworks.db.base import Base

class Client(Base):
    __tablename__ = "clients"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), index=True, nullable=False)
    address = Column(String(255), nullable=True)
    email = Column(String(255), unique=True, index=True)
    phone_number = Column(String(20), nullable=True)
    is_active = Column(Boolean, default=True)

    purchases = relationship("Purchase", back_populates="client")

class Purchase(Base):
    __tablename__ = "purchases"

    id = Column(Integer, primary_key=True, index=True)
    client_id = Column(Integer, ForeignKey("clients.id"))
    product_name = Column(String(255), nullable=False)
    amount = Column(Integer, nullable=False)
    date = Column(String(10))

    client = relationship("Client", back_populates="purchases")
