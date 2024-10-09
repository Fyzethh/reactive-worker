

class ClientsUsecase:
    def __init__(self, clients_repository):
        self.repository = clients_repository

    def get_client(self, client_id) -> list:
        return self.repository.get_client(client_id)

