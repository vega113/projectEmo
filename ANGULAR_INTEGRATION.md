# Angular Integration with Scala Play Framework

This document explains how to integrate an Angular frontend with a Scala Play Framework backend, based on the setup in this project.

## Project Structure

The project follows this structure:

```
project-root/
├── app/                  # Scala Play backend code
├── conf/                 # Play configuration files
├── project/              # SBT project definition
│   ├── FrontendCommands.scala  # Frontend npm commands
│   ├── FrontendRunHook.scala   # Play run hook for Angular
├── public/               # Compiled frontend assets (generated)
├── ui/                   # Angular frontend code
│   ├── src/              # Angular source code
│   │   ├── app/          # Angular components, services, etc.
│   │   ├── proxy.conf.js # Proxy configuration for development
│   ├── angular.json      # Angular configuration
│   ├── package.json      # NPM dependencies and scripts
├── build.sbt             # SBT build configuration
├── ui-build.sbt          # SBT configuration for frontend integration
```

## Configuration Files

### 1. FrontendCommands.scala

This file defines the npm commands used to interact with the Angular frontend:

```scala
object FrontendCommands {
  val dependencyInstall: String = "npm install"
  val test: String = "npm run test:ci"
  val serve: String = "npm run start"
  val build: String = "npm run build:dev"
}
```

### 2. FrontendRunHook.scala

This file implements a PlayRunHook to integrate the Angular development server with Play's development mode:

```scala
object FrontendRunHook {
  def apply(base: File): PlayRunHook = {
    object UIBuildHook extends PlayRunHook {
      // Runs npm install if node_modules doesn't exist
      override def beforeStarted(): Unit = {
        if (!(base / "ui" / "node_modules").exists()) Process(install, base / "ui").!
      }

      // Starts Angular dev server when Play starts (if not in PROD mode)
      override def afterStarted(): Unit = {
        if (!"PROD".equals(System.getProperty("PLAY_MODE"))) {
          process = Option(Process(run, base / "ui").run)
        }
      }

      // Cleans up Angular process when Play stops
      override def afterStopped(): Unit = {
        process.foreach(_.destroy())
        process = None
      }
    }
  }
}
```

### 3. ui-build.sbt

This file defines SBT tasks for the frontend build process:

```scala
// Execute frontend test task
lazy val `ui-test` = taskKey[Unit]("Run UI tests when testing application.")

// Execute frontend prod build task
lazy val `ui-prod-build` = taskKey[Unit]("Run UI build when packaging the application.")

// Execute frontend prod build task prior to play dist/stage execution
dist := (dist dependsOn `ui-prod-build`).value
stage := (stage dependsOn `ui-prod-build`).value

// Execute frontend test task prior to play test execution
test := ((Test / test) dependsOn `ui-test`).value
```

### 4. build.sbt

The main build.sbt file includes frontend-related configurations:

```scala
// Watch Angular source files for changes
watchSources ++= (baseDirectory.value / "ui/src" ** "*").get
```

### 5. Angular proxy.conf.js

This file configures the Angular development server to proxy API requests to the Play backend:

```javascript
const PROXY_CONFIG = {
  "**": {
    "target": "http://localhost:9000",
    "secure": false,
    "bypass": function (req) {
      if (req && req.headers && req.headers.accept && req.headers.accept.indexOf("html") !== -1) {
        console.log("Skipping proxy for browser request.");
        return "/index.html";
      }
    }
  }
};
```

### 6. package.json

The package.json file defines npm scripts for the Angular application:

```json
{
  "scripts": {
    "start": "ng serve --open --proxy-config src/proxy.conf.js --host 0.0.0.0",
    "build:dev": "ng build --progress --output-path ../public",
    "build:prod": "ng build --progress --configuration production --output-path ../public"
  }
}
```

## Build Process

The build process integrates Angular with Play Framework:

1. **Development Mode**:
   - When running `sbt run`, the FrontendRunHook automatically:
     - Installs npm dependencies if needed
     - Starts the Angular development server
   - The Angular dev server runs on its own port (typically 4200)
   - API requests are proxied to the Play backend (port 9000)

2. **Production Build**:
   - When running `sbt dist` or `sbt stage`, the `ui-prod-build` task:
     - Builds the Angular app with production settings
     - Outputs the compiled assets to the `public` directory
   - The Play application serves these static assets directly

3. **Testing**:
   - When running `sbt test`, the `ui-test` task runs Angular tests first

## Development Workflow

1. **Start the Development Environment**:
   ```
   sbt run
   ```
   This starts both the Play backend and the Angular development server.

2. **Frontend Development**:
   - Edit Angular files in the `ui/src` directory
   - Changes are automatically detected and the browser refreshes
   - API requests are proxied to the Play backend

3. **Backend Development**:
   - Edit Scala files in the `app` directory
   - Changes are automatically detected and the Play server restarts

4. **Testing**:
   - Run backend tests: `sbt test`
   - Run frontend tests: `cd ui && npm test`
   - Run all tests: `sbt test` (includes frontend tests)

## Deployment Process

1. **Build the Production Package**:
   ```
   sbt dist
   ```
   This creates a distribution package with both backend and frontend.

2. **Deploy the Package**:
   - Extract the distribution zip file
   - Run the application using the provided scripts

## How to Add Angular to an Existing Scala Play Project

Follow these steps to add an Angular frontend to an existing Scala Play project:

1. **Create Angular App**:
   ```bash
   # From the project root
   mkdir -p ui
   cd ui
   ng new app-name --directory .
   ```

2. **Configure Angular for Integration**:
   - Update `angular.json` to set the output path to `../public`
   - Create `src/proxy.conf.js` for API forwarding
   - Update `package.json` scripts:
     ```
     "start": "ng serve --open --proxy-config src/proxy.conf.js --host 0.0.0.0",
     "build:dev": "ng build --progress --output-path ../public",
     "build:prod": "ng build --progress --configuration production --output-path ../public"
     ```

3. **Add Frontend Integration Files**:
   - Create `project/FrontendCommands.scala`
   - Create `project/FrontendRunHook.scala`
   - Create `ui-build.sbt`

4. **Update build.sbt**:
   ```scala
   // Add to build.sbt
   PlayKeys.playRunHooks += baseDirectory.map(FrontendRunHook.apply).value
   watchSources ++= (baseDirectory.value / "ui/src" ** "*").get
   ```

5. **Update .gitignore**:
   ```
   # Angular
   /ui/node_modules
   /ui/.angular
   ```

6. **Test the Integration**:
   ```
   sbt run
   ```

## Communication Between Angular and Play

Angular services communicate with the Play backend using HTTP requests:

```typescript
// Example service
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient) {}

  getData(): Observable<any> {
    return this.http.get('/api/data');
  }

  postData(data: any): Observable<any> {
    return this.http.post('/api/data', data);
  }
}
```

During development, these requests are proxied to the Play backend. In production, they're handled directly by the Play server since the Angular app is served from the `public` directory.

## Conclusion

This integration approach provides a seamless development experience with:
- Hot reloading for both frontend and backend
- Unified build and test process
- Simple deployment with a single package
- Clear separation of concerns between frontend and backend

By following this pattern, you can maintain a modern, responsive frontend with Angular while leveraging the power and type safety of Scala on the backend.
