# AKAUTAUTO

### Compile jar file

- Require install maven
- cd to the project location, paste all command in `precommand` to the cmd/terminal in order to add all local .jar lib to maven dependency
- run `mvn package` to compile source code and get .jar file. In case not want to compile test code, add option `-Dmaven.test.skip=true`. See 2 jar file located in *target* folder

### Run project

- Compile yourself to get .jar file or simply download it in release page
- Run it by command: `java -jar akautauto.jar ` and follow the help on shell