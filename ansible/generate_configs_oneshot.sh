#!/bin/sh -e

SERVICES="apps"
ANSIBLE_HOME=/home/ansible/DE/ansible
INVENTORY=/home/ansible/ansible-vars/inventory
GROUPVARS=/home/ansible/ansible-vars/group_vars.yaml

for SERVICE in $SERVICES; do
  ansible-playbook -vvvv $ANSIBLE_HOME/local-service-config.yaml -i $INVENTORY -e @$GROUPVARS --extra-vars="service=$SERVICE"
done
