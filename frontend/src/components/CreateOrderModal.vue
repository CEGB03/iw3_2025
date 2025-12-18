<template>
  <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <h5>Crear Nueva Orden</h5>
        <button type="button" class="btn-close" @click="closeModal"></button>
      </div>

      <!-- Formulario -->
      <div v-if="!passwordGenerated" class="modal-body">
        <form @submit.prevent="submitForm">
          <div class="mb-3">
            <label class="form-label">Número de Orden</label>
            <input v-model="form.orderNumber" type="text" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label">Camión (Código Externo)</label>
            <input v-model="form.truckCodeExt" type="text" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label">Chofer (Código Externo)</label>
            <input v-model="form.driverCodeExt" type="text" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label">Cliente (Código Externo)</label>
            <input v-model="form.customerCodeExt" type="text" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label">Producto (Código Externo)</label>
            <input v-model="form.productCodeExt" type="text" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label">Fecha de Carga Prevista</label>
            <input v-model="form.scheduledDate" type="datetime-local" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label">Preset (kg)</label>
            <input v-model.number="form.preset" type="number" class="form-control" required />
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Cancelar</button>
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ loading ? 'Creando...' : 'Crear Orden' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Contraseña generada -->
      <div v-else class="modal-body text-center">
        <div class="alert alert-success">
          <h4>¡Orden Creada Exitosamente!</h4>
        </div>

        <div class="mb-4">
          <p class="text-muted">Contraseña de Activación:</p>
          <div class="password-display">
            <h2 class="text-primary fw-bold">{{ generatedPassword }}</h2>
          </div>
          <button 
            type="button" 
            class="btn btn-outline-primary btn-sm mt-2"
            @click="copyToClipboard"
          >
            📋 Copiar al portapapeles
          </button>
        </div>

        <p class="text-muted small">
          Guarda esta contraseña. La necesitarás para registrar la tara y obtener el preset.
        </p>

        <div class="modal-footer">
          <button type="button" class="btn btn-primary" @click="finishAndReload">
            Aceptar
          </button>
        </div>
      </div>

      <!-- Error -->
      <div v-if="errorMessage" class="alert alert-danger m-3">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import api from '../services/api'

export default {
  props: {
    show: Boolean
  },
  emits: ['close', 'created'],
  setup(props, { emit }) {
    const showModal = ref(props.show)
    const loading = ref(false)
    const errorMessage = ref('')
    const passwordGenerated = ref(false)
    const generatedPassword = ref('')

    const form = ref({
      orderNumber: '',
      truckCodeExt: '',
      driverCodeExt: '',
      customerCodeExt: '',
      productCodeExt: '',
      scheduledDate: new Date().toISOString().slice(0, 16),
      preset: 1000
    })

    const submitForm = async () => {
      loading.value = true
      errorMessage.value = ''

      try {
        const payload = {
          numeroOrden: form.value.orderNumber,
          camionCodExt: form.value.truckCodeExt,
          choferCodExt: form.value.driverCodeExt,
          clienteCodExt: form.value.customerCodeExt,
          productoCodExt: form.value.productCodeExt,
          fechaCargaPrevista: new Date(form.value.scheduledDate).toISOString(),
          preset: form.value.preset
        }

        const res = await api.post('/orders', payload)
        generatedPassword.value = res.data.password || res.data.activationPassword || 'N/A'
        passwordGenerated.value = true
      } catch (error) {
        errorMessage.value = error.response?.data?.message || 'Error al crear la orden'
      } finally {
        loading.value = false
      }
    }

    const copyToClipboard = () => {
      navigator.clipboard.writeText(generatedPassword.value).then(() => {
        alert('Contraseña copiada al portapapeles')
      })
    }

    const finishAndReload = () => {
      closeModal()
      emit('created')
    }

    const closeModal = () => {
      showModal.value = false
      passwordGenerated.value = false
      errorMessage.value = ''
      form.value = {
        orderNumber: '',
        truckCodeExt: '',
        driverCodeExt: '',
        customerCodeExt: '',
        productCodeExt: '',
        scheduledDate: new Date().toISOString().slice(0, 16),
        preset: 1000
      }
      emit('close')
    }

    return {
      showModal,
      loading,
      errorMessage,
      passwordGenerated,
      generatedPassword,
      form,
      submitForm,
      copyToClipboard,
      finishAndReload,
      closeModal
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #dee2e6;
}

.modal-header h5 {
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  color: #6c757d;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid #dee2e6;
}

.password-display {
  background: #f8f9fa;
  padding: 2rem;
  border-radius: 8px;
  border: 2px dashed #0d6efd;
  margin: 1rem 0;
}
</style>
