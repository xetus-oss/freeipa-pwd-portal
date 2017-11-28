<template>
  <pwd-portal-form ref="formContainer"
                   :dirty="dirty"
                   :error="error"
                   successTitle="Your password was successfully reset."
                   failureTitle="Password reset request failed"
                   @submit="preSubmit"
                   @recpatcha-submit="submit"
                   @success-closed="() => $router.push('/')">
    <p class="pb-3" slot="description">
      Use the form below to reset your FreeIPA password. You
      will need to use your LDAP name and the reset token included
      in your reset email to complete this form.
    </p>

    <div slot="left-col">
      <div class="form-group small-centered">
        <label for="name">LDAP Name</label>
        <input type="text" 
                name="name"
                ref="nameInput"
                placeholder="LDAP name" 
                class="form-control"
                required
                v-model="name" />
        <div class="invalid-feedback">Please enter your LDAP name</div>
      </div>
      <div class="form-group small-centered">
        <label for="name">Reset Token</label>
        <input type="text" 
                name="name"
                ref="nameInput"
                placeholder="Reset token" 
                class="form-control"
                required
                v-model="token" />
        <div class="invalid-feedback">Please enter password reset token</div>
      </div>
      <new-password-input v-model="newPassword"
                          @input="v => dirty = true"
                          @validation="v => newInvalid = v">
      </new-password-input>
    
    </div> <!-- end col -->
  
    <div slot="right-col">
      <label>&nbsp;</label> <!-- hack for exact veritcal whitespace -->
      <div class="text-center justify-content-center small-centered">
        <button role="button"
                type="submit"
                class="btn btn-secondary"
                :class="{ 'btn-danger': !!error }"
                :disabled="invalid">Reset Password</button>
        <small class="pt-2 form-text text-danger" v-if="error">
          {{ errorMsg }}
        </small>
        <small class="pt-2 form-text text-danger" v-if="error && error.data">
          <ul>
            <li v-for="violation in error.data">{{ violation }}</li>
          </ul>
        </small>
      </div>
    </div>
  </pwd-portal-form>
</template>

<script>
export default {
  name: 'PasswordReset',
  data() {
    return {
      name:  '',
      token: '',
      newPassword: '',
      newInvalid: true,
      dirty: false,
      error: null
    };
  },
  computed: {
    invalid: function() {
      return this.newInvalid || !this.name
    },
    errorMsg: function() {
      if (this.error) {
        var msg = this.error.message;
        /*
         * If there's error data, add a colon to the message
         * since we'll display the data after the message.
         */
        if (this.error.data) {
          msg += ":";
        }
      }
      return msg;
    }
  },
  methods: {
    _failure(result) {
      if (result) {
        let status = result.status;
        this.error = status == 401 || status == 403 || 
                     !result.response ? 
          "invalid token or LDAP name" : result.response.data;
        this.newPassword = ''
        this.$refs.formContainer.fail();
      }
    },
    _success() {
      this.$refs.formContainer.succeed();
    },
    preSubmit() {
      this.dirty = true;
      this.error = null;
      this.$refs.formContainer.showRecaptcha();
    },
    submit(recaptchaResponse) {
      var namePath = encodeURIComponent(this.name.replace(" ", ""));
      this.axios.post('/api/password/' + this.name + '/reset', {
          user: this.name,
          password: this.newPassword,
          token: this.token,
          resetId: this.$route.params['id'],
          recaptchaResponse: recaptchaResponse
        })
        .then(this._success)
        .catch(this._failure)
    }
  }
};
</script>