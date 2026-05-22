# boto3 is the AWS SDK for Python, which allows Python developers to 
# write software that makes use of Amazon services like S3 and EC2. 
# In this code snippet, we are connecting to a local instance of 
# AWS services provided by LocalStack, which is a fully functional 
# local AWS cloud stack. We specify the endpoint URL for LocalStack, 
# along with dummy AWS credentials and the region name. This setup 
# allows us to interact with S3 buckets in a local environment without
# needing access to the actual AWS cloud.
import boto3
import json


# creating the bucket policy to allow public read access to the bucket
policy = {
    "Version": "2012-10-17",
    "Statement": [
        {
            # Effect - allow or deny the permissions specified in the policy statement.
            "Effect": "Allow",  
            # Principal - the user, account, service, or other entity that is allowed
            # or denied access to a resource. In this case, we are allowing access 
            # to everyone by using "*".
            "Principal": "*",
            # Action - the specific actions that are allowed or denied by the policy 
            # statement.
            "Action": "s3:GetObject",
            # Resource - the specific AWS resource or resources that the policy 
            # statement applies to.
            "Resource": "arn:aws:s3:::my-second-bucket/*"
        }
    ]
}



def create_bucket(bucket_name: str):
    s3.create_bucket(Bucket=bucket_name)
    print(f"Bucket '{bucket_name}' created successfully.")
    # aws --endpoint-url=http://localhost:4566 s3 ls       


def file_upload(s3_client, bucket_name: str, file_name: str, file_content: str):
    # key - file name + path
    s3_client.put_object(Bucket=bucket_name, Key=file_name, Body=file_content)
    print(f"File '{file_name}' uploaded successfully to bucket '{bucket_name}'.")
    # aws --endpoint-url=http://localhost:4566 s3 ls s3://my-second-bucket/ --recursive

def file_download(s3_client, bucket_name: str, file_name: str):
    response = s3_client.get_object(Bucket=bucket_name, Key=file_name)
    file_content = response['Body'].read().decode('utf-8')
    print(f"File '{file_name}' downloaded successfully from bucket '{bucket_name}'.")
    return file_content

# expires in seconds, default is 1 hour (3600 seconds)
def generate_presigned_url(s3_client, bucket_name: str, file_name: str, expiration: int = 3600):
    url = s3_client.generate_presigned_url('get_object', Params={'Bucket': bucket_name, 'Key': file_name}, ExpiresIn=expiration)
    print(f"Pre-signed URL generated successfully for file '{file_name}' in bucket '{bucket_name}'.")
    return url

def created_versioned_bucket(s3_client):
    s3_client.create_bucket(Bucket=VERSIONED_BUCKET_NAME)
    s3_client.put_bucket_versioning(Bucket=VERSIONED_BUCKET_NAME, VersioningConfiguration={'Status': 'Enabled'})
    print(f"Versioned bucket '{VERSIONED_BUCKET_NAME}' created successfully.")

def upload_multiple_versions(s3_client, bucket_name: str, file_name: str, contents: list):
    for content in contents:
        s3_client.put_object(Bucket=bucket_name, Key=file_name, Body=content)
        print(f"Uploaded version with content: '{content}' to bucket '{bucket_name}'.")

# A bucket policy is a JSON document that defines permissions for a bucket 
# and the objects within it.
def apply_bucket_policy(s3_client, bucket_name: str, policy: dict):
    # Dump: Convert Python data (like dictionaries or lists) into JSON format.
    s3_client.put_bucket_policy(Bucket=bucket_name, Policy=json.dumps(policy))
    print(f"Bucket policy applied successfully to bucket '{bucket_name}'.")

# This function retrieves the bucket policy for a specified bucket 
# and returns it as a Python dictionary.
def get_bucket_policy(s3_client, bucket_name: str):
    response = s3_client.get_bucket_policy(Bucket=bucket_name)
    # Load: Convert JSON data into Python data structures.
    policy = json.loads(response['Policy'])
    print(f"Bucket policy retrieved successfully for bucket '{bucket_name}'.")
    return policy

# connecting with localstack
s3 = boto3.client(
    "s3", 
    endpoint_url = "http://localhost:4566",
    aws_access_key_id = "test",
    aws_secret_access_key = "test",
    region_name = "us-east-1"
)
         
BUCKET_NAME = "my-second-bucket"
# create_bucket(BUCKET_NAME)
# file_upload(s3, BUCKET_NAME, "file1.txt", "This is the content of file1.")
# content = file_download(s3, BUCKET_NAME, "file1.txt")
# print(content)

# Generate a pre-signed URL for the uploaded file
# presigned_url = generate_presigned_url(s3, BUCKET_NAME, "file1.txt")
# print(f"Pre-signed URL: {presigned_url}")


VERSIONED_BUCKET_NAME = "versioned-bucket"
created_versioned_bucket(s3)

# list of all the versions of the file in the versioned bucket
# when we upload a new version of the file, it creates a new version with a unique version ID.
# with version id we can get that specific version of the file, and we can also check if 
# it's the latest version or not.
response = s3.list_object_versions(Bucket=VERSIONED_BUCKET_NAME)
# upload_multiple_versions(s3, VERSIONED_BUCKET_NAME, "versioned-file.txt", ["Version 1 content", "Version 2 content", "Version 3 content"])
for v in response.get("Versions", []):   
    print(v)                     
    print(f"VersionId: {v['VersionId']} | Latest: {v['IsLatest']}") 