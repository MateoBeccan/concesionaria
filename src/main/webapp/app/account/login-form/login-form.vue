<template>
  <div class="login-shell">
    <div class="login-panel">
      <section class="login-aside">
        <div class="brand-badge">AutoGestión</div>
        <h1 class="login-title">Ingresá a tu concesionaria desde un solo lugar</h1>
        <p class="login-copy">Gestioná clientes, vehículos, cotizaciones y ventas con una experiencia simple, segura y profesional.</p>

        <div class="login-highlights">
          <div class="highlight-item">
            <span class="highlight-dot" />
            <span>Acceso rápido al dashboard comercial</span>
          </div>
          <div class="highlight-item">
            <span class="highlight-dot" />
            <span>Seguimiento centralizado de operaciones</span>
          </div>
          <div class="highlight-item">
            <span class="highlight-dot" />
            <span>Sesión segura compatible con tu backend actual</span>
          </div>
        </div>
      </section>

      <section class="login-card">
        <div class="login-card-header">
          <p class="eyebrow">Acceso</p>
          <h2>Iniciar sesión</h2>
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
              type="email"
              autocomplete="username"
              class="form-control form-control-lg"
              :class="{ 'is-invalid': loginError }"
              placeholder="nombre@empresa.com"
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

          <div class="login-form-footer">
            <div class="form-check">
              <input id="rememberMe" v-model="rememberMe" type="checkbox" class="form-check-input" />
              <label class="form-check-label" for="rememberMe">Mantener sesión iniciada</label>
            </div>
          </div>

          <button type="submit" class="btn btn-primary btn-lg w-100 login-submit" :disabled="isSubmitDisabled">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2" aria-hidden="true" />
            {{ loading ? 'Ingresando...' : 'Ingresar' }}
          </button>
        </form>
      </section>
    </div>
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

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    const loginError = computed(() => {
      if (!touched.login && login.value.length === 0) return '';
      if (!login.value.trim()) return 'Ingresá tu usuario o email.';
      if (!emailPattern.test(login.value.trim())) return 'Ingresá un email válido.';
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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.25rem;
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.18), transparent 35%),
    linear-gradient(135deg, #eef4ff 0%, #f8fafc 55%, #e8eef8 100%);
}

.login-panel {
  width: min(1040px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(14px);
}

.login-aside {
  padding: 3rem;
  background: linear-gradient(160deg, #0f172a 0%, #16233c 55%, #1d4ed8 100%);
  color: #f8fafc;
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.45rem 0.8rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.login-title {
  margin: 1.5rem 0 0.9rem;
  font-size: clamp(2rem, 4vw, 3rem);
  line-height: 1.05;
  font-weight: 800;
}

.login-copy {
  max-width: 32rem;
  margin: 0;
  color: rgba(226, 232, 240, 0.9);
  font-size: 1rem;
  line-height: 1.7;
}

.login-highlights {
  margin-top: 2rem;
  display: grid;
  gap: 0.9rem;
}

.highlight-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: rgba(241, 245, 249, 0.92);
  font-size: 0.95rem;
}

.highlight-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #60a5fa;
  box-shadow: 0 0 0 6px rgba(96, 165, 250, 0.18);
}

.login-card {
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.94);
}

.login-card-header {
  margin-bottom: 1.75rem;
}

.eyebrow {
  margin-bottom: 0.5rem;
  color: #2563eb;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.login-card-header h2 {
  margin: 0;
  font-size: 1.9rem;
  font-weight: 800;
  color: #0f172a;
}

.login-card-copy {
  margin: 0.5rem 0 0;
  color: #64748b;
}

.login-alert {
  margin-bottom: 1.25rem;
  border-radius: 14px;
}

.login-form {
  display: grid;
  gap: 1.15rem;
}

.field-group {
  display: grid;
  gap: 0.45rem;
}

.form-label {
  margin: 0;
  font-size: 0.92rem;
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
  font-size: 0.88rem;
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
  font-size: 0.85rem;
  font-weight: 700;
  padding: 0.2rem 0.35rem;
}

.login-form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.login-submit {
  min-height: 3.3rem;
  border-radius: 14px;
  font-weight: 700;
}

.form-control-lg {
  min-height: 3.25rem;
  border-radius: 14px;
  border-color: #cbd5e1;
}

.form-control-lg:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 0.2rem rgba(37, 99, 235, 0.14);
}

@media (max-width: 991.98px) {
  .login-panel {
    grid-template-columns: 1fr;
  }

  .login-aside,
  .login-card {
    padding: 2rem;
  }
}

@media (max-width: 575.98px) {
  .login-shell {
    padding: 1rem;
  }

  .login-panel {
    border-radius: 22px;
  }

  .login-aside,
  .login-card {
    padding: 1.4rem;
  }

  .password-label-row,
  .login-form-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .login-title {
    font-size: 1.9rem;
  }
}
</style>
