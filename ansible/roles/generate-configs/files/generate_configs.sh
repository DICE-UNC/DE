#!/bin/sh -e

SERVICES="anon-files apps clockwork condor-log-monitor data-info dewey exim-sender infosquito info-typer iplant-email iplant-groups jexevents jex kifshare metadata monkey nginx notificationagent saved-searches terrain tree-urls user-preferences user-sessions"
ANSIBLE_HOME=/home/hamilton/DE/ansible
INVENTORY=/home/hamilton/inventories/odum-test
GROUPVARS=/home/hamilton/group_vars/odum

for SERVICE in $SERVICES; do
  ansible-playbook $ANSIBLE_HOME/local-service-config.yaml -i $INVENTORY -e @$GROUPVARS --extra-vars="service=$SERVICE"
done
