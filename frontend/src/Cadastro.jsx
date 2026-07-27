import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

function Cadastro() {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [login, setLogin] = useState('');
  const [senha, setSenha] = useState('');
  
  const navigate = useNavigate();

  const fazerCadastro = async (e) => {
    e.preventDefault();

    const novoUsuario = { nome, email, login, senha };

    try {
      const resposta = await fetch('http://localhost:8080/usuarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(novoUsuario)
      });

      if (resposta.ok) {
        alert('Usuário cadastrado com sucesso!');
        navigate('/login'); // Joga o usuário de volta pra tela de login
      } else {
        alert('Erro ao cadastrar usuário.');
      }
    } catch (erro) {
      console.error("Erro ao conectar com o servidor:", erro);
    }
  }

  return (
    <div style={{ padding: '50px', fontFamily: 'Arial' }}>
      <h2>Criar Nova Conta</h2>
      <form onSubmit={fazerCadastro} style={{ display: 'flex', flexDirection: 'column', width: '300px', gap: '10px' }}>
        <input type="text" placeholder="Nome Completo" value={nome} onChange={(e) => setNome(e.target.value)} required />
        <input type="email" placeholder="E-mail" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input type="text" placeholder="Login" value={login} onChange={(e) => setLogin(e.target.value)} required />
        <input type="password" placeholder="Senha" value={senha} onChange={(e) => setSenha(e.target.value)} required />
        
        <button type="submit">Cadastrar</button>
      </form>
      <p>Já tem uma conta? <Link to="/login">Voltar para o Login</Link></p>
    </div>
  )
}

export default Cadastro