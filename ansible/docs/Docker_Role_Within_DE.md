## Discovery Environment makes use of Docker in three primary ways:
1. Long-running internal services (UI, notifications, metadata indexing)
1. Configuration management (via data containers)
1. End-user job execution (called via Condor DAG)

Note that the DE group_var file contains a docker.version setting, but it's only used internally for QA. DE seems to function well as each new version of Docker is released.

Docker has historically not played well with firewalls, and while this is improving, the Docker project's tendency to change its network specifications every so often may lead one to consider a hardware firewall or other proxy for sanity's sake. Unless you reconfigure Docker's default behavior in interacting with host-based firewalls, all Dockerized services will be exposed to the world. This is probably Not What You Want(tm).

[Jeekajoo reference](https://fralef.me/docker-and-iptables.html) on customizing iptables for use with Docker.

[StackOverflow reference](http://stackoverflow.com/questions/27512926/how-to-configure-docker-default-bridge-work-with-unusual-network-configuration) on dealing with Docker and certain VPN ranges.
