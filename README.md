http://localhost:8080/spring-docker-db-sm/hello
http://localhost:8080/spring-docker-db-sm/helloPerson
with docker for windows
http://10.0.75.1:8080/spring-docker-db-sm/hello


create a mysql db named cce and add an entry to the table persons

create a secret in aws secrets manager, you need the secret name and the ARN for this examplef

change the class DatabasePropertiesListener to test the 3 approaches

approach 3: env variables
for fargate service you can add the environment variable to the  container defitions
using secrets or environment 
 ContainerDefinitions:

        Environment:
          - Name: dbSecret    
            Value: '{{resolve:secretsmanager:arn:aws:secretsmanager:eu-central-1:....}}'

          Secrets:
            - Name: dbSecret
              ValueFrom: arn:aws:secretsmanager:eu-central-1:....

for aws batch you can only use 

        Environment:
          - Name: dbSecret    
            Value: '{{resolve:secretsmanager:arn:aws:secretsmanager:eu-central-1:....}}'


## building the docker image
docker build -f src/main/docker/Dockerfile -t $TAG_NAME .
docker build -f src/main/docker/Dockerfile -t 0169......amazonaws.com/ecs-example-repository:spring-boot-db-sm .
  
docker run -d -p 8080:8080 0169......dkr.ecr.us-west-1.amazonaws.com/ecs-example-repository:spring-boot-db-sm

aws configure

$(aws ecr get-login --no-include-email --region us-west-1)
 docker push 0169......dkr.ecr.us-west-1.amazonaws.com/ecs-example-repository:spring-boot-db-sm

  
