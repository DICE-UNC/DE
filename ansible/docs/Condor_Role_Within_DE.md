## Discovery Environment's use of Condor

All end-user computation is handled via a Condor DAG. End users may specify their own "apps" in the user interface, which in the context of DE are JSON wrappers which call "tools" which are Docker containers vetted/curated via the admin interface. A user may request a tool, chain a string of apps into a workflow, and then share that workflow with other users.

When a user submits a job, the porklock service pulls the specified data from iRODS, presents it to Condor as a Docker data container, and returns any resulting output along with service and system logs to the user's "Analyses" folder in iRODS. Note that, while the de-condor-submission and de-condor-node roles worked well for Odum and RENCI's test installations and Condor scales quite effortlessly, for repeated jobs of any size you're going to want a sizeable /var partition and you may want to look into Condor's shared NFS capability for this purpose.

## Inventory touchpoints

Sample inventory condor-related touchpoints annotated with purpose

```
[clockwork]
services.example.org

[condor-log-monitor]
condor-submission-node.example.org

[condor]
condor-worker-node.example.org

[condor-submission]
condor-submission-node.example.org

[jex]
condor-submission-node.example.org

[jexevents]
condor-submission-node.example.org


```

## Group var touchpoints

```

condor_submission_ip_range: 172.0.0.0/8
condor_allow_write: "*.renci.org,{{ condor_submission_ip_range }}"
condor:
 cred_dir: /var/cred_dir
 #host: "{{ groups['condor'][0] }}"
 host: condor-submission-node.example.org
 admin: xxx@renci.org
 collector_name: "{{ environment_name }} pool"
 flock_to: condor-worker-node.example.org
 filesystem_domain: example.org
 uid_domain: example.org
 allow_write: "{{ condor_allow_write }}"
 allow_read: "{{ condor_allow_write }}"

condor_log_monitor_event_log: /var/log/condor/event_log
condor_log_monitor:
 service_name: condor-log-monitor.service
 service_name_short: condor-log-monitor
 compose_service: clm
 service_description: CLM; Condor log monitor
 image_name: condor-log-monitor
 container_name: clm
 properties_file: condor_log_monitor.properties
 log_file: condor-log-monitor-docker.log


# Jex does not actually have a container. The container name is how most syslog entries
# are identified. See rsyslog-config role.

jex_host:  "{{ groups['jex'][0] }}"
jex_port: 5004
jex:
 host: "{{ groups['jex'][0] }}"
 port: "{{jex_port}}"
 base: "http://{{ jex_host }}:{{jex_port}}"
 service_name: jex.service
 service_name_short: jex
 compose_service: jex
 log_file: jex/jex.log
 container_name: jex
 batch_group: batch_processing
 nfs_base: /export/condor
 icommands_path: /usr/local/icommands/:/usr/local/bin/:/usr/bin/
 request_disk: 0

jexdb_driver: "{{db_driver}}"
jexdb_host: "{{db_host}}"
jexdb_db: jex
jexdb_password: "{{db_password}}"
jexdb_port: "{{db_port}}"
jexdb_user: "{{db_user}}"
jexdb_vendor: "{{db_vendor}}"


jexevents_host: "{{ groups['jexevents'][0] }}"
jexevents_port: 5005
jexevents:
 host: "{{jexevents_host}}"
 port: "{{jexevents_port}}"
 base: "http://{{ jexevents_host }}:{{jexevents_port}}/"
 event_url: "{{ apps.base }}/callbacks/de-job"
 service_name: jexevents.service
 service_name_short: jexevents
 compose_service: jex_events
 service_description: jex events service
 image_name: jex-events
 container_name: jex-events
 properties_file: jexevents.properties
 log_file: jex-events-docker.log

job_status_poll_interval: 15





```
