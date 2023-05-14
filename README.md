[![Scala CI](https://github.com/vega113/projectEmo/actions/workflows/scala.yml/badge.svg)](https://github.com/vega113/projectEmo/actions/workflows/scala.yml)
# Emotion Tracker

Emotion Tracker is a web application that allows users to track and analyze their emotions over time. Users can log in and record their emotions, including the intensity of the emotion, sub-emotions, and triggers that contributed to the emotion. They can also view their emotion history and analyze trends in their emotions over time.

## Features

- User authentication: Users can log in and securely authenticate their identity using JWT tokens.
- Emotion recording: Users can record their emotions, including the intensity of the emotion, sub-emotions, and triggers that contributed to the emotion.
- Emotion history: Users can view a history of their recorded emotions.
- Trend analysis: Users can analyze trends in their emotions over time using charts and graphs.

## Technology Stack

- Play Framework: A web application framework for the Scala programming language.
- MySQL: An open-source relational database management system.
- Anorm: A Scala library for interacting with relational databases.
- JWT: A JSON-based authentication protocol used to secure web applications.
- Angular: A TypeScript-based open-source web application framework.

## Getting Started

### Prerequisites

- JDK 1.8 or later
- sbt 1.5.5 or later
- MySQL 8.0 or later
- Docker

### Installation
```agsl
npm install date-fns --save
npm install karma-jasmine --save-dev                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
npm install --save-dev karma                                                                                                                                                         
npm install -g karma-cli                                                                                                                                                             
npm install jasmine-core --save-dev                                                                                                                                                  
npm install karma-coverage-istanbul-reporter --save-dev   
npm install @auth0/angular-jwt
npm install @angular/material
```


1. Clone the repository: `git clone https://github.com/<username>/emotion-tracker.git`
2. Change into the project directory: `cd emotion-tracker`
3. Create the database using docker : `docker compose up -d`
4. Set the database connection properties in `conf/application.conf`.
5. Start the application: `sbt run`

## Usage

1. Navigate to `http://localhost:9000` in a web browser.
2. Log in with your user credentials or sign up for a new account.
3. Record your emotions using the web interface.
4. View your emotion history and analyze trends over time.

## Contributing

Contributions to Emotion Tracker are welcome and encouraged! To contribute, please follow these steps:

1. Fork the repository on GitHub.
2. Create a new branch for your feature or bug fix.
3. Write tests for your changes to ensure that they don't break existing functionality.
4. Implement your feature or bug fix.
5. Push your changes to your fork.
6. Submit a pull request to the main repository.

## License

This project is licensed under the GNU General Public License v3.0 - see the LICENSE.txt file for details.


