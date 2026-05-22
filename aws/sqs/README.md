# AWS SQS - Complete Guide

## SQS kya hai?

SQS = Simple Queue Service

Real life example:
```
McDonald's mein order dete ho → Counter pe queue lagti hai → 
Ek ek karke process hota hai → Tum wait karte ho

SQS bhi same kaam karta hai - messages ki queue!
```

```
Producer (jo bhejta hai) → [MSG1, MSG2, MSG3] → Consumer (jo process karta hai)
                              (Queue)
```

**Kyun use karte hain?**
```
Bina SQS:  User → Backend → Payment → Email → SMS → Shipping (sab ek saath, slow!)
SQS ke saath: User → Backend → Queue mein daalo → Return karo (fast!)
                                     ↓
                              Background mein:
                              Payment process
                              Email bhejo
                              SMS bhejo
                              Shipping schedule
```

---

## ARN kya hota hai?

ARN = Amazon Resource Name

**Har AWS resource ka ek unique address hota hai - wahi ARN hai!**

```
arn:aws:sqs:us-east-1:000000000000:meri-queue
^   ^   ^   ^         ^             ^
|   |   |   |         |             Resource name
|   |   |   |         Account ID
|   |   |   Region
|   |   Service (sqs, s3, lambda)
|   AWS
arn (prefix - hamesha same)
```

**Kyun ARN use karte hain?**
```
Naam se identify nahi kar sakte - same naam alag regions mein ho sakta hai
ARN globally unique hota hai - koi confusion nahi
Permissions mein use hota hai - "is ARN ko allow karo"
Triggers mein use hota hai - "is Lambda ARN ko call karo"
```

---

## Queue URL kya hota hai?

```
http://localhost:4566/000000000000/meri-queue
^                    ^             ^
Endpoint             Account ID    Queue naam
```

**ARN vs Queue URL:**

| | ARN | Queue URL |
|--|-----|-----------|
| Kab use hota hai | Permissions, Triggers, Policies | Actual operations (send, receive, delete) |
| Format | `arn:aws:sqs:...` | `http://...` |

---

## SQS ke Types

### 1. Standard Queue
```
- Order guarantee nahi (MSG3 pehle aa sakta hai)
- At-least-once delivery (message dobara aa sakta hai)
- Unlimited throughput
- Use case: Email, notifications, logging
```

### 2. FIFO Queue
```
- Order guarantee (First In First Out)
- Exactly-once delivery (message sirf ek baar aayega)
- 300 messages/second limit
- Use case: Payment, orders, banking
- Naam .fifo se khatam hona chahiye
```

---

## Setup

```python
import boto3
import json

sqs = boto3.client(
    "sqs",
    endpoint_url="http://localhost:4566",   # LocalStack
    aws_access_key_id="test",
    aws_secret_access_key="test",
    region_name="us-east-1",
)
```

---

## 1. Queue Banana

```python
# Standard Queue
response = sqs.create_queue(QueueName="meri-queue")
queue_url = response["QueueUrl"]

# FIFO Queue (.fifo suffix zaroori hai!)
response = sqs.create_queue(
    QueueName="meri-queue.fifo",
    Attributes={
        "FifoQueue": "true",
        "ContentBasedDeduplication": "true",  # Duplicate messages automatically hatao
    }
)
```

---

## 2. Message Bhejna (Producer)

```python
# Single message
sqs.send_message(
    QueueUrl=queue_url,
    MessageBody=json.dumps({
        "order_id": "ORD-001",
        "item": "Laptop",
        "price": 50000
    }),
    DelaySeconds=0,        # Kitne seconds baad visible ho (0-900)
)

# Multiple messages ek saath (Max 10, efficient)
sqs.send_message_batch(
    QueueUrl=queue_url,
    Entries=[
        {"Id": "1", "MessageBody": json.dumps({"order_id": "ORD-002"})},
        {"Id": "2", "MessageBody": json.dumps({"order_id": "ORD-003"})},
    ]
)
```

