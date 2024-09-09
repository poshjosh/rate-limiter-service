#!/bin/bash

# Must match the bucket name in src/test/resources/application.yml
bucket_name="test-raas-bucket"

awslocal s3api create-bucket --bucket $bucket_name

echo "S3 bucket '$bucket_name' created successfully"
echo "Executed init-s3-bucket.sh"