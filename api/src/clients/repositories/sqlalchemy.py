from typing import List
from sqlalchemy.orm import joinedload
from src.clients.models.client import Client


class SQLAlchemyClientsRepository:
    def __init__(self, sqlalchemy_client):
        self.client = sqlalchemy_client
        self.session_factory = sqlalchemy_client.session_factory

    def get_client(self, client_id: int):
        with self.session_factory() as session:
            return (
                session.query(Client)
                .options(joinedload(Client.purchases))
                .filter(Client.id == client_id)
                .first()
            )