---

## 3. Message Receive Karna (Consumer)

```python
response = sqs.receive_message(
    QueueUrl=queue_url,
    MaxNumberOfMessages=10,    # Ek baar mein kitne messages (max 10)
    WaitTimeSeconds=20,        # Long polling - 20 sec wait karo (efficient)
    VisibilityTimeout=30,      # Process karne ka time (seconds)
)

messages = response.get("Messages", [])

for msg in messages:
    body = json.loads(msg["MessageBody"])
    receipt = msg["ReceiptHandle"]   # Ye save karo - delete ke liye chahiye
    
    # Process karo
    print(f"Processing: {body}")
    
    # DELETE karo - BAHUT ZAROORI HAI!
    sqs.delete_message(
        QueueUrl=queue_url,
        ReceiptHandle=receipt,
    )
```

---

## ReceiptHandle kya hota hai?

```
Message receive karne ke baad ek temporary token milta hai = ReceiptHandle

Ye token use karke message delete karte hain.
Agar delete nahi kiya → VisibilityTimeout ke baad message wapas aata hai!
```

**Visibility Timeout ka flow:**
```
Message receive kiya
       ↓
Message "invisible" ho gaya (dusra consumer nahi dekh sakta)
       ↓
VisibilityTimeout = 30 seconds
       ↓
Agar 30 sec mein delete kiya → Message gone ✅
Agar 30 sec mein delete nahi kiya → Message wapas visible ❌ (dobara process hoga!)
```

---

## 4. Short Polling vs Long Polling

```python
# Short Polling - turant return karta hai (empty bhi ho toh)
# Zyada API calls = zyada cost
sqs.receive_message(
    QueueUrl=queue_url,
    WaitTimeSeconds=0,    # 0 = short polling
)

# Long Polling - message aane ka wait karta hai (efficient)
# Kam API calls = kam cost - RECOMMENDED!
sqs.receive_message(
    QueueUrl=queue_url,
    WaitTimeSeconds=20,   # 20 = long polling (max 20)
)
```

---

## 5. Dead Letter Queue (DLQ)

**DLQ kya hai?**
```
Koi message baar baar fail ho raha hai?
→ N baar ke baad automatically DLQ mein chala jaata hai
→ Developer investigate kar sakta hai
→ Fix karke reprocess kar sakta hai
```

```python
# Step 1: DLQ banao
dlq = sqs.create_queue(QueueName="failed-orders-dlq")
dlq_url = dlq["QueueUrl"]

# Step 2: DLQ ka ARN lo
dlq_attrs = sqs.get_queue_attributes(
    QueueUrl=dlq_url,
    AttributeNames=["QueueArn"]
)
dlq_arn = dlq_attrs["Attributes"]["QueueArn"]

# Step 3: Main queue mein DLQ attach karo
sqs.create_queue(
    QueueName="main-orders-queue",
    Attributes={
        "RedrivePolicy": json.dumps({
            "deadLetterTargetArn": dlq_arn,
            "maxReceiveCount": "3",   # 3 baar fail → DLQ mein jaao
        })
    }
)
```

**Real life flow:**
```
Order message aaya
      ↓
Process kiya → Failed (network error)
      ↓
Message wapas aaya (attempt 1)
      ↓
Process kiya → Failed again
      ↓
Message wapas aaya (attempt 2)
      ↓
Process kiya → Failed again
      ↓
3 attempts complete → DLQ mein gaya
      ↓
Team ko alert → Investigate → Fix → Reprocess
```

---

## 6. FIFO Queue - Order Guarantee

