#!/bin/bash

server="freeipa.local.xetus.com"
realm="LOCAL.XETUS.COM"
config="krb5.conf"

usage() 
{
  echo "usage: create-krb5-conf [-s server] [-r realm] [-c krb5.conf]"
  echo
  echo " -s | --server      the FreeIPA server"
  echo " -r | --realm       the realm to which the password portal should be added"
  echo " -c | --config      the path where the krb5 config should be created"
  echo " -h | --help        show this helpful help message and exit"
  echo  
}

[[ "$1" == *-h* ]] && usage && exit

while [ "$2" != "" ]; do
  case $1 in
    -r | --realm )            shift
                              realm=$1
                              ;;
    -s | --server )           shift
                              server=$1
                              ;;
    -c | --config )           shift
                              config=$1
                              ;;
    -h | --help )             usage
                              exit
                              ;;
    * )                       usage
                              exit 1
  esac
  shift
done

cat << EOF > "$config"
[libdefaults]
  default_realm = $realm
   dns_lookup_realm = true
   dns_lookup_kdc = true
   ticket_lifetime = 24h
   forwardable = true

[realms]
  $realm = {
    kdc = $server
    admin_server = $server
  }
EOF

if [[ "$?" -ne 0 ]]; then
  echo "Failed to write krb5 config to $config"
else
  echo "Wrote krb5 config to $config"
fi