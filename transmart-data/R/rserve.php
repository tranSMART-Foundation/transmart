<?php
$rdir = "$_ENV[R_PREFIX]/lib/R";
$c = $_ENV['RSERVE_CONF'];
$l = $_ENV['RSERVE_LOG'];
?>
#!/bin/bash

### BEGIN INIT INFO
# Provides:             rserve
# Required-Start:       $local_fs $remote_fs $network $syslog
# Required-Stop:        $local_fs $remote_fs $network $syslog
# Default-Start:        2 3 4 5
# Default-Stop:         0 1 6
# Short-Description:    rserve
# Description:    	Rserve server for tranSMART
### END INIT INFO

# Variables to be defined in /etc/default/rserve
# RSERVE_USER (required) username to execute rserve process
# RSERVE_LOG
# RSERVE_CONF

if [[ -f /etc/default/rserve ]]; then
        . /etc/default/rserve
fi
if [[ -z $RSERVE_USER ]]; then
    echo '$RSERVE_USER not defined' >&2
    exit 1
fi
if [[ -z $RSERVE_LOG ]]; then
    RSERVE_LOG="<?= $l ?>"
fi
if [[ -z $RSERVE_CONF ]]; then
    RSERVE_CONF="<?= $c ?>"
fi

NAME="rserve"
DESC="R execution server for tranSMART"
DAEMON="<?= $rdir ?>/bin/Rserve"

export R_HOME="<?= $rdir ?>"
export LD_LIBRARY_PATH="<?= $rdir ?>/lib"

. /lib/lsb/init-functions

PID=/var/run/rserve.pid

running() {
  kill -0 `cat ${PID}` > /dev/null 2>&1
}

start_rserve() {
  touch ${PID} && chown ${RSERVE_USER}:${RSERVE_USER} ${PID}
  sudo --user ${RSERVE_USER} ${RDAEMON} CMD Rserve --quiet --RS-pidfile ${PID} --vanilla >>$RSERVE_LOG 2>&1
  EXIT_VAL=$?
  if [ $EXIT_VAL -eq 0 ]; then
    echo "Rserve started"
  else
    echo "Failed starting Rserve"
  fi
}

stop_rserve() {
  start-stop-daemon --stop --quiet --pidfile ${PID} \
        --exec ${DAEMON} || true
}

status_rserve() {
  status_of_proc -p ${PID} $NAME $NAME && exit 0 || exit $?
}

case "$1" in
  start)
    log_daemon_msg "Starting $DESC" "$NAME"
    start_rserve
    log_end_msg $?
    ;;
  stop)
    log_daemon_msg "Stopping $DESC" "$NAME"
    stop_rserve
    log_end_msg $?
   ;;
  restart|force-reload)
    log_daemon_msg "Restarting $DESC" "$NAME"
    stop_rserve
    sleep 1
    start_rserve
    log_end_msg $?
    ;;
  status)
    status_rserve
    ;;
  *)
    echo "Usage: $NAME {start|stop|restart|status}" >&2
    exit 1
    ;;
esac


exit 0
