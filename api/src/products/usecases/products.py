

class ProductsUsecase:
    def __init__(self, products_repository):
        self.repository = products_repository

    def get_products_by_ids(self, ids) -> list:
        return self.repository.get_products_by_ids(ids)

    def update_product_stock(self, stock_updates) -> list:
        return self.repository.update_product_stock(stock_updates)