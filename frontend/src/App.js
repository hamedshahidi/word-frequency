import './App.css';
import React, { useState } from 'react';
import axios from 'axios';

function App() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [kValue, setKValue] = useState('');

  const handleFileChange = (event) => {
    setSelectedFile(event.target.files[0]);
  };

  const handleKValueChange = (event) => {
    setKValue(event.target.value);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!selectedFile) {
      alert('Please select a file.');
      return;
    }

    if (!kValue) {
      alert('Please enter a value for K.');
      return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('k', kValue);

    try {
      const response = await axios.post('/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      // Handle response from the backend API
      console.log(response.data);

    } catch (error) {
      console.error(error);
      // Handle error from the backend API
    }
  };

  return (
    <div>
      <h1>Top K Words</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="fileInput">Select File:</label>
          <input type="file" id="fileInput" onChange={handleFileChange} />
        </div>
        <div>
          <label htmlFor="kValueInput">K Value:</label>
          <input type="number" id="kValueInput" value={kValue} onChange={handleKValueChange} />
        </div>
        <button type="submit">Submit</button>
      </form>
    </div>
  );
}

export default App;
