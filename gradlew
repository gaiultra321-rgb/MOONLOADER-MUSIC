#!/bin/sh
#
# Gradle startup script for UN*X
#
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

MAX_FD="maximum"

GRADLE_OPTS="${GRADLE_OPTS:-""}"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

OS="`uname`"
case $OS in
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  CYGWIN* )
    cygwin=true
    ;;
  *MSYS* )
    msys=true
    ;;
esac

if [ "$cygwin" = "true" -o "$msys" = "true" ] ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`
fi

JAVA_HOME_CMD="$JAVA_HOME/bin/java"
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME_CMD" ] ; then
        JAVACMD="$JAVA_HOME_CMD"
    elif [ -x "${JAVA_HOME_CMD}.exe" ] ; then
        JAVACMD="${JAVA_HOME_CMD}.exe"
    fi
fi
if [ -z "$JAVACMD" ] ; then
    JAVACMD=java
fi

APP_HOME="`pwd -P`"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
