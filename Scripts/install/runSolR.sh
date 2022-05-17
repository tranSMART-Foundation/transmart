#!/bin/bash

cd $TMINSTALL_BASE/transmart-data/
. ./vars

function solr_start()
{
    echo "solrtransmart: starting service"
    make -C solr start
    echo "pid is $(cat solr/solr.pid)"
}

function solr_stop()
{
    echo "solrtransmart: stopping service (pid = $(cat solr/solr.pid))"
    kill -9 $(cat solr/solr.pid)
    rm solr/solr.pid
}

function solr_status()
{
    echo "solrtransmart: status with PID $(cat solr/solr.pid 2>/dev/null)"
    ps -ef | grep 'start[.]jar' | grep -v grep
}


# always run

touch /var/lock/solrtransmart

echo "Running runSolr.sh with param $1"
case "$1" in
start )
    solr_start
    ;;
stop )
    solr_stop
    ;;
reload )
    solr_stop
    sleep 5
    solr_start
    ;;
status )
    solr_status
    ;;
*)
    echo "Usage: $0 {start|stop|reload|status}"
    exit 1
    ;;
esac
