from fastapi import APIRouter


def create_products_routers(products_usecase):
    router = APIRouter()

    @router.get("/products")
    async def get_products():
        return products_usecase.get_products()


    return router