#!/bin/bash

set -e

source $1

# Echo the platform, or list of platforms
# The PLATFORM_DATA_TYPE here is ignored as it can be multiple types
# The PLATFORM_DATA_TYPE is taken from each PLATFORM annotation target

if [ -z "$PLATFORMS" ]; then
    echo "$PLATFORM"
else
    echo "$PLATFORMS"
fi

