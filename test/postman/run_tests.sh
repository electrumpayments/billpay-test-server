#!/bin/bash
basedir=$1
testdir=$2
echo "Starting docker container"
docker build -t="billpay-test-server" ${basedir}/target
docker run -d -p 8080:8080 --name billpay-test-server_container billpay-test-server
/git/circlecitools/bin/waitForServer.sh localhost:8080 5000
${testdir}/run_newman.sh ${testdir}
rc=$?
echo "Cleaning up Docker"
docker stop billpay-test-server_container
docker rm billpay-test-server_container
docker rmi billpay-test-server
exit $rc