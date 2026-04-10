  <template>
    <div class="d-flex justify-content-center">
      <div class="col-8">
        <form name="editForm" novalidate @submit.prevent="save()">
          <h2 id="concesionariaApp.modelo.home.createOrEditLabel" data-cy="ModeloCreateUpdateHeading">Crear o editar Modelo</h2>
          <div>
            <div class="mb-3" v-if="modelo.id">
              <label for="id">ID</label>
              <input type="text" class="form-control" id="id" name="id" v-model="modelo.id" readonly />
            </div>
            <div class="mb-3">
              <label class="form-control-label" for="modelo">Nombre</label>
              <input
                type="text"
                class="form-control"
                name="nombre"
                id="modelo-nombre"
                data-cy="nombre"
                :class="{ valid: !v$.nombre.$invalid, invalid: v$.nombre.$invalid }"
                v-model="v$.nombre.$model"
                required
              />
              <div v-if="v$.nombre.$anyDirty && v$.nombre.$invalid">
                <small class="form-text text-danger" v-for="error of v$.nombre.$errors" :key="error.$uid">{{ error.$message }}</small>
              </div>
            </div>
            <div class="mb-3">
              <label class="form-control-label" for="modelo">Anio Lanzamiento</label>
              <input
                type="number"
                class="form-control"
                name="anioLanzamiento"
                id="modelo-anioLanzamiento"
                data-cy="anioLanzamiento"
                :class="{ valid: !v$.anioLanzamiento.$invalid, invalid: v$.anioLanzamiento.$invalid }"
                v-model.number="v$.anioLanzamiento.$model"
                required
              />
              <div v-if="v$.anioLanzamiento.$anyDirty && v$.anioLanzamiento.$invalid">
                <small class="form-text text-danger" v-for="error of v$.anioLanzamiento.$errors" :key="error.$uid">{{
                  error.$message
                }}</small>
              </div>
            </div>
            <div class="mb-3">
              <label class="form-control-label" for="modelo">Created Date</label>
              <div class="d-flex">
                <input
                  id="modelo-createdDate"
                  data-cy="createdDate"
                  type="datetime-local"
                  class="form-control"
                  name="createdDate"
                  :class="{ valid: !v$.createdDate.$invalid, invalid: v$.createdDate.$invalid }"
                  :value="convertDateTimeFromServer(v$.createdDate.$model)"
                  @change="updateInstantField('createdDate', $event)"
                />
              </div>
            </div>
            <div class="mb-3">
              <label class="form-control-label" for="modelo">Last Modified Date</label>
              <div class="d-flex">
                <input
                  id="modelo-lastModifiedDate"
                  data-cy="lastModifiedDate"
                  type="datetime-local"
                  class="form-control"
                  name="lastModifiedDate"
                  :class="{ valid: !v$.lastModifiedDate.$invalid, invalid: v$.lastModifiedDate.$invalid }"
                  :value="convertDateTimeFromServer(v$.lastModifiedDate.$model)"
                  @change="updateInstantField('lastModifiedDate', $event)"
                />
              </div>
            </div>
            <div class="mb-3">
              <label class="form-control-label" for="modelo">Marca</label>
              <select class="form-control" id="modelo-marca" data-cy="marca" name="marca" v-model="modelo.marca">
                <option :value="null" disabled>Seleccione una marca</option>
               <option
  :value="modelo.marca && marcaOption.id === modelo.marca.id ? modelo.marca : marcaOption"
  v-for="marcaOption in marcas"
  :key="marcaOption.id"
>
  {{ marcaOption.nombre }}
</option>
              </select>
            </div>
            <div class="mb-3">
              <label class="form-control-label" for="modelo">Carroceria</label>
              <select class="form-control" id="modelo-carroceria" data-cy="carroceria" name="carroceria" v-model="modelo.carroceria">
                <option :value="null"></option>
                <option
                  :value="modelo.carroceria && carroceriaOption.id === modelo.carroceria.id ? modelo.carroceria : carroceriaOption"
                  v-for="carroceriaOption in carrocerias"
                  :key="carroceriaOption.nombre"
                >
                  {{ carroceriaOption.nombre }}
                </option>
              </select>
            </div>
          </div>
          <div>
            <button type="button" id="cancel-save" data-cy="entityCreateCancelButton" class="btn btn-secondary" @click="previousState()">
              <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span>Cancelar</span>
            </button>
            <button
              type="submit"
              id="save-entity"
              data-cy="entityCreateSaveButton"
              :disabled="v$.$invalid || isSaving"
              class="btn btn-primary"
            >
              <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span>Guardar</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </template>
  <script lang="ts" src="./modelo-update.component.ts"></script>
