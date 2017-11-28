# Troubleshooting

The FreeIPA Password Portal by definition has _a lot_ of complicated 
parts. This document is intended to help troubleshoot various problems
you migt encounter while trying to connect the FreeIPA Password Portal
to a FreeIPA instance (even just the docker-compsoe one!)

## Kerberos

There are a fewrelevant components to the Kerberos authentication:

1. the keytab and contained principal;
2. the KDC configuraiton in the krb5.conf;
3. network access from the password portal to the FreeIPA server (KDC); and
4. properly configuring your `application.yml`.

To verify you have these setup properly, you can use the following from
a system _with the same network configuration_ as the password portal
that has `kinit` installed. All this troubleshooting starts with an
attempted init:

```bash
#
# Modify the below variables with the appropriate paths
#
KRB5_PATH=server/config/krb5.conf
KEYTAB_PATH=server/config/pw-portal.local.xetus.com.keytab
KEYTAB_PRINCIPAL="host/pw-portal.local.xetus.com@LOCAL.XETUS.COM"

#
# This will attempt to authenticate with the KDC configured in
# KRB5_PATH using the KEYTAB_PRINCIPAL account that should be saved 
# in the keytab at KEYTAB_PATH.
#
KRB5_CONFIG="$KRB5_PATH" kinit -kt "$KEYTAB_PATH" "$KEYTAB_PRINCIPAL"
```

If the above command executed without issue, run `klist` to verify 
you are authenticated with `KEYTAB_PRINCIPAL`. If you are, run `kdestroy`
to logout; the parameters you used above work! Apply them your 
`application.yaml`:

|variable|yaml value|
|--------|----------|
|KRB5_PATH|iris.krb5ConfPath|
|KEYTAB_PATH|iris.keytabPath|
|KEYTAB_PRINCIPAL|iris.principal|

#### Known Behaviors

``FreeIPA server responds to Kerberos requests with a 500 error``

If the FreeIPA server responds with a 500 server error to Kerberos 
requests (e.g. a password reset), check the FreeIPA server's 
`/var/log/httpd/error_log` for the error.

* "KRB5CCNAME not defined"

If you see n error like the following:

```
ipa: ERROR: 500 Internal Server Error: KerberosWSGIExecutioner.__call__: KRB5CCNAME not defined in HTTP request environment
```

It most likely means that you don't have `forwardable=true` in your
`krb5.conf` file. See https://pagure.io/freeipa/issue/4745.