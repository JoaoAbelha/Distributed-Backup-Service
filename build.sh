function compile {

	mkdir -p bin
    javac -cp src src/main/java/*.java -d bin
}

compile
sleep 2