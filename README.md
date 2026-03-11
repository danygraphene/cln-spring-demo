# CLN Spring Demo

A simple Spring Boot application that connects to Core Lightning via gRPC.

## Features

- **GET /api/info** - Get node information
- **GET /api/channels** - List peer channels with balances
- **POST /api/pay-offer** - Pay a BOLT12 offer

## Prerequisites

- Java 21+
- Maven 3.8+
- Core Lightning with gRPC enabled
- mTLS certificates from CLN

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
cln:
  grpc:
    host: 127.0.0.1
    port: 9736
    ca-cert: /path/to/ca.pem
    client-cert: /path/to/client.pem
    client-key: /path/to/client-key.pem
```

## Build

```bash
mvn clean compile
```

## Run

```bash
mvn spring-boot:run
```

## Usage

### Get node info
```bash
curl http://localhost:8080/api/info
```

### List channels
```bash
curl http://localhost:8080/api/channels
```

### Pay an offer
```bash
curl -X POST http://localhost:8080/api/pay-offer \
  -H "Content-Type: application/json" \
  -d '{
    "offer": "lno1qgsq...",
    "amountMsat": 1000000,
    "label": "my-payment"
  }'
```

## Project Structure

```
src/main/
├── java/com/example/clnspringdemo/
│   ├── ClnSpringDemoApplication.java
│   ├── config/
│   │   └── ClnGrpcConfig.java      # gRPC channel + mTLS setup
│   ├── controller/
│   │   └── ClnController.java      # REST endpoints
│   ├── dto/
│   │   ├── ChannelInfo.java
│   │   ├── NodeInfo.java
│   │   ├── PaymentResult.java
│   │   └── PayOfferRequest.java
│   └── service/
│       └── ClnService.java         # CLN gRPC calls
├── proto/
│   ├── node.proto                  # CLN gRPC definitions
│   └── primitives.proto
└── resources/
    └── application.yml
```

## Notes

- The proto files are from [CLN repository](https://github.com/ElementsProject/lightning/tree/master/cln-grpc/proto)
- mTLS is required - CLN generates certs at `~/.lightning/bitcoin/`
- BOLT12 offers require CLN with experimental features enabled
