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
      <button v-if="role === 'ADMIN'" class="btn btn-success ms-2" @click="openCreateModal">➕ Crear Orden</button>
      <CreateOrderModal :show="showCreateModal" @close="showCreateModal = false" @created="refresh" />
    </div>

    <!-- Controles ADMIN -->
    <div v-if="role === 'ADMIN'" class="mb-3">
      <div class="d-flex flex-wrap gap-2">
        <!-- Crear entidades -->
        <button class="btn btn-outline-success" @click="toggleCreate('truck')">🚚 Crear Camión</button>
        <button class="btn btn-outline-success" @click="toggleCreate('driver')">👷 Crear Conductor</button>
        <button class="btn btn-outline-success" @click="toggleCreate('customer')">🧑‍💼 Crear Cliente</button>
        <button class="btn btn-outline-success" @click="toggleCreate('product')">🛢️ Crear Producto</button>
        <!-- Listar entidades -->
        <button class="btn btn-outline-primary" @click="loadList('orders')">📄 Listar Órdenes</button>
        <button class="btn btn-outline-primary" @click="loadList('truck')">🚚📋 Listar Camiones</button>
        <button class="btn btn-outline-primary" @click="loadList('driver')">👷📋 Listar Conductores</button>
        <button class="btn btn-outline-primary" @click="loadList('customer')">🧑‍💼📋 Listar Clientes</button>
        <button class="btn btn-outline-primary" @click="loadList('product')">🛢️📋 Listar Productos</button>
      </div>
    </div>
    <!-- Listado dinámico ADMIN -->
    <div v-if="role === 'ADMIN'">
      <table v-if="listSection === 'orders'" class="table table-striped">
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

      <div v-if="listSection === 'truck'" class="card mb-3">
        <div class="card-body">
          <h6>🚚 Camiones</h6>
          <table class="table table-sm align-middle">
            <thead>
              <tr><th>Patente</th><th>Descripción</th><th>Cisterna</th></tr>
            </thead>
            <tbody>
              <template v-for="t in trucksList" :key="t.id">
                <tr>
                  <td>{{ t.licensePlate }}</td>
                  <td>{{ t.description }}</td>
                  <td>
                    <button class="btn btn-sm btn-outline-primary" @click="toggleTruck(t.id)" :aria-expanded="!!expandedTrucks[t.id]">
                      Ver
                    </button>
                  </td>
                </tr>
                <tr v-if="expandedTrucks[t.id]">
                  <td colspan="3">
                    <div v-if="t.truncker && t.truncker.length">
                      <table class="table table-sm mb-0">
                        <thead><tr><th>Patente cisterna</th><th>Capacidad (L)</th></tr></thead>
                        <tbody>
                          <tr v-for="(c, i) in t.truncker" :key="i">
                            <td>{{ c.licence_plate || c.licencePlate }}</td>
                            <td>{{ c.capacity }}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                    <div v-else class="text-muted">Sin cisternas</div>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="listSection === 'driver'" class="card mb-3">
        <div class="card-body">
          <h6>👷 Conductores</h6>
          <table class="table table-sm align-middle">
            <thead>
              <tr><th>DNI</th><th>Nombre</th><th>Apellido</th><th>Camiones</th></tr>
            </thead>
            <tbody>
              <template v-for="d in driversList" :key="d.id">
                <tr>
                  <td>{{ d.dni }}</td>
                  <td>{{ d.name }}</td>
                  <td>{{ d.lastName }}</td>
                  <td>
                    <button class="btn btn-sm btn-outline-primary" @click="toggleDriver(d.id)" :aria-expanded="!!expandedDrivers[d.id]">
                      Ver
                    </button>
                  </td>
                </tr>
                <tr v-if="expandedDrivers[d.id]">
                  <td colspan="4">
                    <div v-if="driverTrucks[d.id] && driverTrucks[d.id].length">
                      <table class="table table-sm mb-0">
                        <thead><tr><th>Patente camión</th><th>Descripción</th><th>Cisterna</th></tr></thead>
                        <tbody>
                          <template v-for="t in driverTrucks[d.id]" :key="t.id">
                            <tr>
                              <td>{{ t.licensePlate }}</td>
                              <td>{{ t.description }}</td>
                              <td>
                                <button class="btn btn-sm btn-outline-secondary" @click="toggleDriverTruck(d.id, t.id)">Ver</button>
                              </td>
                            </tr>
                            <tr v-if="expandedDriverTrucks[d.id + '_' + t.id]">
                              <td colspan="3">
                                <div v-if="t.truncker && t.truncker.length">
                                  <table class="table table-sm mb-0">
                                    <thead><tr><th>Patente cisterna</th><th>Capacidad (L)</th></tr></thead>
                                    <tbody>
                                      <tr v-for="(c, i) in t.truncker" :key="i">
                                        <td>{{ c.licence_plate || c.licencePlate }}</td>
                                        <td>{{ c.capacity }}</td>
                                      </tr>
                                    </tbody>
                                  </table>
                                </div>
                                <div v-else class="text-muted">Sin cisternas</div>
                              </td>
                            </tr>
                          </template>
                        </tbody>
                      </table>
                    </div>
                    <div v-else class="text-muted">Sin camiones asignados</div>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="listSection === 'customer'" class="card mb-3">
        <div class="card-body">
          <h6>🧑‍💼 Clientes</h6>
          <table class="table table-sm"><thead><tr><th>CUIT/CUIL</th><th>Teléfono</th><th>Mail</th></tr></thead><tbody>
            <tr v-for="c in customersList" :key="c.id"><td>{{ c.socialNumber }}</td><td>{{ c.phoneNumber }}</td><td>{{ c.mail }}</td></tr>
          </tbody></table>
        </div>
      </div>

      <div v-if="listSection === 'product'" class="card mb-3">
        <div class="card-body">
          <h6>🛢️ Productos</h6>
          <table class="table table-sm"><thead><tr><th>Nombre</th><th>Descripción</th></tr></thead><tbody>
            <tr v-for="p in productsList" :key="p.id"><td>{{ p.productName }}</td><td>{{ p.description }}</td></tr>
          </tbody></table>
        </div>
      </div>
    </div>

    <!-- Formularios de creación ADMIN -->
    <div v-if="role === 'ADMIN'">
      <!-- Crear Camión -->
      <div v-if="createSection === 'truck'" class="card mb-3">
        <div class="card-body">
          <h6>🚚 Crear Camión + 🛢️ Cisterna</h6>
          <div class="row g-2">
            <div class="col-md-3"><input v-model="truckForm.licensePlate" class="form-control" placeholder="Patente camión (ABC123)" /></div>
            <div class="col-md-3"><input v-model="truckForm.description" class="form-control" placeholder="Descripción" /></div>
            <div class="col-md-3"><input v-model="truckForm.cisternLicencePlate" class="form-control" placeholder="Patente cisterna (C1-ABC)" /></div>
            <div class="col-md-2"><input v-model.number="truckForm.cisternCapacity" type="number" min="0" step="0.01" class="form-control" placeholder="Capacidad (L)" /></div>
            <div class="col-md-1 d-grid"><button class="btn btn-success" :disabled="!canCreateTruck" @click="createTruck">Crear</button></div>
          </div>
        </div>
      </div>

      <!-- Crear Conductor -->
      <div v-if="createSection === 'driver'" class="card mb-3">
        <div class="card-body">
          <h6>👷 Crear Conductor</h6>
          <div class="row g-2">
            <div class="col-md-3"><input v-model.number="driverForm.dni" type="number" class="form-control" placeholder="DNI" /></div>
            <div class="col-md-3"><input v-model="driverForm.name" class="form-control" placeholder="Nombre" /></div>
            <div class="col-md-3"><input v-model="driverForm.lastName" class="form-control" placeholder="Apellido" /></div>
            <div class="col-md-3 d-grid"><button class="btn btn-success" :disabled="!driverForm.dni" @click="createDriver">Crear</button></div>
          </div>
        </div>
      </div>

      <!-- Crear Cliente -->
      <div v-if="createSection === 'customer'" class="card mb-3">
        <div class="card-body">
          <h6>🧑‍💼 Crear Cliente</h6>
          <div class="row g-2">
            <div class="col-md-4"><input v-model.number="customerForm.socialNumber" type="number" class="form-control" placeholder="CUIT/CUIL" /></div>
            <div class="col-md-4"><input v-model.number="customerForm.phoneNumber" type="number" class="form-control" placeholder="Teléfono" /></div>
            <div class="col-md-4"><input v-model="customerForm.mail" class="form-control" placeholder="Mail" /></div>
            <div class="col-md-12 d-grid mt-2"><button class="btn btn-success" :disabled="!customerForm.socialNumber" @click="createCustomer">Crear</button></div>
          </div>
        </div>
      </div>

      <!-- Crear Producto -->
      <div v-if="createSection === 'product'" class="card mb-3">
        <div class="card-body">
          <h6>🛢️ Crear Producto</h6>
          <div class="row g-2">
            <div class="col-md-6"><input v-model="productForm.productName" class="form-control" placeholder="Nombre" /></div>
            <div class="col-md-6"><input v-model="productForm.description" class="form-control" placeholder="Descripción" /></div>
            <div class="col-md-12 d-grid mt-2"><button class="btn btn-success" :disabled="!productForm.productName" @click="createProduct">Crear</button></div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import api from '../services/api'
