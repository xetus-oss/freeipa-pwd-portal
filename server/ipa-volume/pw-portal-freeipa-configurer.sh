#!/bin/bash

server="freeipa.local.xetus.com"
realm="LOCAL.XETUS.COM"
truststore="/root/pw-portal/config/pw-portal.keystore"
krb5_config="/root/pw-portal/config/krb5.conf"
keytab="/root/pw-portal/config/pw-portal.local.xetus.com.keytab"
host_name="pw-portal.local.xetus.com"
host_ip="192.168.51.1"
dn="dc=local,dc=xetus,dc=com"


usage() 
{
  echo "usage: pw-portal-freeipa-configurer [-s server] [-r realm] [-h host_name] [-ip host_ip] [-t truststore] [-krb krb5.conf] [-k keytab] admin_pass"
  echo
  echo " -s | --server      the FreeIPA server"
  echo " -r | --realm       the realm to which the password portal should be added"
  echo " -h | --host_name   the host name for the passord portal"
  echo " -p | --host_ip     the password portal's host ip"
  echo " -t | --truststore  the output path for the generated java truststore"
  echo " -krb | --krb5-conf the output path for the generated krb5.conf"
  echo " -dn                the LDAP dn for the FreeIPA instance"
  echo " -h | --help        show this helpful help message and exit"
  echo  
}

[[ "$1" == *-h* ]] && usage && exit

while [ "$2" != "" ]; do
  case $1 in
    -r | --realm )          shift
                            realm=$1
                            ;;
    -s | --server )         shift
                            server=$1
                            ;;
    -h | --hostname )       shift
                            host_name=$1
                            ;;
    -ip | --host-ip )       shift
                            host_ip=$1
                            ;;
    -t | --truststore )     shift
                            truststore=$1
                            ;;
    -k | --keytab )         shift
                            keytab=$1
                            ;;
    -krb | --krb5-conf )    shift
                            krb5_config=$1
                            ;;
    -dn )                   shift
                            realm=$1
                            ;;
    -h | --help )           usage
                            exit 1
                            ;;
    * )                     usage
                            exit 1
  esac
  shift
done

admin_pass="$1"

[[ -z "$admin_pass" ]] && usage && exit $?

echo "Performing kinit as admin..."
echo 
echo "$admin_pass" | kinit admin

[[ "$?" -ne 0 ]] && \
  echo "" && \
  echo "failed to kinit as admin; is your password correct?" && \
  echo "" && \
  exit $?

#
# Create the host with the required role and service
#
echo
echo "Creating the pw-portal host, with requisite role and service..."
echo
ipa host-add --ip-address $host_ip $host_name
ipa role-add-member --hosts $host_name "User Administrator"
ipa service-add HTTP/$host_name@$realm

#
# create the service keytab
#
echo
echo "Generating the keytab for the host with required principals..."
echo
rm $keytab
ipa-getkeytab -s $server -p host/$host_name@$realm -k $keytab
ipa-getkeytab -s $server -p HTTP/$host_name@$realm -k $keytab
chmod 644 $keytab

#
# add the ability to modify admin user accounts
#
echo
echo "Adding a custom group for the password portal that allows "
echo "for resetting administrator passwords..."
echo
ipa hostgroup-add --desc="Self-service password reset portals" pw-reset-portal
ipa hostgroup-add-member --hosts=$host_name pw-reset-portal

cat << EOF > pw-reset-portal.hostgroup.ldif
# Add the ability to change passwords for all accounts (including) admins
# using this host account
dn: $dn
changetype: modify
add: aci
aci: (targetattr = "userPassword || krbPrincipalKey || sambaLMPassword || sambaNTPassword || passwordHistory || ipaNTHash")(version 3.0; acl "PWD Portal can write passwords"; allow (add,delete,write) groupdn="ldap:///cn=pw-reset-portal,cn=hostgroups,cn=accounts,$dn";)
EOF

echo "$admin_pass" | ldapmodify -h $server -x -W \
                                -p 389 \
                                -D "cn=Directory Manager" \
                                -f pw-reset-portal.hostgroup.ldif

script_prefix="."
[[ ! -f create-freeipa-truststore.sh ]] && script_prefix="${BASH_SOURCE%/*}"

bash "$script_prefix"/create-freeipa-truststore.sh -s $server -t $truststore --rm

bash "$script_prefix"/create-krb5-conf.sh -s $server -r $realm -c $krb5_config

echo
echo "Done!"
echo