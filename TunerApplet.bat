del *.class 
set JAVA_HOME="C:\Program Files\Java\jdk1.5.0_05"
set CP=.
%JAVA_HOME%\bin\javac -classpath %CP% TunerApplet.java 

pause
del RecorderApplet.jar
%JAVA_HOME%\bin\jar cvf TunerApplet.jar .\*.class
del *.class
pause

%JAVA_HOME%\bin\jarsigner TunerApplet.jar apachot
pause