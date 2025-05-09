import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import RoomListingPage from './Components/Dashboard';
import ChatPage from './Components/Chat';
import './Styles/App.css';

function App() {
  const [currentRoom, setCurrentRoom] = useState(null);
  const [username, setUsername] = useState('');

  return (
    <Router>
      <div className="App">
        <Routes>
          <Route 
            path="/" 
            element={<RoomListingPage setCurrentRoom={setCurrentRoom} setUsername={setUsername} />} 
          />
          <Route 
            path="/chat/:roomId" 
            element={
              currentRoom ? (
                <ChatPage roomData={currentRoom} username={username} setUsername={setUsername} />
              ) : (
                <Navigate to="/" replace />
              )
            } 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;