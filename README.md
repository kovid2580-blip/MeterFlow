# MeterFlow

MeterFlow is a usage-based API billing platform. Developers can register, create API projects, generate API keys, route requests through a gateway, enforce daily rate limits, log usage, and generate monthly billing records.

## Stack

- Backend: Spring Boot, Spring Security, JWT, Spring Data MongoDB, Redis
- Frontend: React, Tailwind CSS, Recharts
- Payments: Razorpay subscriptions on top of billing records
- Deployment: Render for backend, Vercel for frontend, Render-hosted MongoDB instead of MongoDB Atlas

## Backend

```bash
cd backend
mvn spring-boot:run
```

Configure with environment variables or copy `backend/.env.example` into your runtime environment. The backend accepts both `MONGO_URL` and `MONGODB_URI`; Railway MongoDB exposes `MONGO_URL`.

Core endpoints:

- `POST /auth/register`
- `POST /auth/login`
- `POST /api/create`
- `GET /api/myapis`
- `DELETE /api/{id}`
- `POST /apikey/generate/{apiId}`
- `GET /apikey/mykeys`
- `POST /apikey/revoke/{id}`
- `POST /apikey/rotate/{id}`
- `GET /gateway/{apiName}/**` with `x-api-key`
- `GET /billing`
- `POST /api/subscriptions/create`
- `POST /api/subscriptions/verify`

Gateway flow:

1. Reads `x-api-key`
2. Validates the key and matching API
3. Checks Redis daily rate limits
4. Proxies the request to the stored API base URL
5. Stores endpoint, method, timestamp, status code, and latency

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Set `VITE_API_URL=http://localhost:8080` when the backend runs on a different host.

## MVP Build Order

1. Auth
2. API CRUD
3. API key generation
4. Gateway forwarding
5. Logging
6. Usage count
7. Billing
8. Dashboard
9. Rate limit
10. Deploy

## Deployment

For the lower-cost setup, use Render-hosted MongoDB with a persistent disk instead of MongoDB Atlas. See [DEPLOYMENT.md](DEPLOYMENT.md) for the Render backend/database and Vercel frontend steps.
