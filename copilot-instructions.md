When writing tests, always:
- Use MockitoBean, not MockBean
- Use import tools.jackson.databind.ObjectMapper; when ObjectMapper is necessary
- If not present, add a application-test.properties file to the test resources folder with the following content:
  spring.datasource.url=jdbc:h2:mem:testdb
  spring.datasource.driverClassName=org.h2.Driver
  spring.jpa.hibernate.ddl-auto=create-drop
  spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
  spring.jpa.show-sql=false
- If not present, add the h2 dependency to the build.gradle file:
  testRuntimeOnly 'com.h2database:h2'