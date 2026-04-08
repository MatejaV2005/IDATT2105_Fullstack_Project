#!/bin/sh
set -eu # eu make seaweedfs fail if env vars are missing or commands errors occur

# Create an s3.conf file for seaweedfs at runtime to attach keys without hardcoding them
# actions i think is what each identity has access to do
cat > /etc/seaweedfs/s3.conf <<EOF
{
  "identities": [
    {
      "name": "app-storage",
      "credentials": [
        {
          "accessKey": "${S3_ACCESS_KEY}",
          "secretKey": "${S3_SECRET_KEY}"
        }
      ],
      "actions": ["Admin", "Read", "List", "Tagging", "Write"]
    }
  ]
}
EOF

# We set max volume size of our file storage to 3gb, and shows seaweedfs where our config file is & where out persistant storage is (/data)
exec weed server -dir=/data -master.volumeSizeLimitMB=3072 -s3 -s3.config=/etc/seaweedfs/s3.conf -s3.port=8333 -ip.bind=0.0.0.0
