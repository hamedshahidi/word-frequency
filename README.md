# Word Frequency Application

This is a full-stack web application that finds the top K most frequent words in a given text file. The backend is built with Java 17 using Spring Boot, while the frontend is built with React.

## Features

### Backend

- The API has endpoint that takes a text file and a value for K as input, reads the text file and return the K most frequent words and their frequency in descending order.
- The API has tests to ensure its functionality.

### Frontend

- The frontend allows users to upload a text file and specify a value for K, then displays the top K most frequent words and their frequency in a table
- The frontend is responsive so it adapts well to both small and large screens.

## Bonus features

- Backend implements a caching mechanism to avoid re-calculating the top K most frequent words every time the API is called with the same text file and K value.
- Frontend displays a progress bar on the frontend while the API is processing the file and finding the top K most frequent words.

## Installation

### Backend Setup

1. Install [Java 17](https://www.oracle.com/java/technologies/).
2. Install [Redis Server](https://redis.io/download).
3. Clone the repository:
   ```
   https://github.com/hamedshahidi/word-frequency
   ```
4. Open the backend project in your preferred IDE.
5. Build and run the project.

### Frontend Setup

1. Install [Node.js](https://nodejs.org/).
2. Install [npm](https://www.npmjs.com/).
3. Clone the repository: (if you already haven't!)
   ```
   https://github.com/hamedshahidi/word-frequency
   ```
4. Open the frontend project in your preferred IDE.
5. Navigate to frontend project Install the project dependencies:
   ```
   npm install
   ```

## Running the Application

1. Start the backend API.
2. Start the frontend app by running:
   ```
   npm start
   ```
3. Access the application in your browser at: `http://localhost:3000`

## Usage

1. Open the application in your browser.
2. Upload a text file.
3. Specify the value of K.
4. Submit the form by clicking the button.
5. View the top K most frequent words and their frequency in the displayed table.

## Known Limitations

//TODO

## Contact Information

For any inquiries or support, please contact hamed.shahidi.dev@gmail.com.

## License

This project is licensed under the GNU GENERAL PUBLIC LICENSE. See the [LICENSE](LICENSE) file for details.
