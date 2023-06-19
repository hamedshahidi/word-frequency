import './App.css';
import React, { useState } from 'react';
import axios from 'axios';
import 'bootstrap/dist/css/bootstrap.min.css';
import ProgressBar from 'react-bootstrap/ProgressBar';


function App() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [kValue, setKValue] = useState('');
  const [wordFrequency, setWordFrequency] = useState([]);
  const [processingChunks, setProcessingChunks] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [processingProgress, setProcessingProgress] = useState(0);

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
    if (processingChunks) {
      // Prevent submitting another request if processing is already in progress
      return;
    }
  
    const chunkSize = 1024 * 1024; // 1MB chunk size
    const fileSize = selectedFile.size;
    let offset = 0;
  
    const handleChunkLoad = (event) => {
      const chunk = event.target.result;
      const formData = new FormData();
      formData.append('file', new Blob([chunk]));
      formData.append('k', kValue);
      formData.append('offset', offset);
    
      // Send the chunk to the backend API
      axios
        .post('/upload-chunk', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
          onUploadProgress: (progressEvent) => {
            const progress = Math.round((100 * progressEvent.loaded) / progressEvent.total);
            setUploadProgress(progress);
          },
        })
        .then((response) => {
          // Handle response from the backend API
          console.log(response.data);
    
          // Update the offset for the next chunk
          offset += chunkSize;
    
          // Check if there are more chunks to read
          if (offset < fileSize) {
            readNextChunk();
          } else {
            // All chunks have been processed
            setProcessingChunks(false);
            setUploadProgress(0); // Reset upload progress
            fetchData();
          }
        })
        .catch((error) => {
          console.error(error);
          setProcessingChunks(false);
          setUploadProgress(0); // Reset upload progress
          // Handle error from the backend API
        });
    };
  
    const readNextChunk = () => {
      const fileReader = new FileReader();
      const chunk = selectedFile.slice(offset, offset + chunkSize);
      fileReader.onload = handleChunkLoad;
      fileReader.readAsArrayBuffer(chunk);
    };
  
    // Start reading the first chunk
    setProcessingChunks(true);
    readNextChunk();
  };

  const fetchData = async () => {
    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('k', kValue);
  
    try {
      const response = await axios.post('/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (progressEvent) => {
          const progress = Math.round((100 * progressEvent.loaded) / progressEvent.total);
          setProcessingProgress(progress);
        },
      });
  
      // Handle response from the backend API
      const words = response.data.words;
      const frequencies = response.data.frequencies;
      const wordFrequencyData = words.map((word, index) => ({
        word: word,
        frequency: frequencies[index],
      }));
      setWordFrequency(wordFrequencyData);
      console.log(response.data);
    } catch (error) {
      console.error(error);
      // Handle error from the backend API
    } finally {
      setProcessingProgress(0); // Reset processing progress
    }
  };

  return (
    <div className="container">
      <h1 className="mt-5">Top K Words</h1>
      <form onSubmit={handleSubmit}>
        {/* File input */}
        <div className="mb-3">
          <label htmlFor="fileInput" className="form-label">
            Select File:
          </label>
          <input type="file" className="form-control" id="fileInput" onChange={handleFileChange} />
        </div>
  
        {/* K value input */}
        <div className="mb-3">
          <label htmlFor="kValueInput" className="form-label">
            K Value:
          </label>
          <input type="number" className="form-control" id="kValueInput" value={kValue} onChange={handleKValueChange} />
        </div>
  
        {/* Submit button */}
        <button type="submit" className="btn btn-primary">
          Submit
        </button>
  
        {/* Upload progress bar */}
        {processingChunks && (
          <ProgressBar now={uploadProgress} label={`${uploadProgress}%`} className="mt-3" animated striped />
        )}
  
        {/* Processing progress bar */}
        {!processingChunks && processingProgress > 0 && (
          <ProgressBar now={processingProgress} label={`${processingProgress}%`} className="mt-3" animated striped />
        )}
      </form>
  
      {/* Word frequency table */}
      <table className="table mt-4">
        {/* Table headers */}
        <thead>
          <tr>
            <th>Word</th>
            <th>Frequency</th>
          </tr>
        </thead>
  
        {/* Table body */}
        <tbody>
          {wordFrequency.map((entry) => (
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
