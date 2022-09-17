# FlightBookingSystem
Booking flight ticket system written by java spring boot framework

1- Framework: Spring Boot 2
2- Database: MySQL

Hibernate, Thymeleaf, Spring Boot Security, Thymeleaf Dialect, JPA

Roles:
1. Admin: username=john, password:john123, Add/Remove flight, airplain, and aircraft, search flight, verfity ticket
2. Agent: username=mike, password:mike123, Book/Cancel ticket for passengers, search flight, verfity ticket




-----------
HOW-TOs

1. Run MySql Server locally in a Docker container:

> docker pull mysql/mysql-server
> 
> docker run -p 3306:3306  --name cont-mysql -e MYSQL_ROOT_HOST=% -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=ftb_db -d mysql/mysql-server
2. 


