#!/usr/bin/env bash

docker build -t angrygoat.irss.unc.edu/config_de -f DockerfileConfigs .
docker push angrygoat.irss.unc.edu/config_de
