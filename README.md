# 📧 Email Service Java

This project is the final assignment for the *Programming 3* course. It implements both the **client-side** and **server-side** of a basic email service, using Java and JavaFX.

## 🖥️ Overview

The system simulates email exchange through a local client-server architecture. Both the client and the server feature a **graphical user interface (GUI)** built with FXML and styled with CSS. The goal is to demonstrate knowledge of JavaFX, multithreading, file I/O, and JSON serialization.

## 🔧 Features

- Full implementation of both client and server applications
- Both sides feature a modern **JavaFX GUI**
- Emails are stored locally using **JSON files**:
  - Each client has its own inbox file
  - The server stores a separate folder for each client
- The **server** runs a continuous loop, listening for incoming connections and dispatching each request to a dedicated thread using a **thread pool of service executors**
- File access is **synchronized using locks** to ensure safe concurrent read/write operations on the JSON files

## 🧪 How It Works

- At launch, the client **randomly selects a predefined email identity** from a pool of available users (no manual login or account creation)
- Communication between client and server occurs through sockets
- The project follows the course’s guidelines, **excluding features like registration or authentication** to focus on architectural and technical correctness

## 📁 Data Management

- All email and user data is stored as JSON in the `/resources/` folder
- File-level locking ensures mutual exclusion when accessing inboxes
- Each client has its own dedicated inbox file both on the client and server sides

## 🚧 Current Status

- The project is currently written in **Italian** and follows the internal naming conventions used during development
- Future refactoring is planned to:
  - Translate all class, variable, and resource names to English
  - Improve code structure and maintainability

## 🌱 Possible Expansions

The following features are not currently implemented but are considered for future releases:

- 📓 **Email event logging**: Track deletions, deliveries, and reads. This would enable multiple simultaneous client instances using the **same account**
- 📎 **File attachment support**: Add support for sending and receiving attachments alongside the email body
- 🔐 **Account creation and login**: Introduce real user registration and authentication systems
