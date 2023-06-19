import './App.css';
import React, { useState } from 'react';
import axios from 'axios';
import 'bootstrap/dist/css/bootstrap.min.css';


function App() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [kValue, setKValue] = useState('');
  const [wordFrequency, setWordFrequency] = useState([]);

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
      const words = response.data.words;
      const frequencies = response.data.frequencies;
      const wordFrequencyData = words.map((word, index) => ({
        word: word,
        frequency: frequencies[index]
      }));
      setWordFrequency(wordFrequencyData);
      console.log(response.data);

    } catch (error) {
      console.error(error);
      // Handle error from the backend API
    }
  };

  return (
    <div className="container">
      <h1 className="mt-5">Top K Words</h1>
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="fileInput" className="form-label">Select File:</label>
          <input type="file" className="form-control" id="fileInput" onChange={handleFileChange} />
        </div>
        <div className="mb-3">
          <label htmlFor="kValueInput" className="form-label">K Value:</label>
          <input type="number" className="form-control" id="kValueInput" value={kValue} onChange={handleKValueChange} />
        </div>
        <button type="submit" className="btn btn-primary">Submit</button>
      </form>
      <table className="table mt-4">
        <thead>
          <tr>
            <th>Word</th>
            <th>Frequency</th>
          </tr>
        </thead>
        <tbody>
          {wordFrequency && wordFrequency.map((entry) => (
            <tr key={entry.word}>
              <td>{entry.word}</td>
              <td>{entry.frequency}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default App;
