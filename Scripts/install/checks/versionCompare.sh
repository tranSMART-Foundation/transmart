#!/bin/bash

#*************************************************************************************
# Helper functions for checkVersion.sh
#*************************************************************************************

# the code for vercomp() was cribbed from 
# web: http://stackoverflow.com/questions/4023830/bash-how-compare-two-strings-in-version-format
# copied on 30 Nov 2015
# Results 0:match 1:higher 2:lower 3:
function vercomp () {
    if [[ $1 == $2 ]]
    then
        return 0
    fi
    local IFS="._"		# bash Internal Field Separator
    local i ver1=($1) ver2=($2)
    local j=${#ver1[@]}
    # fill empty fields in ver1 with zeros
    for ((i=${#ver1[@]}; i<${#ver2[@]}; i++))
    do
        ver1[i]=0
    done
    for ((i=0; i<${#ver1[@]}; i++))
    do
	if (( i >= j ))
	then
	    return 1
	fi
        if [[ -z ${ver2[i]} ]]
        then
            # fill empty fields in ver2 with zeros
            ver2[i]=0
        fi
        if ((10#${ver1[i]} > 10#${ver2[i]}))
        then
            return 2
        fi
        if ((10#${ver1[i]} < 10#${ver2[i]}))
        then
            return 3
        fi
    done
    return 0
}
export -f vercomp

function check() {
    desired=$1
    given=$2
    probe=$(echo $given | tr '_' '.')
    return $(vercomp $desired $probe)
}
export -f check

function reportCheckOrHigher () {
    type=$1
    desiredVersion=$2
    givenVersion=$3
    check $desiredVersion $givenVersion
    versionTest=$?
    okFlag=0
    if [ "$versionTest" != 2 ]; then
        echo "The $type version, $givenVersion, is good!"
    else 
        echo "Expected $type version $desiredVersion or higher; currently $givenVersion; needs to be upgraded."
        okFlag=1
    fi
    return $okFlag
}
export -f reportCheckOrHigher

function reportCheckRange () {
    type=$1
    desiredVersion=$2
    excessVersion=$3
    givenVersion=$4
    check $desiredVersion $givenVersion
    versionTest=$?
    check $givenVersion $excessVersion
    versionTestMax=$?
    okFlag=0

    if [ "$versionTest" != 2 ] && [ "$versionTestMax" != 2 ]; then
        echo "The $type version, $givenVersion, is good!"
    elif [ "$versionTestMax" == 2 ]; then
        echo "Expected $type version less than $excessVersion; currently $givenVersion; needs to be downgraded."
        okFlag=1
    else 
        echo "Expected $type version $desiredVersion or higher; currently $givenVersion; needs to be upgraded."
        okFlag=1
    fi
    return $okFlag
}
export -f reportCheckRange

function reportCheckExact () {
    type=$1
    desiredVersion=$2
    givenVersion=$3
    check $desiredVersion $givenVersion
    versionTest=$?
    okFlag=0
    if [ "$versionTest" -eq 0 ]; then
        echo "The $type version, $givenVersion, is good!"
    elif [ "$versionTest" -eq 3 ]; then
        echo "Expected $type version $desiredVersion exactly; currently $givenVersion; needs to be downgraded."
	okFlag=1
    else
        echo "Expected $type version $desiredVersion exactly; currently $givenVersion; needs to be upgraded."
        okFlag=1
    fi
    return $okFlag
}
export -f reportCheckExact

function reportCheckExactAny () {
    type=$1
    desiredVersion=$2
    givenVersion=$3
    check $desiredVersion $givenVersion
    versionTest=$?
    okFlag=0
    if [ "$versionTest" -eq 0 ] || [ "$versionTest" -eq 1 ]; then
        echo "The $type version, $givenVersion, is good!"
    elif [ "$versionTest" -eq 3 ]; then
        echo "Expected $type version $desiredVersion exactly; currently $givenVersion; needs to be downgraded."
		okFlag=1
    else
        echo "Expected $type version $desiredVersion exactly; currently $givenVersion; needs to be upgraded."
        okFlag=1
    fi
    return $okFlag
}
export -f reportCheckExactAny

