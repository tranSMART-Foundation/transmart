#!/bin/bash

# helper function to check for basic command line tool

function checkForCommandLineTool {
    name=$1
    if type $name >/dev/null 2>&1; then
        echo "$name ok"
	return 0
    else
        echo "$name required but does not exist" >&2
	return 1
    fi
}