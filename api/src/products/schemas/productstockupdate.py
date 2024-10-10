from pydantic import BaseModel

class ProductStockUpdate(BaseModel):
    product_id: int
    quantity: int

    class Config:
        schema_extra = {
            "example": [
                {"product_id": 1, "quantity": 10},
                {"product_id": 2, "quantity": 5},
            ]
        }
