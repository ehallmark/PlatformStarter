#!/usr/bin/env bash
cd /home/ehallmark1122/PlatformStarter
sudo -u ehallmark1122 bash -c '    echo "Changed Dirs"'
sudo -u ehallmark1122 bash -c '    git pull origin master'
sudo -u ehallmark1122 bash -c '    echo "Pulled from git"'
sudo -u ehallmark1122 bash -c '    /opt/maven/bin/mvn clean install'
sudo -u ehallmark1122 bash -c '    echo "Mvn clean installed"'
sudo -u ehallmark1122 bash -c '    java -cp target/classes:"target/dependency/*" mini_server.Server &'
