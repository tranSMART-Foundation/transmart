#!/bin/bash

# Check we have maven installed
if ! type mvn >/dev/null 2>&1; then
    echo "maven required but mvn does not exist"
    exit 1
fi

# Check we have java installed
if ! type java >/dev/null 2>&1; then
    echo "java required but java does not exist"
    exit 1
fi

export JAVA_OPTS="$JAVA_OPTS -Xms2048m -Xmx8192m"

PREFIX="build"
OUT=`pwd`

clean=1
cleangrails=0
sleeptime=0


buildapi=0
buildshared=0
buildlegacy=0
buildfractalis=0
builddas=0
builddalliance=0
buildcore=0
buildcoretests=0
buildmydas=0
buildjava=0
buildbiomart=0
buildsearch=0
buildrestapi=0
buildcustom=0
buildfolder=0
buildrmodules=0
buildauth=0
buildipa=0
buildsmartr=0
buildgalaxy=0
buildmetacore=0
buildxnatimport=0
buildxnatviewer=0
buildgwas=0
buildplink=0

buildi2b2core=0
buildgnome=0

buildworkspace=0
buildi2b2=0

buildgwava=0
buildwar=0

build="war"
buildb=""
if  [ "$1" != "" ] ; then
    echo "Argument '$1'"
    build=$1
fi
if  [ "$2" != "" ] ; then
    echo "Argument '$2'"
    if  [ "$2" == "-noclean" ] ; then
	clean=0
    elif  [ "$2" == "-clean" ] ; then
	clean=1
    elif  [ "$2" == "-nocleangrails" ] ; then
	cleangrails=0
    elif  [ "$2" == "-cleangrails" ] ; then
	cleangrails=1
    else
	buildb=$2
    fi
fi
if  [ "$3" != "" ] ; then
    echo "Argument '$3'"
    if  [ "$3" == "-noclean" ] ; then
	clean=0
    elif  [ "$3" == "-clean" ] ; then
	clean=1
    elif  [ "$3" == "-nocleangrails" ] ; then
	cleangrails=0
    elif  [ "$3" == "-cleangrails" ] ; then
	cleangrails=1
    else
	buildb=$2
    fi
fi

 
#dependencies:

# war <- rmodules
# war <- gwas <- folder <- search <- biomart <- java
#                      <- legacy

# war <- galaxy <- rmodules
#               <- legacy

# war <- galaxy <- gwas <- folder <- search <- biomart <- java
#                                <- legacy

if [ "$build" == "war" ] || [ "$buildb" == "war" ] ; then
    buildwar=1
fi

if [ "$build" == "all" ] || [ "$buildb" == "all" ] ; then
    buildapi=1			# 01-core-api
    buildshared=1		# 02-transmart-shared
    buildlegacy=1		# 03-transmart-legacy-db
    buildfractalis=1		# 05-transmart-fractalis
    builddas=1			# 08-mydas
    builddalliance=1		# 09-dalliance-plugin
    buildcore=1			# 11-transmart-core-db
    buildcoretests=1		# 12-transmart-core-db-tests
    buildmydas=1		# 13-transmart-mydas
    buildjava=1			# 21-transmart-java
    buildbiomart=1		# 22-biomart-domain
    buildsearch=1		# 23-search-domain
    buildrestapi=1 		# 24-transmart-rest-api
    buildcustom=1		# 31-transmart-custom
    buildfolder=1		# 32-folder-management-plugin
    buildmetacore=1		# 33-transmart-metacore-plugin
    buildrmodules=1		# 34-Rmodules
    buildauth=1			# 41-spring-security-auth0
    buildipa=1			# 51-IpaIpi
    buildsmartr=1		# 52-SmartR
    buildgalaxy=1		# 61-galaxy-export-plugin
    buildxnatviewer=1		# 71-transmart-xnat-viewer
    buildxnatimport=1		# 72-transmart-xnat-importer-plugin
    buildgwas=1			# 81-transmart-gwas-plugin
    buildplink=1		# 82-transmart-gwas-plink
    # buildgnome=1
    # buildi2b2core=1
fi

