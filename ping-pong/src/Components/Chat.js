import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import '../Styles/Chat.css';

function Chat({ roomData, username, setUsername }) {
  const { roomId } = useParams();
  const navigate = useNavigate();
  
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [participants, setParticipants] = useState([]);
  const [connected, setConnected] = useState(false);
  const [showNamePrompt, setShowNamePrompt] = useState(!username);
  const [tempUsername, setTempUsername] = useState('');
  const [error, setError] = useState(null);

  const websocketURL = process.env.REACT_APP_WEBSOCKET_API_URL
  const socketRef = useRef(null);
  const messagesEndRef = useRef(null);

  // Handle initial connection and cleanup
  useEffect(() => {
    if (username && !connected) {
      connectToRoom();
    }

    return () => {
      disconnectFromRoom();
    };
  }, [roomId, username]);


  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const connectToRoom = () => {
    try {
      const encodedUsername = encodeURIComponent(username);

      const wsUrl = websocketURL + `/roomId=${roomId}?username=${encodedUsername}`;
      const socket = new WebSocket(wsUrl);
      socketRef.current = socket;


      socket.onopen = () => {
        console.log('WebSocket connection established');
        setConnected(true);
        
        // Send join message with username
        // const joinMessage = {
        //   type: 'JOIN',
        //   username: username,
        //   timestamp: new Date().toISOString()
        // };
        // socket.send(JSON.stringify(joinMessage));
        
        // Add system message
        setMessages(prev => [...prev, { 
          text: `You joined the chat as ${username}`, 
          type: 'system' 
        }]);
      };

      socket.onmessage = (event) => {
        console.log(event.data)
        const message = JSON.parse(event.data);
        
        if (message.type === 'USER_LIST') {
          setParticipants(message.users);
        } else if (message.type === 'USER_JOINED') {
          console.log("in here")
          setParticipants(prev => [...prev, message.username]);
          setMessages(prev => [...prev, { 
            text: `${message.username} joined the chat`, 
            type: 'system' 
          }]);
        } else if (message.type === 'USER_LEFT') {
          setParticipants(prev => prev.filter(user => user !== message.username));
          setMessages(prev => [...prev, { 
            text: `${message.username} left the chat`, 
            type: 'system' 
          }]);
        } else {
          // Regular chat message
          setMessages(prev => [...prev, { 
            text: message.text, 
            type: message.username === username ? 'sent' : 'received', 
            sender: message.username 
          }]);
        }
      };

      // socket.onclose = () => {
      //   console.log('WebSocket connection closed');
      //   setConnected(false);
      //   setMessages(prev => [...prev, { text: 'Disconnected from chat server', type: 'system' }]);
      // };

      socket.onerror = (error) => {
        console.error('WebSocket error:', error);
        setError('Connection error. Please try again.');
        setConnected(false);
      };
    } catch (err) {
      setError(`WebSocket connection error: ${err.message}`);
    }
  };

  const disconnectFromRoom = () => {
    if (socketRef.current) {
      socketRef.current.close();
      socketRef.current = null;
    }
  };

  const handleSetUsername = () => {
    if (!tempUsername.trim()) return;
    
    setUsername(tempUsername);
    setShowNamePrompt(false);
  };

  const sendMessage = (e) => {
    e.preventDefault();
    if (!inputMessage.trim() || !connected || !socketRef.current) return;

    const messageObject = {
      type: 'MESSAGE',
      text: inputMessage,
      username: username,
      timestamp: new Date().toISOString()
    };

    socketRef.current.send(JSON.stringify(messageObject));
    setInputMessage('');
  };

  const leaveRoom = () => {
    disconnectFromRoom();
    navigate('/');
  };

  if (showNamePrompt) {
    return (
      <div className="name-prompt-overlay">
        <div className="name-prompt-modal">
          <h2>Join {roomData?.name || 'Chat Room'}</h2>
          <p>Please enter your name to join the chat</p>
          <input
            type="text"
            value={tempUsername}
            onChange={(e) => setTempUsername(e.target.value)}
            placeholder="Your name"
            autoFocus
          />
          <button 
            onClick={handleSetUsername}
            disabled={!tempUsername.trim()}
          >
            Join Chat
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-page">
      <div className="chat-header">
        <div className="room-info">
          <h2>{roomData?.name || 'Chat Room'}</h2>
          <span className="participant-count">
            {participants.length} participant{participants.length !== 1 ? 's' : ''}
          </span>
        </div>
        <button onClick={leaveRoom} className="leave-button">
          Leave Room
        </button>
      </div>

      <div className="chat-container">
        <div className="sidebar">
          <h3>Participants</h3>
          <ul className="participants-list">
            {participants.map((user, index) => (
              <li key={index} className={user === username ? 'current-user' : ''}>
                {user} {user === username && '(you)'}
              </li>
            ))}
          </ul>
        </div>

        <div className="chat-main">
          <div className="messages-container">
            {messages.map((msg, index) => (
              <div key={index} className={`message ${msg.type}`}>
                {msg.type === 'received' && <span className="sender">{msg.sender}</span>}
                <div className="message-bubble">{msg.text}</div>
                {msg.type === 'sent' && <span className="sender">You</span>}
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>

          <form onSubmit={sendMessage} className="message-form">
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              placeholder="Type a message..."
              disabled={!connected}
              className="message-input"
            />
            <button 
              type="submit" 
              disabled={!connected || !inputMessage.trim()}
              className="send-button">
              Send
            </button>
          </form>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}
    </div>
  );
}

export default Chat;