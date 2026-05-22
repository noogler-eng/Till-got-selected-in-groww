import boto3
import json

# creating lambda client
lambda_clinet = boto3.client(
    'lambda',
    endpoint_url="http://localhost:4566",
    aws_access_key_id="test",                                                                        
    aws_secret_access_key="test",
    region_name='us-east-1'
)

# deploying lambda function
with open("function.zip", "rb") as f:
    zipped_code = f.read()

try:
    lambda_clinet.create_function(
        FunctionName="handler",
        Runtime="python3.11",
        Role="arn:aws:iam::000000000000:role/lambda-role",
        Handler="handler_lambda.handler",
        Code={"ZipFile": zipped_code},
    )
    print("Lambda deploy ho gayi!")
    print("Active hone ka wait kar raha hoon...")                                  
    waiter = lambda_clinet.get_waiter("function_active")                           
    waiter.wait(FunctionName="handler")
    print("Lambda Active ho gayi!")     
except lambda_clinet.exceptions.ResourceConflictException:
    lambda_clinet.update_function_code(
        FunctionName="handler",
        ZipFile=zipped_code,
    )                                                                                                
    print("Lambda update ho gayi!")

# print("Lambda function deployed successfully.")

response = lambda_clinet.invoke(
    FunctionName='handler',
    # InvocationType can be "Event" for asynchronous invocation or 
    # "RequestResponse" for synchronous invocation
    InvocationType="RequestResponse",
    Payload=json.dumps({"name": "Alice"})
)

result = json.loads(response['Payload'].read())
print(result)


# aws --endpoint-url=http://localhost:4566 lambda delete-function --function-name handler
# aws --endpoint-url=http://localhost:4566 lambda list-functions