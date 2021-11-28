# Lost 'n Souls
A beautiful functional Roguelike game

## Requirements
- Scala version 3.0.2
- sbt version 1.5.5

## Compilation process (via sbt shell)
run command ```buildGame```

## Run the game

#### Requirements
- An http server that will serve static from a directory (suggestions: [http-server](https://www.npmjs.com/package/http-server) via npm)

#### Process
- Compile the project. This will create a folder in place ```target/indigoBuild/```
- From a new terminal, start the static web server in position ```target/indigoBuild/```

Using http-server run ```http-server -c-1``` and go to http://127.0.0.1:8080/ in your browser of choice.
