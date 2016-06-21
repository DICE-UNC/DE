#!/bin/sh -e

SERVICES="anon-files apps clockwork condor-log-monitor data-info dewey exim-sender infosquito info-typer iplant-email iplant-groups jexevents jex kifshare metadata monkey notificationagent saved-searches terrain tree-urls user-preferences user-sessions"
ANSIBLE_HOME=/home/ansible/DE/ansible
INVENTORY=/home/ansible/ansible-vars/inventory
GROUPVARS=/home/ansible/ansible-vars/group_vars.yaml

for SERVICE in $SERVICES; do
  ansible-playbook --flush-cache $ANSIBLE_HOME/local-service-config.yaml -i $INVENTORY -e @$GROUPVARS --extra-vars="service=$SERVICE"
done
