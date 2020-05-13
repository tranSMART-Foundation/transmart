<?php
$s = "$_ENV[SOLR_PREFIX]";
$l = "$_ENV[SOLR_LOG]";
?>
#!/bin/bash

### BEGIN INIT INFO
# Provides:          solr
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Solr
# Description:       Solr advanced search engine for tranSMART
### END INIT INFO

# Do NOT "set -e"

# PATH should only include /usr/* if it runs after the mountnfs.sh script
PATH=/sbin:/usr/sbin:/bin:/usr/bin

DESC="Solr"
# Process name ( For display )
NAME="solr"
# Daemon name, where is the actual executable
DAEMON=/usr/bin/java
# Read about the available options at: http://docs.oracle.com/javase/1.3/docs/tooldocs/solaris/java.html
DAEMON_ARGS="-Xmx1024m -DSTOP.PORT=8079 -DSTOP.KEY=stopkey -Dsolr.solr.home=solr -jar start.jar"
# pid file for the daemon
PIDFILE=/var/run/$NAME.pid
# script name
SCRIPTNAME=/etc/init.d/$NAME

# Solr home
SOLR_HOME="<?= $s ?>"

# Load the VERBOSE setting and other rcS variables
. /lib/init/vars.sh

# Define LSB log_* functions.
# Depend on lsb-base (>= 3.2-14) to ensure that this file is present
# and status_of_proc is working.
. /lib/lsb/init-functions

# Exit if the package is not installed
[ -x $DAEMON ] || exit 5

# Read configuration variable file if it is present
[ -r /etc/default/$NAME ] && . /etc/default/$NAME

if [[ -z $SOLR_USER ]]; then
    echo '$SOLR_USER not defined' >&2
    exit 1
fi
if [[ -z $SOLR_LOG ]]; then
    echo '$SOLR_LOG not defined' >&2
    SOLR_LOG="<?= $l ?>"
fi

#
# Function that starts the daemon/service
#
do_start()
{
        # Return
        #   0 if daemon has been started
        #   1 if daemon was already running
        #   2 if daemon could not be started
        start-stop-daemon --start --quiet --pidfile $PIDFILE --exec $DAEMON --test > /dev/null \
                || return 1
	touch ${SOLR_LOG}
	chown ${SOLR_USER}:${SOLR_USER} ${SOLR_LOG}
        start-stop-daemon --start --user ${SOLR_USER} --quiet --pidfile $PIDFILE --chdir $SOLR_HOME --background --make-pidfile --exec $DAEMON >>$SOLR_LOG 2>&1 -- \
                $DAEMON_ARGS \
                || return 2
        # Add code here, if necessary, that waits for the process to be ready
        # to handle requests from services started subsequently which depend
        # on this one.  As a last resort, sleep for some time.
}

#
# Function that stops the daemon/service
#
do_stop()
{
        # Return
        #   0 if daemon has been stopped
        #   1 if daemon was already stopped
        #   2 if daemon could not be stopped
        #   other if a failure occurred
        # there was on both:--retry=TERM/30/KILL/5
        start-stop-daemon --stop --quiet --retry=TERM/4/KILL/5 --pidfile $PIDFILE --name $NAME
        RETVAL="$?"
        [ "$RETVAL" = 2 ] && return 2
        # Wait for children to finish too if this is a daemon that forks
        # and if the daemon is only ever run from this initscript.
        # If the above conditions are not satisfied then add some other code
        # that waits for the process to drop all resources that could be
        # needed by services started subsequently.  A last resort is to
        # sleep for some time.
        start-stop-daemon --stop --quiet --oknodo --retry=TERM/4/KILL/5 --exec $DAEMON
        [ "$?" = 2 ] && return 2
        # Many daemons don't delete their pidfiles when they exit.
        rm -f $PIDFILE
        return "$RETVAL"
}

#
# Function that sends a SIGHUP to the daemon/service
#
do_reload() {
        #
        # If the daemon can reload its configuration without
        # restarting (for example, when it is sent a SIGHUP),
        # then implement that here.
        #
        start-stop-daemon --stop --signal 1 --quiet --pidfile $PIDFILE --name $NAME
        return 0
}

case "$1" in
  start)
        echo "$DESC: Starting $NAME" >&2
        do_start
        case "$?" in
                0) log_daemon_msg "$DESC" "is now running" && log_end_msg 0 ;;
                1) log_daemon_msg "$DESC" "was already running" && log_end_msg 0 ;;
                2) log_daemon_msg "$DESC" "could not be started" && log_end_msg 2 ;;
        esac
        ;;
  stop)
        echo "$DESC: Stopping $NAME" >&2
        do_stop
        case "$?" in
                0|1) log_daemon_msg "$DESC" "is stopped" && log_end_msg 0 ;; #It is always returning 0
                2) log_daemon_msg "$DESC" "could not be stopped" && log_end_msg 2 ;;
        esac
        ;;
  status)
       status_of_proc "$DAEMON" "$NAME" && exit 0
       ;;
  restart)
        echo "$DESC: Restarting $NAME" >&2
        do_stop
        case "$?" in
          0|1)
                do_start
                case "$?" in
                        0) log_daemon_msg "$DESC" "restarted" && log_end_msg 0 ;;
                        1) log_daemon_msg "$DESC" "old precess is still running" && log_end_msg 1 ;;
                        *) log_daemon_msg "$DESC" "failed to restart" && log_end_msg 1 ;;
                esac
                ;;
          *)
                # Failed to stop
                log_daemon_msg "$DESC" "failed to stop" && log_end_msg 1
                ;;
        esac
        ;;
  *)
        echo "Usage: $SCRIPTNAME {start|stop|status|restart}" >&2
        exit 3
        ;;
esac

:
