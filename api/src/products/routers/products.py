from typing import List
from fastapi import APIRouter, Query, HTTPException
from starlette.status import HTTP_404_NOT_FOUND

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

    return router