```python
# FIFO mein MessageGroupId aur MessageDeduplicationId zaroori hai
sqs.send_message(
    QueueUrl=fifo_queue_url,
    MessageBody=json.dumps({"step": "payment"}),
    MessageGroupId="order-123",           # Same group = same order mein aayenge
    MessageDeduplicationId="payment-001", # Duplicate prevent karta hai
)

sqs.send_message(
    QueueUrl=fifo_queue_url,
    MessageBody=json.dumps({"step": "shipping"}),
    MessageGroupId="order-123",
    MessageDeduplicationId="shipping-001",
)
# payment pehle process hoga, shipping baad mein - GUARANTEED!
```

---

## 7. Queue Attributes (Info Dekhna)

```python
attrs = sqs.get_queue_attributes(
    QueueUrl=queue_url,
    AttributeNames=["All"]
)
print(attrs["Attributes"])

# Important attributes:
# ApproximateNumberOfMessages        - Queue mein kitne messages hain
# ApproximateNumberOfMessagesNotVisible - Process ho rahe hain kitne
# MessageRetentionPeriod             - Kitne din tak message rakha jaaye (default 4 din)
# VisibilityTimeout                  - Default visibility timeout
```

---

## 8. Message Attributes (Extra Info Message ke saath)

```python
# Message ke saath extra metadata bhej sakte ho
sqs.send_message(
    QueueUrl=queue_url,
    MessageBody=json.dumps({"order_id": "ORD-001"}),
    MessageAttributes={
        "Priority": {
            "StringValue": "High",
            "DataType": "String",
        },
        "RetryCount": {
            "StringValue": "0",
            "DataType": "Number",
        },
    }
)

# Receive karte waqt attributes bhi lo
response = sqs.receive_message(
    QueueUrl=queue_url,
    MessageAttributeNames=["All"],   # Ye add karo
)
```

---

## Common Patterns

### Pattern 1: Worker Pattern
```
Backend → Queue → Multiple Workers process karen
                  Worker 1 ─┐
                  Worker 2 ─┼─ Queue se messages lete hain
                  Worker 3 ─┘
```

### Pattern 2: Fan-out (SNS + SQS)
```
Ek event → SNS Topic → Multiple SQS Queues
                        Email Queue → Email service
                        SMS Queue   → SMS service
                        Push Queue  → Push notification
```

### Pattern 3: Lambda + SQS
```
SQS Queue → Lambda automatically trigger hoti hai
          → Batch mein messages process karti hai
          → Scale automatically hota hai
```

---

## LocalStack CLI Commands

```bash
# Queues list karo
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Queue details
aws --endpoint-url=http://localhost:4566 sqs get-queue-attributes \
    --queue-url http://localhost:4566/000000000000/meri-queue \
    --attribute-names All

# Message bhejo CLI se
aws --endpoint-url=http://localhost:4566 sqs send-message \
    --queue-url http://localhost:4566/000000000000/meri-queue \
    --message-body '{"test": "message"}'

# Message receive karo
aws --endpoint-url=http://localhost:4566 sqs receive-message \
    --queue-url http://localhost:4566/000000000000/meri-queue

# Queue delete karo
aws --endpoint-url=http://localhost:4566 sqs delete-queue \
    --queue-url http://localhost:4566/000000000000/meri-queue
```

---

## SQS vs Direct API Call - Kab kya use karein?

| Situation | SQS Use Karo | Direct Call Use Karo |
|-----------|-------------|---------------------|
| Time-consuming task | ✅ | ❌ |
| Response turant chahiye | ❌ | ✅ |
| Multiple services ko batana ho | ✅ | ❌ |
| Retry logic chahiye | ✅ | ❌ |
| Simple CRUD operation | ❌ | ✅ |

---

## Summary

```
Standard Queue  → Order matter nahi, high volume
FIFO Queue      → Order matter karta hai, payments
DLQ             → Failed messages handle karna
Long Polling    → Efficient message receive
VisibilityTimeout → Process karne ka time window
ReceiptHandle   → Delete karne ke liye token
ARN             → Resource ka unique global address
```
