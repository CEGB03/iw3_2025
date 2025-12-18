<template>
  <div v-if="show" class="modal-overlay" @click.self="closeModal">
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
            <label class="form-label">Camión</label>
            <template v-if="trucks.length">
              <select v-model="form.selectedTruck" class="form-select">
                <option :value="null">Seleccione un camión</option>
                <option v-for="t in trucks" :key="t.id" :value="t">
                  {{ t.licensePlate }} - {{ t.description || 'sin descripción' }}
                </option>
              </select>
              <small class="text-muted">Usará la patente como código externo.</small>
            </template>
            <template v-else>
              <input v-model="form.truckCodeExt" type="text" class="form-control" required placeholder="Patente (p. ej. ABC123)" />
            </template>
          </div>

          <div class="mb-3">
            <label class="form-label">Chofer</label>
            <template v-if="drivers.length">
              <select v-model="form.selectedDriver" class="form-select">
                <option :value="null">Seleccione un chofer</option>
                <option v-for="d in drivers" :key="d.id" :value="d">
                  {{ d.name }} {{ d.lastName }} - DNI {{ d.dni }}
                </option>
              </select>
              <small class="text-muted">Usará el DNI como código externo.</small>
            </template>
            <template v-else>
              <input v-model="form.driverCodeExt" type="text" class="form-control" required placeholder="DNI (p. ej. 12345678)" />
            </template>
          </div>

          <div class="mb-3">
            <label class="form-label">Cliente</label>
            <template v-if="customers.length">
              <select v-model="form.selectedCustomer" class="form-select">
                <option :value="null">Seleccione un cliente</option>
                <option v-for="c in customers" :key="c.id" :value="c">
                  {{ c.socialNumber }} - {{ c.mail || 'sin mail' }}
                </option>
              </select>
              <small class="text-muted">Usará el CUIT/CUIL como código externo.</small>
            </template>
            <template v-else>
              <input v-model="form.customerCodeExt" type="text" class="form-control" required placeholder="CUIT/CUIL (p. ej. 30123456789)" />
            </template>
          </div>

          <div class="mb-3">
            <label class="form-label">Producto</label>
            <template v-if="products.length">
              <select v-model="form.selectedProduct" class="form-select">
                <option :value="null">Seleccione un producto</option>
                <option v-for="p in products" :key="p.id" :value="p">
                  {{ p.productName }} - {{ p.description || '' }}
                </option>
              </select>
              <small class="text-muted">Usará el nombre del producto como código externo.</small>
            </template>
            <template v-else>
              <input v-model="form.productCodeExt" type="text" class="form-control" required placeholder="Nombre (p. ej. GasOil)" />
            </template>
          </div>

          <div class="mb-3">
            <label class="form-label">Preset (kg)</label>
            <input v-model.number="form.preset" type="number" class="form-control" required min="1" placeholder="Ingrese preset en kg" />
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
import { ref, onMounted, toRef } from 'vue'
import api from '../services/api'

export default {
  props: { show: Boolean },
  emits: ['close', 'created'],
  setup(props, { emit }) {
    const show = toRef(props, 'show') // reactividad del prop
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
      preset: null,
      // selecciones
      selectedTruck: null,
      selectedDriver: null,
      selectedCustomer: null,
      selectedProduct: null
    })

    // Listas para autocompletar
    const trucks = ref([])
    const drivers = ref([])
    const customers = ref([])
    const products = ref([])

    const loadOptions = async () => {
      try {
        const [t, d, c, p] = await Promise.all([
          api.get('/trucks').catch(() => ({ data: [] })),
          api.get('/drivers').catch(() => ({ data: [] })),
          api.get('/customers').catch(() => ({ data: [] })),
          api.get('/products').catch(() => ({ data: [] }))
        ])
        trucks.value = t.data || []
        drivers.value = d.data || []
        customers.value = c.data || []
        products.value = p.data || []
      } catch (e) {
        // Silencioso: si falla, se mantienen inputs manuales
      }
    }

    onMounted(loadOptions)

    const validateForm = () => {
      const f = form.value
      if (!f.orderNumber || (typeof f.orderNumber === 'string' && f.orderNumber.trim() === '')) {
        errorMessage.value = 'El número de orden es obligatorio'
        return false
      }
      const truckCode = f.selectedTruck?.licensePlate || f.truckCodeExt
      if (!truckCode) { errorMessage.value = 'Debe seleccionar o ingresar un camión'; return false }
      const driverCode = f.selectedDriver?.dni || f.driverCodeExt
      if (!driverCode) { errorMessage.value = 'Debe seleccionar o ingresar un chofer'; return false }
      const customerCode = f.selectedCustomer?.socialNumber || f.customerCodeExt
      if (!customerCode) { errorMessage.value = 'Debe seleccionar o ingresar un cliente'; return false }
      const productCode = f.selectedProduct?.productName || f.productCodeExt
      if (!productCode) { errorMessage.value = 'Debe seleccionar o ingresar un producto'; return false }
      if (!f.preset || Number(f.preset) <= 0) { errorMessage.value = 'Preset debe ser positivo'; return false }
      errorMessage.value = ''
      return true
    }

    const submitForm = async () => {
      loading.value = true
      errorMessage.value = ''

      try {
        if (!validateForm()) { loading.value = false; return }
        // Mapear al DTO que espera el backend (OrderRequestDTO)
        const payload = {
          externalCode: form.value.orderNumber,
          preset: form.value.preset,
          truck: {
            licensePlate: form.value.selectedTruck?.licensePlate || form.value.truckCodeExt,
            description: form.value.selectedTruck?.description || undefined,
            truncker: form.value.selectedTruck?.truncker || undefined
          },
          driver: {
            dni: Number(form.value.selectedDriver?.dni || form.value.driverCodeExt) || undefined,
            name: form.value.selectedDriver?.name || undefined,
            lastName: form.value.selectedDriver?.lastName || undefined
          },
          customer: {
            socialNumber: Number(form.value.selectedCustomer?.socialNumber || form.value.customerCodeExt) || undefined,
            mail: form.value.selectedCustomer?.mail || undefined,
            phoneNumber: form.value.selectedCustomer?.phoneNumber || undefined
          },
          product: {
            productName: form.value.selectedProduct?.productName || form.value.productCodeExt,
            description: form.value.selectedProduct?.description || undefined
          }
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
      passwordGenerated.value = false
      errorMessage.value = ''
      form.value = {
        orderNumber: '',
        truckCodeExt: '',
        driverCodeExt: '',
        customerCodeExt: '',
        productCodeExt: '',
        preset: null,
        selectedTruck: null,
        selectedDriver: null,
        selectedCustomer: null,
        selectedProduct: null
      }
      emit('close')
    }

    return {
      show,
      loading,
      errorMessage,
      passwordGenerated,
      generatedPassword,
      form,
      trucks,
      drivers,
      customers,
      products,
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
