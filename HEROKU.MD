```
heroku buildpacks:clear
heroku buildpacks:add --index 1 heroku/nodejs
heroku buildpacks:add --index 2 heroku/scala
```
```
heroku addons:create jawsdb-maria:kitefin
```
