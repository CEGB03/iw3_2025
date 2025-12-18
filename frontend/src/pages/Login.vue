<template>
  <div class="col-md-6 offset-md-3">
    <h3>Login</h3>
    <form @submit.prevent="login">
      <div class="mb-3">
        <label class="form-label">Usuario</label>
        <input v-model="username" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">Contraseña</label>
        <input v-model="password" type="password" class="form-control" />
      </div>
      <div class="mb-3">
        <button class="btn btn-primary">Ingresar</button>
      </div>
      <div v-if="error" class="alert alert-danger">{{ error }}</div>
    </form>
  </div>
</template>

<script>
import api from '../services/api'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export default {
  setup() {
    const username = ref('')
    const password = ref('')
    const error = ref(null)
    const router = useRouter()

    const login = async () => {
      error.value = null
      try {
        const res = await api.post('/login', { username: username.value, password: password.value })
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('username', res.data.username)
        router.push('/orders')
      } catch (e) {
        error.value = e.response?.data?.message || 'Error en autenticación'
      }
    }

    return { username, password, login, error }
  }
}
</script>
