#!/bin/bash

#************************************************************************************
#  Script to load all that is needed to run an example/demo
#  for tranSMART release 19.1
#************************************************************************************

# to run the install scripts
#   cd $HOME
#   cd transmart
#   ./Scripts/install/InstallTransmart.sh
#
# to run the checking scripts
#   ./Scripts/install/checks/checkAll.sh

# on error; stop/exit
set -e

# Initial variable settings
# Update the release number here

TMRELEASE=19.1
export TMRELEASE
TMRELEASEPATH=19_1_0
export TMRELEASEPATH
TMRELEASEDIR="release${TMRELEASEPATH}_artifacts"
export TMRELEASEDIR

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallTransmart starting"

echo "Set TMRELEASE ${TMRELEASE}"
echo "Set TMRELEASEPATH ${TMRELEASEPATH}"

# check we have everything we need. Assume we have just the scripts directory
# http://library.transmartfoundation.org/release/release19_1_0_artifacts/Scripts-release-19.1.zip

# Define TMSCRIPTS_BASE as path to this script
# Find directory where Scripts.zip was unpacked
# two levels up from this script as TMINSTALL_BASE

TMSCRIPTS_BASE="$(dirname -- "$(readlink -f "${BASH_SOURCE}")")"
export TMSCRIPTS_BASE
TMINSTALL_BASE="$(dirname -- "$(dirname -- "$(readlink -f "${TMSCRIPTS_BASE}")")")"
export TMINSTALL_BASE

echo "Set TMSCRIPTS_BASE ${TMSCRIPTS_BASE}"
echo "Set TMINSTALL_BASE ${TMINSTALL_BASE}"

# TABLEBASES   defined in transmart-data needed to prove vars file has been sourced

# OS and version
# ==============

# Can check in bash for OSTYPE
# gnu: *linux*  *hurd* *msys* *cygwin* "sua" *interix*
# bsd: *bsd* *darwin*
# sun: *sunos* *solaris* *indiana* *illumos* *smartos*

# systemd requires /etc/os-release
# --------------------------------
# Ubuntu 14.04:
# Ubuntu 16.04:
# Ubuntu 18.04: /etc/os-release NAME="Ubuntu" ID="ubuntu" ID_LIKE="debian" VERSION_ID="18.04" VERSION_CODENAME="bionic" PRETTY_NAME="Ubuntu 18.04.6 LTS" VERSION="18.04.6 LTS (Bionic Beaver)" OSTYPE="linux-gnu"
# Ubuntu 20.04:
# Ubuntu 22.04:
# Fedora 33.1.2:
# Fedora 34:
# Fedora 35: /etc/os-release NAME="Fedora Linux" ID="fedora" ID_LIKE=undef VERSION_ID="35" VERSION_CODENAME="" PRETTY_NAME="Fedora Linux 35 (Workstation Edition)" VERSION="35 (Workstation Edition)" OSTYPE="linux"
# RHEL 7.9:
# RHEL 8.4:
# CentOS 7.9:
# CentOS 8.3:
# OpenSuse 15.2
# Debian 10.9.0:

# older release files in case they are needed
# -------------------------------------------
# /etc/SuSE-release
# /etc/redhat-release
# /etc/redhat_version
# /etc/fedora-release
# /etc/slackware-release
# /etc/slackware-version
# /etc/debian_release
# /etc/debian_version
# /etc/mandrake-release
# /etc/yellowdog-release
# /etc/sun-release
# /etc/release
# /etc/gentoo-release
# /etc/UnitedLinux-release
# /etc/lsb-release

# For examples of most OS versions: https://github.com/chef/os_release

TMINSTALL_OS=""

release_file=""
if [ -e "/etc/os-release" ]; then
    release_file="/etc/os-release"
elif [ -e "/etc/redhat-release" ]; then
    release_file="/etc/redhat-release"
elif [ -e "/etc/SuSE-release" ]; then
    release_file="/etc/SuSE-release"
elif [ -e "/etc/mandrake-release" ]; then
    release_file="/etc/mandrake-release"
elif [ -e "/etc/debian-release" ]; then
    release_file="/etc/debian-release"
elif [ -e "/etc/UnitedLinux-release" ]; then
    release_file="/etc/UnitedLinux-release"
