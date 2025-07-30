.PHONY: help install dev build test clean docker-up docker-down server-clean

.DEFAULT_GOAL := help

help:
	@echo "JournAI Makefile Commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'

install:
	cd client && npm install
	@echo "Java dependencies will be installed automatically by Maven"

dev:
	make -j2 dev-client dev-server

dev-client:
	cd client && npm run dev

dev-server:
	cd server && ./mvnw spring-boot:run

build:
	cd client && npm run build
	cd server && ./mvnw clean package -DskipTests

build-client:
	cd client && npm run build

build-server:
	cd server && ./mvnw clean package -DskipTests

test:
	cd client && npm test
	cd server && ./mvnw test

test-client:
	cd client && npm test

test-server:
	cd server && ./mvnw test

lint:
	cd client && npm run lint:fix

format:
	cd client && npm run format

clean:
	rm -rf client/node_modules client/.next
	rm -rf server/target
	rm -rf node_modules

server-clean:
	cd server && ./mvnw clean

docker-up:
	docker-compose up --build

docker-down:
	docker-compose down

docker-dev:
	chmod +x start-dev.sh && ./start-dev.sh

setup: install
	@echo "Setup complete! Run 'make dev' to start development"
