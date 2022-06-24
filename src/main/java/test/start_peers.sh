#!/usr/bin/env bash

cd $(dirname $0)

FULL_PATH="$(pwd -P)"

CLASS_PATH="src/main/java"
PEER_CLASS="Peer"

PEER_PREFIX="Peer"

if ! [[ $1 =~ ^[0-9][0-9]$ ]] ; then
    echo "Error: The number of peers is not valid!" >&2; exit 1
fi

echo "Number of peers parsed..."

# Application arguments
PROTOCOL_VERSION="1.0"

MC="228.25.25.25:8823"

MDB="228.25.25.25:8824"

MDC="228.25.25.25:8825"

echo "Built arguments"

for (( i = 0; i < $1; i++ )); do
    x-terminal-emulator -e java -cp "$FULL_PATH/$CLASS_PATH" $PROTOCOL_VERSION $i $PEER_PREFIX$i $MC $MDB $MDC
done