if [ "$build" == "api" ] || [ "$buildb" == "api" ] ; then
    buildapi=1
fi

if [ "$build" == "shared" ] || [ "$buildb" == "shared" ] ; then
    buildshared=1
fi

if [ "$build" == "legacy" ] || [ "$buildb" == "legacy" ] ; then
    buildlegacy=1
fi

if [ "$build" == "fractalis" ] || [ "$buildb" == "fractalis" ] ; then
    buildfractalis=1
fi

if [ "$build" == "das" ] || [ "$buildb" == "das" ] ; then
    builddas=1
fi

if [ "$build" == "dalliance" ] || [ "$buildb" == "dalliance" ] ; then
    builddalliance=1
fi

if [ "$build" == "core" ] || [ "$buildb" == "core" ] ; then
    buildcore=1
fi

if [ "$build" == "coretests" ] || [ "$buildb" == "coretests" ] ; then
    buildcoretests=1
fi

if [ "$build" == "mydas" ] || [ "$buildb" == "mydas" ] ; then
    buildmydas=1
fi

if [ "$build" == "java" ] || [ "$buildb" == "java" ] ; then
    buildjava=1
fi

if [ "$build" == "biomart" ] || [ "$buildb" == "biomart" ] ; then
    buildbiomart=1
fi

if [ "$build" == "search" ] || [ "$buildb" == "search" ] ; then
    buildsearch=1
fi

if [ "$build" == "rest" ] || [ "$buildb" == "rest" ] ; then
    buildrestapi=1
fi

if [ "$build" == "custom" ] || [ "$buildb" == "custom" ] ; then
    buildcustom=1
fi

if [ "$build" == "folder" ] || [ "$buildb" == "folder" ] ; then
    buildfolder=1
fi

if [ "$build" == "rmodules" ] || [ "$buildb" == "rmodules" ] ; then
    buildrmodules=1
fi

if [ "$build" == "auth" ] || [ "$buildb" == "auth" ] ; then
    buildauth=1
fi

if [ "$build" == "ipa" ] || [ "$buildb" == "ipa" ] ; then
    buildipa=1
fi

if [ "$build" == "smartr" ] || [ "$buildb" == "smartr" ] ; then
    buildsmartr=1
fi

if [ "$build" == "galaxy" ] || [ "$buildb" == "galaxy" ] ; then
    buildgalaxy=1
fi

if [ "$build" == "metacore" ] || [ "$buildb" == "metacore" ] ; then
    buildmetacore=1
fi

if [ "$build" == "xnatimport" ] || [ "$buildb" == "xnatimport" ] ; then
    buildxnatimport=1
fi

if [ "$build" == "xnatviewer" ] || [ "$buildb" == "xnatviewer" ] ; then
    buildxnatviewer=1
fi

if [ "$build" == "gwas" ] || [ "$buildb" == "gwas" ] ; then
    buildgwas=1
fi

if [ "$build" == "plink" ] || [ "$buildb" == "plink" ] ; then
    buildplink=1
fi

if [ "$build" == "gnome" ] || [ "$buildb" == "gnome" ] ; then
    buildgnome=1
fi

if [ "$build" == "i2b2core" ] || [ "$buildb" == "i2b2core" ] ; then
    buildi2b2core=1
fi

if [ "$build" == "i2b2" ] || [ "$buildb" == "i2b2" ] ; then
    buildi2b2=1
fi

if [ "$build" == "workspace" ] || [ "$buildb" == "workspace" ] ; then
    buildworkspace=1
fi

# dependencies on other builds
# make sure any that depends on this build are rebuilt after

if [ $buildapi == 1 ] ; then
    buildcore=1
    buildcoretests=1
    buildmydas=1
    buildrmodules=1
    buildsmartr=1
    buildxnatviewer=1
    buildrestapi=1
fi

if [ $buildshared == 1 ] ; then
    buildcore=1
    buildsearch=1
    buildcustom=1
    buildgwas=1
    buildauth=1
    buildmetacore=1
    buildgalaxy=1
    buildrmodules=1
    buildxnatimport=1
    buildrestapi=1
fi

if [ $buildlegacy == 1 ] ; then
    buildgalaxy=1
    buildgwas=1
