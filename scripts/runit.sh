#!/bin/bash

VERSION=1.0

export CLASSPATH=$CLASSPATH:@PROJECT_NAME@-@VERSION@.jar:./lib/*

# set the enviroment
if [ -r "./setenv.sh" ]; then
  . "./setenv.sh"
fi

# ensure eiddo repos are cloned (if do not exists) or up-to-date (if exists)
if [ ! -d ./conf/1p-common/.git ]; then
  #clone from slave ELB if no git repo yet (optionally provide branch name parameter -b <name>)
  git clone git://internal-eiddo-slave-1852879765.us-west-2.elb.amazonaws.com/1p-common ./conf/1p-common
fi
if [ ! -d ./conf/@EIDDO_REPO@/.git ]; then
  #clone from slave ELB if no git repo yet (optionally provide branch name parameter -b <name>)
  git clone git://internal-eiddo-slave-1852879765.us-west-2.elb.amazonaws.com/hystrix-service ./conf/@EIDDO_REPO@
fi


# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

		Please set the JAVA_HOME variable in your environment to match the
		location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

	Please set the JAVA_HOME variable in your environment to match the
	location of your Java installation."
fi

EC2_REGION="$(ec2metadata --availability-zone | grep -Po '(us|sa|eu|ap)-(north|south)?(east|west)?-[0-9]+')"
EC2_INSTANCE="$(ec2metadata --instance-id)"
DEPL_ENV="-Darchaius.deployment.serverId=$EC2_INSTANCE -Darchaius.deployment.region=$EC2_REGION"
INST_COUNT="$(aws ec2 --region=$EC2_REGION describe-tags --filters Name=resource-id,Values=$EC2_INSTANCE Name=key,Values=Instances --output text | cut -f5)"

if [ ! -z "$INST_COUNT" ]
then
  echo "Running extra (($INST_COUNT-1)) instances in non-blocking fasion with own eiddo conf and log file"
  for ((i=1; i<INST_COUNT; i++));
  do
    PORT="$((7001+i*2))"
    ADMIN_PORT="$((8077+i))"
    OVERRIDES="-Deiddo.repo.dir=/home/ubuntu/conf-$PORT -Deureka.name=1P_SERVICE_TEMPLATE-$PORT -Deureka.port=$PORT -Dserver.port=$PORT -Dshutdown.port=$((PORT + 1)) -Dnetflix.platform.admin.resources.port=$ADMIN_PORT"
	if [ ! -d "./conf-$i/1p-common/.git" ]; then
	  git clone git://internal-eiddo-slave-1852879765.us-west-2.elb.amazonaws.com/1p-service "/home/ubuntu/conf-$PORT/1p-common"
	fi
	if [ ! -d "./conf-$i/1p-service/.git" ]; then
	  git clone git://internal-eiddo-slave-1852879765.us-west-2.elb.amazonaws.com/1p-service "/home/ubuntu/conf-$PORT/1p-service"
	fi
    exec "$JAVACMD" $JAVA_OPTS $DEPL_ENV $OVERRIDES -Xmx1400m -XX:MaxPermSize=256m com.thomsonreuters.server.ServerRunner > "log/output-$PORT.log" &
  done
fi

echo "Running default instance"
exec "$JAVACMD" $JAVA_OPTS $DEPL_ENV -Xmx1400m -XX:MaxPermSize=256m com.thomsonreuters.server.ServerRunner 2>&1 | tee log/output.log

