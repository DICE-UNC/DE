# VERSION 3
#
# IPC specific AMQP rules

# Sends a message to a given AMQP topic exchange
#
# PARAMETERS:
#   *Host - The host of the AMQP broker
#   *Port - The port the AMQP broker listens on
#   *User - The user to authenticate as
#   *Password - The password used to authenticate
#   *Ephemeral - Should the exchange be torn down when not in use.
#   *Key - The topic of the message
#   *Msg - The message to send
#
ipc_amqpSend(*Host, *Port, *User, *Password, *Ephemeral, *Key, *Msg) {
  *hostArg = execCmdArg(*Host);
  *userArg = execCmdArg(*User);
  *passwordArg = execCmdArg(*Password);
  *keyArg = execCmdArg(*Key);
  *msgArg = execCmdArg(*Msg);
  msiExecCmd('amqptopicsend.py', '*hostArg *Port *userArg *passwordArg *Ephemeral *keyArg *msgArg', 
      ipc_RE_HOST, 'null', 'null', *out);
}

