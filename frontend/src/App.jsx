import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './Login'
import Cadastro from './Cadastro'
import Perfil from './Perfil' // <-- Adicione isso

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" />} />
      <Route path="/login" element={<Login />} />
      <Route path="/cadastro" element={<Cadastro />} />
      <Route path="/perfil" element={<Perfil />} /> {/* <-- Adicione isso */}
    </Routes>
  )
}

export default App