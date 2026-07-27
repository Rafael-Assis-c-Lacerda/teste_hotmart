import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom' // <-- Adicione o useNavigate aqui

function Login() {
  const [login, setLogin] = useState('');
  const [senha, setSenha] = useState('');
  const navigate = useNavigate(); // <-- Inicialize ele aqui

  const fazerLogin = async (e) => {
    e.preventDefault();

    try {
      const resposta = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login, senha })
      });

      if (resposta.ok) {
        const dados = await resposta.json();
        localStorage.setItem('meu_token_jwt', dados.token);
        
        // APAGUE O ALERT E COLOQUE ISSO:
        navigate('/perfil'); // Joga o usuário direto pro perfil!
        
      } else {
        alert('Falha no login! Credenciais incorretas.');
      }
// ... resto do código continua igual ...cd
    } catch (erro) {
      console.error("Erro ao conectar com o servidor:", erro);
    }
  }

  return (
    <div style={{ padding: '50px', fontFamily: 'Arial' }}>
      <h2>Entrar no Sistema</h2>
      <form onSubmit={fazerLogin} style={{ display: 'flex', flexDirection: 'column', width: '300px', gap: '10px' }}>
        <input 
          type="text" 
          placeholder="Seu Login" 
          value={login} 
          onChange={(e) => setLogin(e.target.value)} 
          required 
        />
        <input 
          type="password" 
          placeholder="Sua Senha" 
          value={senha} 
          onChange={(e) => setSenha(e.target.value)} 
          required 
        />
        <button type="submit">Entrar</button>
      </form>
      <p>Não tem conta? <Link to="/cadastro">Cadastre-se aqui</Link></p>
    </div>
  )
}

export default Login