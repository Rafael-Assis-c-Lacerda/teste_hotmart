import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

function Perfil() {
  const [usuario, setUsuario] = useState(null)
  const [erro, setErro] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    const buscarMeusDados = async () => {
      let token = localStorage.getItem('meu_token_jwt');

      if (!token) {
        navigate('/login');
        return;
      }

      try {
        // 1. Primeira tentativa com o Access Token atual
        let resposta = await fetch('http://localhost:8080/usuarios/meus-dados', {
          method: 'GET',
          headers: { 'Authorization': 'Bearer ' + token },
          credentials: 'include' // Envia os cookies se necessário
        });

        // 2. Se o Access Token venceu (Erro 401 ou 403)
        if (resposta.status === 403 || resposta.status === 401) {
          console.log("Access Token expirado! Tentando renovação invisível...");
          
          // Tenta renovar batendo na rota de refresh
         const refreshResposta = await fetch('http://localhost:8080/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }, 
            credentials: 'include' 
          });

          if (refreshResposta.ok) {
            // Sucesso na renovação!
            const dadosRefresh = await refreshResposta.json();
            token = dadosRefresh.token; 
            localStorage.setItem('meu_token_jwt', token);
            
            // Refaz a chamada para os dados do usuário com o NOVO token
            resposta = await fetch('http://localhost:8080/usuarios/meus-dados', {
              method: 'GET',
              headers: { 'Authorization': 'Bearer ' + token },
              credentials: 'include'
            });
          } else {
            // Se o Refresh Token também expirou, chuta pro login
            setErro('Sessão expirada. Faça login novamente.');
            localStorage.removeItem('meu_token_jwt');
            return;
          }
        }

        // 3. Exibe os dados se tudo deu certo
        if (resposta.ok) {
          const dados = await resposta.json();
          setUsuario(dados);
        } else {
          setErro('Erro ao carregar seus dados.');
        }

      } catch (erro) {
        setErro('Erro crítico ao conectar com o servidor.');
      }
    }

    buscarMeusDados();
  }, [navigate])

  // ==========================================
  // NOVA FUNÇÃO DE SAIR DA CONTA (LOGOUT COMPLETO)
  // ==========================================
  const sairDaConta = async () => {
    try {
      // 1. Pede pro Java destruir o cookie de renovação invisível
      await fetch('http://localhost:8080/auth/logout', {
        method: 'POST',
        credentials: 'include' // OBRIGATÓRIO: Permite o envio do cookie para ser destruído
      });
    } catch (error) {
      console.error("Erro ao deslogar no backend", error);
    } finally {
      // 2. Apaga o crachá rápido do cofre do React
      localStorage.removeItem('meu_token_jwt');
      // 3. Manda para a tela de Login
      navigate('/login');
    }
  }

  if (erro) return <div style={{ padding: '50px' }}><h3>{erro}</h3><button onClick={() => navigate('/login')}>Voltar</button></div>;
  if (!usuario) return <div style={{ padding: '50px' }}>Carregando seus dados...</div>;

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