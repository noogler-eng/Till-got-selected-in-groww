# AWS Lambda - Complete Guide

## Lambda kya hai?

Normal Server = 24/7 chalta hai, hamesha paise lagte hain
Lambda = Sirf tab chalta hai jab call karo - baki time ZERO cost

```
Request aai → Lambda ON → Code run → Response → Lambda OFF
```

---

## Basic Structure

```python
def handler(event, context):
    return {"statusCode": 200, "body": "Hello!"}
```

| Parameter | Kya hota hai |
|-----------|-------------|
| `event`   | Input data jo bheja gaya (dict) |
| `context` | Lambda ki info - timeout, memory, request ID |

---

## Deploy Karna (Steps)

```bash
# 1. Code ZIP karo
zip function.zip handler_lambda.py

# 2. Python se deploy karo
lambda_client.create_function(
    FunctionName="meri-lambda",
    Runtime="python3.11",
    Role="arn:aws:iam::000000000000:role/lambda-role",
    Handler="handler_lambda.handler",  # filename.function_name
    Code={"ZipFile": zip_bytes},
)

# 3. Active hone ka wait karo
waiter = lambda_client.get_waiter("function_active")
waiter.wait(FunctionName="meri-lambda")
```

---

## Invoke Karna (Call Karna)

### Synchronous (Response wait karo)
```python
response = lambda_client.invoke(
    FunctionName="meri-lambda",
    InvocationType="RequestResponse",
    Payload=json.dumps({"name": "Sharad"}),
)
result = json.loads(response["Payload"].read())
```

### Asynchronous (Fire and forget)
```python
lambda_client.invoke(
    FunctionName="meri-lambda",
    InvocationType="Event",        # Response nahi chahiye
    Payload=json.dumps({"key": "value"}),
)
```

| InvocationType | Kab use karo |
|----------------|-------------|
| `RequestResponse` | API response, turant result chahiye |
| `Event` | Image resize, email bhejni, background kaam |

---

## S3 Trigger Setup

```python
# Step 1: Permission do S3 ko Lambda invoke karne ki
lambda_client.add_permission(
    FunctionName="meri-lambda",
    StatementId="s3-invoke-permission",
    Action="lambda:InvokeFunction",
    Principal="s3.amazonaws.com",
    SourceArn="arn:aws:s3:::mera-bucket",
)

# Step 2: Lambda ARN dynamically lo (hardcode mat karo!)
info = lambda_client.get_function(FunctionName="meri-lambda")
lambda_arn = info["Configuration"]["FunctionArn"]

# Step 3: S3 notification set karo
s3.put_bucket_notification_configuration(
    Bucket="mera-bucket",
    NotificationConfiguration={
        "LambdaFunctionConfigurations": [
            {
                "LambdaFunctionArn": lambda_arn,
                "Events": ["s3:ObjectCreated:*"],
            }
        ]
    },
)
```

### S3 Event ka structure
```python
def handler(event, context):
    for record in event["Records"]:
        bucket = record["s3"]["bucket"]["name"]
        key    = record["s3"]["object"]["key"]
        size   = record["s3"]["object"]["size"]
        print(f"File aaya: {bucket}/{key} ({size} bytes)")
```

---

## Extra - Ye Bhi Jaanna Chahiye

### 1. Environment Variables
```python
# Deploy ke time set karo
lambda_client.create_function(
    ...
    Environment={
        "Variables": {
            "DB_HOST": "localhost",
            "ENV": "production",
        }
    },
)

# Lambda function mein padhna
import os
db_host = os.environ["DB_HOST"]
```

### 2. Timeout aur Memory
```python
lambda_client.create_function(
    ...
    Timeout=30,        # Max 30 seconds (default 3s, max 15 min)
    MemorySize=256,    # MB mein (128 to 10240)
)
```

### 3. Lambda Layers (Shared Dependencies)
```
Agar multiple Lambda functions ek hi library use karti hain
(jaise pandas, numpy) toh Layer banao - ek baar upload, sab use karein
```

### 4. Cold Start kya hota hai?
```
Pehli baar Lambda call hoti hai → Container start hota hai → Thoda slow
(Cold Start = ~100ms to 1s)

Baar baar call hoti hai → Container ready rehta hai → Fast
(Warm Start = ~1-10ms)
```

### 5. Lambda + Layers (External Libraries)
```bash
# Layer ke liye folder structure
mkdir -p layer/python
pip install requests -t layer/python/
cd layer && zip -r layer.zip python/

# Layer upload karo
lambda_client.publish_layer_version(
    LayerName="my-dependencies",
    Content={"ZipFile": open("layer.zip", "rb").read()},
    CompatibleRuntimes=["python3.11"],
)
```

### 6. Update Karna
```python
# Code update
lambda_client.update_function_code(
    FunctionName="meri-lambda",
    ZipFile=new_zip_bytes,
)

# Config update (timeout, memory, env vars)
lambda_client.update_function_configuration(
    FunctionName="meri-lambda",
    Timeout=60,
    Environment={"Variables": {"ENV": "staging"}},
)
```

### 7. Logs Dekhna
```bash
# Docker/LocalStack logs
docker logs localstack_container -f

# Real AWS mein CloudWatch mein hote hain logs
aws logs get-log-events --log-group-name /aws/lambda/meri-lambda
```

### 8. Delete Karna
```bash
aws --endpoint-url=http://localhost:4566 lambda delete-function --function-name meri-lambda
```

---

## Common Triggers (Kya kya Lambda trigger kar sakta hai)

| Trigger | Use Case |
|---------|----------|
| S3 | File upload hone pe kuch karo |
| API Gateway | HTTP request aane pe |
| SQS | Message queue se process karo |
| DynamoDB Streams | DB mein change hone pe |
| CloudWatch Events | Cron job / Schedule |
| SNS | Notification aane pe |

---

## Real World Architecture

```
User → API Gateway → Lambda → DynamoDB (data save)
                            → S3 (file save)
                            → SQS (background job queue)
```

---

## LocalStack Commands Reference

```bash
# List all functions
aws --endpoint-url=http://localhost:4566 lambda list-functions

# Function details
aws --endpoint-url=http://localhost:4566 lambda get-function --function-name meri-lambda

# Delete function
aws --endpoint-url=http://localhost:4566 lambda delete-function --function-name meri-lambda

# Invoke from CLI
aws --endpoint-url=http://localhost:4566 lambda invoke \
    --function-name meri-lambda \
    --payload '{"name": "Sharad"}' \
    output.txt && cat output.txt
```