fi

if [ "$release-file" == "" ]; then
    now="$(date +'%d-%b-%y %H:%M')"
    echo "${now} Unable to find OS info in /etc/os-release or similar files"
    exit 1
fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Reading release file ${release_file}"

# Pick up values from /etc/os-release or equivalent
# remove name and any double quotes

tmosid=$(grep '^ID=' "$release_file" | sed 's/ID=//'  | sed 's/"//g' | head -1)
tmosdist=$(grep '^ID_LIKE=' "$release_file" | sed 's/ID_LIKE=//'  | sed 's/"//g' | head -1)
tmosvers=$(grep '^VERSION=' "$release_file" | sed 's/VERSION=//' | sed 's/"//g' | head -1)
tmosversid=$(grep '^VERSION_ID=' "$release_file" | sed 's/VERSION_ID=//'  | sed 's/"//g' | head -1)

# set TMINSTALL_OS
for test in "$tmosdist" "$tmosid"
do
    if [ "$test" == "debian" ]; then	# debian 7-10+; ubuntu 14-22+
	TMINSTALL_OS="$test"
	TMINSTALL_OSVERSION=$tmosversid
	break 1
    elif [ "$test" == "ubuntu" ]; then		# 14-22+
	TMINSTALL_OS="$test"
	TMINSTALL_OSVERSION=$tmosversid
	break 1
    elif [ "$test" == "fedora" ]; then		# 28-35+
	TMINSTALL_OS="$test"
	TMINSTALL_OSVERSION=$tmosversid
	break 1
    elif [ "$test" == "rhel" ]; then		# redhat 7, 8, 9
	TMINSTALL_OS="$test"
	TMINSTALL_OSVERSION=$tmosversid
	break 1
    elif [ "$test" == "centos" ]; then		# 7, 8, stream-8
	TMINSTALL_OS="$test"
	TMINSTALL_OSVERSION=$tmosversid
	break 1
    elif [ "$test" == "opensuse" ]; then
	TMINSTALL_OS="opensuse"
	TMINSTALL_OSVERSION=$tmosversid
	break 1
    elif [ "$test" == "suse" ]; then
	TMINSTALL_OS="suse"
	TMINSTALL_OSVERSION=$tmosversid
	break 1
    fi
done


echo "Set TMINSTALL_OS ${TMINSTALL_OS} TMINSTALL_OSVERSION ${TMINSTALL_OSVERSION}"

#Check command line
#-b beta (not full release) changes names of download files
#-g git use download from git and install from source directories
#-s [URL] source URL for downloads
#-u unix type

while getopts :bgts:u: OPTION
do
    case $OPTION in
	b) TMRELEASEDIR="beta${TMRELEASEPATH}_artifacts"
	   TMSOURCE=beta
	  ;;
	t) TMRELEASEDIR="beta${TMRELEASEPATH}_artifacts"
	   TMSOURCE=testing
	   ;;
	g) TMRELEASEDIR=git
	   ;;
	s) TMURL="$OPTARG"
	   ;;
	u) TMUNIX="$OPTARG"
	   ;;
	\:) printf "Argument missing from -%s option \n" $OPTARG
	    printf "Usage: %s: [-b] [-g] [-s url] [-u unixtype]\n" ${basename $0}
	    exit 2
	    ;;
	\?) printf "Argument unknown: -%s\n" $OPTARG
	    printf "Usage: %s: [-b] [-g] [-s url] [-u unixtype]\n" ${basename $0}
	    exit 2
	    ;;
    esac >&2
done
shift $(($OPTIND - 1))

echo "Set TMRELEASEDIR ${TMRELEASEDIR}"

cd $TMINSTALL_BASE

# Helper function: check latest returnValue and quit on error with the provided message text
function checkInstallError {
	local returnValue=$?
	local errorMessage="$1"

	if (( $returnValue )); then
		echo "************"
		echo "** $errorMessage"
		echo "************"
	fi

	return $returnValue
}
export -f checkInstallError