fi

if [ $buildfractalis == 1 ] ; then
    :
fi

if [ $builddas == 1 ] ; then
    buildmydas=1
fi

if [ $builddalliance == 1 ] ; then
    :
fi

if [ $buildcore == 1 ] ; then
    buildcoretests=1
    buildgwas=1
    buildfolder=1
    buildauth=1
    buildsmartr=1
    buildrestapi=1
fi

if [ $buildcoretests == 1 ] ; then
    buildsmartr=1
    buildrestapi=1
fi

if [ $buildmydas == 1 ] ; then
    :
fi

if [ $buildjava == 1 ] ; then
    buildbiomart=1
fi

if [ $buildbiomart == 1 ] ; then
    buildsearch=1
    buildxnatimport=1
fi

if [ $buildsearch == 1 ] ; then
    buildcustom=1
    buildgwas=1
    buildauth=1
    buildfolder=1
    buildxnatviewer=1
fi

if [ $buildrestapi == 1 ] ; then 
    :
fi

if [ $buildcustom == 1 ] ; then
    buildauth=1
fi

if [ $buildfolder == 1 ] ; then 
    buildgwas=1
fi

if [ $buildmetacore == 1 ] ; then 
    buildrmodules=1
fi

if [ $buildrmodules == 1 ] ; then 
    buildgalaxy=1
    buildgwas=1
    buildplink=1
fi

if [ $buildauth == 1 ] ; then 
    :
fi

if [ $buildipa == 1 ] ; then 
    buildsmartr=1
fi

if [ $buildsmartr == 1 ] ; then 
    :
fi

if [ $buildgalaxy == 1 ] ; then 
    :
fi

if [ $buildxnatimport == 1 ] ; then 
    :
fi

if [ $buildxnatviewer == 1 ] ; then 
    :
fi

if [ $buildgwas == 1 ] ; then 
    :
fi

if [ $buildplink == 1 ] ; then 
    :
fi


# Check dependencies for WAR file
# ===============================

if [ $buildrestapi == 1 ] ; then
    buildwar=1
fi

if [ $buildgwas == 1 ] ; then
    buildwar=1
fi

if [ $buildplink == 1 ] ; then
    buildwar=1
fi

if [ $buildmetacore == 1 ] ; then
    buildwar=1
fi

if [ $buildgalaxy == 1 ] ; then
    buildwar=1
fi

if [ $buildcoretests == 1 ] ; then
    buildwar=1
fi

if [ $buildcore == 1 ] ; then
    buildwar=1
fi

if [ $buildapi == 1 ] ; then
    buildwar=1
fi

if [ $builddalliance == 1 ] ; then
    buildwar=1
fi

if [ $buildmydas == 1 ] ; then
    buildwar=1
fi

if [ $buildipa == 1 ] ; then
    buildsmartr=1
fi

if [ $buildsmartr == 1 ] ; then
    buildwar=1
fi

if [ $buildfractalis == 1 ] ; then
    buildwar=1
fi

if [ $buildxnatviewer == 1 ] ; then
    buildwar=1
fi

if [ $buildxnatimport == 1 ] ; then
    buildwar=1
fi
if [ $buildcustom == 1 ] ; then
    buildwar=1
fi
if [ $buildshared == 1 ] ; then
    buildwar=1
fi
if [ $buildauth == 1 ] ; then
    buildwar=1
fi

# GWAVA dependencies

if [ "$build" == "gwava" ] || [ "$buildb" == "gwava" ] ; then
    buildgwava=1
fi

# Report builds required
# ======================

if [ $buildapi != 0 ] ; then
    echo "api:        $buildapi"
fi

if [ $buildshared != 0 ] ; then
    echo "shared:     $buildshared"
fi

if [ $buildlegacy != 0 ] ; then
    echo "legacy:     $buildlegacy"
fi

if [ $buildfractalis != 0 ] ; then
    echo "fractalis:  $buildfractalis"
fi

if [ $builddas != 0 ] ; then
    echo "das:        $builddas"
fi

if [ $builddalliance != 0 ] ; then
    echo "dalliance:  $builddalliance"
