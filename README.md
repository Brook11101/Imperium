# Imperium

Imperium is a Roman-governance-inspired multi-agent system.

## Project Structure

- `backend/imperium-domain`: shared domain models and workflow types
- `backend/imperium-api`: Spring Boot API service
- `backend/imperium-worker`: Spring Boot worker service
- `frontend`: React + Vite web application
- `docs`: product and technical design documents

## Prerequisites

- `mise install`
- `npm install` in `frontend/`

## Common Commands

```bash
mise install
mvn clean verify
cd frontend && npm install && npm run build
```
