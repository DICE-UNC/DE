support-filebeat
================

Installs and configures a Filebeat instance.

https://www.elastic.co/guide/en/beats/filebeat/current/index.html

Requirements
------------

Ansible 2.x
Requires sudo.

Role Variables
--------------

|   Variable                         | required | default                 | choices | comments                                               |
|------------------------------------|----------|-------------------------|---------|--------------------------------------------------------|
| filebeat_version                   |  no      |                         |         | When defined, will install the specified version of Filebeat, otherwise, the most current will be installed. |
| filebeat_install                   |  no      | true                    |         | A flag used to control whether the role should perform installation steps. |
| filebeat_config                    |  no      |                         |         | If defined, is used to populate the filebeat config file. If undefined, the config file is unchanged. |


Dependencies
------------

N/A

Example Playbook
----------------

    - hosts: systems
      vars:
      roles:
         - { role: username.rolename, x: 42 }

License
-------

BSD

Author Information
------------------

Jonathan Strootman "jstroot@cyverse.org"
