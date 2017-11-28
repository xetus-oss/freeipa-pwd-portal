#!/bin/bash

alias="freeipa"
server="freeipa.local.xetus.com"
pem_path="/tmp/freeipa.local.xetus.com.pem"
remove="false"
truststore="server/config/pw-portal.keystore"

usage() 
{
  echo "usage: create_freeipa_truststore [-a alias] [-s server] [-p pem_path] [-t truststore] [-rm]"
  echo
  echo " -a | --alias       the alias with which the certificate should be stored"
  echo " -s | --server      the FreeIPA server"
  echo " -p | --pem-path    the path to which the certificate should be saved"
  echo " -t | --truststore  the output path for the generated java truststore"
  echo " -h | --help        show this helpful help message and exit"
  echo  
}

[[ "$1" == *-h* ]] && usage && exit

while [ "$1" != "" ]; do
  case $1 in
    -a | --alias)           shift
                            alias_path=$1
                            ;; 
    -p | --pem-path)        shift
                            pem_path=$1
                            ;; 
    -s | --server )         shift
                            server=$1
                            ;;
    -t | --truststore )     shift
                            truststore=$1
                            ;;
    -rm | --remove )        remove="true"
                            ;;
    -h | --help )           usage
                            exit
                            ;;
    * )                     usage
                            exit 1
  esac
  shift
done

if [ "true" = "$remove" ]; then
  echo "Removing $truststore..."
  rm $truststore;
fi

openssl s_client -showcerts \
                 -connect $server:443 \
                 < /dev/null 2> /dev/null \
        | openssl x509 -outform PEM > $pem_path

[[ "$?" -ne 0 ]] && \
    echo "" && \
    echo "failed to download the certificate from $server" && \
    echo "" && \
    exit $?

keytool -import -trustcacerts -noprompt \
        -alias $alias \
        -file $pem_path \
        -keystore $truststore \
        -storepass changeit

[[ "$?" -ne 0 ]] && \
    echo "" && \
    echo "failed to import the certificate into the keystore" && \
    echo "" && \
    exit $?

echo "added cert for $server to $truststore"
echo