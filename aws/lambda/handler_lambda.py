# This is a simple AWS Lambda function that returns a greeting message.
# The function takes an event as input, which can contain a "name" key. 
# If the "name" key is not provided, it defaults to "World". The function 
# then returns a JSON response with a greeting message and a status code.

# To deploy this function, you would typically package it and upload it to 
# AWS Lambda, and then set up an API Gateway to trigger the function when an 
# HTTP request is made.

def handler(event, context):
    # event = input
    # context = runtime information

    # Get the name from the event, default to "World" if not provided
    name = event.get("name", "World")

    return {
        "message": f"Hello, {name}!",
        "statusCode": 200
    }