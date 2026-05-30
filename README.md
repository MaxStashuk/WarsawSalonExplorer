# Warsaw Beauty Salon Explorer

A full-stack local services marketplace for Warsaw hair and beauty salons. Real data collected from the Google Places API, exposed through a REST API, and presented in a browsable web UI with search, filtering, pagination, and inline editing.

Built as a take-home project for the SumUp Warsaw Accelerator 2026.

---

## How to run

### Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17+     |
| Node | 20+     |
| make | any     |

No Google Places API key is needed to run the app - the database ships pre-seeded with ~240 Warsaw salons.

### Scraping prerequisites

To test the scraping tool you need:

1. Delete the ```data/salons.db```
2. Create ```.env``` file in root directory. Which should contain following line:
    ```.dotenv
    GOOGLE_PLACES_API_KEY=your_api_key
    ```
    > Legacy Google Places API was used.

3. run
    ```bash
    make scrape
    ```

This will create and populate the ```data/salons.db``` database with the scraped data.

### Start everything

```bash
make dev
```



This opens the Ktor backend in a separate console window on `:8080`, then starts the Next.js frontend in the current terminal on `:3000`. Open `http://localhost:3000` in your browser.

>###### **Important**
>
>the makefile is designed specifically for Windows PowerShell style.
> might have limited compatibility on UNIX-based OS

### Individual targets

```bash
make backend      # Ktor REST API on :8080
make frontend     # Next.js dev server on :3000
make test-backend # Kotlin unit + integration tests
make scrape       # Re-scrape from Google Places API (requires GOOGLE_PLACES_API_KEY env var)
```

### Re-scraping (optional)

If you want to refresh the data, set your API key and run the scraper:

```bash
# create a .env file in the project root
echo GOOGLE_PLACES_API_KEY=your_key_here > .env
make scrape
```

The scraper upserts by `place_id`, so re-running it never creates duplicates.

---

## Technical solution

### Architecture

```
make dev
  ├── Ktor backend  :8080   (Kotlin, SQLite)
  └── Next.js frontend :3000 (TypeScript, Tailwind)
```

The frontend talks to the backend over HTTP. All data lives in a single SQLite file at `data/salons.db`, which is committed to the repository so the app works out of the box without running the scraper.

### Data collection - `scraper/`

A one-shot Kotlin CLI that populates the database. Run once via `make scrape`; reviewers don't need to run it.

**Algorithm:**
1. Eight hand-picked anchor coordinates covering Warsaw's districts (Śródmieście, Mokotów, Wola, Praga-Północ, Ursynów, Bemowo, Bielany, Targówek).
2. Nearby Search at each anchor for `beauty_salon` and `hair_care` types, 2 500 m radius - yields ~400 raw hits.
3. Deduplication by `place_id` before any enrichment calls.
4. Place Details fetch per unique place: phone, website, address components, price level, service types. 100 ms sleep between calls to stay within rate limits.
5. District derived from `sublocality_level_1` in address components; recorded as `"Unknown"` if absent.
6. Records missing name, address, or district are dropped. ~240 salons survive.
7. Upsert by `place_id` - re-runs refresh data without creating duplicates.

**Why Google Places API?** It is the authoritative, structured, publicly accessible source for local business data. It provides real ratings, review counts, phone numbers, websites, and geocoordinates in a single API family, which no scraping approach on a consumer website could match reliably.

### Backend - `backend/`

