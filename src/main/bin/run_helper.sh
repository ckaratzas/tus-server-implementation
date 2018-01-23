#!/bin/bash

APPLICATION=tus.oss.server

APPLICATION_HOME=$( cd $(dirname $0)/.. && pwd )
APPLICATION_CFG_DIR=$APPLICATION_HOME/config
APPLICATION_LOGS=$APPLICATION_HOME/logs
SPRING_CONFIG=tus-server-beans.xml

JVM_ARGS="\
 -Xms1g\
 -Xmx1g\
 -XX:+UseConcMarkSweepGC\
 -XX:+UseParNewGC\
 -Xloggc:${APPLICATION_LOGS}/gc.log.$(date +%Y%m%d_%H%M%S)\
 -verbose:gc\
 -XX:+PrintGCDetails\
 -XX:+UnlockDiagnosticVMOptions\
 -XX:+LogVMOutput\
 -XX:LogFile=${APPLICATION_LOGS}/jvm.log.$(date +%Y%m%d_%H%M%S)\
 -XX:-OmitStackTraceInFastThrow\
 -Dcom.sun.management.jmxremote.port=20000\
 -Dcom.sun.management.jmxremote.authenticate=false\
 -Dcom.sun.management.jmxremote.ssl=false\
 $JVM_ARGS"


JVM_ARGS="$JVM_ARGS\
 -Dlogback.configurationFile=${APPLICATION_CFG_DIR}/logback.xml\
 -Dlogging.config=file:${APPLICATION_CFG_DIR}/logback.xml"

for props in application.properties

do
    [ -f $APPLICATION_CFG_DIR/$props ] && PROPS_ARGS="$PROPS_ARGS -p $props"
done

[ "$SPRING_CONFIG" ] && SPRING_ARGS="-b $SPRING_CONFIG"

MAIN_CLASS=com.$APPLICATION.application.Application

cd $APPLICATION_HOME

run_me() {
    exec java -cp "$APPLICATION_HOME/lib/*" $JVM_ARGS $MAIN_CLASS -c $APPLICATION_CFG_DIR $PROPS_ARGS $SPRING_ARGS
}