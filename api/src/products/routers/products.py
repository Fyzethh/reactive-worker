from typing import List
from fastapi import APIRouter, Query, HTTPException, Body
from starlette.status import HTTP_404_NOT_FOUND, HTTP_400_BAD_REQUEST

from src.products.schemas.productstockupdate import ProductStockUpdate

def create_products_routers(products_usecase):
    router = APIRouter(
        prefix="/products",
        tags=["products"]
    )

    @router.get("/")
    async def get_products(ids: List[int] = Query(..., description="Comma-separated list of product IDs")):
        products = products_usecase.get_products_by_ids(ids)

        if not products:
            raise HTTPException(status_code=HTTP_404_NOT_FOUND, detail="No products found for the given IDs")

        return products

    @router.patch("/stock")
    async def update_product_stock(stock_updates: List[ProductStockUpdate] = Body(..., description="List of products and their stock updates")):
        try:
            result = products_usecase.update_product_stock(stock_updates)
            if not result:
                raise HTTPException(status_code=HTTP_400_BAD_REQUEST, detail="Failed to update product stock")

            return {"message": "Stock updated successfully", "updated_products": result}
        except Exception as e:
            raise HTTPException(status_code=HTTP_400_BAD_REQUEST, detail=str(e))

    return router
