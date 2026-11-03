# SalonBooking Frontend

Minimal React + Vite single-page app, deployed on Vercel.

## Features

- Register and log in
- Check a stylist's availability for a time slot
- Book an appointment and cancel it

The backend has no endpoints yet for listing stylists, services or a customer's appointments, so stylist and service are entered as IDs and the bookings list only shows what was booked in the current session.

## Run locally

```bash
cp .env.example .env     # VITE_API_URL=http://localhost:8080
npm install
npm run dev              # http://localhost:5173
```

The backend allows `http://localhost:5173` by default (`CORS_ALLOWED_ORIGINS`).

## Deploy on Vercel

1. Import the repo in Vercel and set **Root Directory** to `frontend`.
2. Framework preset: Vite (auto-detected). Build command `npm run build`, output `dist`.
3. Add the environment variable `VITE_API_URL` = your Render backend URL (no trailing slash).
4. After deploying, set `CORS_ALLOWED_ORIGINS` on the Render service to the Vercel URL.

## Colors

Palette from Coolors: `#d6c7cf #c6bbc1 #d5cbd0 #e8dce2 #ebe1e6 #f2e4ea #f2e4eb #f6e7ee #f4e6ed #f6ebf0`. These are all very light, so text, buttons and status colors use darker plum tones derived from them (`--ink`, `--accent` in `src/styles.css`).
