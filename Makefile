.PHONY: dev scrape backend frontend test
include .env
export

scrape:
	gradlew.bat :scraper:run

backend:
	gradlew.bat :backend:run

frontend:
	cd frontend && npm run dev

dev:
	make backend & make frontend