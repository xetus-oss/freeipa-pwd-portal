
recaptchaPrivateKey = "YOUR_GOOGLE_RECAPTCHA_PRIVATE_KEY_HERE"
recaptchaPublicKey = "YOUR_GOOGLE_RECAPTCHA_PUBLIC_KEY_HERE"

freeipaConfig {
  hostname = "freeipa.example.com"
  realm = "EXAMPLE.COM"
  krb5ConfigPath = "/etc/krb/krb5.conf"
  jaasConfigPath = "/etc/krb/jaas.conf"
}

defaultEmailConfig {
  smtpHost = "smtp.example.com"
  smtpPort = "25"
  fromAddress = "freeipa-pwd-portal@example.com"
}