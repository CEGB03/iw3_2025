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
        <label>
          Contraseña de cuenta (para revelar)
          <span
            class="ms-2 text-muted"
            title="Ingresa tu contraseña de cuenta para revelar la contraseña de activación por 30 segundos. La contraseña de activación se utiliza para obtener el preset y agregar detalle durante la carga."
          >ℹ️</span>
        </label>
        <div class="input-group">
          <input 
            type="password" 
            v-model="manualPassword" 
            class="form-control" 
            placeholder="Ingrese su contraseña de cuenta para revelar la contraseña de activacion nuevamente"
            @input="handlePasswordInput"
          />
          <button class="btn btn-outline-secondary" type="button" @click="revealPassword" v-if="!passwordVisible && manualPassword">
            👁️ Mostrar
          </button>
          <button class="btn btn-outline-secondary" type="button" @click="hidePassword" v-if="passwordVisible">
            👁️‍🗨️ Ocultar
          </button>
        </div>
        <div v-if="passwordVisible" class="alert alert-info mt-2">
          <strong>Contraseña de activación:</strong> <span class="fw-bold text-primary">{{ activationPassword }}</span>
          <div v-if="passwordTimeoutMessage" class="text-muted small mt-2">{{ passwordTimeoutMessage }}</div>
        </div>
      </div>

      <div class="mb-3">
        <button v-if="order.state === 1 && !showTareForm" class="btn btn-success me-2" @click="showTareForm = true">Registrar Tara</button>
        <button class="btn btn-primary me-2" :disabled="order.state !== 2" @click="startOrder">Obtener Preset</button>
        <button class="btn btn-warning me-2" :disabled="order.state !== 2" @click="openDetailModal">Agregar Detalle</button>
        <button class="btn btn-danger me-2" :disabled="order.state !== 2" @click="closeOrder">Cerrar Orden</button>
        <button class="btn btn-info" :disabled="order.state !== 4" @click="toggleReconciliation">{{ showReconciliation ? 'Ocultar' : 'Ver' }} Conciliación</button>
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

      <div v-if="showReconciliation && reconciliation" class="mt-4">
        <h5>Conciliación</h5>
        <table class="table table-bordered">
          <tbody>
            <tr><th>Pesaje inicial (tara)</th><td>{{ reconciliation.initialWeighing }} kg</td></tr>
            <tr><th>Pesaje final</th><td>{{ reconciliation.finalWeighing }} kg</td></tr>
            <tr><th>Producto cargado</th><td>{{ reconciliation.productLoaded }} kg</td></tr>
            <tr><th>Neto por balanza</th><td>{{ reconciliation.netByScale }} kg</td></tr>
            <tr><th>Diferencia balanza/caudalímetro</th><td>{{ reconciliation.differenceScaleFlow }} kg</td></tr>
            <tr><th>Temperatura promedio</th><td>{{ reconciliation.avgTemperature?.toFixed(2) || 'N/A' }} °C</td></tr>
            <tr><th>Densidad promedio</th><td>{{ reconciliation.avgDensity?.toFixed(4) || 'N/A' }}</td></tr>
            <tr><th>Caudal promedio</th><td>{{ reconciliation.avgFlow?.toFixed(2) || 'N/A' }} kg/h</td></tr>
          </tbody>
        </table>
      </div>

      <div class="mt-4">
        <h5>Detalle guardado</h5>
        <table class="table">
          <thead><tr><th>Timestamp</th><th>Masa</th><th>Densidad</th><th>Temp</th><th>Caudal</th></tr></thead>
          <tbody>
            <tr v-for="d in order.details" :key="d.timeStamp || d.time_stamp || Math.random()">
              <td>{{ d.timeStamp || d.time_stamp }}</td>
              <td>{{ d.massAccumulated ?? d.mass_accumulated }}</td>
              <td>{{ d.density }}</td>
              <td>{{ d.temperature }}</td>
              <td>{{ d.flow }}</td>
            </tr>
          </tbody>
        </table>
      </div>

    </div>
  </div>

  <!-- Modal para agregar detalle -->
  <div v-if="showDetailModal" style="position:fixed; inset:0; background: rgba(0,0,0,0.5); z-index:1050;" @click.self="closeDetailModal">
    <div class="card" style="max-width: 720px; margin: 5% auto;">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h5 class="mb-0">Agregar detalle de carga</h5>
          <button class="btn btn-sm btn-outline-secondary" @click="closeDetailModal">Cerrar</button>
        </div>

        <div class="alert alert-secondary small">
          Validaciones: flow ≥ 0, 0 < density < 1, mass_accumulated ≥ 0, ≥ última masa (actual: {{ order?.lastMassAccumulated || 0 }}) y ≤ preset ({{ order?.preset }}).
        </div>

        <div class="mb-3">
          <h6 class="mb-2">Carga manual</h6>
          <div class="row g-2 align-items-end">
            <div class="col-md-3">
              <label class="form-label">Masa acumulada (kg)</label>
              <input type="number" step="0.01" min="0" v-model.number="detailForm.mass_accumulated" class="form-control" />
            </div>
            <div class="col-md-3">
              <label class="form-label">Densidad (0-1)</label>
              <input type="number" step="0.0001" min="0" max="1" v-model.number="detailForm.density" class="form-control" />
            </div>
            <div class="col-md-3">
              <label class="form-label">Temperatura (°C)</label>
              <input type="number" step="0.01" v-model.number="detailForm.temperature" class="form-control" />
            </div>
            <div class="col-md-3">
              <label class="form-label">Caudal (kg/h)</label>
              <input type="number" step="0.01" min="0" v-model.number="detailForm.flow" class="form-control" />
            </div>
          </div>
          <div class="d-flex gap-2 mt-2">
            <button class="btn btn-success" :disabled="!isValidManualDetail" @click="submitManualDetail">Enviar</button>
            <small class="text-muted" v-if="manualError">{{ manualError }}</small>
          </div>
          <div v-if="detailManualResult" class="mt-2" :class="detailManualResult.ok ? 'alert alert-success' : 'alert alert-danger'">
            {{ detailManualResult.message }}
          </div>
        </div>

        <hr />

        <div>
          <h6 class="mb-2">Carga masiva (CSV: mass_accumulated,density,temperature,flow)</h6>
          <input type="file" accept=".csv,.txt" @change="onCsvSelected" class="form-control" />
          <div class="d-flex gap-2 mt-2">
            <button class="btn btn-outline-primary" :disabled="!csvRows.length" @click="submitBulkDetails">Enviar archivo</button>
            <small class="text-muted" v-if="csvSummary">{{ csvSummary }}</small>
          </div>
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
    const activationPassword = ref('')
    const manualPassword = ref('')
    const passwordVisible = ref(false)
    const passwordTimeoutMessage = ref('')
    const passwordTimeoutHandle = ref(null)
    const reconciliation = ref(null)
    const showReconciliation = ref(false)
    const showTareForm = ref(false)
    const tare = ref(null)
    // Modal detalle
    const showDetailModal = ref(false)
    const detailForm = ref({ mass_accumulated: null, density: null, temperature: null, flow: null })
    const manualError = ref('')
    const detailManualResult = ref(null)
    // CSV
    const csvRows = ref([])
    const csvSummary = ref('')

    const load = async () => {
      const res = await api.get(`/orders/${id}`)
      order.value = res.data
      // Cargar contraseña de activación pero NO mostrarla hasta que se revele
      activationPassword.value = res.data.activationPassword || res.data.password || ''
      // Si ya no está en estado 1, ocultar formulario de tara
      if (order.value && order.value.state !== 1) {
        showTareForm.value = false
      }
    }

    const handlePasswordInput = () => {
      // Resetear timeout si había uno
      if (passwordTimeoutHandle.value) {
        clearTimeout(passwordTimeoutHandle.value)
      }
      // Cuando se ingresa contraseña manualmente, ocultarla de inmediato
      passwordVisible.value = false
      passwordTimeoutMessage.value = ''
    }

    const revealPassword = () => {
      if (!manualPassword.value) return
      passwordVisible.value = true
      passwordTimeoutMessage.value = 'Se ocultará en 30 segundos...'
      // Limpiar timeout anterior si existe
      if (passwordTimeoutHandle.value) {
        clearTimeout(passwordTimeoutHandle.value)
      }
      // Mostrar durante 30 segundos (30000 ms)
      passwordTimeoutHandle.value = setTimeout(() => {
        passwordVisible.value = false
        passwordTimeoutMessage.value = ''
        passwordTimeoutHandle.value = null
      }, 30000)
    }

    const hidePassword = () => {
      passwordVisible.value = false
      passwordTimeoutMessage.value = ''
      if (passwordTimeoutHandle.value) {
        clearTimeout(passwordTimeoutHandle.value)
        passwordTimeoutHandle.value = null
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
        // Actualizar vista (no mostrar contraseña por seguridad)
        await load()
        alert('Tara registrada correctamente.')
      } catch (e) {
        alert(e.response?.data?.message || 'Error registrando tara')
      } finally {
        cancelTare()
      }
    }

    const startOrder = async () => {
      try {
        const res = await api.post(`/orders/${id}/start`, null, { headers: { 'X-Activation-Password': activationPassword.value ? Number(activationPassword.value) : undefined }})
        alert(`Preset: ${res.data.preset}. Recuerda que la carga máxima es de: ${res.data.preset}`)
      } catch (e) {
        alert(e.response?.data?.message || 'Error')
      }
    }

    const openDetailModal = async () => {
      // Ejecuta start (punto 3) antes de permitir carga de detalle
      await startOrder()
      showDetailModal.value = true
      manualError.value = ''
      csvRows.value = []
      csvSummary.value = ''
    }

    const isValidManualDetail = computed(() => {
      const last = Number(order.value?.lastMassAccumulated || 0)
      const preset = Number(order.value?.preset || 0)
      const m = detailForm.value.mass_accumulated
      const d = detailForm.value.density
      const t = detailForm.value.temperature
      const f = detailForm.value.flow
      const ok = (m !== null && m <= preset && !isNaN(m) && m >= 0 && m >= last) && (d !== null && d > 0 && d < 1) && (t !== null && !isNaN(t)) && (f !== null && f >= 0)
      return ok
    })

    const submitManualDetail = async () => {
      manualError.value = ''
      detailManualResult.value = null
      if (!isValidManualDetail.value) { manualError.value = 'Revisá los valores: flow ≥ 0, 0 < density < 1, masa ≥ 0 y ≥ última.'; return }
      try {
        const payload = { ...detailForm.value, time_stamp: new Date().toISOString() }
        await api.post(`/orders/${id}/detail`, payload, { headers: { 'X-Activation-Password': activationPassword.value ? Number(activationPassword.value) : undefined }})
        await load()
        detailManualResult.value = { ok: true, message: 'Detalle agregado correctamente.' }
        // Reset parcial para siguiente carga
        detailForm.value = { mass_accumulated: null, density: null, temperature: null, flow: null }
      } catch (e) {
        const msg = e.response?.data?.message || 'Error agregando detalle'
        detailManualResult.value = { ok: false, message: msg }
      }
    }

    const onCsvSelected = (evt) => {
      csvRows.value = []
      csvSummary.value = ''
      const file = evt.target.files?.[0]
      if (!file) return
      const reader = new FileReader()
      reader.onload = () => {
        try {
          const text = String(reader.result || '')
          const lines = text.split(/\r?\n/).filter(l => l.trim().length)
          if (!lines.length) return
          let startIdx = 0
          // Detectar header
          const first = lines[0].toLowerCase()
          if (first.includes('mass') || first.includes('density')) startIdx = 1
          for (let i = startIdx; i < lines.length; i++) {
            const parts = lines[i].split(',').map(p => p.trim())
            if (parts.length < 4) continue
            const row = {
              mass_accumulated: Number(parts[0]),
              density: Number(parts[1]),
              temperature: Number(parts[2]),
              flow: Number(parts[3])
            }
            csvRows.value.push(row)
          }
          csvSummary.value = `Leídas ${csvRows.value.length} filas`;
        } catch (err) {
          csvSummary.value = 'Error leyendo archivo.'
        }
      }
      reader.readAsText(file)
    }

    const submitBulkDetails = async () => {
      if (!csvRows.value.length) return
      let sent = 0, skipped = 0
      let last = Number(order.value?.lastMassAccumulated || 0)
      const preset = Number(order.value?.preset || Infinity)
      for (const r of csvRows.value) {
        // Validaciones locales
        const valid = r && typeof r.mass_accumulated === 'number' && !isNaN(r.mass_accumulated) && r.mass_accumulated >= 0 && r.mass_accumulated >= last && r.mass_accumulated <= preset && r.density > 0 && r.density < 1 && r.flow >= 0
        if (!valid) { skipped++; continue }
        try {
          const payload = { ...r, time_stamp: new Date().toISOString() }
          await api.post(`/orders/${id}/detail`, payload, { headers: { 'X-Activation-Password': activationPassword.value ? Number(activationPassword.value) : undefined }})
          sent++
          last = r.mass_accumulated
        } catch (e) {
          skipped++
        }
      }
      await load()
      csvSummary.value = `Enviadas ${sent}, omitidas ${skipped}`
    }

    const closeDetailModal = () => {
      showDetailModal.value = false
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

    const toggleReconciliation = async () => {
      if (!showReconciliation.value && !reconciliation.value) {
        await getReconciliation()
      }
      showReconciliation.value = !showReconciliation.value
    }

    onMounted(load)

    return { order, activationPassword, manualPassword, passwordVisible, passwordTimeoutMessage, handlePasswordInput, revealPassword, hidePassword, startOrder, closeOrder, reconciliation, showReconciliation, getReconciliation, toggleReconciliation, showTareForm, tare, isValidTare, submitTare, cancelTare, 
      // detalle
      showDetailModal, openDetailModal, closeDetailModal, detailForm, isValidManualDetail, submitManualDetail, manualError, detailManualResult, onCsvSelected, csvRows, csvSummary, submitBulkDetails }
  }
}
</script>
