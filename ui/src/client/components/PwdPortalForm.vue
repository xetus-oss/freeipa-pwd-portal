<template>
  <div class="container">
    <slot name="description">
      <p class="pb-3 text-center">{{ title }}</p>
    </slot>

    <form @submit.stop.prevent="(v) => $emit('submit', v)"
          :class="{ 'was-validated': dirty }">
      <div class="row">
        <div class="col">
          <slot name="left-col"></slot>
        </div>

        <div class="col">
          <slot name="right-col"></slot>
        </div>
      </div>
    </form>

    <b-modal ref="recaptchaModal"
             title="Recaptcha"
             lazy
             busy
             @ok.prevent="() => {}">
      <vue-recaptcha ref="recaptcha"
                     @verify="recaptchaVerify"
                     @expired="recaptchaExpire"
                     :sitekey="recaptchaPublicKey">
      </vue-recaptcha>
      <div slot="modal-footer">
        <button class="btn btn-secondary" 
                @click="resetRecaptcha">Reset ReCAPTCHA</button>
      </div>
    </b-modal>

    <b-modal ref="successModal" 
             title="Success"
             lazy
             ok-only
             ok-variant="success"
             header-text-variant="success"
             @hidden="() => this.$emit('success-closed')">
      <slot name="success-content">
        <p>{{ successTitle }}</p>
      </slot>
    </b-modal>

    <b-modal ref="failureModal"
             title="Failure"
             lazy
             ok-only
             ok-variant="danger"
             header-text-variant="danger"
             @hidden="() => this.$emit('failure-closed')">
      <slot name="failure-content">
        <p>
          <strong>{{ failureTitle }}</strong>
        </p>

        <p class="text-danger">{{ errorMsg }}</p>

        <ul v-if="error && error.data" class="text-danger">
          <li v-for="violation in error.data">{{ violation }}</li>
        </ul>

        <small class="text-muted" v-if="error && error.help">
          {{ error.help }}
        </small>
      </slot>
    </b-modal>
  </div>
</template>

<script>
export default {
  name: 'PwdPortalForm',
  props: {
    title: String,
    dirty: { 
      type: Boolean,
      default: false
    },
    successTitle: String,
    failureTitle: String,
    error: Object,
  },
  computed: {
    recaptchaEnabled: function() {
      return this.$appConfig.recaptchaEnabled;
    },
    recaptchaPublicKey: function() {
      return this.$appConfig.recaptchaPublicKey;
    },
    errorMsg: function() {
      if (this.error) {
        var msg = this.error.message;
        /*
         * If there's error data, add a colon to the message
         * since we'll display the data after the message.
         */
        if (this.error && this.error.data) {
          msg += ":";
        }
      }
      return msg;
    }
  },
  methods: {
    recaptchaVerify(recaptchaResponse) {
      this.$refs.recaptchaModal.hide();
      this.$emit('recpatcha-submit', recaptchaResponse);
    },
    recaptchaExpire() { 
      this.$refs.recaptchaModal.hide();
      this.fail({}, "Recaptcha timed out")
    },
    resetRecaptcha() {
      this.$refs.recaptcha.reset()
    },
    showRecaptcha() {
      if (this.recaptchaEnabled) {
        this.$refs.recaptchaModal.show();
      } else {
        this.recaptchaVerify(null);
      }
    },
    fail() {
      this.$refs.failureModal.show();
    },
    succeed() {
      this.$refs.successModal.show();
    }
  }
};
</script>