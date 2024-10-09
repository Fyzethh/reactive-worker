from fastapi import FastAPI

def create_fastapi_app(routers: list):

    app = FastAPI()

    for router in routers:
        app.include_router(router)

    return app
