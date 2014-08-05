#!/bin/bash

root=$(
  cd $(dirname $(readlink $0 || echo $0))/..
  /bin/pwd
)

jarname=sbt-launch.jar
homedir=~/
sbtjar=$homedir$jarname
if [ ! -f $sbtjar ]; then
  echo 'downloading '$sbtjar 1>&2
  curl -k -O http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.0/sbt-launch.jar
  #TMPDIR=tmp
  #mkdir $TMPDIR
  #cp $jarname $TMPDIR
  #cd $TMPDIR
  #jar xf $jarname
  #rm -f sbt/sbt.boot.properties $jarname
  #rm -f sbt.boot.properties* $jarname
  #jar cmf META-INF/MANIFEST.MF $jarname *
  mv $jarname $homedir
  #cd ..
  #rm -rf $TMPDIR
  #rm $jarname
#else
#  current_sbtsha = $(shasum )
fi

test -f $sbtjar || exit 1

#downloadedSbtMd5=
#platform=`uname`

#if [ $platform == "Linux" ]; then
#  downloadedSbtMd5=`openssl md5 < sbt-launch.jar | awk {'print $2'}`
#else
#  downloadedSbtMd5=`openssl md5 < sbt-launch.jar`
#fi



#if [ $downloadedSbtMd5 != 56649e2bbbc9ac34fc69d5aaf0711730 ]; then
#  echo "bad sbtjar! $downloadedSbtMd5" 1>&2
#  exit 1
#fi

test -f ~/.sbtconfig && . ~/.sbtconfig

SBT_OPTS="-Dscalac.patmat.analysisBudget=off"
java -ea                          \
-Divy.home=./libs  \
-Dsbt.boot.realm="Sonatype Nexus Repository Manager" \
#-Dsbt.boot.host=yourhost \
#-Dsbt.boot.user=publisher \
#-Dsbt.boot.password=xyz \
$SBT_OPTS                       \
  $JAVA_OPTS                      \
  -Djava.net.preferIPv4Stack=true \
  -Dsbt.boot.properties=sbt.boot.properties \
  -XX:+AggressiveOpts             \
  -XX:+UseParNewGC                \
  -XX:+UseConcMarkSweepGC         \
  -XX:+CMSParallelRemarkEnabled   \
  -XX:+CMSClassUnloadingEnabled   \
  -XX:+UseCodeCacheFlushing       \
  -XX:ReservedCodeCacheSize=256m  \
  -XX:MaxPermSize=1024m           \
  -XX:SurvivorRatio=128           \
  -XX:MaxTenuringThreshold=0      \
  -Xss8M                          \
  -Xms512M                        \
  -Xmx1G                          \
  -server                         \
  -jar $sbtjar "$@"
