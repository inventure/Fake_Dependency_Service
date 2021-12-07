# PROJECT NAME

Single sentence summary of the project

### Key objectives
Sample
```
* Consolidate all the decision criteria that is needed for each loan application i.e. rules, credit decision, fraud decision, downsample
* Provide transactional record at the end of each final decision
* Provide records for loan terms
```


## Project Structure

### Versioning
This project uses [Semantic Versioning 2.0.0](https://talamobile.atlassian.net/wiki/spaces/ATLAS/pages/885948433/Semantic+Versioning) starting with `v1.0.0`.

### Project Language/Platform/Boundaries
List of framework/platform/dependent components this project uses.

* Language [VERSION]
* Framework [VERSION]  (Possibly add link to confluence on how TALA utilizes the framework including any specific features of the framework.)
* Any specific plugins and module for the above framework
* ORM Framework, HTTP Framework, etc.
* Queues
* Databases (Redis, MariaDB, Cassandra)
* INBOUND Boundary Components/Services (internal/external)
* OUTBOUND Boundary Components/Services (internal/external)

### Project Architecture
This part should explain HOW the project achieves the key objectives for engineers/devops audience.  
This may be very different from the design doc which may have different audiences.
The focus should be on the HOW the service achieves its goals such as explaining the internal framework/architecture.

Should include flow chart of the happy path(s) that includes the internal details such as intra messaging, any usage of db/apis, etc.
Include the names of core implementation file in the flow chart.

Make sure explain how the inbound/outbound channels are configured. (REST vs Queue)

Optional: (Links to OpenAPI doc on any REST endpoints)


### Metrics
Explain which metrics are collected and how they are configured.

### Alerts
What types of alerts are configured for this service.  Explain what each alert may mean.

### Logging
Logging sources configured. Any other logging specific info.


## Running and testing

Subsections can include more details on any local configuration such as but not limited to
* Environment variable such as JFROG credentials
* Downloading any prerequisite binaries.
* Test configuration
* Starting locally vs starting with Docker
* Charles proxy set up
* Localstack set up

## Packaging and Deployment 
Any notes regarding the build process, deployment including containerization.

### Quality of Service
Expected load and performance. Scalability.

## Release Approval Service
Auto-update the changelog and create release tag by using following command `sbt "release with-defaults"`. This will push the changes to github as well.

## Troubleshooting
Add any common issues that developer/devops/operation needs to be aware of.

## Service Maintainers
List current SME and committers here.
