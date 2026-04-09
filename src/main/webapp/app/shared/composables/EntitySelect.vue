<template>
  <div>
    <label v-if="label" class="form-label">{{ label }} <span v-if="required" class="text-danger">*</span></label>
    <select
      class="form-select"
      :class="{ 'is-invalid': invalid }"
      :value="modelValue"
      @change="emit('update:modelValue', parseValue(($event.target as HTMLSelectElement).value))"
      :disabled="disabled"
    >
      <option :value="null">{{ placeholder }}</option>
      <option
        v-for="item in items"
        :key="item[valueKey]"
        :value="item[valueKey]"
      >
        {{ getLabel(item) }}
      </option>
    </select>
    <div class="invalid-feedback" v-if="invalid && errorMsg">{{ errorMsg }}</div>
  </div>
</template>

<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    modelValue: any;
    items: Record<string, any>[];
    labelKey: string | ((item: any) => string);
    valueKey?: string;
    label?: string;
    placeholder?: string;
    required?: boolean;
    invalid?: boolean;
    errorMsg?: string;
    disabled?: boolean;
    returnObject?: boolean;
  }>(),
  {
    valueKey: 'id',
    placeholder: '— Seleccionar —',
    required: false,
    invalid: false,
    disabled: false,
    returnObject: false,
  },
);

const emit = defineEmits<{ 'update:modelValue': [value: any] }>();

function getLabel(item: Record<string, any>): string {
  if (typeof props.labelKey === 'function') return props.labelKey(item);
  return item[props.labelKey] ?? String(item[props.valueKey ?? 'id']);
}

function parseValue(raw: string) {
  if (raw === 'null' || raw === '') return null;
  if (props.returnObject) {
    return props.items.find(i => String(i[props.valueKey ?? 'id']) === raw) ?? null;
  }
  const num = Number(raw);
  return isNaN(num) ? raw : num;
}
</script>
