#!/bin/sh
set -eu

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

exec weed server -dir=/data -master.volumeSizeLimitMB=3072 -s3 -s3.config=/etc/seaweedfs/s3.conf -s3.port=8333 -ip.bind=0.0.0.0
