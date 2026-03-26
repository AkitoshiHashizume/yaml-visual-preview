.PHONY: help build test run clean sign release

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

build: ## Build the plugin
	./gradlew build --no-daemon

test: ## Run unit tests
	./gradlew test --no-daemon

run: ## Launch IntelliJ sandbox with the plugin loaded
	./gradlew runIde --no-daemon

sign: ## Build and sign the plugin for Marketplace
	./gradlew clean signPlugin --no-daemon

release: sign ## Build, sign, and show the upload file path
	@echo ""
	@echo "\033[32m=== Release artifact ===\033[0m"
	@ls -lh build/distributions/*-signed.zip
	@echo ""

clean: ## Clean build artifacts
	./gradlew clean --no-daemon
