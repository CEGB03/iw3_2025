<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3>Orden {{ order?.id }}</h3>
      <div>
        <router-link to="/orders" class="btn btn-secondary">Volver</router-link>
      </div>
    </div>

    <div v-if="!order">Cargando...</div>

    <div v-else>
      <div class="mb-3">
        <strong>Estado:</strong> {{ order.state }}
      </div>
      <div class="mb-3">
        <strong>Camión:</strong> {{ order.truck?.licensePlate }}
      </div>
      <div class="mb-3">
        <strong>Preset:</strong> {{ order.preset }}
      </div>
      <div class="mb-3">
        <strong>Última masa acumulada:</strong> {{ order.lastMassAccumulated }}
      </div>

      <div class="mb-3" v-if="password">
        <div class="alert alert-info">
          <strong>Contraseña de activación:</strong> <span class="fw-bold text-primary">{{ password }}</span>
        </div>
      </div>

      <div class="mb-3" v-else>
        <label>Contraseña de activación (si tiene)</label>
        <input class="form-control" placeholder="Ingrese la contraseña si es necesaria" />
      </div>

      <div class="mb-3">
        <button v-if="order.state === 1 && !showTareForm" class="btn btn-success me-2" @click="showTareForm = true">Registrar Tara</button>
        <button class="btn btn-primary me-2" @click="startOrder">Obtener Preset</button>
        <button class="btn btn-warning me-2" @click="addDetail">Agregar Detalle (simulado)</button>
        <button class="btn btn-danger" @click="closeOrder">Cerrar Orden</button>
      </div>

      <div v-if="showTareForm" class="card mb-3">
        <div class="card-body">
          <h6 class="card-title">Registrar pesaje inicial (tara)</h6>
          <div class="row g-2 align-items-end">
            <div class="col-sm-6">
              <label class="form-label">Tara (kg)</label>
              <input type="number" min="0" step="0.01" v-model.number="tare" class="form-control" placeholder="Ej: 10000" />
            </div>
            <div class="col-sm-6">
              <button class="btn btn-secondary me-2" @click="cancelTare">Cancelar</button>
              <button class="btn btn-success" :disabled="!isValidTare" @click="submitTare">Guardar</button>
            </div>
          </div>
          <div class="form-text mt-2">Al registrar la tara se generará la contraseña de activación y la orden pasará a estado 2.</div>
        </div>
      </div>

      <div class="mt-4">
        <h5>Detalle guardado</h5>
        <table class="table">
          <thead><tr><th>Timestamp</th><th>Masa</th><th>Densidad</th><th>Temp</th><th>Caudal</th></tr></thead>
          <tbody>
            <tr v-for="d in order.details" :key="d.timeStamp">
              <td>{{ d.timeStamp }}</td>
              <td>{{ d.massAccumulated }}</td>
              <td>{{ d.density }}</td>
              <td>{{ d.temperature }}</td>
              <td>{{ d.flow }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mt-4">
        <h5>Conciliación</h5>
        <div v-if="reconciliation"> 
          <pre>{{ reconciliation }}</pre>
        </div>
        <div v-else>
          <button class="btn btn-outline-info" @click="getReconciliation">Obtener conciliación</button>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import api from '../services/api'
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'

export default {
  setup() {
    const route = useRoute()
    const id = route.params.id
    const order = ref(null)
    const password = ref('')
    const reconciliation = ref(null)
    const showTareForm = ref(false)
    const tare = ref(null)

    const load = async () => {
      const res = await api.get(`/orders/${id}`)
      order.value = res.data
      password.value = res.data.password || res.data.activationPassword || ''
      // Si ya no está en estado 1, ocultar formulario de tara
      if (order.value && order.value.state !== 1) {
        showTareForm.value = false
      }
    }

    const isValidTare = computed(() => typeof tare.value === 'number' && !isNaN(tare.value) && tare.value > 0)

    const cancelTare = () => {
      tare.value = null
      showTareForm.value = false
    }

    const submitTare = async () => {
      if (!isValidTare.value) return
      try {
        const res = await api.post(`/orders/${id}/initial-weighing`, JSON.stringify(tare.value), {
          headers: { 'Content-Type': 'application/json' }
        })
        // Actualizar vista y mostrar password si llegó
        await load()
        const pw = res.data?.activationPassword
        if (pw) {
          alert('Tara registrada. Contraseña de activación: ' + pw)
        } else {
          alert('Tara registrada correctamente.')
        }
      } catch (e) {
        alert(e.response?.data?.message || 'Error registrando tara')
      } finally {
        cancelTare()
      }
    }

    const startOrder = async () => {
      try {
        const res = await api.post(`/orders/${id}/start`, null, { headers: { 'X-Activation-Password': password.value ? Number(password.value) : undefined }})
        alert('Preset: ' + res.data.preset)
      } catch (e) {
        alert(e.response?.data?.message || 'Error')
      }
    }

    const addDetail = async () => {
      // Simula un detalle válido incremental
      const last = order.value.lastMassAccumulated || 0
      const detail = {
        massAccumulated: (last || 0) + Math.floor(Math.random()*50 + 10),
        density: 0.6 + Math.random()*0.2,
        temperature: 10 + Math.random()*5,
        flow: 500 + Math.random()*100,
        timeStamp: new Date().toISOString()
      }
      try {
        await api.post(`/orders/${id}/detail`, detail, { headers: { 'X-Activation-Password': password.value ? Number(password.value) : undefined }})
        await load()
      } catch (e) {
        alert(e.response?.data?.message || 'Error agregando detalle')
      }
    }

    const closeOrder = async () => {
      try {
        await api.post(`/orders/${id}/close`)
        await load()
      } catch (e) {
        alert(e.response?.data?.message || 'Error cerrando orden')
      }
    }

    const getReconciliation = async () => {
      try {
        const res = await api.get(`/orders/${id}/reconciliation`)
        reconciliation.value = res.data
      } catch (e) {
        alert(e.response?.data?.message || 'Error obteniendo conciliación')
      }
    }

    onMounted(load)

    return { order, password, startOrder, addDetail, closeOrder, reconciliation, getReconciliation, showTareForm, tare, isValidTare, submitTare, cancelTare }
  }
}
</script>
