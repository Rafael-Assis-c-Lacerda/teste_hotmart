import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

function Perfil() {
  const [usuario, setUsuario] = useState(null)
  const [erro, setErro] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    // Essa função roda automaticamente assim que o usuário entra na tela
    const buscarMeusDados = async () => {
      // 1. Pega o crachá do cofre
      const token = localStorage.getItem('meu_token_jwt');

      // 2. Se não tiver crachá, chuta o cara de volta pro Login na hora
      if (!token) {
        navigate('/login');
        return;
      }

      try {
        // 3. Bate na porta do Spring Boot mostrando o crachá!
        const resposta = await fetch('http://localhost:8080/usuarios/meus-dados', {
          method: 'GET',
          headers: { 
            'Authorization': 'Bearer ' + token 
          }
        });

        if (resposta.ok) {
          const dados = await resposta.json();
          setUsuario(dados); // Guarda os dados do Java na memória do React
        } else {
          // Se o token for inválido ou expirado (Status 403)
          setErro('Sessão expirada. Faça login novamente.');
          localStorage.removeItem('meu_token_jwt'); // Joga fora o token velho
        }
      } catch (erro) {
        setErro('Erro ao conectar com o servidor.');
      }
    }

    buscarMeusDados();
  }, [navigate])

  // Função para deslogar (apagar o token)
  const sairDaConta = () => {
    localStorage.removeItem('meu_token_jwt');
    navigate('/login');
  }

  // Telas de carregamento ou erro
  if (erro) return <div style={{ padding: '50px' }}><h3>{erro}</h3><button onClick={() => navigate('/login')}>Voltar</button></div>;
  if (!usuario) return <div style={{ padding: '50px' }}>Carregando seus dados...</div>;

  // A tela principal com os dados do banco!
  return (
    <div style={{ padding: '50px', fontFamily: 'Arial' }}>
      <h2>Bem-vindo(a), {usuario.nome}!</h2>
      <div style={{ border: '1px solid #ccc', padding: '20px', width: '300px', borderRadius: '8px' }}>
        <p><strong>Login:</strong> {usuario.login}</p>
        <p><strong>Email:</strong> {usuario.email}</p>
        <p><strong>Nível de Acesso:</strong> <span style={{ color: 'blue', fontWeight: 'bold' }}>{usuario.role}</span></p>
      </div>
      
      <button 
        onClick={sairDaConta} 
        style={{ marginTop: '20px', padding: '10px 20px', backgroundColor: '#d9534f', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
      >
        Sair
      </button>
    </div>
  )
}

export default Perfil