```
heroku buildpacks:clear
heroku buildpacks:add --index 1 https://github.com/lstoll/heroku-buildpack-monorepo
heroku buildpacks:add --index 2 heroku/scala
heroku config:set NODEJS_APP_SUBDIR=ui
```


