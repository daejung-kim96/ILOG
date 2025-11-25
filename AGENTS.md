# Repository Guidelines

## Project Structure & Module Organization
Mono-repo hosts the UI, Spring services, AI workers, and infra. `frontend/` (Vite + React) keeps features inside `src/components`, `pages`, and `layouts`. `backend/`, `auth/`, `user/`, and `gateway/` reuse the Gradle wrapper; place code under `src/main/java`, configs in `src/main/resources`, and mirror packages inside `src/test/java`. FastAPI apps live in `ai/emotion-ai` and `ai/video-ai` with `app.py` entrypoints and per-service `requirements.txt`. Postgres assets sit in `database/postgre`, and every service is wired through the `docker-compose*.yml` files.

## Build, Test & Development Commands
- `docker-compose up -d database kafka-{1..3} connect auth backend user gateway` boots the core; append `frontend emotion-ai video-ai` for end-to-end slices.
- `cd frontend && npm install && npm run dev` serves :3000; `npm run build` writes artifacts to `frontend/dist`, which production nginx mounts at `/usr/share/nginx/html`, so sync that folder before deploying, and run `npm run lint` to catch regressions.
- `cd <service> && ./gradlew bootRun` runs any Spring module; `./gradlew build test` mirrors CI. FastAPI apps install `requirements.txt` in a venv and start via `uvicorn app:app --reload --port 8000`.

## Coding Style & Naming Conventions
- TypeScript: 2-space indent, PascalCase components, camelCase hooks, route folders under `src/pages/<feature>`, and Tailwind tokens managed in `tailwind.config.js`.
- Java/Python: 4-space indent for Spring layers (`config`, `domain`, `application`, `interfaces`) with explicit `*Controller/*Service/*Repository` suffixes and DTOs in `dto/`. FastAPI modules should follow PEP 8, keep inference code in `services/`, and load secrets through `python-dotenv`.

## Testing Guidelines
- Spring modules rely on JUnit 5; mirror `src/main` packages inside `src/test/java`, suffix files with `*Test`, and run `./gradlew test` before every push.
- Frontend and AI work should include automated coverage—Vitest/React Testing Library specs in `frontend/src/__tests__` (`*.test.tsx`) and contract tests in `ai/<service>/tests` using `pytest` + FastAPI `TestClient`, executed via `npx vitest run` and `python -m pytest`. If automation is impossible, document manual steps in the PR and open a follow-up task.

## Commit & Pull Request Guidelines
- History shows merge commits and terse imperatives (`1106front`, `필요없는 폴더 삭제`); keep subjects under 72 characters, verb-first, optionally prefixed (`feat(user): refresh token`), and describe schema/config deltas with linked issues.
- PRs must summarize context, test evidence (commands + results), impacted services, UI screenshots or GIFs when relevant, and env/port updates; tag reviewers from each domain and squash WIP commits.

## Security & Configuration Tips
- Store secrets in untracked `.env` files for Docker Compose and FastAPI, and keep `register-connector.json` synced with those credentials.
- Update `database/postgre/init.sql`, `nginx.conf`, `gateway`, and `docker-compose*.yml` together whenever schemas or ports move.
