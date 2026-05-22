import boto3
import json

# making an s3 client to interact with localstack's s3 service
s3 = boto3.client(
    's3',
    endpoint_url="http://localhost:4566",                                                                          
    aws_access_key_id="test",
    aws_secret_access_key="test",
    region_name='us-east-1'
)

# creating a bucket in localstack's s3
lambda_client = boto3.client(
    'lambda',
    endpoint_url="http://localhost:4566",                                                                          
    aws_access_key_id="test",
    aws_secret_access_key="test",
    region_name='us-east-1'
)

# deploying lambda
with open("s3_trigger.zip", "rb") as f:
    zipped_code = f.read()

lambda_client.create_function(
    FunctionName='s3_trigger_lambda',
    Runtime='python3.8',
    Role='arn:aws:iam::123456789012:role/lambda-role',
    Handler='s3_trigger_lambda.handler',
    Code={'ZipFile': zipped_code},
    Timeout=300,
)

waiter = lambda_client.get_waiter("function_active")
waiter.wait(FunctionName="s3_trigger_lambda")
print("S3 Trigger Lambda deploy ho gayi!")

# giving lambda permission to be triggered by s3
lambda_client.add_permission(
    FunctionName='s3_trigger_lambda',
    StatementId='s3_trigger_permission',
    Action='lambda:InvokeFunction',
    Principal='s3.amazonaws.com',
    SourceArn='arn:aws:s3:::my-bucket'
)

# creating bucket
s3.create_bucket(Bucket='trigger-bucket')

lambda_info = lambda_client.get_function(FunctionName="s3_trigger_lambda")                              
lambda_arn = lambda_info["Configuration"]["FunctionArn"]                                                 
print(f"Lambda ARN: {lambda_arn}")   

# s3 trigger setup
s3.put_bucket_notification_configuration(
    Bucket='trigger-bucket',
    NotificationConfiguration={
        'LambdaFunctionConfigurations': [
            {
                # ARN of the Lambda function to invoke when an object is created in the bucket
                # Lambda ARN: arn:aws:lambda:us-east-1:000000000000:function:s3_trigger_lambda
                'LambdaFunctionArn': lambda_arn,
                # any file comes created a trigger to lambda function
                'Events': ['s3:ObjectCreated:*']
            }
        ]
    }
)

# uploading a file to trigger the lambda function
s3.put_object(
    Bucket='trigger-bucket', 
    Key='test-file.txt', 
    Body='Hello, this is a test file to trigger the Lambda function!'
)
print("File uploaded, Lambda function should be triggered!")