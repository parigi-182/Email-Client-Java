# Email Client Java

A client-server email application written in Java, developed as the final assignment for the *Programming 3* course. Both the client and server applications include a graphical user interface built with JavaFX and styled with CSS.

---

## Overview

The system simulates email exchange over a local network using a socket-based client-server architecture. The server manages multiple concurrent client connections through a thread pool and persists all email data as JSON files. Each side of the application — client and server — runs as an independent JavaFX process with its own GUI.

---

## Architecture

**Client**

- At startup, the client randomly selects one of a pool of predefined email identities (no login or registration flow)
- Connects to the server via TCP socket to send and receive emails
- Maintains a local JSON inbox file that mirrors the server-side copy
- UI is defined in FXML and styled with CSS

**Server**

- Runs a continuous listener loop, accepting incoming client connections
- Each connection is dispatched to a dedicated worker thread from an `ExecutorService` thread pool
- One inbox JSON file per registered client is maintained under `resources/`
- All file read/write operations are protected by explicit locks to ensure safe concurrent access

**Communication**

Client-server communication occurs over plain TCP sockets. Serialization and deserialization of email objects use Gson.

---

## Dependencies

| Library | Version |
|---------|---------|
| Java | 11 |
| JavaFX base | 21-ea+5 |
| JavaFX controls | 19.0.2 |
| JavaFX FXML | 19.0.2 |
| Gson | 2.10.1 |

Build is managed with Maven.

---

## Building and running

Build the project:

```sh
mvn clean package
```

Run the server:

```sh
mvn javafx:run -Djavafx.mainClass=com.mailapp.demo.Mains.EmailServerMain
```

Run a client instance:

```sh
mvn javafx:run -Djavafx.mainClass=com.mailapp.demo.Mains.EmailClientMain
```

Both processes must be running simultaneously. Start the server before launching any client.

---

## Data storage

All data is stored under `src/main/resources/`. The server maintains one JSON file per client inbox. The client keeps a local copy of its own inbox. File-level locking ensures mutual exclusion when multiple threads attempt concurrent reads or writes to the same inbox file.

---

## Screenshots

![Client startup](https://github.com/parigi-182/Email-Client-Java/raw/main/screenshots/start.png)
![Compose window](https://github.com/parigi-182/Email-Client-Java/raw/main/screenshots/write.png)
![Reply / Re: thread](https://github.com/parigi-182/Email-Client-Java/raw/main/screenshots/reply_re.png)
![JSON inbox storage](https://github.com/parigi-182/Email-Client-Java/raw/main/screenshots/json_emails.png)

---

## Project structure

```
.
├── src/main/
│   ├── java/com/mailapp/demo/
│   │   ├── Mains/
│   │   │   ├── EmailClientMain.java
│   │   │   └── EmailServerMain.java
│   │   └── ...
│   └── resources/
│       └── <client-inboxes>.json
├── screenshots/
├── pom.xml
└── README.md
```

---

## Notes

- Source code identifiers and resources are currently in Italian, following the naming conventions used during development. Translation to English is planned for a future refactor.
- Authentication and account registration are intentionally omitted per course specification.

---

## Possible extensions

- **Event logging**: recording deliveries, reads, and deletions would enable multiple simultaneous sessions on the same account
- **Attachment support**: extending the email model and transfer protocol to handle binary payloads alongside the message body
- **Authentication**: introducing a proper registration and login flow with credential storage
