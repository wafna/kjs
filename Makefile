FORCE: ;

test: FORCE
	@./gradlew test

server: FORCE
	@./gradlew :server:run

browser: FORCE
	@./gradlew :browser:browserDevelopmentRun --continuous

build: FORCE
	@./gradlew clean build --warning-mode all
