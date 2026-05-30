.PHONY: dev scrape backend frontend test-backend
include .env
export

scrape:
	gradlew.bat :scraper:run

backend:
	gradlew.bat :backend:run

test-backend:
	gradlew.bat :backend:test

frontend:
	cd frontend && npm run dev

dev:
	cmd /c start "Warsaw Salons — Backend" gradlew.bat :backend:run
	cd frontend && npm run dev