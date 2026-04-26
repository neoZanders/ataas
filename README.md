# Automated Teaching Assistant Allocation System (ATAAS)

---

ATAAS is a web-based application designed to automate the allocation of Teaching Assistants (TAs) to course sessions.
The system focuses on reducing manual administrative work while ensuring fair and efficient scheduling based on
constraints such as availability, workload limits, and preferences.

## How to Install and Run

---

### Prerequisites

- Docker and Docker Compose
- Node.js and npm

### Backend

1. Navigate to the project root:
```
cd ataas
```

2. Build and run the backend and database:
```
docker compose up --build
```

3. The backend will start on:
```
http://localhost:8080
```

### Frontend

1. Navigate to the frontend folder:
```
cd frontend
```

2. Install dependencies:
```
npm install
```

3. Start the development server:
```
npm run dev
```

4. Open in browser:
```
http://localhost:5173
```

## How to Use

---

1. Open the frontend:
```
http://localhost:5173
```

2. Open Swagger to test the backend API:
```
http://localhost:8080/swagger-ui/index.html
```

## Credits

---

Developed as part of a Bachelor's Thesis at Chalmers University of Technology & University of Gothenburg.

**Authors:**
- Kusai Al Malt
- Nova Allden
- Anna Blomberg
- Mai Uy Vuong
- Neo Zanders

**Supervisor:**
- Niklas Broberg

## License

---

![License](https://img.shields.io/badge/license-MIT-blue)

## Badges

---

![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-646CFF?logo=vite&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=white)
