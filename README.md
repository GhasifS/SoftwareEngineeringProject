![Build and Test](https://github.com/drphamwit/swe-sample-project-github-repo/workflows/Build%20and%20Test/badge.svg)

# Cryptext
A more secure way to chat.

## Introduction

An end-to-end encrypted chat client and server.

## Features
### Functional Requirements
1. Manage account
    1. The system sends a welcome email to the user upon account creation.
    2. The system allows users to change their password.
2. Add and manage contacts
    1. The system allows users to add other users
    2. The system allows users to block other users
3. Create account
    1. The system allows users to create accounts
4. Send message
    1. The system allows users to send messages to other users
5. Encrypt messages
    1. All messages are end-to-end encrypted using asymmetric encryption

### Non-functional requirements
1. Security
    * After 10 login attempts, the system locks a users account to protect their information
2. Usability
    * The system allows users to find friends in two clicks
3. Customize profile
    * The system allows users to change their username and password

## Getting Started
### Installation and Setup
1. Download the code
2. Confirm that the correct libraries and dependencies are loaded; A `lib` folder is present that contains said libraries.
3. Configure the VM Options of the `UI.java` instance to: `--module-path %path%;out/production --add-modules javafx.controls,javafx.fxml,java.mail`
4. Import the `chatApp.sql` file into PostgreSQL in order to setup the database with correct tables and relations.
5. Configure the server with the correct database connection information. Set this in `contactDB.java`
6. Optionally configure the server and clients to connect on a different address than the default (`localhost:2020`).
7. Compile the code

### Usage
Run Server.main to start the server. It will listen for connections on the configured port (default `2020`).
Run Client.main to start the text client. It will connect to the server on the configured port and address (default `localhost:2020`).
Run UI.main to start the graphical client. It will connect to the server on the configured port and address (default `localhost:2020`).

## Demo video
https://youtu.be/s7yOMBDmo00

## Contributors

* Alejandro Ramos (aeramos.work@gmail.com), Backend Developer
* Ghasif Syed (syedg@wit.edu), Database and Email Specialist
* Ryan Salazar (salazarr2@wit.edu), UI Engineer
