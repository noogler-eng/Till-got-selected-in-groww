import boto3
import json

sqs = boto3.client(
    'sqs',
    endpoint_url="http://localhost:4566",                                                              
    aws_access_key_id="test",
    aws_secret_access_key="test",
    region_name='us-east-1',
)

# making an queue
response = sqs.create_queue(
    QueueName='first-queue',
)

# Queue URL = Queue ka address
# account id = 000000000000, region = us-east-1, queue name = first-queue
# http://localhost:4566/000000000000/first-queue
queue_url = response['QueueUrl']
print("Queue URL:", queue_url)

sqs.send_message(
    QueueUrl=queue_url,
    MessageBody=json.dumps({
        "name": "John Doe",
        "age": 30,
        "city": "New York"
    })
)

print("Message sent to the queue.")

# sending multiple messages
sqs.send_message_batch(
    QueueUrl=queue_url,
    Entries=[
        {
            'Id': 'msg1',
            'MessageBody': json.dumps({
                "name": "Alice",
                "age": 25,
                "city": "Los Angeles"
            })
        },
        {
            'Id': 'msg2',
            'MessageBody': json.dumps({
                "name": "Bob",
                "age": 35,
                "city": "Chicago"
            })
        }
    ]
)
print("Multiple messages sent to the queue.")



# message receive
response = sqs.receive_message(
    QueueUrl=queue_url,
    # Ek baar mein max 1 messages  
    MaxNumberOfMessages=1, 
    # 10 sec wait karo agar queue khali ho    
    WaitTimeSeconds=10
)

messages = response.get("Messages", [])                                                                  
print(f"Kitne messages mile: {len(messages)}")

for msg in messages:
    body = json.loads(msg["MessageBody"])
    print(f"Order: {body}")                                                                              

    # IMPORTANT - Process hone ke baad DELETE karo!                                                      
    sqs.delete_message(                                                                                
        QueueUrl=queue_url, 
        # Unique ID har message ka                                                                                                             
        ReceiptHandle=msg["ReceiptHandle"],  
    )                                                                                                    
    print(f"Message delete ho gaya!")

# Dead Letter Queue (DLQ)
dlq_response = sqs.create_queue(QueueName="first-dead-letter-queue")                                      
dlq_url = dlq_response["QueueUrl"]                                                                       
                                      
dlq_attrs = sqs.get_queue_attributes(                                                                    
    QueueUrl=dlq_url,                                                                                    
    AttributeNames=["QueueArn"]
)

dlq_arn = dlq_attrs["Attributes"]["QueueArn"]
print(f"DLQ ban gayi! ARN: {dlq_arn}")                                                                   

main_response = sqs.create_queue(
    QueueName="main-queue-with-dlq",                                                                     
    Attributes={
        "RedrivePolicy": json.dumps({                                                                    
            "deadLetterTargetArn": dlq_arn,
            # 3 baar fail ho toh DLQ mein jaao 
            "maxReceiveCount": "3",                                  
        })                                                                                               
    }
) 

main_url = main_response["QueueUrl"]
print("Main queue with DLQ ban gayi!")

sqs.send_message(
    QueueUrl=main_url,                                                                                   
    MessageBody=json.dumps({"order_id": "FAILED-001"}),
)  


  
  # Step 4: 3 baar receive karo lekin DELETE mat karo (simulate failure)                                   
for i in range(3):
    resp = sqs.receive_message(                                                                          
        QueueUrl=main_url,
        MaxNumberOfMessages=1,
        VisibilityTimeout=1,   # 1 sec mein wapas visible                                                
    )
    msgs = resp.get("Messages", [])                                                                      
    if msgs:                                                                                             
        print(f"Attempt {i+1}: Message mila lekin process nahi kiya!")
                                                                            


import time                                                                                              
time.sleep(2)                                                                                            
                
# Step 5: DLQ mein check karo
dlq_resp = sqs.receive_message(QueueUrl=dlq_url, MaxNumberOfMessages=1)
dlq_msgs = dlq_resp.get("Messages", [])                                                                  
if dlq_msgs:
    print(f"DLQ mein failed message aa gaya: {dlq_msgs[0]['MessageBody']}")    
                                                                            