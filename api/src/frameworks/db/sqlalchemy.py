import os

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.engine import url
from sqlalchemy_utils import database_exists, create_database
from src.frameworks.db.base import Base



class SQLAlchemyClient:
    """
    Conexi�n a una base de datos SQL por medio del ORM SQL Alchemy.
    Es agn�stico a la base de datos misma (MySQL, Postgres, etc).
    """
    def __init__(self, test=False):

        driver = os.environ["SQL_ALCHEMY_DRIVER"]
        username = os.environ["SQL_ALCHEMY_USERNAME"]
        password = os.environ["SQL_ALCHEMY_PASSWORD"]
        database = os.environ["SQL_ALCHEMY_DATABASE"]
        # Creamos DB de test
        # self.test = test
        # if test:
        #     database += "_test"

        host = os.environ.get("SQL_ALCHEMY_HOST", None)

        db_url = url.URL.create(
            drivername=driver,
            username=username,
            password=password,
            database=database,
            host=host,
        )

        self.engine = create_engine(db_url, echo=False)
        # en caso de usar DB de test
        if test and not database_exists(self.engine.url):
            create_database(self.engine.url)
    
        self.session_factory = sessionmaker(bind=self.engine)


    def create_tables(self):
        """
        Crea tables
        """
        Base.metadata.create_all(self.engine)

    def drop_all(self):
        """
        Elimina todas las tablas
        """
        Base.metadata.drop_all(self.engine)
