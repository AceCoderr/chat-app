
import axios from "axios";
import React, { useRef, useState } from 'react';
import './Styles/App.css';

function App() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [guid, setGuid] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error,setError] = useState(null);
  const [connected,setConnected] = useState(false);
  const socketRef = useRef(null);
  const [messages, setMessages] = useState([]);


  const handleCreateChatRoom = async ()=>{
    setLoading(true);
    const loginData = {
      username,
      password
  }
  setUsername('user')
  setPassword('b155be02-de9e-49a8-8f0d-322abffae2f4')
  try{
    const response = await axios.post('http://localhost:8080/create',loginData);
    console.log(response)
    setError(null)
    if (!response.ok) {
      throw new Error(`Server returned ${response.status}: ${response.statusText}`);
    }
    const data = await response.json();
    setGuid(data.guid)
    return data.guid
  }catch(error)
  {
    setError(`Failed to obtain connection ID: ${error.message}`);
    return null
  }
  finally{
    setLoading(false);
  }
}

// Function to establish WebSocket connection
const connectWebSocket = (connectionGuid) => {
  if (!connectionGuid) return;
  
  try {
    // Create WebSocket connection using the GUID
    const wsUrl = `ws://localhost:8080/ws/chat/roomId=${connectionGuid}`;
    const socket = new WebSocket(wsUrl);
    socketRef.current = socket;

    // Set up WebSocket event handlers
    socket.onopen = () => {
      console.log('WebSocket connection established');
      setConnected(true);
      setMessages(prev => [...prev, { text: 'Connected to chat server', type: 'system' }]);
    };

    // socket.onmessage = (event) => {
    //   const message = JSON.parse(event.data);
    //   setMessages(prev => [...prev, { text: message.text, type: 'received', sender: message.sender }]);
    // };

    // socket.onclose = () => {
    //   console.log('WebSocket connection closed');
    //   setConnected(false);
    //   setMessages(prev => [...prev, { text: 'Disconnected from chat server', type: 'system' }]);
    // };

    socket.onerror = (error) => {
      console.error('WebSocket error:', error);
      setError('WebSocket connection error');
      setConnected(false);
    };
  } catch (err) {
    setError(`WebSocket connection error: ${err.message}`);
  }
};


  //Function to initialize the chat
  const initializeChat = async () => {
    const newGuid = await handleCreateChatRoom();
    if (newGuid) {
      connectWebSocket(newGuid);
    }
  };


  return (
    <div className="App">
      <header className="App-header">
        <h1>Ping-Pong</h1>
      </header>
      
      <main className="chat-container">
          <div className="connection-section">
            <button 
              onClick={initializeChat} 
              disabled={loading}
              className="connect-button">
              {loading ? 'Connecting...' : 'Connect to Chat'}
            </button>
            {error && <p className="error-message">{error}</p>}
          </div>
      </main>
    </div>
  );
}

export default App;