**Stack:** Kotlin · Ktor 2.3 · Exposed 0.50 · SQLite (xerial JDBC) · kotlinx.serialization

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/health` | Liveness check |
| `GET` | `/api/salons` | Paginated list with optional `district`, `q`, `sort`, `page`, `pageSize` |
| `GET` | `/api/salons/{id}` | Full salon detail |
| `PATCH` | `/api/salons/{id}` | Partial update of editable fields |
| `GET` | `/api/districts` | Distinct district list for the filter dropdown |

**Layering:**
- `routes/` - thin HTTP layer: parse params, call service, serialize response.
- `service/` - business logic: validation, pagination math, result assembly.
- `db/` - Exposed transactions and schema; no Ktor dependencies.

**PATCH semantics:** only `name`, `address`, `district`, `services`, and `priceLevel` are editable. Fields owned by the scraper (`phone`, `website`, `rating`, `reviewsCount`, coordinates) are read-only. Unknown fields in the request body return 400.

**Pagination:** two SQL queries per list request - a `COUNT(*)` with the active filters, and a `SELECT` with the same filters plus `LIMIT`/`OFFSET`. The service computes `totalPages` using integer ceiling division.

**Tests:** five service unit tests covering all validation branches in `patchSalon` (no database needed), plus four Ktor `testApplication` integration tests covering the list endpoint shape, 404 detail, successful PATCH, and unknown-field rejection. Integration tests use a temporary file-based SQLite database per test case.

### Frontend - `frontend/`

**Stack:** Next.js 14 (App Router) · TypeScript · Tailwind CSS · lucide-react · sonner

**Routes:**

| Path | Component type | What it does |
|------|---------------|-------------|
| `/` | Server | Fetches salons + districts in parallel, renders card grid |
| `/salons/[id]` | Server | Fetches salon detail, renders info + edit panel |
| `/salons/[id]` (edit) | Client | Dialog form, PATCH on submit, toast on success |

**Filter and pagination state lives in URL query params** (`?district=Mokotów&q=hair&sort=reviews&page=2&pageSize=20`). The list page is a server component that re-renders on navigation - no client-side fetching, no loading spinners, and links are fully shareable.

**Edit flow:** the "Edit details" button opens a native `<dialog>` element (focus-trapped, Escape to close). On submit it calls `PATCH /api/salons/{id}`, shows a toast via sonner on success and triggers `router.refresh()` to repaint the server-rendered data, or shows an inline error banner on failure.

### Data model

Single `salons` table in SQLite:

| Column | Type | Notes |
|--------|------|-------|
| `id` | INTEGER PK | Auto-increment |
| `place_id` | VARCHAR UNIQUE | Deduplication key |
| `name` | VARCHAR | Required |
| `address` | VARCHAR | Required |
| `district` | VARCHAR (indexed) | Required; `"Unknown"` if not derivable |
| `phone` | VARCHAR nullable | From Google |
| `website` | VARCHAR nullable | From Google |
| `services` | TEXT nullable | JSON array of Google `types` |
| `price_level` | INTEGER nullable | Google's 0–4 scale |
| `rating` | DOUBLE nullable | Google aggregate rating |
| `reviews_count` | INTEGER nullable | Google review count |
| `lat` / `lng` | DOUBLE nullable | Stored for future map view |
| `updated_at` | BIGINT | Epoch milliseconds, bumped on every PATCH |

---

## What I'd improve with more time

**Map view.** `lat` and `lng` are already stored for every salon. Adding a Mapbox or Leaflet map to the list page with salon pins is the highest-impact visual improvement.

**Place Photos.** The Google Places API returns photo references that can be resolved into real images. Salon cards with a cover photo would dramatically improve the browsing experience.

**Scheduled re-scrape.** The scraper is a one-shot CLI. A production version would run on a cron schedule, tile Warsaw into a grid (rather than eight hand-picked points), and persist which tiles have been scraped so the job is resumable after interruption.

**Scale to all of Poland.** Replace the static anchor grid with city centroids weighted by population, set radii proportional to city size, and run the same scraper logic. The algorithm is identical - only the input coordinates change.

**Pagination UX.** The current prev/next controls are functional but basic. Numbered page buttons and a "jump to page" input would help on large result sets.

**Auth and per-user favourites.** Right now any visitor can edit any salon. A production app would gate edits behind authentication and add a favourites list per user.

**Add-a-salon flow.** The brief only asked for editing, but a submission form with admin review would round out the marketplace model.

**E2E tests.** Unit and integration tests cover the backend well. Playwright tests driving the full browser flow (search → detail → edit → verify change) would close the remaining gap.

**Tighter production config.** CORS is wide-open in dev. A production deployment would scope CORS to the known frontend origin, add rate limiting on the API, and use structured JSON logging.
