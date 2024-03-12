# MCVE spring boot project for issue with lettuce
This project for https://github.com/spring-projects/spring-data-redis/issues/2865 

It has an integration test 
[LettuceIssueTest.java]
(src%2Ftest%2Fjava%2Fcom%2Fponomarev%2Flettuce%2Fissue%2Fmcve%2FLettuceIssueTest.java)
which launches a redis container (using test containers for that) and reproduces the issue with redis 