import CreateOrderModal from '../components/CreateOrderModal.vue'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

export default {
  components: { CreateOrderModal },
  setup() {
    const orders = ref([])
    const user = localStorage.getItem('username') || ''
    const router = useRouter()
    const showCreateModal = ref(false)
    const role = ref('')

    // Admin sections/forms
    const createSection = ref('')
    const listSection = ref('')
    const trucksList = ref([])
    const driversList = ref([])
    const customersList = ref([])
    const productsList = ref([])
    const truckForm = ref({ licensePlate: '', description: '', cisternLicencePlate: '', cisternCapacity: null })
    const expandedTrucks = ref({})
    const expandedDrivers = ref({})
    const expandedDriverTrucks = ref({})
    const driverTrucks = ref({})
    const canCreateTruck = computed(() => !!truckForm.value.licensePlate && !!truckForm.value.cisternLicencePlate && truckForm.value.cisternCapacity > 0)
    const driverForm = ref({ dni: null, name: '', lastName: '' })
    const customerForm = ref({ socialNumber: null, phoneNumber: null, mail: '' })
    const productForm = ref({ productName: '', description: '' })

    const load = async () => {
      try {
        // Cargar rol
        try {
          const me = await api.get('/login/me')
          role.value = me.data?.role || ''
        } catch (e) {
          role.value = ''
        }
        // Si es admin, cargar órdenes por defecto
        if (role.value === 'ADMIN') {
          listSection.value = 'orders'
          await loadList('orders')
        } else {
          orders.value = []
        }
      } catch (e) {
        if (e.response && e.response.status === 401) {
          router.push('/login')
        }
      }
    }

    const refresh = async () => {
      // Refresca sólo el listado activo
      if (listSection.value) {
        await loadList(listSection.value)
      } else {
        await load()
      }
    }
    const logout = () => { localStorage.removeItem('token'); localStorage.removeItem('username'); router.push('/login') }
    const openCreateModal = () => {
      showCreateModal.value = true
    }

    // Admin helpers
    const toggleCreate = (what) => { createSection.value = createSection.value === what ? '' : what }
    const loadList = async (what) => {
      listSection.value = what
      try {
        if (what === 'orders') { const r = await api.get('/orders'); orders.value = r.data }
        if (what === 'truck') { const r = await api.get('/trucks'); trucksList.value = r.data }
        if (what === 'driver') { const r = await api.get('/drivers'); driversList.value = r.data }
        if (what === 'customer') { const r = await api.get('/customers'); customersList.value = r.data }
        if (what === 'product') { const r = await api.get('/products'); productsList.value = r.data }
      } catch (e) {
        alert(e.response?.data?.message || 'Error al listar ' + what)
      }
    }
    const toggleTruck = (id) => { expandedTrucks.value[id] = !expandedTrucks.value[id] }
    
    const toggleDriver = async (id) => {
      expandedDrivers.value[id] = !expandedDrivers.value[id]
      if (expandedDrivers.value[id] && !driverTrucks.value[id]) {
        try {
          const r = await api.get(`/drivers/${id}/trucks`)
          driverTrucks.value[id] = r.data
        } catch (e) {
          alert(e.response?.data?.message || 'Error al cargar camiones del conductor')
          driverTrucks.value[id] = []
        }
      }
    }
    
    const toggleDriverTruck = (driverId, truckId) => {
      const key = driverId + '_' + truckId
      expandedDriverTrucks.value[key] = !expandedDriverTrucks.value[key]
    }

    const createTruck = async () => {
      try {
        await api.post('/trucks', {
          licensePlate: truckForm.value.licensePlate,
          description: truckForm.value.description,
          // el backend espera `truncker` y dentro `licence_plate`
          truncker: [
            {
              licence_plate: truckForm.value.cisternLicencePlate,
              capacity: Number(truckForm.value.cisternCapacity)
            }
          ]
        })
        alert('Camión creado')
        truckForm.value = { licensePlate: '', description: '', cisternLicencePlate: '', cisternCapacity: null }
        if (listSection.value === 'truck') { loadList('truck') }
      } catch (e) { alert(e.response?.data?.message || 'Error creando camión') }
    }

    const createDriver = async () => {
      try {
        await api.post('/drivers', { dni: Number(driverForm.value.dni), name: driverForm.value.name, lastName: driverForm.value.lastName })
        alert('Conductor creado')
        driverForm.value = { dni: null, name: '', lastName: '' }
        if (listSection.value === 'driver') { loadList('driver') }
      } catch (e) { alert(e.response?.data?.message || 'Error creando conductor') }
    }

    const createCustomer = async () => {
      try {
        await api.post('/customers', { socialNumber: Number(customerForm.value.socialNumber), phoneNumber: Number(customerForm.value.phoneNumber), mail: customerForm.value.mail })
        alert('Cliente creado')
        customerForm.value = { socialNumber: null, phoneNumber: null, mail: '' }
        if (listSection.value === 'customer') { loadList('customer') }
      } catch (e) { alert(e.response?.data?.message || 'Error creando cliente') }
    }

    const createProduct = async () => {
      try {
        await api.post('/products', { productName: productForm.value.productName, description: productForm.value.description })
        alert('Producto creado')
        productForm.value = { productName: '', description: '' }
        if (listSection.value === 'product') { loadList('product') }
      } catch (e) { alert(e.response?.data?.message || 'Error creando producto') }
    }

    onMounted(load)

    return { orders, refresh, logout, user, openCreateModal, showCreateModal, role, toggleCreate, loadList, createSection, listSection, trucksList, driversList, customersList, productsList, truckForm, driverForm, customerForm, productForm, createTruck, createDriver, createCustomer, createProduct, canCreateTruck, expandedTrucks, toggleTruck, expandedDrivers, expandedDriverTrucks, driverTrucks, toggleDriver, toggleDriverTruck }
  }
}
</script>