fi

if [ $buildcore != 0 ] ; then
    echo "core:       $buildcore"
fi

if [ $buildcoretests != 0 ] ; then
    echo "coretests:  $buildcoretests"
fi

if [ $buildmydas != 0 ] ; then
    echo "mydas:      $buildmydas"
fi

if [ $buildjava != 0 ] ; then
    echo "java:       $buildjava"
fi

if [ $buildbiomart != 0 ] ; then
    echo "biomart:    $buildbiomart"
fi

if [ $buildsearch != 0 ] ; then
    echo "search:     $buildsearch"
fi

if [ $buildrestapi != 0 ] ; then
    echo "restapi:    $buildrestapi"
fi

if [ $buildcustom != 0 ] ; then
    echo "custom:     $buildcustom"
fi

if [ $buildfolder != 0 ] ; then
    echo "folder:     $buildfolder"
fi

if [ $buildrmodules != 0 ] ; then
    echo "rmodules:   $buildrmodules"
fi

if [ $buildauth != 0 ] ; then
    echo "auth:       $buildauth"
fi

if [ $buildipa != 0 ] ; then
    echo "ipa:        $buildipa"
fi

if [ $buildsmartr != 0 ] ; then
    echo "smartr:     $buildsmartr"
fi

if [ $buildgalaxy != 0 ] ; then
    echo "galaxy:     $buildgalaxy"
fi

if [ $buildmetacore != 0 ] ; then
    echo "metacore:   $buildmetacore"
fi

if [ $buildxnatimport != 0 ] ; then
    echo "xnatimport: $buildxnatimport"
fi

if [ $buildxnatviewer != 0 ] ; then
    echo "xnatviewer: $buildxnatviewer"
fi

if [ $buildgwas != 0 ] ; then
    echo "gwas:       $buildgwas"
fi

if [ $buildplink != 0 ] ; then
    echo "plink:      $buildplink"
fi

if [ $buildgnome != 0 ] ; then
    echo "gnome:      $buildgnome"
fi

if [ $buildi2b2core != 0 ] ; then
    echo "i2b2core:   $buildi2b2core"
fi

if [ $buildi2b2 != 0 ] ; then
    echo "i2b2:       $buildi2b2"
fi

if [ $buildworkspace != 0 ] ; then
    echo "workspace:  $buildworkspace"
fi

if [ $buildwar != 0 ] ; then
    echo "war:        $buildwar"
fi

if [ $buildgwava != 0 ] ; then
    echo "gwava:      $buildgwava"
fi

if [ $clean != 0 ] ; then
    echo "CLEAN:      YES"
fi
if [ $cleangrails != 0 ] ; then
    echo "CLEANGRAILS:YES"
fi

if [ $buildwar == 0 ] && [ $buildgwava == 0 ] ; then
    echo "Invalid argument $1"
    exit
fi

# 01-core-api

if [ $buildapi == 1 ] ; then
    DIR="transmart-core-api"
    PNAME="transmart-core-api"
    echo "01-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	# no grails plugin files, only maven install
	rm -rf ~/.m2/repository/org/transmartproject/$PNAME
    fi

    ./gradlew clean build publishToMavenLocal  >  $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out

    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 02-transmart-shared

if [ $buildshared == 1 ] ; then
    DIR="transmart-shared"
    PNAME="transmart-shared"
    echo "02-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME-1*-SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME-1*-SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out

    cd ..
    
fi

# 03-legacy-db

if [ $buildlegacy == 1 ] ; then
    DIR="transmart-legacy-db"
    PNAME="transmart-legacy-db"
    echo "03-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi
	
    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 05-Fractalis

if [ $buildfractalis == 1 ] ; then
    DIR="transmart-fractalis"
    PNAME="transmart-fractalis"
    echo "05-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
    
    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..
    
    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 08-mydas

if [ $builddas == 1 ] ; then
    DIR="mydas"
    PNAME="mydas"
    echo "08-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	# No grails, only the maven install
	rm -rf ~/.m2/repository/uk/ac/ebi/$PNAME
    fi

    cd mydas

