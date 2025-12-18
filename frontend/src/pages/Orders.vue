<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3>Órdenes</h3>
      <div>
        <strong>{{ user }}</strong>
        <button class="btn btn-sm btn-outline-secondary ms-2" @click="logout">Salir</button>
      </div>
    </div>

    <div class="mb-3">
      <button class="btn btn-primary" @click="refresh">Refrescar</button>
      <button class="btn btn-danger ms-2" @click="openCreateModal">🔴 :V Crear Orden</button>
      <CreateOrderModal :show="showCreateModal" @close="showCreateModal = false" @created="refresh" />
    </div>

    <table class="table table-striped">
      <thead>
        <tr>
          <th>ID</th>
          <th>Nº Orden</th>
          <th>Estado</th>
          <th>Camión</th>
          <th>Preset</th>
          <th>Última masa</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="o in orders" :key="o.id">
          <td>{{ o.id }}</td>
          <td>{{ o.externalCode }}</td>
          <td>{{ o.state }}</td>
          <td>{{ o.truck?.licensePlate }}</td>
          <td>{{ o.preset }}</td>
          <td>{{ o.lastMassAccumulated }}</td>
          <td><router-link :to="`/orders/${o.id}`" class="btn btn-sm btn-primary">Ver</router-link></td>
        </tr>
      </tbody>
    </table>

  </div>
</template>

<script>
import api from '../services/api'
import CreateOrderModal from '../components/CreateOrderModal.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  components: { CreateOrderModal },
  setup() {
    const orders = ref([])
    const user = localStorage.getItem('username') || ''
    const router = useRouter()
    const showCreateModal = ref(false)

    const load = async () => {
      try {
        const res = await api.get('/orders')
        orders.value = res.data
      } catch (e) {
        if (e.response && e.response.status === 401) {
          router.push('/login')
        }
      }
    }

    const refresh = () => load()
    const logout = () => { localStorage.removeItem('token'); localStorage.removeItem('username'); router.push('/login') }
    const openCreateModal = () => {
      showCreateModal.value = true
    }

    onMounted(load)

    return { orders, refresh, logout, user, openCreateModal, showCreateModal }
  }
}
</script>
