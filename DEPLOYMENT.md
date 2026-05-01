# Deployment Guide

Use Vercel for the frontend and Render for the backend. To avoid MongoDB Atlas costs while keeping the current Spring Data MongoDB code, run MongoDB as a Render Private Service with a persistent disk.

## Recommended Stack

- Backend: Render Web Service
- Database: Render MongoDB Private Service with persistent disk
- Frontend: Vercel
- Payments: Razorpay subscription checkout
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
RAZORPAY_KEY_ID=<razorpay-key-id>
RAZORPAY_KEY_SECRET=<razorpay-key-secret>
RAZORPAY_PRO_PLAN_ID=<razorpay-plan-id-for-pro-monthly>
```

You can also use the included `render.yaml` blueprint for the backend, then fill `MONGO_URL` and payment secrets in the Render dashboard.

## Vercel Frontend

1. Import the repository into Vercel.
2. Set the root directory to `frontend`.
3. Build command: `npm run build`
4. Output directory: `dist`
5. Add these variables:

```bash
VITE_API_URL=https://<your-backend>.onrender.com
VITE_RAZORPAY_KEY_ID=<razorpay-key-id>
```

After Vercel gives you the final URL, add that URL to the backend `FRONTEND_URL` variable and redeploy the backend.

## Razorpay Setup

1. In Razorpay Dashboard, create a subscription plan for MeterFlow PRO, for example monthly INR 999.
2. Copy the generated plan id, such as `plan_xxxxx`.
3. In Render backend env vars, set:

```bash
RAZORPAY_KEY_ID=<key_id>
RAZORPAY_KEY_SECRET=<key_secret>
RAZORPAY_PRO_PLAN_ID=<plan_xxxxx>
```

4. In Vercel frontend env vars, set:

```bash
VITE_RAZORPAY_KEY_ID=<key_id>
```

The frontend opens Razorpay Checkout with the subscription id created by the backend. After payment, the backend verifies the Razorpay signature and marks the user as `PRO`.

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
