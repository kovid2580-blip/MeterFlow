# Deployment Guide

Use Vercel for the frontend and Render for the backend. To avoid MongoDB Atlas costs while keeping the current Spring Data MongoDB code, run MongoDB as a Render Private Service with a persistent disk.

## Recommended Stack

- Backend: Render Web Service
- Database: Render MongoDB Private Service with persistent disk
- Frontend: Vercel
- Payments: PhonePe Standard Checkout for PRO plan upgrades
- Optional cache/rate limit: Render Key Value or disable Redis for early demos

Render supports MongoDB as a private Docker service backed by a persistent disk. This is cheaper than a managed Atlas production cluster, but it is self-managed: set up `mongodump` backups before using it for real customers.

## Render MongoDB

1. In Render, deploy MongoDB from Render's MongoDB guide/template.
2. Create it as a Private Service.
3. Use Docker.
4. Attach a persistent disk:
   - Mount path: `/data/db`
   - Size: start small, for example `10 GB`
5. After it deploys, copy the internal host and port shown in the dashboard. It usually looks like `mongo-xyz:27017`.

Use this app connection string:

```bash
MONGO_URL=mongodb://mongo-xyz:27017/meterflow
```

## Render Backend

1. Create a new Render Web Service from this repository.
2. Root directory: `backend`
3. Runtime: Docker
4. Health check path: `/health`
5. Add these variables:

```bash
MONGO_URL=mongodb://<render-mongo-private-host>:27017/meterflow
JWT_SECRET=<long-random-secret>
FRONTEND_URL=https://<your-vercel-app>.vercel.app
PHONEPE_ENVIRONMENT=SANDBOX
PHONEPE_CLIENT_ID=<phonepe-client-id>
PHONEPE_CLIENT_VERSION=<phonepe-client-version>
PHONEPE_CLIENT_SECRET=<phonepe-client-secret>
PHONEPE_REDIRECT_URL=https://<your-vercel-app>.vercel.app
PHONEPE_PRO_AMOUNT_PAISE=99900
```

You can also use the included `render.yaml` blueprint for the backend, then fill `MONGO_URL` and payment secrets in the Render dashboard.

## Vercel Frontend

1. Import the repository into Vercel.
2. Set the root directory to `frontend`.
3. Build command: `npm run build`
4. Output directory: `dist`
5. Add this variable:

```bash
VITE_API_URL=https://<your-backend>.onrender.com
```

After Vercel gives you the final URL, add that URL to the backend `FRONTEND_URL` variable and redeploy the backend.

## PhonePe Setup

1. In PhonePe Business Dashboard, open Developer Settings and copy your Standard Checkout credentials.
2. Use `SANDBOX` while testing with UAT credentials and switch to `PRODUCTION` only after PhonePe gives you live credentials.
3. In Render backend env vars, set:

```bash
PHONEPE_ENVIRONMENT=SANDBOX
PHONEPE_CLIENT_ID=<client_id>
PHONEPE_CLIENT_VERSION=<client_version>
PHONEPE_CLIENT_SECRET=<client_secret>
PHONEPE_REDIRECT_URL=https://<your-vercel-app>.vercel.app
PHONEPE_PRO_AMOUNT_PAISE=99900
```

The frontend opens PhonePe Checkout with the redirect URL created by the backend. After checkout concludes, the backend checks PhonePe order status and marks the user as `PRO` only when the order state is `COMPLETED`.

## Local Smoke Test

```bash
cd backend
mvn spring-boot:run
```

```bash
cd frontend
npm run build
```

Keep `MONGO_URL` or `MONGODB_URI` pointed at a local MongoDB instance when running locally.
