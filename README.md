# fa-reportbuilder

## How to run

1. git clone https://github.com/rodionks/fa-reportbuilder.git
2. cd fa-reportbuilder
3. edit file src/main/resources/application.properties: change values at  fa.graphql.username and fa.graphql.password
4. also you can edit spring.security.user.name, spring.security.user.password and server.port if you need
5. chmod +x gradlew (if you on linux or mac)
6. ./gradlew generateJava
7. ./gradlew bootRun

## How to use
Service will be available on `localhost:8087`
You can use it with browser or postman, using basic auth with username and password from application.properties:
```
localhost:8087/transactions?portfolioId=216&startDate=2022-01-01&endDate=2022-12-31
```