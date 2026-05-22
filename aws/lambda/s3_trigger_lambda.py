def handler(event, context):
    # when files comes in s3
    for record in event['Records']:
        bucket = record['s3']['bucket']['name']
        key = record['s3']['object']['key']
        print(f"File {key} has been uploaded to bucket {bucket}.")
    
    return {
        "message": "S3 event processed successfully!",
        "statusCode": 200
    }