function fetchZipfile {
    local _source=$1
    local _filename=$2
    local _zipname="${_filename}-release-${TMRELEASE}"

    now="$(date +'%d-%b-%y %H:%M')"
    echo "${now} fetchZipFile ${_source} ${_zipname}.zip"
    
    case $_source in
	release)
	    echo "URL http://library.transmartfoundation.org/release/${TMRELEASEDIR}/${_zipname}.zip"
	    curl --silent "http://library.transmartfoundation.org/release/${TMRELEASEDIR}/${_zipname}.zip" --output "${_zipname}.zip" 
	    ;;
	beta)
	    echo "URL http://library.transmartfoundation.org/beta/${TMRELEASEDIR}/${_zipname}.zip"
	    curl --silent "http://library.transmartfoundation.org/beta/${TMRELEASEDIR}/${_zipname}.zip" --output "${_zipname}.zip" 
	    ;;
	testing)
	    echo "URL http://sativa.home/beta/${TMRELEASEDIR}/${_zipname}.zip"
	    curl --silent  "http://sativa.home/beta/${TMRELEASEDIR}/${_zipname}.zip" --output "${_zipname}.zip" 
	    ;;
	git)
	    ;;
	*)
	    echo "Undefined zip file source '${_source}' for ${_zipname}.zip"
	    exit 1
	    ;;
    esac

    now="$(date +'%d-%b-%y %H:%M')"
    if [ -e "${_zipname}.zip" ]; then
	echo "${now} Fetched ${_zipname}.zip"
    else
	echo "${now} Fetch ${_zipname}.zip failed for source ${_source}"
	exit 1
    fi
}
export -f fetchZipfile

function fetchWarfile {
    local _source=$1
    local _filename=$2
    local _outfilename=$3
    local _warname="${_filename}-release-${TMRELEASE}"

    now="$(date +'%d-%b-%y %H:%M')"
    echo "${now} fetchWarFile ${_source} ${_warname}.war"
    case $_source in
	release)
	    curl --silent "http://library.transmartfoundation.org/release/${TMRELEASEDIR}/${_warname}.war" --output "${_outfilename}.war"
	;;
	beta)
	    curl --silent "http://library.transmartfoundation.org/beta/${TMRELEASEDIR}/${_warname}.war" --output "${_outfilename}.war"
	;;
	testing)
	    curl --silent "http://sativa.home/beta/${TMRELEASEDIR}/${_warname}.war" --output "${_outfilename}.war"
	;;
	git)
	    cp "${INSTALL_BASE}/transmartApp/target/${_outfilename}.war" "./${_outfilename}.war"
	;;
	*)
	    echo "Undefined war file source for ${_outfilename}*.war"
	    exit 1
	;;
    esac

    now="$(date +'%d-%b-%y %H:%M')"
    if [ -e "${_outfilename}.war" ]; then
	echo "${now} Fetched ${_warname}.war as ${_outfilename}.war"
    else
	echo "${now} Fetch ${_warname}.war as ${_outfilename}.war failed for source ${_source}"
	exit 1
    fi
}
export -f fetchWarfile

function packageInstall {
    local _package=$1
    case $TMINSTALL_OS in
	debian | ubuntu | suse | opensuse)
	    sudo apt-get -q install -y "${_package}"
	    ;;
	fedora | rhel | centos)
	    sudo dnf -q install -y "${_package}"
	    ;;
	*)
	    sudo apt-get -q install -y "${_package}"
	    ;;
    esac
}
export -f packageInstall

if [ "$TMSOURCE" == "" ] && [ -d "Scripts" ] && [ -d ".git" ] && [ -e "README.md" ]; then
    echo "Found Scripts and top level .git"
    TMSOURCE=git
elif [ "$TMSOURCE" == "" ] && [ -d "../.git" ]; then
    echo "Found .git but no Scripts"
elif [ "$TMSOURCE" == "" ] && [ -e "Scripts-release-${TMRELEASE}.zip" ]; then
    echo "Found Scripts-release-${TMRELEASE}.zip"
    TMSOURCE=release
elif [ "$TMSOURCE" == "" ]; then
    echo "No zip file for Scripts and no .git directory found"
    echo "Unable to determine source"
    echo "Run with one of release|beta to define download targets"
    exit 1
fi;

echo "Set TMSOURCE ${TMSOURCE}"
export TMSOURCE

now="$(date +'%d-%b-%y %H:%M')"
echo "Starting at ${now}"

