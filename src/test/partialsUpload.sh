#!/usr/bin/env bash

TUS_URL=http://localhost:6969/uploads/

SIZE1=27
FILE1="./toUploadChunk1.txt"
SIZE2=24
FILE2="./toUploadChunk2.txt"
SIZE3=30
FILE3="./toUploadChunk3.txt"

OUTPUT1=$(curl -X POST -i $TUS_URL -H 'Tus-Resumable: 1.0.0'  -H "Upload-Concat: partial" -H "Upload-Length: $SIZE1" )
echo "$OUTPUT1"

URL1=$(echo "$OUTPUT1" | \
	grep 'Location:' |  \
	sed "s/$(printf '\r')\$//" | \
	awk '{print $2}')

echo "URL1 is $URL1"

curl -X PATCH -i $URL1 \
	-H 'Tus-Resumable: 1.0.0'  \
	-H 'Upload-Offset: 0' \
	-H 'Content-Type: application/offset+octet-stream' \
--upload-file $FILE1

OUTPUT2=$(curl -X POST -i $TUS_URL -H 'Tus-Resumable: 1.0.0'   -H "Upload-Concat: partial" -H "Upload-Length: $SIZE2" )
echo "$OUTPUT2"

URL2=$(echo "$OUTPUT2" | \
	grep 'Location:' |  \
	sed "s/$(printf '\r')\$//" | \
	awk '{print $2}')

echo "URL2 is $URL2"

curl -X PATCH -i $URL2 \
	-H 'Tus-Resumable: 1.0.0'  \
	-H 'Upload-Offset: 0' \
	-H 'Content-Type: application/offset+octet-stream' \
--upload-file $FILE2

OUTPUT3=$(curl -X POST -i $TUS_URL -H 'Tus-Resumable: 1.0.0'   -H "Upload-Concat: partial" -H "Upload-Length: $SIZE3" )
echo "$OUTPUT3"

URL3=$(echo "$OUTPUT3" | \
	grep 'Location:' |  \
	sed "s/$(printf '\r')\$//" | \
	awk '{print $2}')

echo "URL3 is $URL3"

curl -X PATCH -i $URL3 \
	-H 'Tus-Resumable: 1.0.0'  \
	-H 'Upload-Offset: 0' \
	-H 'Content-Type: application/offset+octet-stream' \
--upload-file $FILE3

FINAL="final;$URL1 $URL2 $URL3"

echo "FINAL is $FINAL"

OUTPUT4=$(curl -X POST -i $TUS_URL -H 'Tus-Resumable: 1.0.0'  -H "Upload-Concat: $FINAL" )
echo "$OUTPUT4"

URL4=$(echo "$OUTPUT4" | \
	grep 'Location:' |  \
	sed "s/$(printf '\r')\$//" | \
	awk '{print $2}')

curl -X HEAD -i $URL4