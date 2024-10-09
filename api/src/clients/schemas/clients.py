from pydantic import BaseModel
from typing import List, Optional
from datetime import date

class PurchaseBase(BaseModel):
    product_name: str
    amount: int
    date: date

    class Config:
        orm_mode = True

    def dict(self, **kwargs):
        data = super().dict(**kwargs)
        data['date'] = data['date'].isoformat() if isinstance(data['date'], date) else data['date']
        return data

class PurchaseCreate(PurchaseBase):
    pass

class PurchaseResponse(PurchaseBase):
    id: int

    class Config:
        orm_mode = True

class ClientBase(BaseModel):
    name: str
    address: Optional[str] = None
    email: str
    phone_number: Optional[str] = None
    is_active: Optional[bool] = True

class ClientCreate(ClientBase):
    pass

class ClientUpdate(ClientBase):
    pass

class ClientResponse(ClientBase):
    id: int
    purchases: List[PurchaseResponse] = []

    class Config:
        orm_mode = True