if ! [ -e "$TMSCRIPTS_BASE/InstallTransmart.sh" ] && [ -d "$TMSCRIPTS_BASE/checks"] ; then
	echo "This script assumes that the Scripts directory is installed at $TMSCRIPTS_BASE/InstallTransmart.sh"
	echo "with further scripts in $TMSCRIPTS_BASE/checks"
	echo "It does not appear to be there. Please fix that and restart this script."
	echo "  cd $TMINSTALL_BASE"
	echo "  curl http://library.transmartfoundation.org/release/release19_1_0_artifacts/Scripts-release-19.1.zip"
	echo "  unzip -q Scripts-release-19.1.zip"
	echo "  mv Scripts-release-19.1 Scripts"
	exit 1
else
	echo "Scripts directory found: $TMSCRIPTS_BASE"
fi
now="$(date +'%d-%b-%y %H:%M')"
echo "Finished checking locations of Script Directory at ${now}"

echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  01.01 set up working dir (tranSMART install base) +"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++"

echo "tranSMART will be installed at this location: $TMINSTALL_BASE"

# give user option to extend the sudo timeout (see welcome.sh)
cd $TMSCRIPTS_BASE
source welcome.sh

# set up sudo early - reset timout and start a fresh period
sudo -k
sudo -v

# Checking if a package is installed
# ubuntu
# fedora dnf list installed | grep make
echo "+++++++++++++++++++++++"
echo "+  01.02 install make +"
echo "+++++++++++++++++++++++"

cd $TMINSTALL_BASE

sudo -v
now="$(date +'%d-%b-%y %H:%M')"
echo "Checking installed packages: make, curl, unzip"
packageInstall make
packageInstall curl
packageInstall unzip

echo "++++++++++++++++++++++++++++++++++++++"
echo "  01.03 checking java 8 is installed +"
echo "++++++++++++++++++++++++++++++++++++++"

set +e
now="$(date +'%d-%b-%y %H:%M')"
echo "Checking installed packages: java 1.8"
cd $TMSCRIPTS_BASE/checks
./checkJava.sh
if [ "$( checkInstallError "java not installed correctly; install" )" ] ; then
    packageInstall openjdk-8-jdk openjdk-8-jre
fi
set -e

cd $TMSCRIPTS_BASE
./InstallDatabase.sh

cd $TMSCRIPTS_BASE
./InstallConfig.sh

cd $TMSCRIPTS_BASE
./InstallWarfiles.sh

cd $TMSCRIPTS_BASE
./InstallSolr.sh

cd $TMSCRIPTS_BASE
./InstallRserve.sh

cd $TMSCRIPTS_BASE
./InstallManual.sh

cd $TMSCRIPTS_BASE
./InstallTomcat.sh


echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "+ 09.01 Done with install - making final checks - (may take a while) +"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"

set e+
cd $TMSCRIPTS_BASE/checks

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Final checks on installation"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check tomcat"

sudo ./checkFilesTomcat.sh

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check tools"

./checkTools.sh

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check web interface"

./checkWeb.sh

echo "++++++++++++++++++++++++++++++++"
echo "+ 09.02 Done with final checks +"
echo "++++++++++++++++++++++++++++++++"
echo "+ To redo all checks"
echo "cd $TMSCRIPTS_BASE/checks"
echo "./checkAll.sh"
echo "++++++++++++++++++++++++++++++++"


now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Finished install of basic transmart ${TMRELEASE} system"

echo "-----------------------------------------------------------------"
echo "To load datasets, use these two files in"
echo "the Scripts/install  directory:"
echo "    datasetsList.txt - the list of possible datasets to load, and"
echo "    load_datasets.sh - the script to load the datasets."
echo ""
echo "First, in the file datasetsList.txt, un-comment the lines that "
echo "correspond to the data sets you wish to load."
echo ""
echo "Then run the file load_datasets.sh with:"
echo "    cd $TMINSTALL_BASE"
echo "    ./Scripts/install/load_datasets.sh"
echo ""
echo "-- Note that loading the same dataset twice is not recommended" 
echo "   and may produce unpredictable results"
echo "-----------------------------------------------------------------"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} ALL DONE"
