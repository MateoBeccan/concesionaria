<template>
  <div class="position-relative">
    <div class="input-group">
      <input
        :value="modelValue"
        @input="onInput"
        :placeholder="placeholder"
        class="form-control"
        :class="{ 'is-invalid': !!error }"
        autocomplete="off"
        :disabled="disabled"
      />
      <span v-if="loading" class="input-group-text bg-white border-start-0">
        <span class="spinner-border spinner-border-sm text-secondary" role="status" />
      </span>
      <span
        v-else-if="modelValue && !disabled"
        class="input-group-text bg-white border-start-0"
        style="cursor:pointer"
        @click="onClear"
      >
        <span class="text-muted" style="font-size:.8rem">✕</span>
      </span>
    </div>
    <div class="invalid-feedback d-block" v-if="error">{{ error }}</div>
  </div>
</template>

<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    modelValue: string;
    placeholder?: string;
    loading?: boolean;
    error?: string | null;
    debounce?: number;
    disabled?: boolean;
  }>(),
  {
    placeholder: 'Buscar...',
    loading: false,
    error: null,
    debounce: 350,
    disabled: false,
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: string];
  search: [value: string];
  clear: [];
}>();

let timer: ReturnType<typeof setTimeout> | null = null;

function onInput(e: Event) {
  const val = (e.target as HTMLInputElement).value;
  emit('update:modelValue', val);
  if (timer) clearTimeout(timer);
  timer = setTimeout(() => emit('search', val), props.debounce);
}

function onClear() {
  emit('update:modelValue', '');
  emit('clear');
}
</script>