#    ./gradlew clean build publishToMavenLocal  >  $OUT/$PREFIX-$DIR.out 2>&1
    mvn install  >   $OUT/$PREFIX-$DIR.out 2>&1
    cd ..

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 09-dalliance

if [ $builddalliance == 1 ] ; then
    DIR="dalliance-plugin"
    PNAME="dalliance-plugin"
    echo "09-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
    #
    #egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 11-transmart-core-db

if [ $buildcore == 1 ] ; then
    DIR="transmart-core-db"
    PNAME="transmart-core"
    echo "11-$DIR"
    echo ""
    cd $DIR

    # first we build

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME-1*-SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME-1*-SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out

    cd ..
    
    # then we use the build to test
fi

# 12-transmart-core-db-tests

if [ $buildcoretests == 1 ] ; then
    DIR="transmart-core-db-tests"
    PNAME="transmart-core-db-tests"
    echo "12-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    rm -rf target/stacktrace.log

    ./grailsw test-app --xml >  $OUT/$PREFIX-$DIR.out 2>&1

    ./grailsw  maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out

    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 13-transmart-mydas

if [ $buildmydas == 1 ] ; then
    DIR="transmart-mydas"
    PNAME="transmart-mydas"
    echo "13-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 21-transmart-java

if [ $buildjava == 1 ] ; then
    DIR="transmart-java"
    PNAME="transmart-java"
    echo "21-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
        rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
        rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 22-biomart-domain

if [ $buildbiomart == 1 ] ; then
    DIR="biomart-domain"
    PNAME="biomart-domain"
    echo "22-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
        rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
        rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 23-search-domain

if [ $buildsearch == 1 ] ; then
    DIR="search-domain"
    PNAME="search-domain"
    echo "23-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
        rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
        rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 24-REST-Api-Plugin

if [ $buildrestapi == 1 ] ; then
    DIR="transmart-rest-api"
    PNAME="transmart-rest-api"
    echo "24-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

# build twice to resolve transmart-core-db-tests
    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
    
    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 31-transmart-custom

if [ $buildcustom == 1 ] ; then
    DIR="transmart-custom"
    PNAME="transmart-custom"
    echo "31-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME-1*-SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME-1*-SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out

    cd ..
    
fi

# 32-folder-management-plugin

if [ $buildfolder == 1 ] ; then
    DIR="folder-management-plugin"
    PNAME="folder-management"
    echo "32-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 33-Metacore-Plugin

if [ $buildmetacore == 1 ] ; then
    DIR="transmart-metacore-plugin"
    PNAME="transmart-metacore-plugin"
    echo "33-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
    
    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..
    
    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 34-Rmodules

if [ $buildrmodules == 1 ] ; then
    DIR="Rmodules"
    PNAME="rdc-rmodules"
    echo "34-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 41-spring-security-auth0

if [ $buildauth == 1 ] ; then
    DIR="spring-security-auth0"
    PNAME="spring-security-auth0"
    echo "41-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME-1*-SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME-1*-SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out

    cd ..
    
fi

# 51-ipaapi

if [ $buildipa == 1 ] ; then
    DIR="IpaApi"
    PNAME="IpaApi"
    echo "51-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	# no grails plugin files, only maven install
	rm -rf ~/.m2/repository/com/ittm_solutions/ipacore/$PNAME
    fi

    #    ./gradlew clean build publishToMavenLocal  >  $OUT/$PREFIX-$DIR.out 2>&1
    mvn install >  $OUT/$PREFIX-$DIR.out 2>&1

    #egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 52-SmartR

if [ $buildsmartr == 1 ] ; then
    DIR="SmartR"
    PNAME="smart-r"
    echo "52-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    # build twice to resolve transmart-core-db-tests
    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
    
    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..
    
    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 61-galaxy-export-plugin

if [ $buildgalaxy == 1 ] ; then
    DIR="galaxy-export-plugin"
    PNAME="galaxy-export-plugin"
    echo "61-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
    
    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 71-transmart-xnat-viewer

if [ $buildxnatviewer == 1 ] ; then
    DIR="transmart-xnat-viewer"
    PNAME="xnat-viewer"
    echo "71-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
    
    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..
    
    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 72-transmart-xnat-importer-plugin

