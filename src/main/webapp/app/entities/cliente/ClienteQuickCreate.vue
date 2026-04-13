<template>
  <div class="modal-backdrop-custom" @click.self="emit('cerrar')">
    <div class="modal-dialog-custom shadow-lg rounded-3 bg-white p-4" style="width: 100%; max-width: 520px">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="mb-0 fw-semibold">Nuevo cliente</h5>
        <button class="btn-close" @click="emit('cerrar')" />
      </div>

      <form @submit.prevent="guardar" novalidate>
        <div class="row g-3">
          <div class="col-6">
            <label class="form-label">Nombre <span class="text-danger">*</span></label>
            <input v-model.trim="form.nombre" class="form-control" :class="{ 'is-invalid': errores.nombre }" />
            <div class="invalid-feedback">{{ errores.nombre }}</div>
          </div>

          <div class="col-6">
            <label class="form-label">Apellido <span class="text-danger">*</span></label>
            <input v-model.trim="form.apellido" class="form-control" :class="{ 'is-invalid': errores.apellido }" />
            <div class="invalid-feedback">{{ errores.apellido }}</div>
          </div>

          <div class="col-6">
            <label class="form-label">Nro. Documento <span class="text-danger">*</span></label>
            <input
              v-model.trim="form.nroDocumento"
              class="form-control"
              inputmode="numeric"
              :class="{ 'is-invalid': errores.nroDocumento }"
            />
            <div class="invalid-feedback">{{ errores.nroDocumento }}</div>
          </div>

          <div class="col-6">
            <label class="form-label">Teléfono</label>
            <input v-model.trim="form.telefono" class="form-control" :class="{ 'is-invalid': !!errorTelefono }" />
            <div v-if="errorTelefono" class="invalid-feedback d-block">{{ errorTelefono }}</div>
          </div>

          <div class="col-12">
            <label class="form-label">Email <span class="text-danger">*</span></label>
            <input v-model.trim="form.email" type="email" class="form-control" :class="{ 'is-invalid': errores.email }" />
            <div class="invalid-feedback">{{ errores.email }}</div>
          </div>

          <div v-if="errorServidor" class="col-12">
            <div class="alert alert-danger py-2 mb-0">{{ errorServidor }}</div>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" class="btn btn-outline-secondary" @click="emit('cerrar')">Cancelar</button>
          <button type="submit" class="btn btn-primary" :disabled="guardando">
            <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
            Guardar cliente
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from 'vue';
import ClienteService from '@/entities/cliente/cliente.service';
import type { ICliente } from '@/shared/model/cliente.model';

const DOCUMENTO_REGEX = /^\d{7,11}$/;
const TELEFONO_REGEX = /^[0-9+\-\s]{6,20}$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const emit = defineEmits<{ guardado: [cliente: ICliente]; cerrar: [] }>();

const service = new ClienteService();

const form = reactive({
  nombre: '',
  apellido: '',
  nroDocumento: '',
  telefono: '',
  email: '',
  activo: true,
  fechaAlta: new Date(),
});

const errores = reactive({ nombre: '', apellido: '', nroDocumento: '', email: '' });
const errorServidor = ref<string | null>(null);
const guardando = ref(false);

const errorTelefono = computed(() => (form.telefono && !TELEFONO_REGEX.test(form.telefono.trim()) ? 'Teléfono inválido' : ''));

function validar(): boolean {
  errores.nombre = form.nombre.trim() ? '' : 'El nombre es requerido';
  errores.apellido = form.apellido.trim() ? '' : 'El apellido es requerido';
  errores.nroDocumento = DOCUMENTO_REGEX.test(form.nroDocumento.trim()) ? '' : 'El documento debe tener entre 7 y 11 dígitos';
  errores.email = EMAIL_REGEX.test(form.email.trim()) ? '' : 'Email inválido';
  return !Object.values(errores).some(Boolean) && !errorTelefono.value;
}

async function guardar() {
  if (!validar()) return;
  guardando.value = true;
  errorServidor.value = null;
  try {
    const cliente = await service.create({
      ...form,
      nombre: form.nombre.trim(),
      apellido: form.apellido.trim(),
      nroDocumento: form.nroDocumento.trim(),
      telefono: form.telefono.trim() || null,
      email: form.email.trim().toLowerCase(),
    });
    emit('guardado', cliente);
  } catch (e: any) {
    errorServidor.value = e.response?.data?.message ?? 'Error al guardar el cliente';
  } finally {
    guardando.value = false;
  }
}
</script>

<style scoped>
.modal-backdrop-custom {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}
.modal-dialog-custom {
  animation: slideIn 0.2s ease;
}
@keyframes slideIn {
  from {
    transform: translateY(-16px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
