run:	ImageProcessing.java
	javac ImageProcessing.java
	java ImageProcessing ${ARGS}

clean:
	rm -f *.class