if [ $buildxnatimport == 1 ] ; then
    DIR="transmart-xnat-importer-plugin"
    PNAME="transmart-xnat-importer"
    echo "72-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
    
    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..
    
    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 81-gwas-plugin

if [ $buildgwas == 1 ] ; then
    DIR="transmart-gwas-plugin"
    PNAME="transmart-gwas"
    echo "81-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 82-gwas-plink

if [ $buildplink == 1 ] ; then
    DIR="transmart-gwas-plink"
    PNAME="transmart-gwas-plink"
    echo "82-$DIR"
    echo ""
    cd $DIR

    if [ $clean == 1 ] ; then
	rm -rf ~/.grails/2.*/projects/$PNAME
	rm -rf ~/.grails/2.*/projects/*/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.grails/2.*/projects/*/resources/plugins/$PNAME*SNAPSHOT
	rm -rf ~/.m2/repository/org/grails/plugins/$PNAME
    fi
    if [ $cleangrails == 1 ] ; then
	./grailsw clean-all
    fi

    ./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    ./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi

# 99a-workspace-plugin

##if [ $buildworkspace == 1 ] ; then
##DIR="transmart-workspace-plugin"
##echo "99a-$DIR"
##echo ""
##cd $DIR
##./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
##./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
##
##egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
##cd ..
##    if [ $sleeptime != 0 ]; then
##	sleep $sleeptime
##    fi

##fi



# 99b-i2b2-plugin

if [ $buildi2b2 == 1 ] ; then
    DIR="transmart-i2b2-plugin"
    #echo "99b-$DIR"
    #echo ""
    cd $DIR
    #./grailsw package-plugin >  $OUT/$PREFIX-$DIR.out 2>&1
    #./grailsw maven-install  >> $OUT/$PREFIX-$DIR.out 2>&1
    #
    #egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out
    cd ..

    if [ $sleeptime != 0 ]; then
	sleep $sleeptime
    fi

fi


# 91-transmartApp war file

if [ $buildwar == 1 ] ; then
    DIR="transmartApp"
    echo "91-$DIR"
    echo ""
    cd $DIR

    if [ $cleangrails == 1 ] || [ $buildcoretests == 1 ] ; then
	./grailsw clean-all
	# initial build to resolve transmart-core-db-tests
	./grailsw war            > $OUT/$PREFIX-$DIR.out 2>&1
    fi

    ./grailsw war            > $OUT/$PREFIX-$DIR.out 2>&1

    egrep -v '(warning[.]gif|error[.]gif|errors[.]css|Warnings[.][a-z]*)$' $OUT/$PREFIX-$DIR.out | egrep -i 'error[^-]|warning|fail'

    cd ..

fi

# 99-GWAVA - build war

if [ $buildgwava == 1 ] ; then
    DIR="GWAVA"
    echo "99-$DIR"
    echo ""
    cd $DIR

    ant transmartwar >  $OUT/$PREFIX-$DIR.out 2>&1

    egrep -i 'error[^-]|warning|fail' $OUT/$PREFIX-$DIR.out

    cd ..
    
fi

# deploy war file(s)

if [ $buildwar == 1 ] || [ $buildgwava == 1 ] ; then

    echo "Copying new war file(s), please wait for completion"

    if [ -d /var/lib/tomcat9 ] ; then
	TOMCAT="/var/lib/tomcat9"
    elif [ -d /var/lib/tomcat8 ] ; then
	TOMCAT="/var/lib/tomcat8"
    elif [ -d /var/lib/tomcat7 ] ; then
	TOMCAT="/var/lib/tomcat7"
    else
	TOMCAT="/var/lib/tomcat "
    fi

    if [ $buildwar == 1 ] ; then
	cp transmartApp/target/transmart.war $TOMCAT/webapps/transmart.war
	echo "Copied transmart.war"
    fi

    if [ $buildgwava == 1 ] ; then
	cp GWAVA/dist/gwava.war $TOMCAT/webapps/gwava.war
	echo "Copied gwava.war"
    fi
xs
fi

echo "All done"
