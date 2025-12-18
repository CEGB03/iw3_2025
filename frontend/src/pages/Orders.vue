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
      <button v-if="role === 'ADMIN'" class="btn btn-success ms-2" @click="openCreateUserModal">👤 Crear Usuario</button>
      <button v-if="role === 'OPERADOR'" class="btn btn-outline-info ms-2" @click="loadList('orders')">📄 Mis Órdenes</button>
      <button v-if="role === 'OPERADOR'" class="btn btn-outline-info ms-2" @click="loadList('truck')">🚚 Camiones</button>
      <button v-if="role === 'OPERADOR'" class="btn btn-outline-info ms-2" @click="loadList('driver')">👷 Conductores</button>
      <button v-if="role === 'OPERADOR'" class="btn btn-outline-info ms-2" @click="loadList('customer')">🧑‍💼 Clientes</button>
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
        <button class="btn btn-outline-primary" @click="loadList('users')">👥📋 Listar Usuarios</button>
      </div>
    </div>
    <!-- Mensaje de bienvenida VIEWER -->
    <div v-if="role === 'VIEWER'" class="alert alert-info">
      <strong>Bienvenido, {{ user }}</strong><br>
      Aquí puedes ver tus órdenes y tu información personal.
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
      <!-- Paginación órdenes -->
      <nav v-if="listSection === 'orders' && pagination.orders.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.orders.page === 0 }">
            <button class="page-link" @click="changePage('orders', pagination.orders.page - 1)" :disabled="pagination.orders.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.orders.page + 1 }} de {{ pagination.orders.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.orders.page >= pagination.orders.totalPages - 1 }">
            <button class="page-link" @click="changePage('orders', pagination.orders.page + 1)" :disabled="pagination.orders.page >= pagination.orders.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>

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

      <nav v-if="listSection === 'truck' && pagination.trucks.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.trucks.page === 0 }">
            <button class="page-link" @click="changePage('truck', pagination.trucks.page - 1)" :disabled="pagination.trucks.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.trucks.page + 1 }} de {{ pagination.trucks.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.trucks.page >= pagination.trucks.totalPages - 1 }">
            <button class="page-link" @click="changePage('truck', pagination.trucks.page + 1)" :disabled="pagination.trucks.page >= pagination.trucks.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>

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

      <nav v-if="listSection === 'driver' && pagination.drivers.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.drivers.page === 0 }">
            <button class="page-link" @click="changePage('driver', pagination.drivers.page - 1)" :disabled="pagination.drivers.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.drivers.page + 1 }} de {{ pagination.drivers.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.drivers.page >= pagination.drivers.totalPages - 1 }">
            <button class="page-link" @click="changePage('driver', pagination.drivers.page + 1)" :disabled="pagination.drivers.page >= pagination.drivers.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>

      <div v-if="listSection === 'customer'" class="card mb-3">
        <div class="card-body">
          <h6>🧑‍💼 Clientes</h6>
          <table class="table table-sm"><thead><tr><th>CUIT/CUIL</th><th>Teléfono</th><th>Mail</th></tr></thead><tbody>
            <tr v-for="c in customersList" :key="c.id"><td>{{ c.socialNumber }}</td><td>{{ c.phoneNumber }}</td><td>{{ c.mail }}</td></tr>
          </tbody></table>
        </div>
      </div>

      <nav v-if="listSection === 'customer' && pagination.customers.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.customers.page === 0 }">
            <button class="page-link" @click="changePage('customer', pagination.customers.page - 1)" :disabled="pagination.customers.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.customers.page + 1 }} de {{ pagination.customers.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.customers.page >= pagination.customers.totalPages - 1 }">
            <button class="page-link" @click="changePage('customer', pagination.customers.page + 1)" :disabled="pagination.customers.page >= pagination.customers.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>

      <div v-if="listSection === 'product'" class="card mb-3">
        <div class="card-body">
          <h6>🛢️ Productos</h6>
          <table class="table table-sm"><thead><tr><th>Nombre</th><th>Descripción</th></tr></thead><tbody>
            <tr v-for="p in productsList" :key="p.id"><td>{{ p.productName }}</td><td>{{ p.description }}</td></tr>
          </tbody></table>
        </div>
      </div>

      <nav v-if="listSection === 'product' && pagination.products.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.products.page === 0 }">
            <button class="page-link" @click="changePage('product', pagination.products.page - 1)" :disabled="pagination.products.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.products.page + 1 }} de {{ pagination.products.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.products.page >= pagination.products.totalPages - 1 }">
            <button class="page-link" @click="changePage('product', pagination.products.page + 1)" :disabled="pagination.products.page >= pagination.products.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>

      <div v-if="listSection === 'users'" class="card mb-3">
        <div class="card-body">
          <h6>👥 Usuarios</h6>
          <table class="table table-sm">
            <thead>
              <tr>
                <th>ID</th>
                <th>Usuario</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Creado</th>
                <th>Actualizado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in usersList" :key="u.id">
                <td>{{ u.id.substring(0, 8) }}...</td>
                <td>{{ u.username }}</td>
                <td><span :class="`badge bg-${u.role === 'ADMIN' ? 'danger' : u.role === 'OPERADOR' ? 'primary' : 'info'}`">{{ u.role }}</span></td>
                <td><span :class="`badge bg-${u.enabled ? 'success' : 'secondary'}`">{{ u.enabled ? 'Activo' : 'Inactivo' }}</span></td>
                <td>{{ new Date(u.createdAt).toLocaleDateString('es-AR') }}</td>
                <td>{{ new Date(u.updatedAt).toLocaleDateString('es-AR') }}</td>
              </tr>
            </tbody>
          </table>
          <p v-if="usersList.length === 0" class="text-muted">No hay usuarios</p>
        </div>
      </div>

      <nav v-if="listSection === 'users' && pagination.users.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.users.page === 0 }">
            <button class="page-link" @click="changePage('users', pagination.users.page - 1)" :disabled="pagination.users.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.users.page + 1 }} de {{ pagination.users.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.users.page >= pagination.users.totalPages - 1 }">
            <button class="page-link" @click="changePage('users', pagination.users.page + 1)" :disabled="pagination.users.page >= pagination.users.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>
    </div>

    <!-- Listado dinámico OPERADOR -->
    <div v-if="role === 'OPERADOR'">
      <div v-if="listSection === 'orders'" class="card mb-3">
        <div class="card-body">
          <h6>📄 Órdenes</h6>
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
      </div>

      <nav v-if="listSection === 'orders' && pagination.orders.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.orders.page === 0 }">
            <button class="page-link" @click="changePage('orders', pagination.orders.page - 1)" :disabled="pagination.orders.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.orders.page + 1 }} de {{ pagination.orders.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.orders.page >= pagination.orders.totalPages - 1 }">
            <button class="page-link" @click="changePage('orders', pagination.orders.page + 1)" :disabled="pagination.orders.page >= pagination.orders.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>

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

      <nav v-if="listSection === 'truck' && pagination.trucks.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.trucks.page === 0 }">
            <button class="page-link" @click="changePage('truck', pagination.trucks.page - 1)" :disabled="pagination.trucks.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.trucks.page + 1 }} de {{ pagination.trucks.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.trucks.page >= pagination.trucks.totalPages - 1 }">
            <button class="page-link" @click="changePage('truck', pagination.trucks.page + 1)" :disabled="pagination.trucks.page >= pagination.trucks.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>

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

      <nav v-if="listSection === 'driver' && pagination.drivers.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.drivers.page === 0 }">
            <button class="page-link" @click="changePage('driver', pagination.drivers.page - 1)" :disabled="pagination.drivers.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.drivers.page + 1 }} de {{ pagination.drivers.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.drivers.page >= pagination.drivers.totalPages - 1 }">
            <button class="page-link" @click="changePage('driver', pagination.drivers.page + 1)" :disabled="pagination.drivers.page >= pagination.drivers.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>

      <div v-if="listSection === 'customer'" class="card mb-3">
        <div class="card-body">
          <h6>🧑‍💼 Clientes</h6>
          <table class="table table-sm"><thead><tr><th>CUIT/CUIL</th><th>Teléfono</th><th>Mail</th></tr></thead><tbody>
            <tr v-for="c in customersList" :key="c.id"><td>{{ c.socialNumber }}</td><td>{{ c.phoneNumber }}</td><td>{{ c.mail }}</td></tr>
          </tbody></table>
        </div>
      </div>

      <nav v-if="listSection === 'customer' && pagination.customers.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.customers.page === 0 }">
            <button class="page-link" @click="changePage('customer', pagination.customers.page - 1)" :disabled="pagination.customers.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.customers.page + 1 }} de {{ pagination.customers.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.customers.page >= pagination.customers.totalPages - 1 }">
            <button class="page-link" @click="changePage('customer', pagination.customers.page + 1)" :disabled="pagination.customers.page >= pagination.customers.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>
    </div>

    <!-- Listado dinámico VIEWER -->
    <div v-if="role === 'VIEWER'">
      <div v-if="listSection === 'orders'" class="card mb-3">
        <div class="card-body">
          <h6>📄 Mis Órdenes</h6>
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
          <p v-if="orders.length === 0" class="text-muted">No tienes órdenes</p>
        </div>
      </div>

      <nav v-if="listSection === 'orders' && pagination.orders.totalPages > 1" aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item" :class="{ disabled: pagination.orders.page === 0 }">
            <button class="page-link" @click="changePage('orders', pagination.orders.page - 1)" :disabled="pagination.orders.page === 0">Anterior</button>
          </li>
          <li class="page-item active">
            <span class="page-link">Página {{ pagination.orders.page + 1 }} de {{ pagination.orders.totalPages }}</span>
          </li>
          <li class="page-item" :class="{ disabled: pagination.orders.page >= pagination.orders.totalPages - 1 }">
            <button class="page-link" @click="changePage('orders', pagination.orders.page + 1)" :disabled="pagination.orders.page >= pagination.orders.totalPages - 1">Siguiente</button>
          </li>
        </ul>
      </nav>
    </div>

    <!-- Formularios de creación ADMIN -->
    <div v-if="role === 'ADMIN'">
      <!-- Crear Camión -->
      <div v-if="createSection === 'truck'" class="card mb-3">
        <div class="card-body">
          <h6>🚚 Crear Camión + 🛢️ Cisterna(s)</h6>
          <div class="row g-2 mb-3">
            <div class="col-md-5"><input v-model="truckForm.licensePlate" class="form-control" placeholder="Patente camión (ABC123)" /></div>
            <div class="col-md-5"><input v-model="truckForm.description" class="form-control" placeholder="Descripción" /></div>
            <div class="col-md-2 d-grid"><button class="btn btn-info btn-sm" @click="addCisternRow">+ Cisterna</button></div>
          </div>
          
          <div v-if="truckForm.cisterns && truckForm.cisterns.length" class="mb-3">
            <h7 class="text-muted">Cisternas:</h7>
            <div v-for="(c, i) in truckForm.cisterns" :key="i" class="row g-2 mb-2">
              <div class="col-md-4"><input v-model="c.licence_plate" class="form-control form-control-sm" placeholder="Patente cisterna (C1-ABC)" /></div>
              <div class="col-md-3"><input v-model.number="c.capacity" type="number" min="0" step="0.01" class="form-control form-control-sm" placeholder="Capacidad (L)" /></div>
              <div class="col-md-2 d-grid"><button class="btn btn-danger btn-sm" :disabled="truckForm.cisterns.length === 1" @click="removeCisternRow(i)">Quitar</button></div>
            </div>
          </div>

          <div class="d-grid"><button class="btn btn-success" :disabled="!canCreateTruck" @click="createTruck">Crear</button></div>
        </div>
      </div>

      <!-- Crear Conductor -->
      <div v-if="createSection === 'driver'" class="card mb-3">
        <div class="card-body">
          <h6>👷 Crear Conductor</h6>
          <div class="row g-2">
            <div class="col-md-4"><input v-model.number="driverForm.dni" type="number" class="form-control" placeholder="DNI" /></div>
            <div class="col-md-4"><input v-model="driverForm.name" class="form-control" placeholder="Nombre" /></div>
            <div class="col-md-4"><input v-model="driverForm.lastName" class="form-control" placeholder="Apellido" /></div>
            <div class="col-md-12 d-grid mt-2"><button class="btn btn-success" :disabled="!canCreateDriver" @click="createDriver">Crear</button></div>
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

  <!-- Modal para crear usuario -->
  <div v-if="showCreateUserModal" style="position:fixed; inset:0; background: rgba(0,0,0,0.5); z-index:1050; display:flex; align-items:center; justify-content:center;" @click.self="closeCreateUserModal">
    <div class="card" style="max-width: 500px; width: 90%;">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="mb-0">👤 Crear Usuario</h5>
          <button class="btn btn-sm btn-outline-secondary" @click="closeCreateUserModal">Cerrar</button>
        </div>
        <div class="mb-3">
          <label class="form-label">Nombre de usuario</label>
          <input v-model="userForm.username" class="form-control" placeholder="usuario123" />
        </div>
        <div class="mb-3">
          <label class="form-label">Contraseña</label>
          <input v-model="userForm.password" type="password" class="form-control" placeholder="********" />
        </div>
        <div class="mb-3">
          <label class="form-label">Rol</label>
          <select v-model="userForm.role" class="form-select">
            <option value="">Seleccionar rol...</option>
            <option value="ADMIN">ADMIN</option>
            <option value="OPERADOR">OPERADOR</option>
            <option value="VISOR">VISOR</option>
          </select>
        </div>
        <div v-if="userCreateResult" :class="userCreateResult.ok ? 'alert alert-success' : 'alert alert-danger'">
          {{ userCreateResult.message }}
        </div>
        <div class="d-grid">
          <button class="btn btn-success" :disabled="!canCreateUser" @click="createUser">Crear</button>
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
    const showCreateUserModal = ref(false)
    const userForm = ref({ username: '', password: '', role: '' })
    const userCreateResult = ref(null)
    const role = ref('')

    // Admin sections/forms
    const createSection = ref('')
    const listSection = ref('')
    const trucksList = ref([])
    const driversList = ref([])
    const customersList = ref([])
    const productsList = ref([])
    const usersList = ref([])
    
    // Paginación
    const pagination = ref({
      orders: { page: 0, size: 10, totalPages: 0, totalElements: 0 },
      trucks: { page: 0, size: 10, totalPages: 0, totalElements: 0 },
      drivers: { page: 0, size: 10, totalPages: 0, totalElements: 0 },
      customers: { page: 0, size: 10, totalPages: 0, totalElements: 0 },
      products: { page: 0, size: 10, totalPages: 0, totalElements: 0 },
      users: { page: 0, size: 10, totalPages: 0, totalElements: 0 }
    })
    
    const truckForm = ref({ licensePlate: '', description: '', cisterns: [{ licence_plate: '', capacity: null }] })
    const expandedTrucks = ref({})
    const expandedDrivers = ref({})
    const expandedDriverTrucks = ref({})
    const driverTrucks = ref({})
    const canCreateTruck = computed(() => !!truckForm.value.licensePlate && truckForm.value.cisterns.filter(c => !!c.licence_plate && c.capacity > 0).length > 0)
    const driverForm = ref({
      dni: null,
      name: '',
      lastName: ''
    })
    const canCreateDriver = computed(() => {
      return !!driverForm.value.dni && !!driverForm.value.name && !!driverForm.value.lastName
    })
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
        // Cargar órdenes por defecto para ADMIN, OPERADOR y VIEWER
        if (role.value === 'ADMIN' || role.value === 'OPERADOR' || role.value === 'VIEWER') {
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
    const openCreateUserModal = () => {
      userForm.value = { username: '', password: '', role: '' }
      userCreateResult.value = null
      showCreateUserModal.value = true
    }
    const closeCreateUserModal = () => {
      showCreateUserModal.value = false
    }
    const canCreateUser = computed(() => {
      return !!userForm.value.username && !!userForm.value.password && !!userForm.value.role
    })
    const createUser = async () => {
      userCreateResult.value = null
      try {
        await api.post('/login/signup', {
          username: userForm.value.username,
          password: userForm.value.password,
          role: userForm.value.role
        })
        userCreateResult.value = { ok: true, message: 'Usuario creado correctamente.' }
        userForm.value = { username: '', password: '', role: '' }
      } catch (e) {
        userCreateResult.value = { ok: false, message: e.response?.data?.message || 'Error creando usuario' }
      }
    }

    // Admin helpers
    const toggleCreate = (what) => {
      createSection.value = createSection.value === what ? '' : what
    }
    
    const loadList = async (what, page = 0) => {
      listSection.value = what
      try {
        if (what === 'orders') { 
          const endpoint = role.value === 'VIEWER' ? '/orders/my-orders' : '/orders'
          const r = await api.get(endpoint, { params: { page, size: 10 } })
          orders.value = r.data.content || []
          pagination.value.orders = { page, size: 10, totalPages: r.data.totalPages, totalElements: r.data.totalElements }
        }
        if (what === 'truck') { 
          const r = await api.get('/trucks', { params: { page, size: 10 } })
          trucksList.value = r.data.content || []
          pagination.value.trucks = { page, size: 10, totalPages: r.data.totalPages, totalElements: r.data.totalElements }
        }
        if (what === 'driver') { 
          const r = await api.get('/drivers', { params: { page, size: 10 } })
          driversList.value = r.data.content || []
          pagination.value.drivers = { page, size: 10, totalPages: r.data.totalPages, totalElements: r.data.totalElements }
        }
        if (what === 'customer') { 
          const r = await api.get('/customers', { params: { page, size: 10 } })
          customersList.value = r.data.content || []
          pagination.value.customers = { page, size: 10, totalPages: r.data.totalPages, totalElements: r.data.totalElements }
        }
        if (what === 'product') { 
          const r = await api.get('/products', { params: { page, size: 10 } })
          productsList.value = r.data.content || []
          pagination.value.products = { page, size: 10, totalPages: r.data.totalPages, totalElements: r.data.totalElements }
        }
        if (what === 'users') { 
          const r = await api.get('/login/users', { params: { page, size: 10 } })
          usersList.value = r.data.content || []
          pagination.value.users = { page, size: 10, totalPages: r.data.totalPages, totalElements: r.data.totalElements }
        }
      } catch (e) {
        alert(e.response?.data?.message || 'Error al listar ' + what)
      }
    }
    
    const changePage = (what, newPage) => {
      loadList(what, newPage)
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

    const addCisternRow = () => {
      truckForm.value.cisterns.push({ licence_plate: '', capacity: null })
    }

    const removeCisternRow = (index) => {
      if (truckForm.value.cisterns.length > 1) {
        truckForm.value.cisterns.splice(index, 1)
      } else {
        alert('Debe tener al menos una cisterna')
      }
    }

    const createTruck = async () => {
      try {
        await api.post('/trucks', {
          licensePlate: truckForm.value.licensePlate,
          description: truckForm.value.description,
          // el backend espera `truncker` con arreglo de cisternas
          truncker: truckForm.value.cisterns.filter(c => !!c.licence_plate && c.capacity > 0)
        })
        alert('Camión creado')
        truckForm.value = { licensePlate: '', description: '', cisterns: [{ licence_plate: '', capacity: null }] }
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

    return { orders, refresh, logout, user, openCreateModal, showCreateModal, role, toggleCreate, loadList, createSection, listSection, trucksList, driversList, customersList, productsList, usersList, truckForm, driverForm, customerForm, productForm, createTruck, createDriver, createCustomer, createProduct, canCreateTruck, expandedTrucks, toggleTruck, expandedDrivers, expandedDriverTrucks, driverTrucks, toggleDriver, toggleDriverTruck, addCisternRow, removeCisternRow, canCreateDriver, showCreateUserModal, openCreateUserModal, closeCreateUserModal, userForm, userCreateResult, canCreateUser, createUser, pagination, changePage }
  }
}
</script>
