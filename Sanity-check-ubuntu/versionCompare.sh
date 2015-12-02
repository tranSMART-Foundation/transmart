#!/bin/bash
# this code cribbed from web: http://stackoverflow.com/questions/4023830/bash-how-compare-two-strings-in-version-format
# copied on 30 Nov 2015
function vercomp () {
    if [[ $1 == $2 ]]
    then
        return 0
    fi
    local IFS=.
    local i ver1=($1) ver2=($2)
    # fill empty fields in ver1 with zeros
    for ((i=${#ver1[@]}; i<${#ver2[@]}; i++))
    do
        ver1[i]=0
    done
    for ((i=0; i<${#ver1[@]}; i++))
    do
        if [[ -z ${ver2[i]} ]]
        then
            # fill empty fields in ver2 with zeros
            ver2[i]=0
        fi
        if ((10#${ver1[i]} > 10#${ver2[i]}))
        then
            return 1
        fi
        if ((10#${ver1[i]} < 10#${ver2[i]}))
        then
            return 2
        fi
    done
    return 0
}

function check() {
    desired=$1
    given=$2
    probe=$(echo $given | tr '_' '.')
    return $(vercomp $desired $probe)
}

function reportCheckOrHigher () {
    type=$1
    desiredVersion=$2
    givenVersion=$3
#    echo "$type wants $desiredVersion is $givenVersion"
	check $desiredVersion $givenVersion
    versionTest=$?
#    echo $versionTest
    if [ "$versionTest" -eq 2 ] || [ "$versionTest" -eq 0 ]; then
        echo "The $type version, $givenVersion, is good!"
    else 
        echo "Expected $type version $desiredVersion or higher; currently $givenVersion; needs to be upgraded."
    fi
}

function reportCheckExact () {
    type=$1
    desiredVersion=$2
    givenVersion=$3
#    echo "$type wants $desiredVersion is $givenVersion"
	check $desiredVersion $givenVersion
    versionTest=$?
#    echo $versionTest
    if [ "$versionTest" -eq 0 ]; then
        echo "The $type version, $givenVersion, is good!"
    elif [ "$versionTest" -eq 2 ]; then
        echo "Expected $type version $desiredVersion exactly; currently $givenVersion; needs to be downgraded."
    else
        echo "Expected $type version $desiredVersion exactly; currently $givenVersion; needs to be upgraded."
    fi
}