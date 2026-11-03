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
- Choose a stylist and service from dropdowns (loaded from the API)
- Check availability for a time slot, book it and cancel it
- Switch between English and Georgian (EN / ქარ in the header)

## What it can't do yet (and why)

- The bookings list only shows bookings made in the current session, because the backend has no "my appointments" endpoint yet.
- Booking needs stylists, services and working hours in the database. `backend/src/main/resources/data.sql` seeds them (Monday to Saturday, 09:00-18:00), so pick a time inside that window.
- Error messages that come straight from the API are in English; the common ones are translated in `src/i18n.js`.
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
    ├── i18n.js         English / Georgian translations
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

## Colors

Palette from Coolors: `#d6c7cf #c6bbc1 #d5cbd0 #e8dce2 #ebe1e6 #f2e4ea #f2e4eb #f6e7ee #f4e6ed #f6ebf0`. They are all very light, so text, buttons and status colors use darker plum tones derived from them (`--ink`, `--accent` in `src/styles.css`).
