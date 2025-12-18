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

      <div class="mb-3">
        <label>Contraseña de activación (si aplica)</label>
        <input v-model="password" class="form-control" />
      </div>

      <div class="mb-3">
        <button class="btn btn-success me-2" @click="registerTare">Registrar Tara</button>
        <button class="btn btn-primary me-2" @click="startOrder">Obtener Preset</button>
        <button class="btn btn-warning me-2" @click="addDetail">Agregar Detalle (simulado)</button>
        <button class="btn btn-danger" @click="closeOrder">Cerrar Orden</button>
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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

export default {
  setup() {
    const route = useRoute()
    const id = route.params.id
    const order = ref(null)
    const password = ref('')
    const reconciliation = ref(null)

    const load = async () => {
      const res = await api.get(`/orders/${id}`)
      order.value = res.data
    }

    const registerTare = async () => {
      const tare = parseFloat(prompt('Ingrese tara (kg)', '10000'))
      if (!isNaN(tare)) {
        await api.post(`/orders/${id}/initial-weighing`, tare)
        await load()
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

    return { order, password, registerTare, startOrder, addDetail, closeOrder, reconciliation, getReconciliation }
  }
}
</script>
