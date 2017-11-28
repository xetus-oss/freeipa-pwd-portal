<template>
  <div>
    <div class="form-group small-centered">
      <label for="password">New Password</label>
      <input type="password" 
             name="password"
             placeholder="New password" 
             class="form-control"
             required
             v-model="password"
             @input="validate" />
      <div class="invalid-feedback">
        Please enter your new password
      </div>
    </div>

    <div class="form-group small-centered">
      <label for="confirmation">Confirm Password</label>
      <input type="password" 
             name="confirmation"
             ref="confirmation"
             placeholder="Password confirmation" 
             class="form-control"
             required
             v-model="confirmation"
             @input="update" />
      <div class="invalid-feedback" v-if="passwordsDiff && confirmation">
        Passwords must match
      </div>
      <div class="invalid-feedback" v-else>
        Please confirm your new password
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NewPasswordInput',
  props: ['value'],
  data() {
    return {
      name:  '',
      password: this.value,
      confirmation: ''
    };
  },
  watch: {
    value: function(newVal, oldVal) {
      this.confirmation = newVal;
    }
  },
  computed: {
    passwordsDiff: function() {
      return this.password != this.confirmation;
    },
    invalidConfirmation: function() {
      return !this.confirmation || this.passwordsDiff;
    },
    invalid: function() {
      return (this.invalidConfirmation || !this.password)
    },
    confirmationInput: function() {
      return this.$refs.confirmation
    }
  },
  methods: {
    validate: function() {
      if (this.dirty) {
        this.confirmationInput.setCustomValidity(
          this.invalidConfirmation ? 'invalid' :  ''
        );
      }
      this.$emit('validation', this.invalid);
    },
    update: function(input) {
      this.dirty = true;
      this.validate();
      this.$emit('input', this.confirmation);
    }
  }
};
</script>