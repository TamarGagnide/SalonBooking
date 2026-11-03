# SalonBooking Frontend (demo)

Small React + Vite single-page app that calls the backend API. Deployed on Vercel.

## Links

| What | Link |
|---|---|
| Live frontend (Vercel) | https://salonbooking-frontend-flame.vercel.app/ |
| Backend API (Render) | https://salonbooking-api.onrender.com |
| Source code | https://github.com/TamarGagnide/SalonBooking |
| Project overview | [`../README.md`](../README.md) |

The backend is on a free Render plan that sleeps when idle, so the first request can take 30-60 seconds.

## What it can do

- Register and log in
- Check a stylist's availability for a time slot
- Book an appointment and cancel it

## What it can't do yet (and why)

- The backend has no endpoints for listing stylists, services or a customer's appointments, so stylist and service are typed as ID numbers and the bookings list only shows bookings from the current session.
- Booking needs a stylist, a service and working hours in the database, and there is no seed data yet.
- Login is simplified: the user object is kept in `localStorage` and there are no tokens.

## Structure

```
frontend/
├── index.html
├── vite.config.js
├── package.json
├── .env.example        VITE_API_URL=http://localhost:8080
└── src/
    ├── main.jsx        entry point
    ├── App.jsx         auth form + booking screen
    ├── api.js          all fetch calls to the backend
    └── styles.css      theme (CSS variables)
```

## Run locally

```bash
cp .env.example .env     # VITE_API_URL=http://localhost:8080
npm install
npm run dev              # http://localhost:5173
```

The backend allows `http://localhost:5173` by default (`CORS_ALLOWED_ORIGINS`).

## Deploy on Vercel

1. Import the repo in Vercel, set **Root Directory** to `frontend` and the preset to **Vite**. Build command `npm run build`, output `dist`.
2. Set `VITE_API_URL` to the Render backend URL (no trailing slash) *before* deploying, because Vite bakes it into the build.
3. Add the Vercel URL to `CORS_ALLOWED_ORIGINS` on the Render service.

Troubleshooting: the first Vercel build failed with `vite: command not found` because the frontend commit had not been pushed yet. Check which commit the platform is building.

## Colors

Palette from Coolors: `#d6c7cf #c6bbc1 #d5cbd0 #e8dce2 #ebe1e6 #f2e4ea #f2e4eb #f6e7ee #f4e6ed #f6ebf0`. They are all very light, so text, buttons and status colors use darker plum tones derived from them (`--ink`, `--accent` in `src/styles.css`).
