#!/bin/bash

# ********************************************************************************
# This script tests for connection to and existence of a web site by URL
# cribbed from: http://answers.google.com/answers/threadview/id/276934.html
# ********************************************************************************

function checkURL {
    curl -s -o "/dev/null" $1
    results=$?
    if [ $results -ne 0 ] ; then
        echo "Error occurred getting URL $1:"
        if [ $results -eq 6 ]; then
            echo "  Unable to resolve host"
        fi
        if [ $results -eq 7 ]; then
            echo "  Unable to connect to host"
        fi
        return 1
    fi
    return 0	
}
