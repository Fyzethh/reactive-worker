

class ProductsUsecase:
    def __init__(self, products_repository):
        self.repository = products_repository

    def get_products(self) -> list:
        return self.repository.get_products()

