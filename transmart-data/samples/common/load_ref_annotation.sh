#!/bin/bash

set -e

source $1

# Echo the platform, or list of platforms
# The PLATFORM_DATA_TYPE here is ignored so can be multiple types
# The PLATFORM_DATA_TYPE is taken from the PLATFORM annotation target

echo "$PLATFORM $PLATFORMS"
