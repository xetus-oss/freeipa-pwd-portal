<template>
  <div id="app" class="container">
    <div class="text-center">
      <h3>FreeIPA Password Portal</h3>
      <p class="font-italic text-secondary">
        A self-service password portal for FreeIPA.
      </p>
    </div>

    <ul class="nav justify-content-center">
      <li class="nav-item">
        <router-link class="nav-link" to="/" exact>Home</router-link>
      </li>
      <li class="nav-item">
        <router-link class="nav-link" to="/change">Change</router-link>
      </li>
      <li class="nav-item">
        <router-link class="nav-link" to="/reset">Reset</router-link>
      </li>
    </ul>

    <hr class="pb-3" />

    <router-view />

    <b-modal ref="cxnFailureModal"
             title="Connection Failure"
             header-text-variant="danger"
             ok-only
             ok-variant="danger"
             @hidden="$router.push('/')">

      <div class="alert alert-danger">
        <strong>Drat!</strong>
        The request failed to find the FreeIPA Password Portal server.
      </div>
      
      <p>
        Please reach out to your local password portal administrator or try 
        again later
      </p>
    </b-modal>
  </div>
</template>

<script>
export default {
  name: 'app',
  mounted: function() {
    this.axios.interceptors.response.use((response) => {
      // see https://github.com/axios/axios/issues/383
      if (!response.status || response.status == 404) {
        console.log("intercepting XHR error", response);
        this.$refs.cxnFailureModal.show();
        return Promise.reject();
      }
      return response;
    });
  }
}
</script>

<style>
#app {
  max-width: 600px;
  margin-top: 10px;
}
</style>
