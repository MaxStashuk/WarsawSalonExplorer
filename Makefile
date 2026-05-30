.PHONY: dev scrape backend frontend test
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
	make backend & make frontend