support-logstash-cfg
====================

Provides configuration files for deploying logstash and installs logstash on a remote host (using the support-logstash
role).

Requirements
------------

This role uses the support-logstash role.

Role Variables
--------------

|   Variable                         | required | default                 | choices | comments                                               |
|------------------------------------|----------|-------------------------|---------|--------------------------------------------------------|
| logstash_version                   |  no      | "2.2"                   |         | The version of Logstash to install |
| logstash_install                   |  no      | true                    |         | A flag used to control whether the role should perform installation steps. |
| logstash_base_dir                  |  no      | "/opt/logstash"         |         | Logstash's install location. |
| logstash_cfg_dir                   |  no      | "/etc/logstash/conf.d"  |         | Logstash's config directory. |
| logstash_clean_cfg_dir             |  no      | true                    |         | Determines whether the cfg dir will be cleaned prior to uploading new ones.|
| logstash_elasticsearch_host        |  no      | "localhost:9200"        |         | The host name and port for the elasticsearch instance to forward messages to. |
| logstash_plugins                   |  no      |                         |         | A list of objects representing logstash plugins to be installed. The `plugin` key is required, while the `version` key is optional. |


Dependencies
------------

This role requires the support-logstash role.

Example Playbook
----------------

- hosts: logstash
  become: true
  vars:
      logstash_elasticsearch_host: "localhost:9200"
      logstash_plugins:
          - plugin: logstash-input-beats
  roles:
      - role: support-logstash-cfg

License
-------

BSD

Author Information
------------------

Jonathan Strootman
