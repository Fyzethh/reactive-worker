from fastapi import APIRouter, HTTPException
from src.clients.schemas.clients import ClientResponse

def create_clients_routers(clients_usecase):
    router = APIRouter(
        prefix="/clients", 
        tags=["clients"] 
    )

    @router.get("/{client_id}", response_model=ClientResponse)
    def get_client(client_id: int):
        client = clients_usecase.get_client(client_id)
        if not client:
            raise HTTPException(status_code=404, detail="Client not found")
        return client

    return router