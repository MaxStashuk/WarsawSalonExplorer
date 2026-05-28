.PHONY: dev scrape backend frontend test

scrape:
	./gradlew :scraper:run

backend:
	./gradlew :backend:run

frontend:
	cd frontend && npm run dev

dev:
	make backend & make frontend