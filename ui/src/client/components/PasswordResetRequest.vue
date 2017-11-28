<template>
  <pwd-portal-form ref="formContainer"
                   :dirty="dirty"
                   :error="error"
                   failureTitle="Password reset request failed"
                   @submit="preSubmit"
                   @recpatcha-submit="submit"
                   @success-closed="() => $router.push('/')">
    <p class="pb-3" slot="description">
      You can use this form to initiate an automated password reset.
      You will need access to the email address associated with your 
      LDAP account to complete the reset process.
    </p>

    <div slot="left-col">
      <div class="mb small-centered">
        <label for="name">LDAP Name</label>
        <input type="text" 
                ref="nameInput"
                placeholder="LDAP name" 
                class="form-control"
                required
                v-model="name" />
        <div class="invalid-feedback" v-if="!error">Please enter your LDAP name</div>
      </div>
    </div>

    <div slot="right-col">
      <label>&nbsp;</label> <!-- hack for exact veritcal whitespace -->
      <div class="text-center justify-content-center samll-centered">
        <button role="button"
                type="submit"
                class="btn btn-secondary"
                :class="{ 'btn-danger': !!error }"
                :disabled="invalid">Reset Password</button>
        <small class="pt-2 form-text text-danger" v-if="error && error.message">
          {{ error.message }}
        </small>
      </div>
    </div>

    <div slot="success-content">
      <p>Your password reset request was received.</p>

      <p>
        Please check your email for the reset link and token you 
        will need to use to complete the reset process.
      </p>
    </div>
  </pwd-portal-form>
</template>

<script>
export default {
  name: 'PasswordResetRequest',
  data () {
    return {
      name:  '',
      dirty: false,
      error: null
    };
  },
  computed: {
    invalid: function() {
      return !this.name;
    }
  },
  methods: {
    _failure(result) {
      if (result) {
        let status = result.status;
        this.error = status == 401 || status == 403 ||
                     !result.response ? 
          "invalid credentials" : result.response.data;
        this.$refs.formContainer.fail();
      }
    },
    _success(result) {
      this.$refs.formContainer.succeed();
    },
    preSubmit() {
      this.dirty = true;
      this.error = null;
      this.$refs.formContainer.showRecaptcha();
    },
    submit(recaptchaResponse) {
      var namePath = encodeURIComponent(this.name.replace(" ", ""));
      this.axios.post('/api/password/' + namePath + '/reset/request', {
                  recaptchaResponse: recaptchaResponse
                })
                .then(this._success)
                .catch(this._failure);
    }
  }
};
</script>