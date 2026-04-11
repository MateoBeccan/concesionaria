<template>
  <div class="login-container">
    <div class="login-card">
      <!-- HEADER -->
      <div class="login-header">
        <h2>Bienvenido</h2>
        <p>Iniciá sesión en AutoGestión</p>
      </div>

      <!-- ERROR -->
      <div v-if="authenticationError" class="alert alert-danger">Usuario o contraseña incorrectos</div>

      <!-- FORM -->
      <form @submit.prevent="doLogin">
        <!-- USER -->
        <div class="form-group">
          <label>Usuario</label>
          <input type="text" v-model="login" class="form-control" placeholder="Ingrese su usuario" required />
        </div>

        <!-- PASSWORD -->
        <div class="form-group">
          <label>Contraseña</label>
          <input type="password" v-model="password" class="form-control" placeholder="Ingrese su contraseña" required />
        </div>

        <!-- REMEMBER -->
        <div class="form-check mb-3">
          <input type="checkbox" v-model="rememberMe" class="form-check-input" id="rememberMe" />
          <label class="form-check-label" for="rememberMe"> Recordarme </label>
        </div>

        <!-- BUTTON -->
        <button type="submit" class="btn btn-primary w-100" :disabled="loading">
          <span v-if="loading">Ingresando...</span>
          <span v-else>Iniciar sesión</span>
        </button>
      </form>

      <!-- FOOTER -->
      <div class="login-footer">
        <router-link to="/account/reset/request"> ¿Olvidaste tu contraseña? </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, inject } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import type AccountService from '@/account/account.service';

export default defineComponent({
  setup() {
    const login = ref('');
    const password = ref('');
    const rememberMe = ref(false);
    const authenticationError = ref(false);
    const loading = ref(false);

    const route = useRoute();
    const router = useRouter();
    const accountService = inject<AccountService>('accountService');

    const doLogin = async () => {
      loading.value = true;
      authenticationError.value = false;

      try {
        const response = await axios.post('api/authenticate', {
          username: login.value,
          password: password.value,
          rememberMe: rememberMe.value,
        });

        const bearer = response.headers.authorization;

        if (bearer?.startsWith('Bearer ')) {
          const token = bearer.substring(7);

          if (rememberMe.value) {
            localStorage.setItem('jhi-authenticationToken', token);
            sessionStorage.removeItem('jhi-authenticationToken');
          } else {
            sessionStorage.setItem('jhi-authenticationToken', token);
            localStorage.removeItem('jhi-authenticationToken');
          }
        }

        await accountService.retrieveAccount();

        // 🔥 REDIRECCIÓN PRO
        const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null;
        await router.replace(redirect ?? '/');
      } catch {
        authenticationError.value = true;
      } finally {
        loading.value = false;
      }
    };

    return {
      login,
      password,
      rememberMe,
      authenticationError,
      loading,
      doLogin,
    };
  },
});
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1e293b, #0f172a);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.login-header h2 {
  font-weight: bold;
}

.login-header p {
  color: #6c757d;
}

.form-group {
  margin-bottom: 1rem;
}

.login-footer {
  margin-top: 1rem;
  text-align: center;
  font-size: 0.9rem;
}
</style>
