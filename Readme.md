# Reactive java worker

Este proyecto consiste en un worker Java reactivo que procesa pedidos. 
El worker consume mensajes de un tópico de Kafka que contienen información básica de los pedidos, los enriquece consultando APIs externas, y los almacena en una base de datos MongoDB.

# Tecnologías utilizadas

- fastAPI/python - API para la gestión de información de clientes y productos.

- Springboot/java 21 -  Worker reactivo encargado de procesar los mensajes del tópico en Kafka, consultar las APIs de clientes/productos y almacenar las transacciones en MongoDB.

- Docker - Contenedores para aislar y gestionar servicios como Kafka, MongoDB y Redis.

- Redis - Para gestión de locks y almacenamiento de mensajes fallidos con contador de reintentos.

- Mysql - base de datos usada en las apis de información de clientes y productos.


# Estructura de carpeta
```bash
├── project-enviame (Main API REST empresas)
├── api   <--- api clientes/products
├── worker  <--- reactive worker
├── Makefile
├── .env.example
├── docker-compose.yml
├── README.md
└── .gitignore
```

# Pre-requisitos 📋
Para correr este proyecto, solo se necesita Docker y Docker Compose. 
Esto permite empaquetar y desplegar la aplicación en contenedores sin configuraciones adicionales.

```
Docker
docker-compose 
```
# Correr programa
El programa se puede correr de dos formas, utilizando Docker directamente o a través del archivo Makefile en la raíz del proyecto.

Usando Docker

```
docker-compose up --build
```
Usando Makefile

```
make up
```
# Validar funcionamiento

Para validar el funcionamiento, se deben seguir los siguientes estos pasos:

1.- Acceder al contenedor de Kafka mediante Docker con el siguiente comando:
```
docker exec -it kafka bash
```

2.- Dentro del contenedor, conéctarse al tópico llamado "orders" con el siguiente comando:

```
kafka-console-producer --broker-list localhost:9092 --topic orders
```
3.- Envíar un mensaje de prueba al tópico orders:

```
{"orderId": "12345","clientId": "1","products":[{"productId": 1,"name": "Laptop","quantity": 2},{"productId": 2,"name": "Smartphone","quantity": 1}]}
```
# Proceso de validación

<ol>
  <li>Si el cliente existe y está activo en la API de FastAPI, el Worker verifica que los productos tengan suficiente stock.</li>
  <li>Se implementa un mecanismo de reintentos exponenciales para manejar fallos en las consultas a la API, con un contador que lleva el número de intentos realizados.</li>
  <li>En caso de que todos los reintentos fallen, el error se registra y el mensaje se guarda en Redis junto con el contador de intentos.</li>
  <li>Si la petición es exitosa, el pedido se almacena en MongoDB.</li>
  <li>Redis se utiliza también para implementar un lock que previene que múltiples instancias del Worker procesen el mismo pedido para un cliente de forma simultánea.</li>
</ol>

# Nota sobre Redis

Redis almacena los mensajes fallidos en formato JSON, permitiendo un seguimiento detallado de los pedidos y los intentos de procesamiento.

