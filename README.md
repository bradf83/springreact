# React + Spring Boot
An application built with a Spring Boot back end and ReactJS front end.  This is currently a combination of bits
and parts from the two tutorials linked in the credits below

# Running
Since I already forgot and screwed this up, time I record it
* Start the spring boot application on 8080
* Navigate to the app directory and run 
    ```
    yarn start
    ```
* You will now see your application on localhost:3000
* If you check out package.json you will notice a proxy that is used when running in development mode.  Not needed
when running in production.

## Supporting Data
Insert the following roles:
```sql
INSERT INTO roles(name) VALUES('ROLE_USER');
INSERT INTO roles(name) VALUES('ROLE_ADMIN');
```

## Postman Requests
POST Request to http://localhost:8080/api/auth/signup
```json
{
	"name": "Sterling Archer",
	"username": "archers",
	"email": "sterling.archer@isis.com",
	"password": "secret"
}
```

POST Request to http://localhost:8080/api/auth/signin
```json
{
	"usernameOrEmail": "archers",
	"password": "secret"
}
```

## Production
* Package the JAR file 
```text
mvn package -Pprod
```
* Run the Spring Boot application from JAR file
```text
java -jar target\<your jar>
```
* You will now see your fully functioning application on 8080

# TODO
* Getting Started - Node, Yarn, JDK
* Add Build to Maven
  * Currently have an issue with yarn test but once it's commented works fine
* Push to Github
* Service worker issues (mentioned at the bottom of the blog post)
  * Did not see this issue when running the JAR 
* Try using Spring REST crud out of the box
* Security, OKTA, In Memory, JDBC possibly
* Implement JWT Authentication - Against JDBC and In Memory (Half Done)
  * Refresh token?  Token Valid?  Blacklist Token?


# Credits
Based on the tutorials found here:
* [Okta Tutorial](https://developer.okta.com/blog/2018/07/19/simple-crud-react-and-spring-boot)
* [Callicoder Tutorial](https://www.callicoder.com/spring-boot-spring-security-jwt-mysql-react-app-part-1/)


