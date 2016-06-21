# Install procedures

For the purposes of this fork of DE, we standardized on CentOS 7 VMs, though the number required will be up to the organization. Some services (jex, condor log monitor, condor) need to run on the same machine but most may be safely run on their own node or on shared hardware. Note that if you intend to run all internal services on one node, you'll want to throw a fair amount of resources at that box. Elasticsearch in particular may require a larger RAM allocation, and Condor will need the CPU, RAM, and disk space (/var) to accommodate the needs of your users.

### Ansible Setup
* Create a privileged Ansible user on all boxes, select a head node, generate ssh key, distribute public keys, grant sudo to Ansible user.
* on ansible head node, git clone DE/ansible into a local dir 
* create an ansible-vars dir to contain platform specific configs and inventories

### Prerequisite Playbooks
* install CentOS library prereqs: **$ ansible-playbook -i inventory -e @group_vars -s -K playbooks/prereqs.yaml**
* install docker on "docker-ready" hosts: **$ ansible-playbook -i inventory -e @group_vars -s -K playbooks/docker.yaml**
* install openJDK7 on services VM: **$ ansible-playbook -i inventory -e @group_vars -s -K playbooks/java7.yaml**
* install timezone packages: **$ ansible-playbook -i inventory -e @group_vars -s -K playbooks/timezone.yaml**

### Deploy Discovery Environment
* pull the trigger: **$ ansible-playbook -i inventory -e @group_vars -s -K deploy-all.yaml**

### Other Dependencies
* install Condor: **$ ansible-playbook -i inventory -e @group_vars -s -K playbooks/condor.yaml**
* install and configure cas using our dfc-cas-overlay: **$ ansible-playbook -i inventory -e @group_vars -s -K playbooks/cas-ldap.yaml**

### Setup databases (if doing this manually, otherwise, skip ahead to the data container)

* install postgres: **$ ansible-playbook -i inventory -e @group_vars -s -K playbooks/postgres.yaml**
* create DBs: **$ ansible-playbook -i inventory -e @group_vars -s -K playbooks/db-creator.yaml**

Try your luck with facepalm, but connectivity errors forced us to build it and run it at the command line. Locations for current database tarballs:

```
db_targz: https://everdene.iplantcollaborative.org/jenkins/job/databases-dev/lastSuccessfulBuild/artifact/databases/de-database-schema/database.tar.gz
jexdb_targz: https://everdene.iplantcollaborative.org/jenkins/job/databases-dev/lastSuccessfulBuild/artifact/databases/jex-db/jex-db.tar.gz
metadata_db_targz: https://everdene.iplantcollaborative.org/jenkins/job/databases-dev/lastSuccessfulBuild/artifact/databases/metadata/metadata-db.tar.gz
notification_db_targz: https://everdene.iplantcollaborative.org/jenkins/job/databases-dev/lastSuccessfulBuild/artifact/databases/notification-db/notification-db.tar.gz
```
See also https://everdene.iplantcollaborative.org/jenkins/job/databases-dev/

###Facepalm commands to run:

Template for reference:
```
java -jar /tmp/facepalm-standalone.jar -m {{fmode}} {{db_version_flag|default('')}} -h {{curr_db_host}} -p {{curr_db_port}} -d {{curr_db_name}} -U {{curr_db_user}} -A {{curr_db_admin}} {{tgz_flag}}
```

de db
```
java -jar target/facepalm-standalone.jar -m  init -h dfc-test-vmlab3.edc.renci.org -p 5432 -d de -U de -A postgres -f database.tar.gz
```

jex db
```
java -jar target/facepalm-standalone.jar -m  init -h dfc-test-vmlab3.edc.renci.org -p 5432 -d jex -U jex_user -A postgres -f jex-db.tar.gz
```

metadata db
```
java -jar target/facepalm-standalone.jar -m  init -h dfc-test-vmlab3.edc.renci.org -p 5432 -d metadata -U metadata_db -A postgres -f metadata-db.tar.gz
```

notification db
```
java -jar target/facepalm-standalone.jar -m  init -h dfc-test-vmlab3.edc.renci.org -p 5432 -d notifications -U notification_user -A postgres -f notification-db.tar.gz
```

## Data Container

Data container work is in a private repo, which we need to formalize (mc), in the meantime, we build our own data container docker image and serve it out of a private docker registry, per this [nice setup instructions](https://www.digitalocean.com/community/tutorials/how-to-set-up-a-private-docker-registry-on-ubuntu-14-04)


## Build configs

Edit the generate_configs.sh to point to the correct location for variables and inventory files.  This will generate a set of .properties files for each service, typically in the /etc/iplant/de directory like this:

```

[ansible@dfc-test-vmlab1 de]$ ls
anon-files.properties          data-info.properties    info-typer.properties     jex.properties       notificationagent.properties  user-preferences.properties
apps.properties                dewey.properties        iplant-email.properties   kifshare.properties  saved-searches.properties     user-sessions.properties
clockwork.properties           exim-sender.properties  iplant-groups.properties  metadata.properties  terrain.properties
condor-log-monitor.properties  infosquito.properties   jexevents.properties      monkey.properties    tree-urls.properties


```
## Generate Configs  

Run the ansible playbook generate-configs.yaml to create the consolidated set of properties and configuration that is to be used in the next step to create the config docker images for each service.  

```
ansible-playbook -i /home/ansible/ansible-vars/inventory -e @/home/ansible/ansible-vars/group_vars.yaml -s -K -vvvv playbooks/generate-configs.yaml

```

Then run local services config

```

 ansible-playbook -i /home/ansible/ansible-vars/inventory -e @/home/ansible/ansible-vars/group_vars.yaml -s  -vvvv playbooks/local-services-cfg.yml --extra-vars="group=ansible owner=ansible"


```

This generates additional configuration information under /etc (e.g. docker-gc/)

## Generate config containers and push to a docker repo

Once the configs are in place, run the build_config_images.sh to copy the configs into a docker image and push it to a private docker repo.

To set up a private repo consult this link: https://docs.docker.com/registry/deploying/

The script may need to be configured for your particular Docker repo
