import React, { useState, useEffect, use } from 'react';
import axios from "axios";
import { useNavigate } from 'react-router-dom';
import '../Styles/Dashboard.css';

function Dashboard({ setCurrentRoom, setUsername }) {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [newRoomName, setNewRoomName] = useState('');
  const [pinger, setCreateUsername] = useState('');
  const [joinUsername, setJoinUsername] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const navigate = useNavigate();

  const backendAPI = process.env.REACT_APP_API_URL 
  // Fetch available rooms
  useEffect(() => {
    fetchRooms();
  }, []);

  const fetchRooms = async () => {
    setLoading(true);
    setError(null);
    console.log(backendAPI)
    try {
      const response = await axios.get(backendAPI+'/fetchRooms');
      // {Headers:{
      //   'Authorization':'Basic' + btoa('user:f656073c-447b-4c7e-9ea1-30520066a539')
      // }}
      console.log(response.data)
      
      if (response.status !== 200) {
        throw new Error(`Server returned ${response.status}: ${response.statusText}`);
      }
      const data = response.data;

      // Transform the array of IDs into room objects
      const formattedRooms = Object.entries(data).map(([id,roomName])=>({
        id: id,
        name: roomName,
        participants: Math.floor(Math.random() * 10) + 1 // Random number for demo
      }));
      setRooms(formattedRooms)
      
    } catch (err) {
      console.error('Error fetching rooms:', err);
      setError('Failed to load rooms. Please try again later.');
      // For demo, create dummy data
      setRooms([
        { id: '1', name: 'General Chat', participants: 5 },
        { id: '2', name: 'Tech Discussion', participants: 3 },
        { id: '3', name: 'Coffee Break', participants: 2 },
        { id: '4', name: 'Project Alpha', participants: 8 },
        { id: '5', name: 'Random Talk', participants: 4 },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRoom = async (e) => {
    e.preventDefault()
    if (!newRoomName || !pinger) return;
    setLoading(true);
    try {
      //API call
      const response = await axios.post(backendAPI+'/create',{roomName:newRoomName});
      // ,{Headers:{
      //   'Authorization':'Basic' + btoa('user:f656073c-447b-4c7e-9ea1-30520066a539')
      // }}
      if (response.status !== 200) { 
        throw new Error(`Server returned ${response.status}: ${response.statusText}`);
      }
      console.log(response)
    //Parse the JSON response
    const data = response.data;
    let guid = data.guid
    console.log(data)
    console.log(data.guid)

      // Update the room in the parent component
      const newRoom = {
        id: guid,
        name: newRoomName,
        participants: 1
      };
      
      setCurrentRoom(newRoom);
      setUsername(pinger);
      
    // check if roomId exists before navigating
  if (guid) {
    navigate(`/chat/${guid}`);
  } else {
    console.error("Room ID is undefined or empty");
    setError("Failed to get a valid room ID");
  }

    } catch (err) {
      console.error('Error creating room:', err);
      setError('Failed to create room. Please try again.');
      
      // For demo, create a dummy room and navigate
      const dummyRoomId = `room-${Date.now()}`;
      const dummyRoom = {
        id: dummyRoomId,
        name: newRoomName,
        participants: 1
      };
      
      setCurrentRoom(dummyRoom);
      setUsername(pinger);
      navigate(`/chat/${dummyRoomId}`);
    } finally {
      setLoading(false);
      setShowCreateModal(false);
    }
  };

  const openJoinModal = (room) => {
    setSelectedRoom(room);
    setShowJoinModal(true);
  };

  const handleJoinRoom = () => {
    if (!selectedRoom || !joinUsername) return;
    
    setCurrentRoom(selectedRoom);
    setUsername(joinUsername);
    navigate(`/chat/${selectedRoom.id}`);
    setShowJoinModal(false);
  };

  return (
    <div className="room-listing-page">
      <header className='header'>
        <h1>Welcome to Ping-Pong!!</h1>
        <button 
          className="create-room-btn"
          onClick={() => setShowCreateModal(true)}
          disabled={loading}
        >
          Create a Zone
        </button>
      </header>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading">Loading zones...</div>
      ) : (
        <div className="rooms-list">
          <h2 className='header'>Available Zones</h2>
          {rooms && rooms.length > 0 ? (
            <ul className="room-list">
              {rooms.map(room => (
                <li key={room.id} className="room-item">
                  <div className="room-info">
                    <h3>{room.name}</h3>
                    <p>{room.participants} participants online</p>
                  </div>
                  <button 
                    className="join-btn"
                    onClick={() => openJoinModal(room)}
                  >
                    Join a table
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p className="no-rooms">No Zones available. Create one!</p>
          )}
        </div>
      )}

      {/* Create Room Modal */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal">
            <h2>Create New Zone</h2>
            <div className="form-group">
              <label>Zone Name:</label>
              <input
                type="text"
                value={newRoomName}
                onChange={(e) => setNewRoomName(e.target.value)}
                placeholder="Enter room name"
              />
            </div>
            <div className="form-group">
              <label>Your Name:</label>
              <input
                type="text"
                value={pinger}
                onChange={(e) => setCreateUsername(e.target.value)}
                placeholder="Enter your name"
              />
            </div>
            <div className="modal-actions">
              <button onClick={() => setShowCreateModal(false)}>Cancel</button>
              <button 
                onClick={handleCreateRoom}
                disabled={!newRoomName || !pinger || loading}
              >
                Create Zone
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Join Room Modal */}
      {showJoinModal && (
        <div className="modal-overlay">
          <div className="modal">
            <h2>Join {selectedRoom?.name}</h2>
            <div className="form-group">
              <label>Your Name:</label>
              <input
                type="text"
                value={joinUsername}
                onChange={(e) => setJoinUsername(e.target.value)}
                placeholder="Enter your name"
              />
            </div>
            <div className="modal-actions">
              <button onClick={() => setShowJoinModal(false)}>Cancel</button>
              <button 
                onClick={handleJoinRoom}
                disabled={!joinUsername}
              >
                Join this table
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;