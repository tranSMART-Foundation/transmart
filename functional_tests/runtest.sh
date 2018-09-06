
XVFB_BIN=/usr/bin
MVN_BIN=/usr/bin

echo "Starting display"
${XVFB_BIN}/Xvfb :20 -screen 0 1920x1200x24 -ac +extension GLX +render -noreset &
pid=`ps -ef | grep 'Xvfb' | grep -v 'grep' | awk '{print$2}'`
echo "Xvfb running as pid = ${pid}"
export DISPLAY=:20
echo "DISPLAY set as ${DISPLAY}"

echo "Running tests"
xterm -e "/bin/sh ${MVN_BIN}/mvn test -Pfirefox"

echo "Stopping display"
pid=`ps -ef | grep 'Xvfb' | grep -v 'grep' | awk '{print$2}'`
echo "Xvfb running as pid = ${pid}"
if [ ${pid} ]; then
    echo "killing ${pid}"
    kill ${pid}
    echo "killed ${pid}"
else
    echo "no task"
fi
unset DISPLAY
echo "DISPLAY unset ${DISPLAY}"