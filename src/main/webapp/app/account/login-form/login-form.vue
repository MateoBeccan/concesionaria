<template>
  <div class="login-shell">
    <section class="login-panel">
      <div class="login-intro">

        <p class="intro-eyebrow">Acceso seguro</p>
        <h1 class="intro-title">Gestión comercial de concesionaria</h1>
        <p class="intro-copy">
          Ingresá para operar ventas, inventario, clientes y comprobantes desde una plataforma centralizada.
        </p>
      </div>

      <div class="login-card">
        <div class="login-card-header">
          <p class="eyebrow">Inicio de sesión</p>
          <h2>Bienvenido</h2>
          <p class="login-card-copy">Usá tus credenciales para continuar.</p>
        </div>

        <div v-if="backendError" class="alert alert-danger login-alert" role="alert">
          {{ backendError }}
        </div>

        <form class="login-form" novalidate @submit.prevent="doLogin">
          <div class="field-group">
            <label for="username" class="form-label">Usuario o email</label>
            <input
              id="username"
              v-model.trim="login"
              type="text"
              autocomplete="username"
              class="form-control form-control-lg"
              :class="{ 'is-invalid': loginError }"
              placeholder="Ingresá tu usuario o email"
              @blur="touched.login = true"
            />
            <div v-if="loginError" class="invalid-feedback d-block">{{ loginError }}</div>
          </div>

          <div class="field-group">
            <div class="password-label-row">
              <label for="password" class="form-label">Contraseña</label>
              <router-link class="forgot-link" to="/account/reset/request">Olvidé mi contraseña</router-link>
            </div>

            <div class="password-input-wrap">
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                class="form-control form-control-lg password-input"
                :class="{ 'is-invalid': passwordError }"
                placeholder="Ingresá tu contraseña"
                @blur="touched.password = true"
              />
              <button type="button" class="password-toggle" @click="togglePassword">
                {{ showPassword ? 'Ocultar' : 'Mostrar' }}
              </button>
            </div>

            <div v-if="passwordError" class="invalid-feedback d-block">{{ passwordError }}</div>
          </div>

          <button type="submit" class="btn btn-primary btn-lg w-100 login-submit" :disabled="isSubmitDisabled">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2" aria-hidden="true" />
            {{ loading ? 'Ingresando...' : 'Ingresar' }}
          </button>
        </form>
      </div>
    </section>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, inject, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import type AccountService from '@/account/account.service';

export default defineComponent({
  name: 'LoginForm',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const accountService = inject<AccountService>('accountService');

    const login = ref('');
    const password = ref('');
    const rememberMe = ref(false);
    const loading = ref(false);
    const showPassword = ref(false);
    const backendError = ref('');

    const touched = reactive({
      login: false,
      password: false,
    });

    const loginError = computed(() => {
      if (!touched.login && login.value.length === 0) return '';
      if (!login.value.trim()) return 'Ingresá tu usuario o email.';
      return '';
    });

    const passwordError = computed(() => {
      if (!touched.password && password.value.length === 0) return '';
      if (!password.value) return 'Ingresá tu contraseña.';
      if (password.value.length < 4) return 'La contraseña debe tener al menos 4 caracteres.';
      return '';
    });

    const isFormValid = computed(() => !loginError.value && !passwordError.value && !!login.value.trim() && !!password.value);
    const isSubmitDisabled = computed(() => loading.value || !isFormValid.value);

    const togglePassword = () => {
      showPassword.value = !showPassword.value;
    };

    const doLogin = async () => {
      touched.login = true;
      touched.password = true;
      backendError.value = '';

      if (!isFormValid.value || !accountService) {
        if (!accountService) backendError.value = 'No se pudo inicializar el servicio de autenticación.';
        return;
      }

      loading.value = true;

      try {
        const response = await axios.post('api/authenticate', {
          username: login.value.trim(),
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

        const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null;
        await router.replace(redirect ?? '/');
      } catch (error: any) {
        const status = error?.response?.status;
        if (status === 401) {
          backendError.value = 'Las credenciales ingresadas no son correctas.';
        } else {
          backendError.value = error?.response?.data?.message ?? 'No pudimos iniciar sesión. Intentá nuevamente.';
        }
      } finally {
        loading.value = false;
      }
    };

    return {
      backendError,
      doLogin,
      isSubmitDisabled,
      loading,
      login,
      loginError,
      password,
      passwordError,
      rememberMe,
      showPassword,
      togglePassword,
      touched,
    };
  },
});
</script>

<style scoped>
.login-shell {
  width: 100%;
  display: flex;
  justify-content: center;
}

.login-panel {
  width: min(820px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(340px, 390px);
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 22px 56px rgba(15, 23, 42, 0.1);
}

.login-intro {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background:
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.14), transparent 28%),
    linear-gradient(155deg, #0f172a 0%, #16253f 70%, #1e40af 100%);
  color: #eff6ff;
}

.intro-brand {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  width: fit-content;
  margin-bottom: 1rem;
  padding: 0.34rem 0.62rem;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.42);
  border: 1px solid rgba(186, 230, 253, 0.24);
}

.intro-brand-kicker {
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #e2e8f0;
}

.intro-brand-pill {
  padding: 0.14rem 0.42rem;
  border-radius: 999px;
  background: rgba(56, 189, 248, 0.18);
  color: #bae6fd;
  font-size: 0.66rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.intro-eyebrow,
.eyebrow {
  margin: 0 0 0.45rem;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.intro-eyebrow {
  color: #93c5fd;
}

.intro-title {
  margin: 0;
  font-size: clamp(1.75rem, 3vw, 2.25rem);
  line-height: 1.08;
  font-weight: 800;
}

.intro-copy {
  margin: 0.85rem 0 0;
  max-width: 28rem;
  color: rgba(226, 232, 240, 0.9);
  line-height: 1.6;
  font-size: 0.94rem;
}

.login-card {
  padding: 2rem 1.9rem;
  background: rgba(255, 255, 255, 0.98);
}

.eyebrow {
  color: #0284c7;
}

.login-card-header h2 {
  margin: 0;
  font-size: 1.65rem;
  font-weight: 800;
  color: #0f172a;
}

.login-card-copy {
  margin: 0.4rem 0 0;
  color: #64748b;
  font-size: 0.9rem;
}

.login-alert {
  margin: 0.9rem 0 1rem;
  border-radius: 12px;
}

.login-form {
  display: grid;
  gap: 1rem;
}

.field-group {
  display: grid;
  gap: 0.45rem;
}

.form-label {
  margin: 0;
  font-size: 0.88rem;
  font-weight: 600;
  color: #0f172a;
}

.password-label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.forgot-link {
  color: #2563eb;
  font-size: 0.8rem;
  font-weight: 600;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.password-input-wrap {
  position: relative;
}

.password-input {
  padding-right: 5.5rem;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 0.75rem;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: #2563eb;
  font-size: 0.8rem;
  font-weight: 700;
  padding: 0.2rem 0.35rem;
}

.login-submit {
  min-height: 3.05rem;
  border-radius: 12px;
  font-weight: 700;
}

.form-control-lg {
  min-height: 3.05rem;
  border-radius: 12px;
  border-color: #cbd5e1;
}

.form-control-lg:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 0.2rem rgba(37, 99, 235, 0.14);
}

@media (max-width: 960px) {
  .login-panel {
    grid-template-columns: 1fr;
  }

  .login-intro,
  .login-card {
    padding: 1.5rem;
  }
}

@media (max-width: 640px) {
  .password-label-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .login-intro,
  .login-card {
    padding: 1.2rem;
  }
}
</style>
