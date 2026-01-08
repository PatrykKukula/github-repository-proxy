# Github repository proxy

REST API that serves as Proxy between client and GitHub REST API. The purpose of application is to fetch 
repositories for given owner via GitHub API and present basic data about owner repositories and it's branches, in clean and user-friendly form. Application acts as a proxy 
and provide data mapping from GitHub API response to form, that contains data like repository name, owner login and for each 
branch it's name and last commit sha. Application returns public repositories that are not forks.

## Technologies
- Java 25
- Spring Boot 4.0.1
- HTTP Client
- Lombok
- WireMock

## How to run application

In order to run application user need to have Docker installed on his machine. Docker can be downloaded from the official website
https://www.docker.com/get-started/

To run the application user need to have Docker running on the machine. Then run a command in cli:

```bash
docker run --name github-repository-proxy -p 8090:8090 asteez/github-repository-proxy:1.0
```

## Consume API

In order to use application user need to have installed API testing tool like Postman. It can be downloaded form official website
https://www.postman.com/downloads/

When application container is running, open Postman and create new request. Docker container exposes it's services on port 8090.

### Endpoints

* **GET**  - */api/get-repositories* - fetch all public repositories for given owner, that are not forks

*url:* http://localhost:8090/api/get-repositories?ownerLogin=OWNERLOGIN where OWNERLOGIN shall be replaced with actual owner login.


Postman image for given endpoint:

![img.png](img.png)


#### **API Responses:**

#### *Status: 200 OK*
    [
        { 
            "name": String,
            "ownerLogin": String,
            "branches": [
                {
                "name": String,
                "lastCommitSha": String
                }
            ]
        }
    ]   
![img_1.png](img_1.png)
#### *Status: 404 NOT_FOUND*
    {
        "status": String,
        "message": String
    }

![img_2.png](img_2.png)
#### *Status: 429 TOO_MANY_REQUESTS*
    {
        "status": String,
        "message": String
    }
![img_3.png](img_3.png)
#### *Status: 500 INTERNAL_SERVER_ERROR*
    {
        "status": String,
        "message": String
    }
![img_4.png](img_4.png)

## Requests Rate limits

Application sends requests as an unauthenticated user. GitHub limits amount of REST API requests that 
application consumer can send within a specific amount of time. Whenever this situation occurs, information about rate limit reset
date will be displayed in the response body. 
> *Note*
> 
> If user has many repositories and many branches, rate limit may be exceeded in one API call.
> 