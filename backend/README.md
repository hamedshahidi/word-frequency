## Backend Features

- The API has endpoint that takes a text file and a value for K as input, reads the text file and return the K most frequent words and their frequency in descending order.
- The API has tests to ensure its functionality.

- (Bonus) Backend implements a caching mechanism to avoid re-calculating the top K most frequent words every time the API is called with the same text file and K value.

## Installation And

### Backend Setup

1. Install [Java 17](https://www.oracle.com/java/technologies/).
2. Install [Redis Server](https://redis.io/download).
3. Clone the repository:
   ```
   https://github.com/hamedshahidi/word-frequency
   ```
4. Open the backend project in your preferred IDE.
5. Build and run the project.

## Running the Application

- Start the backend API.

## Testing

- Run the backend tests using the command:
  ```
  mvn test
  ```
