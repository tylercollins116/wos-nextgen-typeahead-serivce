description "@PROJECT_NAME@"

start on net-device-up IFACE=eth0
stop on run level [!2345]
env enabled=1

respawn

exec start-stop-daemon --start --chuid ubuntu --chdir /opt/@PROJECT_NAME@-@VERSION@/ --exec /opt/@PROJECT_NAME@-@VERSION@/runit.sh

