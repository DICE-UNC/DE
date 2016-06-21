support-logstash
================

Installs logstash on a remote host.

Requirements
------------

Requires become.

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
| logstash_cfg_template_glob         |  no      |                         |         | Optionally specify a glob pattern to a directory containing template config files.[1] |
| logstash_cfg_file_glob             |  no      |                         |         | Optionally specify a glob pattern to a directory containing static config files.[1] |
| logstash_plugins                   |  no      |                         |         | A list of objects representing logstash plugins to be installed. The `plugin` key is required, while the `version` key is optional. |

[1] - http://docs.ansible.com/ansible/playbooks_loops.html#id4

Dependencies
------------

There are no external dependencies at this time.

License
-------

BSD

Author Information
------------------

Jonathan Strootman
